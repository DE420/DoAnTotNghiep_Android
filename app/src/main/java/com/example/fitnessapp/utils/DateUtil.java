package com.example.fitnessapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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



}
