package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInUpdateRoom extends Message {
    public int userid;
    public int rm;

    public MessageInUpdateRoom() {
        type = 8;
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
