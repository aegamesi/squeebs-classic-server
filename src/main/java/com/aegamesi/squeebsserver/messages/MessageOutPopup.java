package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageOutPopup extends Message {
    public static final int STYLE_MESSAGE = 1; // message, no response
    public static final int STYLE_PROMPT = 2; // prompt for text
    public static final int STYLE_BUTTONS = 3; // prompt, up to 3 buttons

    public int style;
    public String msg;
    public int tag;
    public String def = ""; // default response
    public String button1 = "";
    public String button2 = "";
    public String button3 = "";

    public MessageOutPopup() {
        type = 35;
    }

    @Override
    public void write(ByteBuffer b) throws IOException {
        b.putInt(style);
        b.putInt(tag);
        Message.putString(b, msg);
        Message.putString(b, def);
        Message.putString(b, button1);
        Message.putString(b, button2);
        Message.putString(b, button3);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        style = b.getInt();
        tag = b.getInt();
        msg = Message.getString(b);
        def = Message.getString(b);
        button1 = Message.getString(b);
        button2 = Message.getString(b);
        button3 = Message.getString(b);
    }
}
