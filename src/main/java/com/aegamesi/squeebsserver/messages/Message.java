package com.aegamesi.squeebsserver.messages;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Message {
    public int type;

    public static void putString(ByteBuffer b, String s) {
        b.put(s.getBytes());
        b.put((byte) 0);
    }

    public static String getString(ByteBuffer b) {
        StringBuilder str = new StringBuilder(b.remaining());
        int chr;
        while ((chr = (b.get() & 0xff)) != 0)
            str.append((char) chr);
        return str.toString();
    }

    public abstract void write(ByteBuffer b) throws IOException;

    public abstract void read(ByteBuffer b) throws IOException;
}
