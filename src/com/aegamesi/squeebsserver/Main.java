package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.squeebs.ClientHandler;
import com.aegamesi.squeebsserver.squeebs.Database;
import com.aegamesi.squeebsserver.squeebs.PhysicsLoop;
import com.aegamesi.squeebsserver.ui.WebInterface;
import com.aegamesi.squeebsserver.util.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PROTOCOL_VERSION = 5;
    public static final int PORT = 12564;
    public static final int WEB_PORT = 12566;
    public static final int PLAYER_MAX = 20;
    public static final int TPS = 20;

    public static long bytes_sent = 0;
    public static long bytes_received = 0;
    public static long program_start_time = 0L;

    public static boolean running = true;
    public static Database db;
    public static ClientHandler clientHandler;
    public static PhysicsLoop physicsLoop;
    public static WebInterface webInterface;

    public static void main(String[] args) throws IOException {
        // setup gui
        Logger.init();
        Logger.log("Starting up Squeebs Java Server...");
        program_start_time = System.currentTimeMillis();

        // setup web interface
        webInterface = new WebInterface(WEB_PORT);
        webInterface.start();

        // setup DB/load from files
        db = new Database();
        db.load();

        clientHandler = new ClientHandler();
        Logger.log("There are " + db.users.size() + " users.");

        // physloop
        physicsLoop = new PhysicsLoop();
        physicsLoop.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Logger.log("Listening on port " + PORT);

            while (running) {
                Socket socket = serverSocket.accept();
                Logger.log("Accepted connection from " + socket.getInetAddress());
                clientHandler.handleNewClient(socket);
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        running = false;
    }
}
