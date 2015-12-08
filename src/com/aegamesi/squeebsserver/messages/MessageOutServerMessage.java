package com.aegamesi.squeebsserver.messages;

import com.aegamesi.squeebsserver.util.Util;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutServerMessage extends Message {
    public String msg;
    public int color;

    public MessageOutServerMessage() {
        type = 9;
    }

    public static MessageOutServerMessage build(String str, Color color) {
        MessageOutServerMessage msg = new MessageOutServerMessage();
        msg.msg = str;
        msg.color = Util.colorToGM(color);
        return msg;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        Message.putString(b, msg);
        b.putInt(color);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        msg = Message.getString(b);
        color = b.getInt();
    }
}