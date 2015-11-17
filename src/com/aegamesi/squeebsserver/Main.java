package com.aegamesi.squeebsserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static final int PORT = 12564;

    public static boolean running = true;
    public static Database db = new Database();

    public static void main(String[] args) {
        Logger.log("Starting up Squeebs Java Server...");

        // test accounts
        {
            Database.User user = new Database.User();
            user.username = "Eli";
            user.password = "test";
            db.users.add(user);
        }

        ClientHandler clientHandler = new ClientHandler();
        Logger.log("There are " + db.users.size() + " users.");

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
