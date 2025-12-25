package com.example.fitnessapp.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String getTime(String postTime) {
        LocalDateTime time = LocalDateTime.parse(postTime);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "Just now";
        }

        if (minutes < 60) {
            return minutes + " minutes ago";
        }

        if (hours < 24) {
            return hours + " hours ago";
        }

        if (days == 1) {
            return "Yesterday at " +
                    time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        if (days < 7) {
            return days + " days ago";
        }

        return time.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

}
