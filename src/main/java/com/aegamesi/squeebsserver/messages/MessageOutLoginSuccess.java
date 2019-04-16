package com.aegamesi.squeebsserver.messages;


import com.aegamesi.squeebsserver.squeebs.Database;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutLoginSuccess extends Message {
    public Database.User user;

    public MessageOutLoginSuccess() {
        type = 24;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        user.write(b, true);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        user = new Database.User();
        user.read(b, true);
    }
}
