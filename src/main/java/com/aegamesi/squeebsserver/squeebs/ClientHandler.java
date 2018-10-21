package com.aegamesi.squeebsserver.squeebs;

import com.aegamesi.squeebsserver.util.Logger;
import com.aegamesi.squeebsserver.Main;
import com.aegamesi.squeebsserver.util.Util;
import com.aegamesi.squeebsserver.messages.*;
import com.github.sheigutn.pushbullet.items.push.sendable.defaults.SendableNotePush;

import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler {
    public List<Client> clients;
    public Client[] players;

    public ClientHandler() {
        clients = new ArrayList<>();
        players = new Client[Main.PLAYER_MAX];
    }

    private static void popupMessage(Client sender, String msg) throws IOException {
        MessageOutPopup response = new MessageOutPopup();
        response.style = MessageOutPopup.STYLE_MESSAGE;
        response.msg = msg;
        sender.sendMessage(response);
    }

    private static void popupPrompt(Client sender, int tag, String msg, String def) throws IOException {
        MessageOutPopup response = new MessageOutPopup();
        response.style = MessageOutPopup.STYLE_PROMPT;
        response.msg = msg;
        response.def = def;
        response.tag = tag;
        sender.sendMessage(response);
    }

    private static void popupButtons(Client sender, int tag, String msg, String btn1, String btn2, String btn3) throws IOException {
        MessageOutPopup response = new MessageOutPopup();
        response.style = MessageOutPopup.STYLE_BUTTONS;
        response.msg = msg;
        response.tag = tag;
        response.button1 = btn1;
        response.button2 = btn2;
        response.button3 = btn3;
        sender.sendMessage(response);
    }

    private static void sendLoginPrompt(Client sender) throws IOException {
        sender._tempPassword = null;
        sender._tempUsername = null;
        popupButtons(sender, 1, Main.config.welcome, "Login", "Register", "Exit");
    }

    public void handlePacket(int type, int messageSize, Client sender) throws IOException {
        // Logger.log(sender + " gives " + type);

        switch (type) {
            case 31: {
                MessageInHello msg = new MessageInHello();
                msg.read(sender.buffer);

                if (msg.version != Main.PROTOCOL_VERSION) {
                    MessageOutConnectResponse response = new MessageOutConnectResponse();
                    response.message = "Your client version is out of date.\nPlease redownload from facebook.com/squeebs";
                    sender.sendMessage(response);
                    sender.disconnect();
                    break;
                }

                sendLoginPrompt(sender);
            }
            break;

            case 36: {
                MessageInPopupResponse msg = new MessageInPopupResponse();
                msg.read(sender.buffer);

                switch (msg.tag) {
                    case 1: { // Login or Register?
                        switch (msg.button) {
                            case 1: // login
                                popupPrompt(sender, 5, "Please enter your username:", "");
                                break;
                            case 2: // register
                                popupPrompt(sender, 2, "Enter your desired username:", "");
                                break;
                            default:
                                MessageOutConnectResponse response = new MessageOutConnectResponse();
                                response.message = "";
                                sender.sendMessage(response);
                                sender.disconnect();
                                break;
                        }
                    }
                    break;
                    case 2: { // register got username
                        String username = msg.response.trim().replace(' ', '_');
                        if (username.length() == 0) {
                            sendLoginPrompt(sender);
                            break;
                        }
                        Database.User user = Main.db.findUserByName(username);
                        if (user == null) {
                            sender._tempUsername = username;
                            popupPrompt(sender, 3, "Enter your password:", "");
                        } else {
                            popupPrompt(sender, 2, "That username is taken. Enter your desired username:", username);
                        }
                    }
                    break;
                    case 3: { // register password 1
                        String password = msg.response;
                        if (password.length() == 0) {
                            sendLoginPrompt(sender);
                        } else {
                            sender._tempPassword = password;
                            popupPrompt(sender, 4, "Confirm your password:", "");
                        }
                    }
                    break;
                    case 4: { // register password 2
                        String password = msg.response;
                        if (!password.equals(sender._tempPassword)) {
                            popupPrompt(sender, 3, "Your passwords do not match. Enter your password:", "");
                        } else {
                            // complete!
                            Database.User user = new Database.User();
                            user.username = sender._tempUsername;
                            user.password = sender._tempPassword;
                            user.firstLogin = System.currentTimeMillis();
                            Main.db.users.add(user);

                            Logger.log("Created a new account " + user.username + " with password " + user.password);

                            popupMessage(sender, "You've successfully registered, " + user.username + "!");
                            sendLoginPrompt(sender);

                            // notify via pushbullet
                            if (Main.pushbullet != null) {
                                SendableNotePush notePush = new SendableNotePush("Squeebs Register", "Register from " + user.username);
                                Main.pushbullet.pushToAllDevices(notePush);
                            }
                        }
                    }
                    break;
                    case 5: { // login username
                        String username = msg.response.trim().replace(' ', '_');
                        if (username.length() == 0) {
                            sendLoginPrompt(sender);
                            break;
                        }

                        Database.User user = Main.db.findUserByName(username);
                        if (user == null) {
                            popupMessage(sender, "User not found. Have you registered?");
                            sendLoginPrompt(sender);
                        } else {
                            sender._tempUsername = username;
                            popupPrompt(sender, 6, "Please enter your password:", "");
                        }
                    }
                    break;
                    case 6: { // login password
                        Database.User user = Main.db.findUserByName(sender._tempUsername);

                        if (!user.password.equals(msg.response)) {
                            popupMessage(sender, "Incorrect password.");
                            sendLoginPrompt(sender);

                            Logger.log("Incorrect password for " + user.username);
                            break;
                        } else if (user.status == 1) {
                            // already online
                            popupMessage(sender, "This account is already logged in.");
                            sendLoginPrompt(sender);
                            break;
                        } else if (user.status == 2) {
                            // user is banned
                            popupMessage(sender, "This account is banned.");
                            sendLoginPrompt(sender);

                            Logger.log("Banned user " + user.username + " tried to log in.");
                            break;
                        }

                        // otherwise...
                        int playerid = Util.findSlot(players);
                        if (playerid == -1) {
                            popupMessage(sender, "Player cap reached. Try again later.");
                            sendLoginPrompt(sender);
                            break;
                        }

                        boolean newAccount = user.lastLogin <= 0;
                        user.status = 1; // set to online
                        user.lastLogin = System.currentTimeMillis();
                        sender.user = user;

                        Logger.log("Player " + user.username + " logged on. ID: " + playerid);
                        sender.playerid = playerid;
                        players[sender.playerid] = sender;

                        popupMessage(sender, "Welcome " + user.username + ". You have successfully logged in. Have fun!");
                        MessageOutLoginSuccess response = new MessageOutLoginSuccess();
                        response.user = user;
                        sender.sendMessage(response);

                        MessageOutPlayerID pidMessage = new MessageOutPlayerID();
                        pidMessage.playerid = playerid;
                        sender.sendMessage(pidMessage);

                        if(newAccount)
                            broadcast(MessageOutServerMessage.build("Welcome " + user.username + " to Squeebs!", Color.yellow), -1, sender);
                        else
                            broadcast(MessageOutServerMessage.build(user.username + " has logged on.", Color.yellow), -1, sender);

                        updatePlayerRoom(sender);

                        // send motd
                        sender.sendMessage(MessageOutServerMessage.build(Main.config.motd, Color.white));
                        String motd_quote = "\"" + Util.motd_quotes[Util.random.nextInt(Util.motd_quotes.length)] + "\"";
                        sender.sendMessage(MessageOutServerMessage.build(motd_quote, Color.white));

                        if(newAccount) {
                            // send how to play
                            for(String line : Util.guide)
                                sender.sendMessage(MessageOutServerMessage.build(line, Color.lightGray));
                        }

                        // notify via pushbullet
                        if (Main.pushbullet != null) {
                            SendableNotePush notePush = new SendableNotePush("Squeebs Login", "Login from " + user.username);
                            Main.pushbullet.pushToAllDevices(notePush);
                        }
                    }
                }
            }
            break;

            case 2: {
                MessageInAppearance msg = new MessageInAppearance();
                msg.read(sender.buffer);
                msg.userid = sender.playerid;

                sender.cachedAppearance = msg;

                // echo to other players
                broadcast(msg, sender.user.rm, null);
            }
            break;

            case 3: {
                MessageInQuit msg = new MessageInQuit();
                msg.read(sender.buffer);

                sender.disconnect();

                // echo to other players
                broadcast(MessageOutServerMessage.build(sender.user.username + " has left the server.", Color.yellow), -1, null);

                MessageOutPlayerLeft response = new MessageOutPlayerLeft();
                response.userid = sender.playerid;
                broadcast(response, sender.user.rm, null);

            }
            break;

            case 4: {
                // incoming chat
                MessageInChat msg = new MessageInChat();
                msg.read(sender.buffer);

                Logger.log("[" + sender.user.username + "] " + msg.msg);

                if(msg.msg.startsWith("/")) {
                    sender.sendMessage(MessageOutServerMessage.build(msg.msg, Color.yellow));
                    CommandHandler.runCommand(msg.msg.substring(1), sender);
                } else {
                    // Echo to other players
                    // TODO profanity filter?
                    MessageOutServerMessage echo = MessageOutServerMessage.build(sender.user.username + ": " + msg.msg, Color.white);
                    broadcast(echo, -1, null);
                }
            }
            break;

            case 5: {
                // position update
                MessageInPosition msg = new MessageInPosition();
                msg.read(sender.buffer);

                // TODO do some sort of verification...
                sender.user.x = msg.x;
                sender.user.y = msg.y;
                sender.user.rm = msg.rm;

                // Echo to other players
                MessageOutPosition echoMsg = new MessageOutPosition();
                echoMsg.userid = msg.userid;
                echoMsg.x = msg.x;
                echoMsg.y = msg.y;
                echoMsg.spr = msg.spr;
                echoMsg.img = msg.img;
                echoMsg.spr_dir = msg.spr_dir;
                echoMsg.move = msg.move;
                echoMsg.jmp = msg.jmp;
                echoMsg.rm = msg.rm;
                broadcast(echoMsg, sender.user.rm, sender);
            }
            break;

            case 6: {
                // spawn monster
                MessageInSpawnMonster msg = new MessageInSpawnMonster();
                msg.read(sender.buffer);

                /*Database.Monster monster = new Database.Monster();
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
                broadcast(spawn, monster.rm, null);*/
            }
            break;

            case 7: {
                // damage monster
                MessageInDamageMonster msg = new MessageInDamageMonster();
                msg.read(sender.buffer);
                Database.Monster monster = Main.db.monsters[msg.mid];

                if (monster != null) {
                    // add damage to damage map for xp purposes
                    int actual_damage = Math.min(monster.hp, msg.dmg);
                    int previous_damage = monster.damageMap.containsKey(sender.user.username) ? monster.damageMap.get(sender.user.username) : 0;
                    monster.damageMap.put(sender.user.username, previous_damage + actual_damage);

                    monster.hp -= msg.dmg;
                    monster.ttl = 60.0f;

                    // echo to other players
                    MessageOutDamageMonster echoMsg = new MessageOutDamageMonster();
                    echoMsg.mid = msg.mid;
                    echoMsg.dmg = msg.dmg;
                    echoMsg.sp = msg.sp;
                    broadcast(echoMsg, monster.rm, sender); /// XXX check if this should actually not be sent to the player who sent it
                }
            }
            break;

            case 8: {
                // update room
                MessageInUpdateRoom msg = new MessageInUpdateRoom();
                msg.read(sender.buffer);
                sender.user.rm = msg.rm;

                updatePlayerRoom(sender);
            }
            break;

            case 9: {
                // use portal
                MessageInUsePortal msg = new MessageInUsePortal();
                msg.read(sender.buffer);

                Database.PortalInfo portal = null;
                if(msg.portal_id == -1) {
                    portal = Database.portalInfo.get(1); // respawn
                    sender.sendMessage(MessageOutServerMessage.build("Oh dear, you are dead!", Color.red));
                } else {
                    Database.PortalInfo source = Database.portalInfo.get(msg.portal_id);
                    if (source == null || source.rm != sender.user.rm)
                        break;
                    portal = Database.portalInfo.get(source.target);
                }

                sender.user.rm = portal.rm;
                sender.user.x = portal.x;
                sender.user.y = portal.y;
                MessageOutChangeRoom reply = new MessageOutChangeRoom();
                reply.rm = portal.rm;
                reply.x = portal.x;
                reply.y = portal.y;
                sender.sendMessage(reply);

                // echo to other players
                MessageOutPlayerLeft echoMsg = new MessageOutPlayerLeft();
                echoMsg.userid = sender.playerid;
                broadcast(echoMsg, -1, sender);

                // tell monster spawners
                for (MonsterSpawner spawner : Main.db.spawners)
                    if(portal.rm == spawner.rm)
                        spawner.playerEntered();
            }
            break;

            case 10: {
                // save. (aka update info) -- we save on our own time.
                MessageInUpdatePlayer msg = new MessageInUpdatePlayer();
                msg.user = sender.user;
                msg.read(sender.buffer);
            }
            break;

            case 11: {
                // player damage
                MessageInDamagePlayer msg = new MessageInDamagePlayer();
                msg.read(sender.buffer);

                // echo to other players
                MessageOutDamagePlayer echoMsg = new MessageOutDamagePlayer();
                echoMsg.userid = sender.playerid;
                echoMsg.damage = msg.damage;
                broadcast(echoMsg, sender.user.rm, sender);
            }
            break;

            case 12: {
                // spawn item
                MessageInCreateItem msg = new MessageInCreateItem();
                msg.read(sender.buffer);

                Database.Item item = new Database.Item();
                item.x = msg.x;
                item.y = msg.y;
                item.t = msg.t;
                item.amt = msg.amt;
                item.rm = msg.rm;
                item.iid = Util.findSlot(Main.db.items);
                Main.db.items[item.iid] = item;

                // tell people
                MessageOutCreateItem spawn = new MessageOutCreateItem();
                spawn.x = item.x;
                spawn.y = item.y;
                spawn.t = item.t;
                spawn.iid = item.iid;
                spawn.amt = item.amt;
                broadcast(spawn, item.rm, null);
            }
            break;

            case 13: {
                // take item
                MessageInTakeItem msg = new MessageInTakeItem();
                msg.read(sender.buffer);

                Database.Item item = Main.db.items[msg.iid];
                if (item != null && sender.user.rm == item.rm) {
                    MessageOutTakeItem echoMsg = new MessageOutTakeItem();
                    echoMsg.iid = msg.iid;
                    echoMsg.user = sender.playerid;
                    broadcast(echoMsg, item.rm, null);

                    Main.db.items[msg.iid] = null;
                }
            }
            break;

            default:
                Logger.log("Unknown message type: " + type + " / size: " + messageSize);
                break;
        }
    }

    public void updatePlayerRoom(Client client) throws IOException {
        // tell client the room name
        Database.RoomInfo rmInfo = Database.roomInfo.get(client.user.rm);
        client.sendMessage(MessageOutFloatingMessage.build(rmInfo.name, 64, 32, 150, Color.YELLOW, Color.DARK_GRAY));

        MessageOutNewPlayer sourceNewPlayerMsg = new MessageOutNewPlayer();
        sourceNewPlayerMsg.playerid = client.playerid;
        sourceNewPlayerMsg.username = client.user.username;
        sourceNewPlayerMsg.admin = client.user.rank;
        for (Client player : players) {
            if (player == null || client == player)
                continue;

            if (client.user.rm == player.user.rm) {
                MessageOutNewPlayer newPlayerMsg = new MessageOutNewPlayer();
                newPlayerMsg.playerid = player.playerid;
                newPlayerMsg.admin = player.user.rank;
                newPlayerMsg.username = player.user.username;
                client.sendMessage(newPlayerMsg);
                client.sendMessage(player.cachedAppearance);
                player.sendMessage(sourceNewPlayerMsg);
                if (client.cachedAppearance != null)
                    player.sendMessage(client.cachedAppearance);
            }
        }
        for (int i = 0; i < Main.db.monsters.length; i++) {
            Database.Monster m = Main.db.monsters[i];
            if (m == null)
                continue;

            if (m.rm == client.user.rm) {
                MessageOutSpawnMonster monsterMsg = new MessageOutSpawnMonster();
                monsterMsg.id = i;
                monsterMsg.x = m.new_x;
                monsterMsg.y = m.y;
                monsterMsg.t = m.t;
                client.sendMessage(monsterMsg);
            }
        }
        for (int i = 0; i < Main.db.items.length; i++) {
            Database.Item m = Main.db.items[i];
            if (m == null)
                continue;

            if (m.rm == client.user.rm) {
                MessageOutCreateItem itemMsg = new MessageOutCreateItem();
                itemMsg.iid = i;
                itemMsg.x = m.x;
                itemMsg.y = m.y;
                itemMsg.amt = m.amt;
                itemMsg.t = m.t;
                client.sendMessage(itemMsg);
            }
        }
    }

    public Client getClientByUsername(String username) {
        for (Client player : players) {
            if (player == null || player.user == null)
                continue;

            if(player.user.username.equalsIgnoreCase(username))
                return player;
        }
        return null;
    }

    public int broadcast(Message message, int rm, Client not_client) {
        int num = 0;
        for(Client player : players) {
            if (player == null || player.user == null)
                continue;

            if(rm != -1 && player.user.rm != rm)
                continue;

            if(not_client != null && player == not_client)
                continue;

            try {
                player.sendMessage(message);
            } catch(IOException e) {
                player.disconnect();
            }
            num++;
        }
        return num;
    }

    public void handleNewClient(Socket socket) {
        Client newClient = new Client(this, socket);
        newClient.start();

        clients.add(newClient);
    }
}
