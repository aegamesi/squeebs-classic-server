package com.aegamesi.squeebsserver.messages;

import com.aegamesi.squeebsserver.util.Util;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutFloatingMessage extends Message {
    public String msg;
    public int x;
    public int y;
    public int time;
    public int col_inner;
    public int col_outer;

    public MessageOutFloatingMessage() {
        type = 32;
    }

    public static MessageOutFloatingMessage build(String str, int x, int y, int time) {
        return build(str, x, y, time, Color.white, Color.black);
    }

    public static MessageOutFloatingMessage build(String str, int x, int y, int time, Color inner, Color outer) {
        MessageOutFloatingMessage msg = new MessageOutFloatingMessage();
        msg.msg = str;
        msg.x = x;
        msg.y = y;
        msg.time = time;
        msg.col_inner = Util.colorToGM(inner);
        msg.col_outer = Util.colorToGM(outer);
        return msg;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        Message.putString(b, msg);
        b.putShort((short) x);
        b.putShort((short) y);
        b.putInt(time);
        b.putInt(col_inner);
        b.putInt(col_outer);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        msg = Message.getString(b);
        x = b.getShort();
        y = b.getShort();
        time = b.getInt();
        col_inner = b.getInt();
        col_outer = b.getInt();
    }
}