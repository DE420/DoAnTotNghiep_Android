package com.example.fitnessapp.constants;

public final class Constants {
    private Constants() {}

    public static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    public static final String KEY_AUTHORIZATION = "Authorization";
    public static final String PREFIX_JWT = "Bearer";

    public static final String BODY_DATA_FORM = "multipart/form-data";
}
