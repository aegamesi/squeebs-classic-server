package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutMoveMonster extends Message {
    public int mid;
    public int new_x;

    public MessageOutMoveMonster() {
        type = 21;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) mid);
        b.putShort((short) new_x);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        mid = b.getShort();
        new_x = b.getShort();
    }
}
