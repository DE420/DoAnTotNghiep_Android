package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("status")
    private boolean status;

    @SerializedName("data")
    private T data;

    public boolean isStatus() {
        return status;
    }

    public T getData() {
        return data;
    }
}