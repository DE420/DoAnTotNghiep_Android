package com.example.fitnessapp.model.request.nutrition;

import com.example.fitnessapp.enums.FitnessGoal;

import java.util.List;

public class MenuRequest {
    private String name;
    private String description;
    private FitnessGoal fitnessGoal;
    private Integer caloriesTarget;
    private Boolean isPrivate;
    private List<MealRequest> meals;

    public MenuRequest() {
    }

    public MenuRequest(String name, String description, FitnessGoal fitnessGoal,
                      Integer caloriesTarget, Boolean isPrivate, List<MealRequest> meals) {
        this.name = name;
        this.description = description;
        this.fitnessGoal = fitnessGoal;
        this.caloriesTarget = caloriesTarget;
        this.isPrivate = isPrivate;
        this.meals = meals;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(FitnessGoal fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public Integer getCaloriesTarget() {
        return caloriesTarget;
    }

    public void setCaloriesTarget(Integer caloriesTarget) {
        this.caloriesTarget = caloriesTarget;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public List<MealRequest> getMeals() {
        return meals;
    }

    public void setMeals(List<MealRequest> meals) {
        this.meals = meals;
    }
}
