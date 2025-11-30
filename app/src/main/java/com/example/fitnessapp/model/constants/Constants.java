package com.example.fitnessapp.model.constants;

import java.util.regex.Pattern;

public final class Constants {
    private Constants() {}

    public static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    public static final String EMAIL_PATTERN =
            "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

    public static boolean patternMatches(String str, String regexPattern) {
        return Pattern.compile(regexPattern)
                .matcher(str)
                .matches();
    }
}
