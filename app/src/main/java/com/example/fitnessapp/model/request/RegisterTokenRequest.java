package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;

public class RegisterTokenRequest {

    @SerializedName("token")
    private String token;

    @SerializedName("deviceType")
    private String deviceType;

    public RegisterTokenRequest(String token, String deviceType) {
        this.token = token;
        this.deviceType = deviceType;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
}
