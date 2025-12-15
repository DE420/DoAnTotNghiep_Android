package com.example.fitnessapp.utils;

import java.util.regex.Pattern;

public class StringUtil {

    public static boolean patternMatches(String str, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(str)
                .matches();
    }

}
