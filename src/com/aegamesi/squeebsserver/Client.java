package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.Message;
import com.macfaq.io.LittleEndianInputStream;
import com.macfaq.io.LittleEndianOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Client extends Thread {
    public ClientHandler handler;
    public Socket socket;
    public LittleEndianOutputStream os;
    public LittleEndianInputStream is;
    public ByteBuffer buffer;

    public Database.User user = null;
    public int playerid = -1;
    private boolean running = true;

    public Client(ClientHandler handler, Socket socket) {
        this.socket = socket;
        this.handler = handler;
        buffer = ByteBuffer.allocate(1024 * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        try {
            socket.setTcpNoDelay(true);
            os = new LittleEndianOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            is = new LittleEndianInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (Main.running && running) {
            try {
                int messageSize = is.readUnsignedShort();
                int packetType = is.readUnsignedByte();
                buffer.clear();

                Main.bytes_received += messageSize + 2 + 1;
                byte[] msg = new byte[messageSize];
                is.read(msg);
                buffer.put(msg);
                buffer.position(0);
                handler.handlePacket(packetType, this);

            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                }
            }
        }
        Logger.log(this + " has disconnected.");
    }

    public void sendMessage(Message m) throws IOException {
        Logger.log("Sending message " + m.type);

        buffer.clear();
        m.write(buffer);

        os.writeShort(buffer.position() + 1); // +1 to account for the BYTE we're writing that isn't part of the message
        os.writeByte(m.type);
        os.write(buffer.array(), buffer.arrayOffset(), buffer.position());
        os.flush();
        Main.bytes_sent += buffer.position() + 2 + 1;
    }

    public void updateRoom() {
        /*
//Update room
playerid = readbyte()
global.rooms[playerid] = readshort()
if global.players[playerid] != -1 {
    global.i = playerid
    global.ctcp = tcp
    with(obj_client) {
        clearbuffer();
        if (id != global.players[global.i] && global.rooms[pid] = global.rooms[global.i]) {
            writebyte(3);
            writebyte(pid);
            writestring(username);
            writebyte(admin);
            sendmessage(global.ctcp);
            clearbuffer();
            writebyte(2)
            writebyte(pid);
            writeshort(weaponsprite)
            writeshort(hairsprite)
            writeshort(bodysprite)
            writeshort(amusprite)
            writeshort(helmsprite)
            writeshort(robesprite)
            writeshort(sheildsprite)
            writeshort(glovesprite)
            writeshort(subtype)
            sendmessage(global.ctcp);
        }
    }
    with(obj_monster) {
        clearbuffer();
        if rm = global.rooms[global.i] {
            writebyte(20);
            writeshort(mid);
            writeshort(xx);
            writeshort(sy);
            writeshort(t);
            sendmessage(global.ctcp);
        }
    }
    with(obj_item) {
        clearbuffer();
        if rm = global.rooms[global.i] {
            writebyte(30)
            writeshort(iid)
            writeshort(sx)
            writeshort(sy)
            writeshort(amnt)
            writeshort(itemid)
            sendmessage(global.ctcp);
        }
    }
    clearbuffer();
    writebyte(3);
    writebyte(pid);
    writestring(username);
    writebyte(admin);
    with(obj_client) {
        if id != global.players[global.i] && global.rooms[pid] = global.rooms[global.i] {
            sendmessage(tcp);
        }
    }
}
         */
    }

    @Override
    public String toString() {
        if (user == null) {
            return "[Unk. Client " + socket.getInetAddress() + "]";
        } else {
            return "[User " + user.username + "]";
        }
    }

    public void disconnect() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {

        }
    }
}
