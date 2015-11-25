package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;
import com.aegamesi.squeebsserver.ui.CommandTextBox;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
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
import java.util.Arrays;

public class Logger {
    private static boolean headless = true;
    private static Label loggerBox;
    private static MultiWindowTextGUI gui;
    private static final int loggerSize = 19;
    private static String loggerBuffer;


    public static void init() {
        try {
            setupGUI();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static void setupGUI() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        if(terminal instanceof SwingTerminal)
            headless = false;


        // logger panel
        loggerBuffer = ".";
        for(int i = 0; i < loggerSize - 1; i++)
            loggerBuffer += "\n.";
        Panel loggerPanel = new Panel();
        loggerBox = new Label(loggerBuffer);
        loggerBox.setPreferredSize(new TerminalSize(60, loggerSize));
        loggerPanel.addComponent(loggerBox);
        BasicWindow loggerWindow = new BasicWindow();
        loggerWindow.setComponent(loggerPanel);

        // cmd panel...
        Panel cmdPanel = new Panel();
        cmdPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        cmdPanel.addComponent(new Label(">"));
        final CommandTextBox cmdBox = new CommandTextBox();
        cmdBox.setPreferredSize(new TerminalSize(60, 1));
        cmdPanel.addComponent(cmdBox);
        final BasicWindow cmdWindow = new BasicWindow();
        cmdWindow.setComponent(cmdPanel);
        cmdBox.command = new Runnable() {
            @Override
            public void run() {
                String cmd = cmdBox.getText();
                cmdBox.setText("");

                // broadcast
                // TODO refactor
                Logger.log("Server: " + cmd);
                for (int i = 0; i < Main.clientHandler.players.length; i++) {
                    Client player = Main.clientHandler.players[i];
                    if (player == null)
                        continue;

                    try {
                        player.sendMessage(MessageOutServerMessage.build("Server: " + cmd, Color.red));
                    } catch (IOException e) {
                    }
                }
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
        if(!headless)
            System.out.println(str);

        if(gui != null) {
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
}
