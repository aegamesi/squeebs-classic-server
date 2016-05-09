package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutMonsterXP extends Message {
    public int mid;
    public int xp;

    public MessageOutMonsterXP() {
        type = 24;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) mid);
        b.putShort((short) xp);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        mid = b.getShort();
        xp = b.getShort();
    }
}
