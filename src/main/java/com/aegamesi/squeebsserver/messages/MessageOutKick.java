package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutKick extends Message {
    public String msg;

    public MessageOutKick() {
        type = 10;
    }

    public static MessageOutKick build(String str) {
        MessageOutKick msg = new MessageOutKick();
        msg.msg = str;
        return msg;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        Message.putString(b, msg);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        msg = Message.getString(b);
    }
}
