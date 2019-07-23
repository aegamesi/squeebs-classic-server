package com.aegamesi.squeebsserver.squeebs;

import com.aegamesi.squeebsserver.Main;
import com.aegamesi.squeebsserver.util.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebSocket
public class GameWebSocket {
    private Map<Session, Client> clientMap = new HashMap<>();

    @OnWebSocketConnect
    public void connected(Session session) {
        Logger.log("New connection from " + session.getRemoteAddress());

        Client client = new Client(session);
        clientMap.put(session, client);
        Main.clientHandler.handleNewClient(client);
    }

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        Client client = clientMap.get(session);
        client.disconnect();
        clientMap.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, byte[] data, int off, int len) throws IOException {
        Client client = clientMap.get(session);
        client.buffer.clear();
        client.buffer.put(data, off, len);
        client.buffer.position(0);

        int packetType = client.buffer.get();
        Main.bytes_received += len;
        Main.clientHandler.handlePacket(packetType, len - 1, client);
    }
}