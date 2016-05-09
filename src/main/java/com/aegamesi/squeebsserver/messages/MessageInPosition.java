package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInPosition extends Message {
    public int userid;
    public int x;
    public int y;
    public int spr;
    public int img;
    public int spr_dir;
    public int move;
    public int jmp;
    public int rm;

    public MessageInPosition() {
        type = 5;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.put((byte) userid);
        b.putShort((short) x);
        b.putShort((short) y);
        b.putShort((short) spr);
        b.put((byte) img);
        b.put((byte) spr_dir);
        b.put((byte) move);
        b.put((byte) jmp);
        b.putShort((short) rm);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        userid = b.get();
        x = b.getShort();
        y = b.getShort();
        spr = b.getShort();
        img = b.get() & 0xff;
        spr_dir = b.get() & 0xff;
        move = b.get() & 0xff;
        jmp = b.get() & 0xff;
        rm = b.getShort();
    }
}
