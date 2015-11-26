package com.aegamesi.squeebsserver;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Util {
    public static String motd = "Welcome to OldSchool Squeebs!";
    public static String[] motd_quotes = {"In Beta 3 since 2007!", "Now even less secure!", "The game's gonna crash, don't blame me!", "Now with 100% more misspellings of 'shield'!", "Reborn almost exactly the same.", "12 Years a Squeeb", "Serving up Squeebs since 2005."};

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
}
