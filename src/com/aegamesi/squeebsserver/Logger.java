package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;
import com.aegamesi.squeebsserver.messages.MessageOutShutdown;
import com.aegamesi.squeebsserver.ui.CommandTextBox;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;

import java.awt.*;
import java.io.IOException;

public class Logger {
    private static final int loggerSize = 19;
    private static boolean headless = true;
    private static Label loggerBox;
    private static MultiWindowTextGUI gui;
    private static String loggerBuffer;


    public static void init() {
        try {
            setupGUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupGUI() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        if (terminal instanceof SwingTerminal)
            headless = false;


        // logger panel
        loggerBuffer = ".";
        for (int i = 0; i < loggerSize - 1; i++)
            loggerBuffer += "\n.";
        Panel loggerPanel = new Panel();
        loggerBox = new Label(loggerBuffer);
        loggerBox.setPreferredSize(new TerminalSize(70, loggerSize));
        loggerPanel.addComponent(loggerBox);
        BasicWindow loggerWindow = new BasicWindow();
        loggerWindow.setComponent(loggerPanel);

        // cmd panel...
        Panel cmdPanel = new Panel();
        cmdPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        cmdPanel.addComponent(new Label(">"));
        final CommandTextBox cmdBox = new CommandTextBox();
        cmdBox.setPreferredSize(new TerminalSize(68, 1));
        cmdPanel.addComponent(cmdBox);
        final BasicWindow cmdWindow = new BasicWindow();
        cmdWindow.setComponent(cmdPanel);
        cmdBox.command = new Runnable() {
            @Override
            public void run() {
                String cmd = cmdBox.getText();
                cmdBox.setText("");

                handleCommand(cmd);
            }
        };

        // Create gui and start gui
        gui = new MultiWindowTextGUI(new SeparateTextGUIThread.Factory(), screen);
        gui.addWindow(loggerWindow);
        gui.addWindow(cmdWindow);
        loggerWindow.setPosition(new TerminalPosition(0, 0));
        cmdWindow.setPosition(new TerminalPosition(0, loggerSize + 2));
        ((SeparateTextGUIThread) gui.getGUIThread()).start();
    }

    public static void log(final String str) {
        if (!headless)
            System.out.println(str);

        if (gui != null) {
            gui.getGUIThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    loggerBuffer = loggerBuffer.substring(loggerBuffer.indexOf("\n") + 1);
                    loggerBuffer += "\n" + str;
                    loggerBox.setText(loggerBuffer);
                }
            });
        }
    }

    public static void handleCommand(String command) {
        Logger.log("> " + command);
        String[] parts = command.trim().split(" ");
        if(parts.length == 0)
            return;
        int args = parts.length - 1;

        switch (parts[0]) {
            case "chat":
            case "say":
                if(!requireArgs(args, 1, -1))
                    break;

                String say = "Server: " + command.substring(parts[0].length() + 1);
                Logger.log(say);
                // broadcast
                Main.clientHandler.broadcast(MessageOutServerMessage.build(say, Color.red), -1, null);
                break;
            case "stats":
                if(!requireArgs(args, 0, 0))
                    break;

                Logger.log("Sent: " + ((float)Main.bytes_sent / 1000.0f) + "kb / Recv: " + ((float)Main.bytes_received / 1000.0f) + "kb");
                break;
            case "save":
                if(!requireArgs(args, 0, 0))
                    break;

                Main.db.save();
                Logger.log("Saved database.");
                break;
            case "shutdown":
            case "stop":
                if(!requireArgs(args, 0, 0))
                    break;

                Logger.log("Shutting down server.");
                Main.db.save();

                Main.clientHandler.broadcast(new MessageOutShutdown(), -1, null);
                System.exit(0);
                break;
            default:
                Logger.log("Unknown command.");
                break;
        }
    }

    public static boolean requireArgs(int given, int min, int max) {
        if((min >= 0 && given < min) || (max >= 0 && given > max)) {
            Logger.log("Required args: [" + min + ", " + max + "]. Given: " + given);
            return false;
        }
        return true;
    }
}
