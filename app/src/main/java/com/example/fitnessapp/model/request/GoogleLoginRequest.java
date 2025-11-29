package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;

public class GoogleLoginRequest {

    private String tokenId;

    public GoogleLoginRequest(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getTokenId() {
        return tokenId;
    }
}