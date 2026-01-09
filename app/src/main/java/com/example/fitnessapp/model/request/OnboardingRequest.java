package com.example.fitnessapp.model.request;

import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;

public class OnboardingRequest {
    private String sex;              // "MALE" or "FEMALE"
    private String dateOfBirth;      // Format: "dd/MM/yyyy"
    private Double weight;           // in kg
    private Double height;           // in meters
    private FitnessGoal fitnessGoal;
    private ActivityLevel activityLevel;

    public OnboardingRequest() {
    }

    public OnboardingRequest(String sex, String dateOfBirth, Double weight, Double height,
                           FitnessGoal fitnessGoal, ActivityLevel activityLevel) {
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.weight = weight;
        this.height = height;
        this.fitnessGoal = fitnessGoal;
        this.activityLevel = activityLevel;
    }

    // Getters and Setters
    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
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

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(FitnessGoal fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    @Override
    public String toString() {
        return "OnboardingRequest{" +
                "sex='" + sex + '\'' +
                ", dateOfBirth='" + dateOfBirth + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                ", fitnessGoal=" + fitnessGoal +
                ", activityLevel=" + activityLevel +
                '}';
    }
}
