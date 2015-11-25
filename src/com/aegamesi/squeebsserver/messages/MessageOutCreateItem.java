package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutCreateItem extends Message {
    public int iid;
    public int x;
    public int y;
    public int amt;
    public int t;

    public MessageOutCreateItem() {
        type = 30;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) iid);
        b.putShort((short) x);
        b.putShort((short) y);
        b.putShort((short) amt);
        b.putShort((short) t);

    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        iid = b.getShort();
        x = b.getShort();
        y = b.getShort();
        amt = b.getShort();
        t = b.getShort();
    }
}
