package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutTakeItem extends Message {
    public int user;
    public int iid;

    public MessageOutTakeItem() {
        type = 31;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.put((byte) user);
        b.putShort((short) iid);

    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        user = b.get();
        iid = b.getShort();
    }
}
