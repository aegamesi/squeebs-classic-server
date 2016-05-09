package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutChangeRoom extends Message {
    public int rm;
    public int x;
    public int y;

    public MessageOutChangeRoom() {
        type = 12;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) rm);
        b.putShort((short) x);
        b.putShort((short) y);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        rm = b.getShort();
        x = b.getShort();
        y = b.getShort();
    }
}
