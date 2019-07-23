package com.aegamesi.squeebsserver.squeebs;

import com.aegamesi.squeebsserver.messages.MessageOutPlayerLeft;
import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;
import com.aegamesi.squeebsserver.util.Logger;
import com.aegamesi.squeebsserver.Main;
import com.aegamesi.squeebsserver.messages.Message;
import com.aegamesi.squeebsserver.messages.MessageInAppearance;
import org.eclipse.jetty.websocket.api.Session;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Client {
    public Session session;
    public ByteBuffer buffer;

    public Database.User user = null;
    public MessageInAppearance cachedAppearance = new MessageInAppearance();
    public int playerid = -1;
    private boolean running = true;
    public long lastMessageTime = 0;

    // temporary state for login
    public String _tempUsername = null;
    public String _tempPassword = null;

    public Client(Session session) {
        this.session = session;
        buffer = ByteBuffer.allocate(1024 * 32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        lastMessageTime = System.currentTimeMillis();
    }

    public void sendMessage(Message m) throws IOException {
        // Logger.log("Sending message " + m.getClass().getSimpleName());
        if (!session.isOpen()) {
            return;
        }

        buffer.clear();
        buffer.put((byte) m.type);
        m.write(buffer);

        int len = buffer.position();
        buffer.position(0);
        buffer.limit(len);

        session.getRemote().sendBytes(buffer);
        Main.bytes_sent += len;
    }

    @Override
    public String toString() {
        if (user == null) {
            return "[Unk. Client " + session.getRemoteAddress() + "]";
        } else {
            return "[User " + user.username + "]";
        }
    }

    public void disconnect() {
        if (!running) {
            return;
        }

        // echo to other players
        if (user != null) {
            Main.clientHandler.broadcast(MessageOutServerMessage.build(user.username + " has left the server.", Color.yellow), -1, this);

            MessageOutPlayerLeft response = new MessageOutPlayerLeft();
            response.userid = playerid;
            Main.clientHandler.broadcast(response, user.rm, this);
        }

        if (playerid >= 0)
            Main.clientHandler.players[playerid] = null;
        if (user != null) {
            if(user.status != 0)
                user.playTime += (System.currentTimeMillis() - user.lastLogin);

            if(user.status == 1)
                user.status = 0;
        }
        Main.clientHandler.clients.remove(this);

        running = false;

        if (session.isOpen()) {
            session.close();
        }
        Logger.log(this + " has disconnected.");
    }
}
