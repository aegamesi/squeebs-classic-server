package com.aegamesi.squeebsserver.messages;


import java.io.IOException;
import java.nio.ByteBuffer;

public class MessageUpdateAppearance extends Message {
    public int userid;
    public int weaponsprite;
    public int hairsprite;
    public int bodysprite;
    public int amusprite;
    public int helmsprite;
    public int robesprite;
    public int shieldsprite;
    public int glovesprite;
    public int subtype;

    public MessageUpdateAppearance() {
        type = 2;
    }

    @Override
    public void write(ByteBuffer b) throws IOException{
        b.put((byte) userid);
        b.putShort((short) weaponsprite);
        b.putShort((short) hairsprite);
        b.putShort((short) bodysprite);
        b.putShort((short) amusprite);
        b.putShort((short) helmsprite);
        b.putShort((short) robesprite);
        b.putShort((short) shieldsprite);
        b.putShort((short) glovesprite);
        b.putShort((short) subtype);
    }

    @Override
    public void read(ByteBuffer b) throws IOException {
        userid = b.get();
        weaponsprite = b.getShort();
        hairsprite = b.getShort();
        bodysprite = b.getShort();
        amusprite = b.getShort();
        helmsprite = b.getShort();
        robesprite = b.getShort();
        shieldsprite = b.getShort();
        glovesprite = b.getShort();
        subtype = b.getShort();
    }
}
