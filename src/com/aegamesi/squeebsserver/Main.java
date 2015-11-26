package com.aegamesi.squeebsserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PROTOCOL_VERSION = 4;
    public static final int PORT = 12564;
    public static final int PLAYER_MAX = 20;
    public static final int TPS = 20;

    public static long bytes_sent = 0;
    public static long bytes_received = 0;

    public static boolean running = true;
    public static Database db = new Database();
    public static ClientHandler clientHandler;
    public static PhysicsLoop physicsLoop;

    public static void main(String[] args) throws IOException {
        // setup gui
        Logger.init();

        Logger.log("Starting up Squeebs Java Server...");

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
