package com.example.fitnessapp.model.response;

public class BasicInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String name;
    private String avatar;
    private RoleResponse role;
    private boolean isOnboardingCompleted;

    public static class RoleResponse {
        private Long id;
        private String name;

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

        @Override
        public String toString() {
            return "RoleResponse{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public RoleResponse getRole() {
        return role;
    }

    public void setRole(RoleResponse role) {
        this.role = role;
    }

    public boolean isOnboardingCompleted() {
        return isOnboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        isOnboardingCompleted = onboardingCompleted;
    }

    @Override
    public String toString() {
        return "BasicInfoResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", avatar='" + avatar + '\'' +
                ", role=" + role +
                ", isOnboardingCompleted=" + isOnboardingCompleted +
                '}';
    }
}
