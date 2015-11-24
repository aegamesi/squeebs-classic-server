package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutDamageMonster extends Message {
    public int mid;
    public int dmg;
    public int sp;

    public MessageOutDamageMonster() {
        type = 22;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) mid);
        b.putShort((short) dmg);
        b.putShort((short) sp);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        mid = b.getShort();
        dmg = b.getShort();
        sp = b.getShort();
    }
}
