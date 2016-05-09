package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutSpawnMonster extends Message {
    public int id;
    public int x;
    public int y;
    public int t;

    public MessageOutSpawnMonster() {
        type = 20;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) id);
        b.putShort((short) x);
        b.putShort((short) y);
        b.putShort((short) t);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        id = b.getShort();
        x = b.getShort();
        y = b.getShort();
        t = b.getShort();
    }
}
