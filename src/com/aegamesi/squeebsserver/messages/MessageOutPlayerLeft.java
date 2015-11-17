package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutPlayerLeft extends Message {
    public int userid;

    public MessageOutPlayerLeft() {
        type = 6;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        b.put((byte) userid);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        userid = b.get();
    }
}
