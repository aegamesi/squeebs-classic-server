package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInDamageMonster extends Message {
    public int userid;
    public int mid;
    public int dmg;
    public int sp;

    public MessageInDamageMonster() {
        type = 7;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.put((byte) userid);
        b.putShort((short) mid);
        b.putShort((short) dmg);
        b.putShort((short) sp);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        userid = b.get();
        mid = b.getShort();
        dmg = b.getShort();
        sp = b.getShort();
    }
}
