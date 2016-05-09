package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInCreateItem extends Message {
    public int t;
    public int x;
    public int y;
    public int amt;
    public int rm;

    public MessageInCreateItem() {
        type = 12;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) t);
        b.putShort((short) x);
        b.putShort((short) y);
        b.putShort((short) amt);
        b.putShort((short) rm);

    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        t = b.getShort();
        x = b.getShort();
        y = b.getShort();
        amt = b.getShort();
        rm = b.getShort();
    }
}
