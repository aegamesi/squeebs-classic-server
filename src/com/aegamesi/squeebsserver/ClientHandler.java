package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
    public List<Client> clients;

    public ClientHandler() {
        clients = new ArrayList<>();
    }

    public void handlePacket(int type, Client client) throws IOException {
        //Logger.log(client + " gives " + type);

        switch(type) {
            case 20: {
                MessageRegisterAccount msg = new MessageRegisterAccount();
                msg.read(client.buffer);

                // TODO validation, check if same version

                String username = msg.username.trim().replace(' ', '_');
                boolean exists = false;
                for(Database.User u : Main.db.users)
                    if(u.username.equalsIgnoreCase(username))
                        exists = true;

                if(exists) {
                    // respond
                    MessageLoginResponseMessage response = new MessageLoginResponseMessage();
                    response.message = "This username, " + username + ", is already taken.";
                    client.sendMessage(response);
                } else {
                    Database.User u = new Database.User();
                    u.username = username;
                    u.password = msg.password;
                    Main.db.users.add(u);

                    Logger.log("Created a new account " + username + " with password " + u.password);

                    // respond
                    MessageLoginResponseMessage response = new MessageLoginResponseMessage();
                    response.message = "Your account, " + username + ", has been created.";
                    client.sendMessage(response);
                }
                break;
            }
            case 21: {
                MessageLoginAccount msg = new MessageLoginAccount();
                msg.read(client.buffer);

                String username = msg.username.trim().replace(' ', '_');

                Database.User user = null;
                for(Database.User u : Main.db.users)
                    if(u.username.equalsIgnoreCase(username))
                        user = u;

                if(user == null) {
                    // respond
                    MessageLoginResponseMessage response = new MessageLoginResponseMessage();
                    response.message = "This username, " + username + ", does not exist.";
                    client.sendMessage(response);
                } else if(user.status == 1) {
                    MessageLoginResponseMessage response = new MessageLoginResponseMessage();
                    response.message = "This account is already logged in.";
                    client.sendMessage(response);
                } else if(user.status == 2) {
                    MessageLoginResponseMessage response = new MessageLoginResponseMessage();
                    response.message = "This account is banned.";
                    client.sendMessage(response);
                } else if(!user.password.equals(msg.password)) {
                    MessageLoginResponseMessage response = new MessageLoginResponseMessage();
                    response.message = "Incorrect password.";
                    client.sendMessage(response);
                } else {
                    user.status = 1; // set to online
                    client.user = user;

                    MessageLoginValid response = new MessageLoginValid();
                    response.message = "Welcome " + user.username + ". You have successfully logged in. Press OK to continue to the game.";
                    response.user = user;
                    client.sendMessage(response);

                    // TODO better way for playerid-- otherwise they'll just get changed when a client leaves... which is very bad
                    int playerid = clients.indexOf(client);

                    MessagePlayerID pidMessage = new MessagePlayerID();
                    pidMessage.playerid = playerid;
                    client.sendMessage(pidMessage);


                    // tell all other users about this one
                    MessageNewPlayer newPlayerMessage = new MessageNewPlayer();
                    newPlayerMessage.username = user.username;
                    newPlayerMessage.playerid = playerid;
                    newPlayerMessage.admin = user.rank;
                    for(Client c : clients) {
                        if(c == client || c.playerid == -1)
                            continue;
                        c.sendMessage(newPlayerMessage);

                        MessageNewPlayer thisPlayerMessage = new MessageNewPlayer();
                        thisPlayerMessage.username = c.user.username;
                        thisPlayerMessage.playerid = c.playerid;
                        thisPlayerMessage.admin = c.user.rank;
                        client.sendMessage(thisPlayerMessage);
                    }

                    // TODO send monsters

                    // TODO send items
                }
                break;
            }
            case 2: {
                MessageUpdateAppearance msg = new MessageUpdateAppearance();
                msg.read(client.buffer);

                // echo to other players
                for(Client c : clients) {
                    if(c == client)
                        continue;
                    if(c.user.rm == client.user.rm) {
                        c.sendMessage(msg);
                    }
                }
            }
            case 3: {
                MessageQuit msg = new MessageQuit();
                msg.read(client.buffer);

                // echo to other players
                MessagePlayerLeft response = new MessagePlayerLeft();
                response.userid = msg.userid;
                for(Client c : clients) {
                    if(c.user.rm == client.user.rm) {
                        c.sendMessage(response);
                    }
                }

                Logger.log(msg.username + " has left.");
                client.disconnect();
                clients.remove(client);
            }
            case 4: {
                // incoming chat
                MessageIncomingChat msg = new MessageIncomingChat();
                msg.read(client.buffer);

                // TODO check for admin messages, etc.

                // echo
                for(Client c : clients) {
                    if(c == client)
                        continue;
                    c.sendMessage(msg);
                }
            }
            default:
                Logger.log("Unknown message type: " + type);
                break;
        }
    }

    public void handleNewClient(Socket socket) {
        Client newClient = new Client(this, socket);
        newClient.start();

        clients.add(newClient);
    }
}
