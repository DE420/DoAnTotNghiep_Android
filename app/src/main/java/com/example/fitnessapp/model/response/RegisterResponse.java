package com.example.fitnessapp.model.response;

import com.example.fitnessapp.constants.AuthProvider;
import com.google.gson.annotations.SerializedName;
public class RegisterResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("height")
    private Double height;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("provider")
    private AuthProvider provider;

    @SerializedName("isLocked")
    private boolean isLocked;

    @SerializedName("currentStreak")
    private Integer currentStreak;

    @SerializedName("longestStreak")
    private Integer longestStreak;

    public RegisterResponse() {
    }

    public RegisterResponse(Long id, String name, String email, String username,
                            String avatar, Double height, Double weight, AuthProvider provider,
                            boolean isLocked, Integer currentStreak, Integer longestStreak) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.height = height;
        this.weight = weight;
        this.provider = provider;
        this.isLocked = isLocked;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public Double getHeight() {
        return height;
    }

    public Double getWeight() {
        return weight;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }
}