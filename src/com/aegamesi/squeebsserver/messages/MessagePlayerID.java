package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessagePlayerID extends Message {
    public int playerid;

    public MessagePlayerID() {
        type = 1;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        b.put((byte) playerid);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        playerid = b.get() & 0xff;
    }
}
