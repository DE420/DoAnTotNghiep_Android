package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;

public class LogoutRequest {
    private String token;

    public LogoutRequest(String token) {
        this.token = token;
    }
}