package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInLogin extends Message {
    public String username;
    public String password;
    public int rm; // ????

    public MessageInLogin() {
        type = 21;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        putString(b, username);
        putString(b, password);
        b.putShort((short) rm);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        username = getString(b);
        password = getString(b);
        rm = b.getShort();
    }
}
