package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInSpawnMonster extends Message {
    public int x;
    public int y;
    public int t;
    public int hp;
    public int xp;
    public int rm;

    public MessageInSpawnMonster() {
        type = 6;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        b.putShort((short) x);
        b.putShort((short) y);
        b.putShort((short) t);
        b.putShort((short) hp);
        b.putShort((short) xp);
        b.putShort((short) rm);

    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        x = b.getShort();
        y = b.getShort();
        t = b.getShort();
        hp = b.getShort();
        xp = b.getShort();
        rm = b.getShort();
    }
}
