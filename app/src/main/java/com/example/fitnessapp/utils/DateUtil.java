package com.example.fitnessapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    public static final String DD_MM_YYYY_DATE_FORMAT = "dd/MM/yyyy";

    public static String convertBirthday(String input, String format) throws ParseException {

        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = inputFormat.parse(input);

        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.ENGLISH);
        int day = Integer.parseInt(dayFormat.format(date));

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

        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM d", Locale.ENGLISH);
        String base = monthDayFormat.format(date);

        return base + suffix;
    }

    public static String getMonthAndYear(String input, String format) throws ParseException {

        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        Date date = inputFormat.parse(input);

        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);

        return monthDayFormat.format(date);
    }

}
