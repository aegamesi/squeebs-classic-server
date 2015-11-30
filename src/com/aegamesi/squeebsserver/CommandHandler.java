package com.aegamesi.squeebsserver;

import com.aegamesi.squeebsserver.messages.MessageOutChangeRoom;
import com.aegamesi.squeebsserver.messages.MessageOutPosition;
import com.aegamesi.squeebsserver.messages.MessageOutServerMessage;
import com.aegamesi.squeebsserver.messages.MessageOutKick;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
                out.print("Sent: " + ((float)Main.bytes_sent / 1000.0f) + "kb / Recv: " + ((float)Main.bytes_received / 1000.0f) + "kb");
            }
        });
        addCommand(new String[]{"save"}, ADMIN, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                Main.db.save();
                out.print("Saved database.");
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
        addCommand(new String[]{"grab", "tphere"}, ADMIN, 1, 1, new Command() {
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
                    if(player.user.rm != sender.user.rm) {
                        MessageOutChangeRoom changeRoom = new MessageOutChangeRoom();
                        changeRoom.rm = sender.user.rm;
                        changeRoom.x = sender.user.x;
                        changeRoom.y = sender.user.y;
                        player.sendMessage(changeRoom);
                    } else {
                        MessageOutPosition newPos = new MessageOutPosition();
                        newPos.x = sender.user.x;
                        newPos.y = sender.user.y;
                        newPos.rm = sender.user.rm;
                        player.sendMessage(newPos);
                    }
                    player.user.rm = sender.user.rm;
                    player.user.x = sender.user.x;
                    player.user.y = sender.user.y;
                }
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
        addCommand(new String[]{"uptime"}, MODERATOR, 0, 0, new Command() {
            public void run(Client sender, OutputHandler out, String cmd, String[] args) {
                long t_millis = System.currentTimeMillis() - Main.program_start_time;
                long t_seconds = (t_millis / (1000)) % 60;
                long t_minutes = (t_millis / (1000 * 60)) % 60;
                long t_hours = (t_millis / (1000 * 60 * 60)) % 24;
                long t_days = (t_millis / (1000 * 60 * 60 * 24));
                out.print("Uptime: " + t_days + "d " + t_hours + "h " + t_minutes + "m " + t_seconds + "s");
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
