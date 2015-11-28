package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutCreateItem;
import com.aegamesi.squeebsserver.messages.MessageOutKillMonster;
import com.aegamesi.squeebsserver.messages.MessageOutMoveMonster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                    Database.MonsterInfo info = Database.monsterInfo[m.t];

                    // Item drops / broadcast
                    if(m.hp < 1) {
                        // only drop items if it's dead
                        List<Database.Item> drops = new ArrayList<>();
                        if (Util.probability(0.8)) {
                            // gold
                            Database.Item item = new Database.Item();
                            item.amt = info.gld;
                            item.t = 4;
                            drops.add(item);
                        }
                        for (Database.MonsterDrop drop : info.drops) {
                            if (Util.probability(drop.prob)) {
                                Database.Item item = new Database.Item();
                                item.t = drop.item;
                                drops.add(item);
                            }
                        }
                        for (Database.Item item : drops) {
                            item.x = m.x;
                            item.y = m.y;
                            item.rm = m.rm;
                            item.iid = Util.findSlot(Main.db.items);
                            Main.db.items[item.iid] = item;

                            MessageOutCreateItem spawn = new MessageOutCreateItem();
                            spawn.x = item.x;
                            spawn.y = item.y;
                            spawn.entity_id = m.id;
                            spawn.t = item.t;
                            spawn.iid = item.iid;
                            spawn.amt = item.amt;
                            Main.clientHandler.broadcast(spawn, item.rm, null);
                        }

                        // if it's a boss, disable room spawners
                        if(info.boss) {
                            for (MonsterSpawner spawner : Main.db.spawners)
                                if(spawner.rm == m.rm)
                                    spawner.disabled = true;
                        }
                    }

                    // kill msg (after drop for entity_id)
                    MessageOutKillMonster killMsg = new MessageOutKillMonster();
                    killMsg.mid = i;
                    Main.clientHandler.broadcast(killMsg, m.rm, null);
                }
            }


            try {
                Thread.sleep(1000 / Main.TPS);
            } catch (InterruptedException e) {
            }
        }
    }
}
