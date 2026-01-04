package com.example.fitnessapp.util;

import android.content.Context;

import com.example.fitnessapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final String DD_MM_YYYY_DATE_FORMAT = "dd/MM/yyyy";
    public static final String YYYY_MM_DD_DATE_FORMAT = "yyyy-MM-dd";
    public static final String MMMM_YYYY_DATE_FORMAT = "MMMM yyyy";
    public static final String D_DATE_FORMAT = "d";
    public static final String MMMM_D_DATE_FORMAT = "MMMM d";

    public static boolean isValidDate(String input, String format) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        try {
            Date date = inputFormat.parse(input);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String convertToBirthday(String input, String format) throws ParseException {

        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = inputFormat.parse(input);

        int day = Integer.parseInt(getDateString(date, D_DATE_FORMAT));

        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th";
        } else {
            switch (day % 10) {
                case 1: suffix = "st"; break;
                case 2: suffix = "nd"; break;
                case 3: suffix = "rd"; break;
                default: suffix = "th";
            }
        }

        String base = getDateString(date, MMMM_D_DATE_FORMAT);

        return base + suffix;
    }

    /**
     * Convert date string to Vietnamese birthday format
     * Example: "2000-01-15" -> "Ngày 15 tháng 1 năm 2000"
     *
     * @param input  The input date string
     * @param format The format of the input date string
     * @return Vietnamese formatted birthday string
     * @throws ParseException if date parsing fails
     */
    public static String convertToVietnameseBirthday(String input, String format) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = inputFormat.parse(input);

        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
        SimpleDateFormat monthFormat = new SimpleDateFormat("M", Locale.ENGLISH);
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);

        String day = dayFormat.format(date);
        String month = monthFormat.format(date);
        String year = yearFormat.format(date);

        return "Ngày " + day + " tháng " + month + " năm " + year;
    }

    public static String getNewDateString(String input, String format, String newFormat) throws ParseException {

        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = inputFormat.parse(input);

        SimpleDateFormat monthDayFormat = new SimpleDateFormat(newFormat, Locale.ENGLISH);

        return monthDayFormat.format(date);
    }

    public static String getDateString(Date input, String format) {
        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        return inputFormat.format(input);
    }

    /**
     * Converts a Date to a relative time string (e.g., "Vừa xong", "1 phút trước", "2 giờ trước")
     * Supports both Vietnamese (primary) and English (fallback) based on device language
     *
     * @param context Android context for accessing string resources
     * @param date    The date to convert to relative time
     * @return Localized relative time string
     */
    public static String getRelativeTimeString(Context context, Date date) {
        if (context == null || date == null) {
            return "";
        }

        long diff = System.currentTimeMillis() - date.getTime();

        // Handle future dates
        if (diff < 0) {
            return context.getString(R.string.time_just_now);
        }

        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        long months = days / 30; // Approximate

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
        // Weeks ago
        else if (weeks < 4) {
            if (weeks == 1) {
                return context.getString(R.string.time_one_week_ago);
            } else {
                return context.getString(R.string.time_weeks_ago, weeks);
            }
        }
        // Months ago
        else {
            if (months == 1) {
                return context.getString(R.string.time_one_month_ago);
            } else {
                return context.getString(R.string.time_months_ago, months);
            }
        }
    }

    /**
     * Converts a timestamp (long) to a relative time string
     *
     * @param context   Android context for accessing string resources
     * @param timestamp Timestamp in milliseconds
     * @return Localized relative time string
     */
    public static String getRelativeTimeString(Context context, long timestamp) {
        return getRelativeTimeString(context, new Date(timestamp));
    }


}
