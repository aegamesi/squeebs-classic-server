package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutKillMonster;
import com.aegamesi.squeebsserver.messages.MessageOutMoveMonster;

import java.io.IOException;

public class PhysicsLoop extends Thread {
    public float save_timer = 300.0f;

    public PhysicsLoop() {

    }

    @Override
    public void run() {
        for(MonsterSpawner spawner : Main.db.spawners)
            spawner.init();

        while (Main.running) {
            float dt = 1.0f / (float) Main.TPS;

            // auto save db
            save_timer -= dt;
            if(save_timer < 0.0f) {
                save_timer = 300.0f;
                Main.db.save();
            }

            // monster spawning
            for (MonsterSpawner spawner : Main.db.spawners) {
                if(spawner.timer > 0.0) {
                    spawner.timer -= dt;
                    if(spawner.timer <= 0.0) {
                        spawner.trigger();
                    }
                }
            }

            // do monster stuff
            for (int i = 0; i < Main.db.monsters.length; i++) {
                Database.Monster m = Main.db.monsters[i];
                if (m == null)
                    continue;

                // monster movement
                m.move_timer -= dt;
                if (m.move_timer < 0.0f) {
                    m.move_timer = 3.0f + Util.random.nextFloat() * 3.0f;
                    m.new_x = m.x - 150 + Util.random.nextInt(300);

                    // echo
                    MessageOutMoveMonster moveMsg = new MessageOutMoveMonster();
                    moveMsg.mid = i;
                    moveMsg.new_x = m.new_x;
                    int receivers = Main.clientHandler.broadcast(moveMsg, m.rm, null);
                    if(receivers > 0)
                        m.ttl = 60.0f;
                }

                // monster death / expiration
                m.ttl -= dt;
                if (m.hp < 1 || m.ttl < 0.0f) {
                    Main.db.monsters[i] = null;

                    MessageOutKillMonster killMsg = new MessageOutKillMonster();
                    killMsg.mid = i;
                    Main.clientHandler.broadcast(killMsg, m.rm, null);

                    // TODO do item drops / broadcast
                }
            }


            try {
                Thread.sleep(1000 / Main.TPS);
            } catch (InterruptedException e) {
            }
        }
    }
}
