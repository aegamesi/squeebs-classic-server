package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInUsePortal extends Message {
    public int portal_id;

    public MessageInUsePortal() {
        type = 9;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putShort((short) portal_id);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        portal_id = b.getShort();
    }
}
