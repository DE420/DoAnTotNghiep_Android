package com.example.fitnessapp.model.request;

public class ChangePasswordRequest {
    private String oldPassword;

    private String newPassword;

    private String confirmNewPassword;

    public ChangePasswordRequest(String oldPassword, String newPassword, String confirmNewPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.confirmNewPassword = confirmNewPassword;
    }
}