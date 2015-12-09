package com.aegamesi.squeebsserver.util;

import com.aegamesi.squeebsserver.squeebs.CommandHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Logger {
    private static PrintWriter logWriter;
    private static SimpleDateFormat sdf = new SimpleDateFormat("[yy-MM-dd HH:mm:ss] ");

    public static final List<String> logHistory = new ArrayList<String>();


    public static void init() {
        try {
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter("log.txt", true)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(final String str) {
        System.out.println(str);
        logHistory.add(str);

        String logStr = sdf.format(new Date()) + str;
        logWriter.println(logStr);
        logWriter.flush();
    }

    public static void handleCommand(String command) {
        Logger.log("> " + command);
        CommandHandler.runCommand(command, null);
    }
}
