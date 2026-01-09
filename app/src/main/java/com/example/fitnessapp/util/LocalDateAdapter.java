package com.example.fitnessapp.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {
    // Các formatter có thể nhận từ API
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Formatter để gửi đi - CHANGED TO dd/MM/yyyy
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(OUTPUT_FORMATTER)); // Now sends as dd/MM/yyyy
        }
    }

    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        String dateString = in.nextString();

        // Thử parse theo các format khác nhau
        try {
            // Thử format custom trước (dd/MM/yyyy)
            return LocalDate.parse(dateString, CUSTOM_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                // Nếu không được thì thử format ISO (yyyy-MM-dd)
                return LocalDate.parse(dateString, ISO_FORMATTER);
            } catch (DateTimeParseException e2) {
                throw new IOException("Cannot parse LocalDate: " + dateString, e2);
            }
        }
    }
}