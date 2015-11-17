package com.aegamesi.squeebsserver;

import java.awt.*;

public class Util {
    public static int colorToGM(Color color) {
        // GM does $BBGGRR
        int bgr = color.getBlue();
        bgr = (bgr << 8) + color.getGreen();
        bgr = (bgr << 8) + color.getRed();
        return bgr;
    }

    public static int findSlot(Object[] arr) {
        for(int i = 0; i < arr.length; i++)
            if(arr[i] == null)
                return i;
        return -1;
    }
}
