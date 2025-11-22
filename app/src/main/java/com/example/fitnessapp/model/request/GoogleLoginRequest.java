package com.example.fitnessapp.model.request;

public class GoogleLoginRequest {
    private final String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }
}