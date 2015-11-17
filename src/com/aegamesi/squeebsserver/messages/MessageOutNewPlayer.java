package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutNewPlayer extends Message {
    public int playerid;
    public String username;
    public int admin;

    public MessageOutNewPlayer() {
        type = 3;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        b.put((byte) playerid);
        Message.putString(b, username);
        b.put((byte) admin);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        playerid = b.get() & 0xff;
        username = Message.getString(b);
        admin = b.get() & 0xff;
    }
}
