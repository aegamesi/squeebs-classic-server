package com.aegamesi.squeebsserver.messages;


import com.macfaq.io.LittleEndianInputStream;
import com.macfaq.io.LittleEndianOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageLoginAccount extends Message {
    public String username;
    public String password;
    public int version;
    public int rm; // ????

    public MessageLoginAccount() {
        type = 21;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        putString(b, username);
        putString(b, password);
        b.put((byte) version);
        b.putShort((short) rm);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        username = getString(b);
        password = getString(b);
        version = b.get() & 0xff;
        rm = b.getShort();
    }
}
