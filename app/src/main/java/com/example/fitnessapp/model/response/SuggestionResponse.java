package com.example.fitnessapp.model.response;

import com.example.fitnessapp.model.response.nutrition.MenuResponse;

import java.util.List;

public class SuggestionResponse {
    private double bmi;
    private double tdee;
    private double targetCalories;
    private List<PlanResponse> suggestedWorkoutPlans;
    private List<MenuResponse> suggestedMenus;

    // Default constructor
    public SuggestionResponse() {}

    // Getters and Setters
    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getTdee() {
        return tdee;
    }

    public void setTdee(double tdee) {
        this.tdee = tdee;
    }

    public double getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(double targetCalories) {
        this.targetCalories = targetCalories;
    }

    public List<PlanResponse> getSuggestedWorkoutPlans() {
        return suggestedWorkoutPlans;
    }

    public void setSuggestedWorkoutPlans(List<PlanResponse> plans) {
        this.suggestedWorkoutPlans = plans;
    }

    public List<MenuResponse> getSuggestedMenus() {
        return suggestedMenus;
    }

    public void setSuggestedMenus(List<MenuResponse> menus) {
        this.suggestedMenus = menus;
    }
}
