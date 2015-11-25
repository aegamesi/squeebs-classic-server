package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.Message;
import com.aegamesi.squeebsserver.messages.MessageInAppearance;
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
    public MessageInAppearance cachedAppearance = new MessageInAppearance();
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
                int messageSize = is.readUnsignedShort() - 1; // minus one to account for that last byte
                int packetType = is.readUnsignedByte();
                buffer.clear();

                byte[] msg = new byte[messageSize];

                int total_read = 0;
                while (total_read < messageSize) {
                    int just_read = is.read(msg, total_read, messageSize - total_read);
                    if (just_read == -1) {
                        running = false;
                        break;
                    }
                    total_read += just_read;
                }

                Main.bytes_received += messageSize + 2 + 1;
                buffer.put(msg);
                buffer.position(0);
                handler.handlePacket(packetType, messageSize, this);

            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException e1) {
                }
            }
        }
        Logger.log(this + " has disconnected.");

        if (playerid >= 0)
            Main.clientHandler.players[playerid] = null;
    }

    public void sendMessage(Message m) throws IOException {
        //Logger.log("Sending message " + m.getClass().getSimpleName());

        buffer.clear();
        m.write(buffer);

        os.writeShort(buffer.position() + 1); // +1 to account for the BYTE we're writing that isn't part of the message
        os.writeByte(m.type);
        os.write(buffer.array(), buffer.arrayOffset(), buffer.position());
        os.flush();
        Main.bytes_sent += buffer.position() + 2 + 1;
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
