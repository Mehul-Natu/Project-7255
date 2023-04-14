package com.edu.info7255.utils;

import java.util.Random;

public class TimeHexaGenerator {

    public static String getHexaValue() {
        Random random = new Random();
        return String.valueOf(System.nanoTime());
    }
}
