package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInHello extends Message {
    public int version;

    public MessageInHello() {
        type = 31;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putInt(version);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        version = b.getInt();
    }
}
