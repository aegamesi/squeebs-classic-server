package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageIncomingChat extends Message {
    public String msg;

    public MessageIncomingChat() {
        type = 8;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        Message.putString(b, msg);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        msg = Message.getString(b);
    }
}
