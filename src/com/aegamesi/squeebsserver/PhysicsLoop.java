package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutKillMonster;

import java.io.IOException;

public class PhysicsLoop extends Thread {

    @Override
    public void run() {
        while (true) {
            // do monster stuff
            for (int i = 0; i < Main.db.monsters.length; i++) {
                Database.Monster m = Main.db.monsters[i];
                if (m == null)
                    continue;

                // do whatever

                // monster death
                if(m.hp < 1) {
                    Main.db.monsters[i] = null;

                    MessageOutKillMonster killMsg = new MessageOutKillMonster();
                    killMsg.mid = i;

                    for (int j = 0; j < Main.clientHandler.players.length; j++) {
                        Client player = Main.clientHandler.players[j];
                        if (player == null)
                            continue;

                        try {
                            if(player.user.rm == m.rm)
                                player.sendMessage(killMsg);
                        } catch(IOException e) {
                        }
                    }
                }
            }


            try {
                Thread.sleep(1000 / Main.TPS);
            } catch (InterruptedException e) {
            }
        }
    }
}
