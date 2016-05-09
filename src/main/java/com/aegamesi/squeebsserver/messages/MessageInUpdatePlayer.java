package com.aegamesi.squeebsserver.messages;


import com.aegamesi.squeebsserver.squeebs.Database;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageInUpdatePlayer extends Message {
    public Database.User user;

    public MessageInUpdatePlayer() {
        type = 10;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        user.write(b, false);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        user.read(b, false);
    }
}
