package com.aegamesi.squeebsserver;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Util {
    public static Random random = new Random();
    public static String motd = "Welcome to OldSchool Squeebs!";
    public static String[] motd_quotes = {"In Beta 3 since 2007!", "Now even less secure!", "The game's gonna crash, don't blame me!", "Now with 100% more misspellings of 'shield'!", "Reborn almost exactly the same.", "12 Years a Squeeb", "Serving up Squeebs since 2005."};
    public static String[] guide = {"How to Play Squeebs", "You can move with the arrow keys.", "Press 'Z' to attack, 'X' to jump, and 'C' to pick up items.", "Press Enter to chat.", "To open your inventory, press the 'Inventory' button.", "Left click to equip an item, right click to drop it.", "You can view this guide at any time with '/guide'."};

    public static int colorToGM(Color color) {
        // GM does $BBGGRR
        int bgr = color.getBlue();
        bgr = (bgr << 8) + color.getGreen();
        bgr = (bgr << 8) + color.getRed();
        return bgr;
    }

    public static int findSlot(Object[] arr) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i] == null)
                return i;
        return -1;
    }

    public static void writeStringToFile(File f, String s) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(f));
        writer.write(s);
        writer.close();
    }

    public static int getMonstersInRoom(int rm) {
        int n = 0;
        for(Database.Monster m : Main.db.monsters)
            if(m != null && m.rm == rm)
                n++;
        return n;
    }
    public static int getPlayersInRoom(int rm) {
        int n = 0;
        for(Database.User m : Main.db.users)
            if(m != null && m.rm == rm && m.status == 1)
                n++;
        return n;
    }
    public static int getItemsInRoom(int rm) {
        int n = 0;
        for(Database.Item m : Main.db.items)
            if(m != null && m.rm == rm)
                n++;
        return n;
    }
    public static boolean probability(double r) {
        // r between 0.0 and 1.0...
        double comp = random.nextDouble();
        return comp < r;
    }
}
