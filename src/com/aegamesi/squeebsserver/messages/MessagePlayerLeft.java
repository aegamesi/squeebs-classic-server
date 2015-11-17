package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessagePlayerLeft extends Message {
    public int userid;

    public MessagePlayerLeft() {
        type = 6; // same as monster spawn...?
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
