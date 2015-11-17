package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;

import java.util.Scanner;

public class PhysicsLoop extends Thread {
    public ClientHandler clientHandler;

    public PhysicsLoop(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    @Override
    public void run() {
        while(true) {
            // do monster stuff
            for (int i = 0; i < Main.db.monsters.length; i++) {
                Database.Monster m = Main.db.monsters[i];
                if (m == null)
                    continue;

                // do whatever
            }

            Scanner sc = new Scanner(System.in);
            while(sc.hasNext()) {
                String line = sc.nextLine();

                for(int i = 0; i < clientHandler.players.length; i++) {
                    Client player = clientHandler.players[i];
                    if (player == null)
                        continue;

                    player.sendMessage(MessageOutServerMessage.build("Server: " + line, Color.red));
                }
            }

                try {
                    Thread.sleep(1000 / Main.TPS);
                } catch (InterruptedException e) {
                }
        }
    }
}
