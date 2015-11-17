package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInChangeRoom extends Message {
    public int userid;
    public int rm;

    public MessageInChangeRoom() {
        type = 9;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.put((byte) userid);
        b.putShort((short) rm);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        userid = b.get();
        rm = b.getShort();
    }
}
