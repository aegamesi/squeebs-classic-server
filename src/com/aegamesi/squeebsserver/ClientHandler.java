package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.*;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientHandler {
    public List<Client> clients;
    public Client[] players;

    public ClientHandler() {
        clients = new ArrayList<>();
        players = new Client[Main.PLAYER_MAX];
    }

    public void handlePacket(int type, Client client) throws IOException {
        //Logger.log(client + " gives " + type);

        switch (type) {
            case 31: {
                MessageInHello msg = new MessageInHello();
                msg.read(client.buffer);

                if (msg.version != Main.PROTOCOL_VERSION) {
                    MessageOutConnectResponse response = new MessageOutConnectResponse();
                    response.message = "Your client version is out of date. Please redownload.";
                    client.sendMessage(response);
                    break;
                }

                MessageOutHello response = new MessageOutHello();
                response.msg = "Welcome to the aegamesi OldSchool Squeebs Server!\nLogin and registration are now combined.";
                client.sendMessage(response);
            }
            break;
            case 21: {
                MessageInLogin msg = new MessageInLogin();
                msg.read(client.buffer);

                boolean newAccount = false;
                String username = msg.username.trim().replace(' ', '_');

                Database.User user = null;
                for (Database.User u : Main.db.users)
                    if (u.username.equalsIgnoreCase(username))
                        user = u;

                if (user == null) {
                    // REGISTER ACCOUNT!
                    newAccount = true;
                    user = new Database.User();
                    user.username = username;
                    user.password = msg.password;
                    Main.db.users.add(user);

                    Logger.log("Created a new account " + username + " with password " + user.password);
                    // NO BREAK!
                } else if (user.status == 1) {
                    // already online
                    MessageOutConnectResponse response = new MessageOutConnectResponse();
                    response.message = "This account is already logged in.";
                    client.sendMessage(response);
                    break;
                } else if (user.status == 2) {
                    // user is banned
                    MessageOutConnectResponse response = new MessageOutConnectResponse();
                    response.message = "This account is banned.";
                    client.sendMessage(response);
                    break;
                } else if (!user.password.equals(msg.password)) {
                    // invalid password
                    MessageOutConnectResponse response = new MessageOutConnectResponse();
                    response.message = "Incorrect password.";
                    client.sendMessage(response);
                    break;
                }

                user.status = 1; // set to online
                client.user = user;

                int playerid = Util.findSlot(players);
                if (playerid == -1) {
                    MessageOutConnectResponse response = new MessageOutConnectResponse();
                    response.message = "Player cap reached. Try again later.";
                    client.sendMessage(response);
                    break;
                }
                Logger.log("Player " + user.username + " logged on. ID: " + playerid);
                client.playerid = playerid;
                players[client.playerid] = client;

                MessageOutLoginSuccess response = new MessageOutLoginSuccess();
                if (newAccount)
                    response.message = "Welcome " + user.username + ". You have successfully registered. Have fun!";
                else
                    response.message = "Welcome " + user.username + ". You have successfully logged in. Have fun!";
                response.user = user;
                client.sendMessage(response);

                MessageOutPlayerID pidMessage = new MessageOutPlayerID();
                pidMessage.playerid = playerid;
                client.sendMessage(pidMessage);

                // tell all other users about this one
                MessageOutNewPlayer newPlayerMessage = new MessageOutNewPlayer();
                newPlayerMessage.username = user.username;
                newPlayerMessage.playerid = playerid;
                newPlayerMessage.admin = user.rank;
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null || player == client)
                        continue;
                    player.sendMessage(newPlayerMessage);
                    player.sendMessage(MessageOutServerMessage.build(user.username + " has joined the server.", Color.yellow));

                    MessageOutNewPlayer thisPlayerMessage = new MessageOutNewPlayer();
                    thisPlayerMessage.username = player.user.username;
                    thisPlayerMessage.playerid = i;
                    thisPlayerMessage.admin = player.user.rank;
                    client.sendMessage(thisPlayerMessage);
                }

                // TODO send monsters

                // TODO send items

                // send motd
                client.sendMessage(MessageOutServerMessage.build(Util.motd, Color.white));
                Random r = new Random();
                String motd_quote = "\"" + Util.motd_quotes[r.nextInt(Util.motd_quotes.length)] + "\"";
                client.sendMessage(MessageOutServerMessage.build(motd_quote, Color.white));
            }
            break;
            case 2: {
                MessageInAppearance msg = new MessageInAppearance();
                msg.read(client.buffer);
                client.cachedAppearance = msg;

                // echo to other players
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null)
                        continue;

                    if (player.user.rm == client.user.rm)
                        player.sendMessage(msg);
                }
            }
            break;
            case 3: {
                MessageInQuit msg = new MessageInQuit();
                msg.read(client.buffer);

                players[client.playerid] = null;
                if (client.user.status == 1)
                    client.user.status = 0;

                // echo to other players
                MessageOutPlayerLeft response = new MessageOutPlayerLeft();
                response.userid = client.playerid;
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null)
                        continue;

                    player.sendMessage(MessageOutServerMessage.build(client.user.username + " has left the server.", Color.yellow));
                    if (player.user.rm == client.user.rm)
                        player.sendMessage(response);
                }

                Logger.log(msg.username + " has left.");
                client.disconnect();
                clients.remove(client);
            }
            break;
            case 4: {
                // incoming chat
                MessageInChat msg = new MessageInChat();
                msg.read(client.buffer);

                // TODO check for admin messages, etc.
                Logger.log("Chat| " + msg.msg);

                // Echo to other players
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null || client == player)
                        continue;

                    player.sendMessage(msg);
                }
            }
            break;

            case 5: {
                // position update
                MessageInPosition msg = new MessageInPosition();
                msg.read(client.buffer);

                // TODO do some sort of verification...
                client.user.x = msg.x;
                client.user.y = msg.y;
                client.user.rm = msg.rm;

                // Echo to other players
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null || client == player)
                        continue;

                    if (player.user.rm == client.user.rm)
                        player.sendMessage(msg);
                }
            }
            break;

            case 6: {
                // spawn monster
                MessageInSpawnMonster msg = new MessageInSpawnMonster();
                msg.read(client.buffer);

                Database.Monster monster = new Database.Monster();
                monster.x = msg.x;
                monster.y = msg.y;
                monster.t = msg.t;
                monster.hp = msg.hp;
                monster.m_hp = msg.hp;
                monster.xp = msg.xp;
                monster.rm = msg.rm;
                monster.id = Util.findSlot(Main.db.monsters);
                monster.new_x = msg.x;
                Main.db.monsters[monster.id] = monster;

                // tell people
                MessageOutSpawnMonster spawn = new MessageOutSpawnMonster();
                spawn.x = monster.x;
                spawn.y = monster.y;
                spawn.t = monster.t;
                spawn.id = monster.id;
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null)
                        continue;

                    if (player.user.rm == monster.rm)
                        player.sendMessage(spawn);
                }
            }
            break;

            case 7: {
                // damage monster
                MessageInDamageMonster msg = new MessageInDamageMonster();
                msg.read(client.buffer);

                if (Main.db.monsters[msg.mid] != null) {
                    int xp = Math.round(((float) Main.db.monsters[msg.mid].xp * ((float) msg.dmg / (float) Main.db.monsters[msg.mid].m_hp)) + 0.5f);
                    Main.db.monsters[msg.mid].hp -= msg.dmg;
                    Main.db.monsters[msg.mid].ttl = 60.0f;

                    // echo to other players
                    MessageOutDamageMonster echoMsg = new MessageOutDamageMonster();
                    echoMsg.mid = msg.mid;
                    echoMsg.dmg = msg.dmg;
                    echoMsg.sp = msg.sp;
                    for (int i = 0; i < players.length; i++) {
                        Client player = players[i];
                        if (player == null || client == player)
                            continue;

                        if (Main.db.monsters[msg.mid].rm == player.user.rm)
                            player.sendMessage(echoMsg);
                    }

                    // send xp
                    MessageOutMonsterXP xpMsg = new MessageOutMonsterXP();
                    xpMsg.mid = msg.mid;
                    xpMsg.xp = xp;
                    client.sendMessage(xpMsg);
                }
            }
            break;

            case 8: {
                // update room
                MessageInUpdateRoom msg = new MessageInUpdateRoom();
                msg.read(client.buffer);
                client.user.rm = msg.rm;

                MessageOutNewPlayer sourceNewPlayerMsg = new MessageOutNewPlayer();
                sourceNewPlayerMsg.playerid = client.playerid;
                sourceNewPlayerMsg.username = client.user.username;
                sourceNewPlayerMsg.admin = client.user.rank;
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null || client == player)
                        continue;

                    if (msg.rm == player.user.rm) {
                        MessageOutNewPlayer newPlayerMsg = new MessageOutNewPlayer();
                        newPlayerMsg.playerid = player.playerid;
                        newPlayerMsg.admin = player.user.rank;
                        newPlayerMsg.username = player.user.username;
                        client.sendMessage(newPlayerMsg);
                        client.sendMessage(player.cachedAppearance);
                        player.sendMessage(sourceNewPlayerMsg);
                    }
                }
                for(int i = 0; i < Main.db.monsters.length; i++) {
                    Database.Monster m = Main.db.monsters[i];
                    if (m == null)
                        continue;

                    if(m.rm == msg.rm) {
                        MessageOutSpawnMonster monsterMsg = new MessageOutSpawnMonster();
                        monsterMsg.id = i;
                        monsterMsg.x = m.new_x;
                        monsterMsg.y = m.y;
                        monsterMsg.t = m.t;
                        client.sendMessage(monsterMsg);
                    }
                }

                // TODO items
                /*
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
                */
            }
            break;

            case 9: {
                // change room
                MessageInChangeRoom msg = new MessageInChangeRoom();
                msg.read(client.buffer);

                // okay, whatever
                client.user.rm = msg.rm;
                MessageOutChangeRoom reply = new MessageOutChangeRoom();
                reply.rm = msg.rm;
                client.sendMessage(reply);

                // echo to other players
                MessageOutPlayerLeft echoMsg = new MessageOutPlayerLeft();
                echoMsg.userid = client.playerid;
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null || client == player)
                        continue;

                    player.sendMessage(echoMsg);
                }
            }
            break;

            case 10: {
                // TODO
                // save. (aka update info) -- we save on our own time.
            }
            break;

            case 11: {
                // player damage
                MessageInDamagePlayer msg = new MessageInDamagePlayer();
                msg.read(client.buffer);

                // echo to other players
                MessageOutDamagePlayer echoMsg = new MessageOutDamagePlayer();
                echoMsg.userid = client.playerid;
                echoMsg.damage = msg.damage;
                for (int i = 0; i < players.length; i++) {
                    Client player = players[i];
                    if (player == null || client == player)
                        continue;

                    player.sendMessage(echoMsg);
                }
            }
            break;

            default:
                Logger.log("Unknown message type: " + type);
                break;
        }
    }

    public void hackAttempt(Client c) {
        Logger.log("Hack attempt from " + c);
    }

    public void handleNewClient(Socket socket) {
        Client newClient = new Client(this, socket);
        newClient.start();

        clients.add(newClient);
    }
}
