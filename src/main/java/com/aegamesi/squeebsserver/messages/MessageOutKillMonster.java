package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutKillMonster extends Message {
    public int mid;

    public MessageOutKillMonster() {
        type = 23;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) mid);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        mid = b.getShort();
    }
}
