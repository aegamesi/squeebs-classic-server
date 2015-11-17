package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;

import java.awt.*;
import java.io.IOException;
import java.util.Scanner;

public class InputLoop extends Thread {
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext()) {
            String line = sc.nextLine();

            for (int i = 0; i < Main.clientHandler.players.length; i++) {
                Client player = Main.clientHandler.players[i];
                if (player == null)
                    continue;

                try {
                    player.sendMessage(MessageOutServerMessage.build("Server: " + line, Color.red));
                } catch (IOException e) {

                }
            }
        }
    }
}
