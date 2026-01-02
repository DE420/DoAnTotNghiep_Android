package com.example.fitnessapp.util;

import android.content.Context;

import com.example.fitnessapp.R;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeUtil {

    /**
     * Get time string in Vietnamese/English based on locale
     * @deprecated Use getTime(Context, String) instead for proper localization
     */
    @Deprecated
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

    /**
     * Get time string with proper Vietnamese/English localization
     */
    public static String getTime(Context context, String postTime) {
        if (context == null || postTime == null || postTime.isEmpty()) {
            return "";
        }

        try {
            LocalDateTime time = LocalDateTime.parse(postTime);
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(time, now);

            long seconds = duration.getSeconds();
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            // Just now (less than 60 seconds)
            if (seconds < 60) {
                return context.getString(R.string.time_just_now);
            }
            // Minutes ago
            else if (minutes < 60) {
                if (minutes == 1) {
                    return context.getString(R.string.time_one_minute_ago);
                } else {
                    return context.getString(R.string.time_minutes_ago, minutes);
                }
            }
            // Hours ago
            else if (hours < 24) {
                if (hours == 1) {
                    return context.getString(R.string.time_one_hour_ago);
                } else {
                    return context.getString(R.string.time_hours_ago, hours);
                }
            }
            // Days ago
            else if (days < 7) {
                if (days == 1) {
                    return context.getString(R.string.time_one_day_ago);
                } else {
                    return context.getString(R.string.time_days_ago, days);
                }
            }
            // More than a week - show formatted date
            else {
                // Use locale-appropriate date format
                Locale locale = context.getResources().getConfiguration().locale;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale);
                return time.format(formatter);
            }
        } catch (Exception e) {
            return postTime;
        }
    }

}
