package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.Message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Database {
    public List<User> users = new ArrayList<>();
    public transient Monster[] monsters = new Monster[2000];
    public transient Item[] items = new Item[2000];

    public static class Monster {
        public int id;
        public int x;
        public int y;
        public int t;
        public int m_hp;
        public int hp;
        public int xp;
        public int rm;
        public int new_x;

        public float ttl = 60.0f;
        public float move_timer = 3.0f;
    }

    public static class Item {
        public int iid;
        public int t;
        public int x;
        public int y;
        public int amt;
        public int rm;

        public float ttl = 300.0f;
    }

    public static class User {
        public String username;
        public String password;
        public int rank = 0; // 0 player, 1 admin, 2 mod
        public int status = 0; // 0 offline, 1 online, 2 banned?

        public int x = 80;
        public int y = 80;
        public int rm = 0;
        public int lvl = 1;
        public double xp = 0;
        public double m_xp = 5;
        public int hp = 50;
        public int m_hp = 50;
        public int mp = 50;
        public int m_mp = 50;
        public int str = 5;
        public int agil = 5;
        public int dext = 5;
        public int inte = 5;
        public int stam = 5;
        public int gold = 0;
        public int silver = 0;
        public int bronze = 0;
        public int vary = 0; // ???

        public int[] item_ids = new int[68];
        public int[] item_counts = new int[68];

        public User() {
            item_ids[0] = 101 + 1000;
            item_ids[1] = 2 + 2000;
            item_ids[2] = 2 + 5000;
            item_ids[3] = 2 + 4000;
            item_counts[0] = 1;
            item_counts[1] = 1;
            item_counts[2] = 1;
            item_counts[3] = 1;
        }

        public void read(ByteBuffer b, boolean full) {
            if (full) {
                username = Message.getString(b);
                rank = b.get();
            }

            x = b.getShort();
            y = b.getShort();
            rm = b.getShort();
            lvl = b.getShort();
            xp = b.getDouble();
            if (full)
                m_xp = b.getDouble();
            hp = b.getShort();
            if (full)
                m_hp = b.getShort();
            mp = b.getShort();
            if (full)
                m_mp = b.getShort();
            str = b.getShort();
            agil = b.getShort();
            dext = b.getShort();
            inte = b.getShort();
            stam = b.getShort();
            gold = b.getShort();
            silver = b.getShort();
            bronze = b.getShort();

            for (int i = 0; i < 68; i++) {
                item_ids[i] = b.getShort();
                item_counts[i] = b.getShort();
            }

            vary = b.get();
        }

        public void write(ByteBuffer b, boolean full) {
            if (full) {
                Message.putString(b, username);
                b.put((byte) rank);
            }

            b.putShort((short) x);
            b.putShort((short) y);
            b.putShort((short) rm);
            b.putShort((short) lvl);
            b.putDouble(xp);
            if (full)
                b.putDouble(m_xp);
            b.putShort((short) hp);
            if (full)
                b.putShort((short) m_hp);
            b.putShort((short) mp);
            if (full)
                b.putShort((short) m_mp);
            b.putShort((short) str);
            b.putShort((short) agil);
            b.putShort((short) dext);
            b.putShort((short) inte);
            b.putShort((short) stam);
            b.putShort((short) gold);
            b.putShort((short) silver);
            b.putShort((short) bronze);

            for (int i = 0; i < 68; i++) {
                b.putShort((short) item_ids[i]);
                b.putShort((short) item_counts[i]);
            }

            b.put((byte) vary);
        }
    }
}
