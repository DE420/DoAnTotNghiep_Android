package com.example.fitnessapp.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    // Các formatter có thể nhận từ API
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter CUSTOM_FORMATTER_2 = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Formatter để gửi đi
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(OUTPUT_FORMATTER));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String dateTimeString = in.nextString();

        // Thử parse theo các format khác nhau
        try {
            return LocalDateTime.parse(dateTimeString, ISO_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(dateTimeString, CUSTOM_FORMATTER);
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.parse(dateTimeString, CUSTOM_FORMATTER_2);
                } catch (DateTimeParseException e3) {
                    try {
                        // Nếu chỉ có ngày, parse thành LocalDate rồi convert sang LocalDateTime với time = 00:00:00
                        LocalDate date = LocalDate.parse(dateTimeString, DATE_ONLY_FORMATTER);
                        return LocalDateTime.of(date, LocalTime.MIDNIGHT);
                    } catch (DateTimeParseException e4) {
                        throw new IOException("Cannot parse LocalDateTime: " + dateTimeString, e4);
                    }
                }
            }
        }
    }
}