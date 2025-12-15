package com.example.fitnessapp.model.request;

import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;

import java.io.Serializable;

public class UpdateProfileRequest implements Serializable {

    public static final String KEY_UPDATE_PROFILE_REQUEST = "UPDATE_PROFILE_REQUEST";
    public static final String KEY_AVATAR = "avatar";
    public static final String KEY_NAME = "name";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_ACTIVITY_LEVEL = "activityLevel";
    public static final String KEY_FITNESS_GOAL = "fitnessGoal";
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";

    public static final String KEY_AVATAR_FILE = "avatarFile";

    private String avatar;

    private String name;

    private Double weight;

    private Double height;

    private ActivityLevel activityLevel;

    private FitnessGoal fitnessGoal;

    private String dateOfBirth;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(FitnessGoal fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    UpdateProfileRequest(Builder builder) {
        this.avatar = builder.avatar;
        this.name = builder.name;
        this.dateOfBirth = builder.dateOfBirth;
        this.weight = builder.weight;
        this.height = builder.height;
        this.activityLevel = builder.activityLevel;
        this.fitnessGoal = builder.fitnessGoal;
    }

    public static class Builder {
        private String avatar;

        private String name;

        private Double weight;

        private Double height;

        private ActivityLevel activityLevel;

        private FitnessGoal fitnessGoal;

        private String dateOfBirth;

        public Builder() {
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder dateOfBirth(String dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder weight(Double weight) {
            this.weight = weight;
            return this;
        }

        public Builder height(Double height) {
            this.height = height;
            return this;
        }

        public Builder activityLevel(ActivityLevel activityLevel) {
            this.activityLevel = activityLevel;
            return this;
        }

        public Builder fitnessGoal(FitnessGoal fitnessGoal) {
            this.fitnessGoal = fitnessGoal;
            return this;
        }

        public UpdateProfileRequest build() {
            return new UpdateProfileRequest(this);
        }
    }
}
