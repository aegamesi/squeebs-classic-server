package com.aegamesi.squeebsserver.messages;


import com.aegamesi.squeebsserver.Database;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutLoginSuccess extends Message {
    public String message;
    public Database.User user;

    public MessageOutLoginSuccess() {
        type = 24;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        putString(b, message);
        user.write(b);
    }

    @Override
    public void read(ByteBuffer b) throws IOException{
        message = getString(b);
        user = new Database.User();
        user.read(b);
    }
}
