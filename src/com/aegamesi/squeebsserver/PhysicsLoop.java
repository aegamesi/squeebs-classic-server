package com.aegamesi.squeebsserver;

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
            }


            try {
                Thread.sleep(1000 / Main.TPS);
            } catch (InterruptedException e) {
            }
        }
    }
}
