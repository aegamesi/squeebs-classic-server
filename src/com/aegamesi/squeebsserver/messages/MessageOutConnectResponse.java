package com.aegamesi.squeebsserver.messages;


import com.macfaq.io.LittleEndianInputStream;
import com.macfaq.io.LittleEndianOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutConnectResponse extends Message {
    public String message;

    public MessageOutConnectResponse() {
        type = 22; // same as 23
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        putString(b, message);
    }

    @Override
    public void read(ByteBuffer b) throws IOException{
        message = getString(b);
    }
}
