package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInPopupResponse extends Message {
    public int tag;
    public String response;
    public int button;

    public MessageInPopupResponse() {
        type = 36;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putInt(tag);
        Message.putString(b, response);
        b.putInt(button);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        tag = b.getInt();
        response = Message.getString(b);
        button = b.getInt();
    }
}
