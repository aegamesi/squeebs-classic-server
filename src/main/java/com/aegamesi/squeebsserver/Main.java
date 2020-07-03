package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.squeebs.ClientHandler;
import com.aegamesi.squeebsserver.squeebs.Config;
import com.aegamesi.squeebsserver.squeebs.Database;
import com.aegamesi.squeebsserver.squeebs.PhysicsLoop;
import com.aegamesi.squeebsserver.ui.WebInterface;
import com.aegamesi.squeebsserver.util.Logger;
import com.github.sheigutn.pushbullet.Pushbullet;

public class Main {
    public static final int PROTOCOL_VERSION = 8;
    public static final int PLAYER_MAX = 20;
    public static final int TPS = 20;

    public static long bytes_sent = 0;
    public static long bytes_received = 0;
    public static long program_start_time = 0L;

    public static boolean running = true;
    public static Config config;
    public static Database db;
    public static ClientHandler clientHandler;
    public static PhysicsLoop physicsLoop;

    public static Pushbullet pushbullet = null;

    public static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return 80;
    }

    public static void main(String[] args) {
        String dbDirectoryPath = args.length == 0 ? null : args[0];

        // setup gui
        Logger.init(dbDirectoryPath);
        Logger.log("Starting up Squeebs Java Server...");
        program_start_time = System.currentTimeMillis();

        // setup DB/load from files
        db = new Database(dbDirectoryPath);
        db.load();

        // setup pushbullet
        if (config.pushbullet_enabled) {
            pushbullet = new Pushbullet(config.pushbullet_api_key);
        }

        clientHandler = new ClientHandler();
        Logger.log("There are " + db.users.size() + " users.");

        // physloop
        physicsLoop = new PhysicsLoop();
        physicsLoop.start();

        // start web + server interface
        WebInterface.start(getPort());
    }
}
