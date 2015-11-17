package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageQuit extends Message {
    public int userid;
    public String username;

    public MessageQuit() {
        type = 3;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        b.put((byte) userid);
        Message.putString(b, username);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        userid = b.get();
        username = Message.getString(b);
    }
}
