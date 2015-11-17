package com.aegamesi.squeebsserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PORT = 12564;
    public static final int PLAYER_MAX = 20;
    public static final int TPS = 20;

    public static boolean running = true;
    public static Database db = new Database();

    public static void main(String[] args) {
        Logger.log("Starting up Squeebs Java Server...");

        // test accounts
        {
            Database.User user = new Database.User();
            user.username = "Eli";
            user.password = "test";
            user.lvl = 10;
            db.users.add(user);
        }

        ClientHandler clientHandler = new ClientHandler();
        Logger.log("There are " + db.users.size() + " users.");

        // physloop
        PhysicsLoop physicsLoop = new PhysicsLoop(clientHandler);
        physicsLoop.start();

        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            Logger.log("Listening on port " + PORT);

            while(running) {
                Socket socket = serverSocket.accept();
                Logger.log("Accepted connection from " + socket.getInetAddress());
                clientHandler.handleNewClient(socket);
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        running = false;
    }
}
