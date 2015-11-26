package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutShutdown extends Message {
    public MessageOutShutdown() {
        type = 7;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
    }
}
