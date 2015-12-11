package com.aegamesi.squeebsserver.squeebs;

import com.aegamesi.squeebsserver.messages.MessageOutPlayerLeft;
import com.aegamesi.squeebsserver.util.Logger;
import com.aegamesi.squeebsserver.Main;
import com.aegamesi.squeebsserver.util.Util;
import com.aegamesi.squeebsserver.messages.MessageOutChangeRoom;
import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;
import com.aegamesi.squeebsserver.messages.MessageOutKick;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommandHandler {
    public static final int PLAYER = 0;
    public static final int MODERATOR = 1;
    public static final int ADMIN = 2;
    public static final int CONSOLE = 3;

    public static Map<String, CommandInfo> commands = new HashMap<>();

    static {
        addCommand(new String[]{"chat", "say"}, CONSOLE, 1, -1, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                String say = "Server: " + cmd.substring(args[0].length() + 1);
                out.print(say);
                // broadcast
                Main.clientHandler.broadcast(MessageOutServerMessage.build(say, Color.red), -1, null);
            }
        });
        addCommand(new String[]{"stats"}, ADMIN, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                out.print("Server Stats:");
                long stat_uptime = System.currentTimeMillis() - Main.program_start_time;
                out.print("Uptime: " + Util.formatDuration(stat_uptime));

                float stat_data_sent = ((float)Main.bytes_sent / 1000.0f);
                float stat_data_recv = ((float)Main.bytes_received / 1000.0f);
                out.print("Data Sent: " + stat_data_sent + "kb / Recv: " + stat_data_recv + "kb");

                int stat_players_total = 0;
                int stat_players_online = 0;
                int stat_players_offline = 0;
                int stat_players_banned = 0;
                for(Database.User u : Main.db.users) {
                    stat_players_total++;
                    if(u.status == 0)
                        stat_players_offline++;
                    if(u.status == 1)
                        stat_players_online++;
                    if(u.status == 2)
                        stat_players_banned++;
                }
                out.print("Players: online/offline/banned (" + stat_players_online + ", " + stat_players_offline + ", " + stat_players_banned + ") total (" + stat_players_total + ")");

                int stat_monsters = 0;
                int stat_items = 0;
                for(Database.Item i : Main.db.items)
                    if(i != null)
                        stat_items++;
                for(Database.Monster m : Main.db.monsters)
                    if(m != null)
                        stat_monsters++;
                out.print("Items: " + stat_items + ", Monsters: " + stat_monsters);

            }
        });
        addCommand(new String[]{"save"}, ADMIN, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                Main.db.save();
                out.print("Saved database.");
            }
        });
        addCommand(new String[]{"guide"}, PLAYER, 0, 0, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException {
                for(String line : Util.guide)
                    out.print(line);
            }
        });
        addCommand(new String[]{"shutdown", "stop"}, ADMIN, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                out.print("Stopping server.");
                Main.db.save();

                Main.clientHandler.broadcast(MessageOutKick.build("The server has shut down."), -1, null);
                System.exit(0);
            }
        });
        addCommand(new String[]{"grab", "tphere"}, MODERATOR, 1, 1, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException {
                if(sender == null)
                    return;

                String username = args[1].trim();
                Client player = Main.clientHandler.getClientByUsername(username);
                if(player == null) {
                    out.print("Player not found.");
                } else {
                    out.print("Grabbing " + player.user.username + ".");

                    MessageOutChangeRoom changeRoom = new MessageOutChangeRoom();
                    changeRoom.rm = sender.user.rm;
                    changeRoom.x = sender.user.x;
                    changeRoom.y = sender.user.y;
                    player.sendMessage(changeRoom);
                    player.sendMessage(MessageOutServerMessage.build(sender.user.username + " has teleported you!", Color.lightGray));

                    player.user.rm = sender.user.rm;
                    player.user.x = sender.user.x;
                    player.user.y = sender.user.y;
                }
            }
        });
        addCommand(new String[]{"tp"}, MODERATOR, 1, 1, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException {
                if(sender == null)
                    return;

                String username = args[1].trim();
                Client player = Main.clientHandler.getClientByUsername(username);
                if(player == null) {
                    out.print("Player not found.");
                } else {
                    out.print("Teleporting to " + player.user.username + ".");

                    MessageOutChangeRoom changeRoom = new MessageOutChangeRoom();
                    changeRoom.rm = player.user.rm;
                    changeRoom.x = player.user.x;
                    changeRoom.y = player.user.y;
                    sender.sendMessage(changeRoom);

                    sender.user.rm = player.user.rm;
                    sender.user.x = player.user.x;
                    sender.user.y = player.user.y;
                }
            }
        });
        addCommand(new String[]{"spawn"}, PLAYER, 0, 0, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException {
                if(sender == null)
                    return;

                out.print("Teleporting to spawn.");
                Database.PortalInfo portal = Database.portalInfo.get(1);

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
                Main.clientHandler.broadcast(echoMsg, -1, sender);
            }
        });
        addCommand(new String[]{"kick"}, MODERATOR, 1, 1, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException {
                String username = args[1].trim();
                Client player = Main.clientHandler.getClientByUsername(username);
                if(player == null) {
                    out.print("Player not found.");
                } else {
                    out.print("Kicking " + player.user.username + ".");
                    player.sendMessage(MessageOutKick.build("You have been kicked."));
                    player.disconnect();
                }
            }
        });
        addCommand(new String[]{"ban"}, MODERATOR, 1, 1, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException {
                String username = args[1].trim();
                Client player = Main.clientHandler.getClientByUsername(username);
                if(player == null) {
                    out.print("Player not found.");
                } else {
                    out.print("Kicking/banning " + player.user.username + ".");
                    player.user.status = 2;
                    player.sendMessage(MessageOutKick.build("You have been banned from the server."));
                    player.disconnect();
                }
            }
        });
        addCommand(new String[]{"uptime"}, PLAYER, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                long t_millis = System.currentTimeMillis() - Main.program_start_time;
                out.print("Uptime: " + Util.formatDuration(t_millis));
            }
        });
        addCommand(new String[]{"playtime"}, PLAYER, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                if(sender == null)
                    return;

                long t_millis = sender.user.playTime + (System.currentTimeMillis() - sender.user.lastLogin);
                out.print("Total Playtime: " + Util.formatDuration(t_millis));
            }
        });
        addCommand(new String[]{"players", "online"}, PLAYER, 0, 0, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                String str = "";
                int num = 0;
                for(Client c : Main.clientHandler.clients) {
                    if (c.user != null) {
                        str += c.user.username + " ";
                        num++;
                    }
                }
                out.print("Players Online (" + num + "): " + str);
            }
        });
        addCommand(new String[]{"help", "commands"}, PLAYER, 0, 0, new Command() {
            @Override
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                String commandString = "";
                int rank = CONSOLE;
                if(sender != null) {
                    if(sender.user.rank == 1)
                        rank = ADMIN;
                    else if(sender.user.rank == 2)
                        rank = MODERATOR;
                    else
                        rank = PLAYER;
                }

                Set<CommandInfo> seenCommands = new HashSet<CommandInfo>();
                for(Map.Entry<String, CommandInfo> helpCommand : commands.entrySet()) {
                    if(helpCommand.getValue().permission > rank)
                        continue;
                    if(seenCommands.contains(helpCommand.getValue()))
                        continue;
                    seenCommands.add(helpCommand.getValue());
                    commandString += "/" + helpCommand.getKey() + " ";
                }

                out.print("Commands: " + commandString);
            }
        });
    }

    public static class CommandInfo {
        public int permission;
        public int min;
        public int max;
        public Command command;
    }

    public interface OutputHandler {
        void print(String line);
    }

    public interface Command {
        void run(Client sender, OutputHandler out, String cmd, String[] args) throws IOException;
    }

    public static void runCommand(String line, final Client sender) {
        String[] parts = line.trim().split(" ");
        if(parts.length == 0)
            return;
        int args = parts.length - 1;

        // proper output handler
        OutputHandler outputHandler;
        if(sender == null) {
            outputHandler = new OutputHandler() {
                @Override
                public void print(String line) {
                    Logger.log(line);
                }
            };
        }
        else {
            outputHandler = new OutputHandler() {
                @Override
                public void print(String line) {
                    try {
                        sender.sendMessage(MessageOutServerMessage.build(line, Color.lightGray));
                    } catch(IOException e) {
                    }
                }
            };
        }

        CommandInfo i = commands.get(parts[0].toLowerCase());
        if(i == null) {
            outputHandler.print("Unknown command.");
            return;
        }
        if(sender != null && ((sender.user.rank == 0 && i.permission > PLAYER) || (sender.user.rank == 1 && i.permission > ADMIN) || (sender.user.rank == 2 && i.permission > MODERATOR))) {
            outputHandler.print("Invalid permission.");
            return;
        }
        if((i.min >= 0 && args < i.min) || (i.max >= 0 && args > i.max)) {
            outputHandler.print("Required args: [" + i.min + ", " + i.max + "]. Given: " + args);
            return;
        }

        try {
            i.command.run(sender, outputHandler, line, line.split(" "));
        } catch(IOException e) {

        }
    }

    public static void addCommand(String[] names, int permission, int min, int max, Command command) {
        CommandInfo i = new CommandInfo();
        i.permission = permission;
        i.max = max;
        i.min = min;
        i.command = command;
        for(String name : names)
            commands.put(name, i);
    }
}
