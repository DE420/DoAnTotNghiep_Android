package com.example.fitnessapp.utils;

public class CountFormatUtils {

    public static String formatCount(Long count) {

        if (count < 1000) {
            return String.valueOf(count);
        }

        if (count < 10_000) {
            return String.format("%.1fK", count / 1000f)
                    .replace(".0", "");
        }

        if (count < 1_000_000) {
            return (count / 1000) + "K";
        }

        if (count < 10_000_000) {
            return String.format("%.1fM", count / 1_000_000f)
                    .replace(".0", "");
        }

        return (count / 1_000_000) + "M+";
    }
}

