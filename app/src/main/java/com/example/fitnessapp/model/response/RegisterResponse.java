package com.example.fitnessapp.model.response;

import com.example.fitnessapp.model.constants.AuthProvider;

public class RegisterResponse {
    private Long id;

    private String name;

    private String email;

    private String username;

    private String avatar;

    private Double height;

    private Double weight;

    private AuthProvider provider;

    private boolean isLocked;

    private Integer currentStreak;

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

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    @Override
    public String toString() {
        return "RegisterResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", provider=" + provider +
                ", isLocked=" + isLocked +
                ", currentStreak=" + currentStreak +
                ", longestStreak=" + longestStreak +
                '}';
    }
}
