package com.example.fitnessapp.model.request;

public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String confirmPassword;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String username,
                           String password, String confirmPassword) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    RegisterRequest(Builder builder) {
        this.email = builder.email;
        this.username = builder.username;
        this.password = builder.password;
        this.confirmPassword = builder.confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public static class Builder {
        private String email;
        private String username;
        private String password;
        private String confirmPassword;

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder confirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
            return this;
        }

        public RegisterRequest build() {
            return new RegisterRequest(this);
        }

    }
}
