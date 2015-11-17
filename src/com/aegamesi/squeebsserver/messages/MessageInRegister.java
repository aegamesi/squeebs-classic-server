package com.aegamesi.squeebsserver.messages;


import com.macfaq.io.LittleEndianInputStream;
import com.macfaq.io.LittleEndianOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInRegister extends Message {
    public String username;
    public String password;
    public int version;

    public MessageInRegister() {
        type = 20;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        putString(b, username);
        putString(b, password);
        b.put((byte) version);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        username = getString(b);
        password = getString(b);
        version = b.get() & 0xff;
    }
}
