package com.example.fitnessapp.model.response.nutrition;

import com.example.fitnessapp.enums.FitnessGoal;

public class MenuListResponse {
    private Long id;
    private Integer displayOrder;
    private String name;
    private String description;
    private FitnessGoal fitnessGoal;
    private Boolean isDefault;
    private Float calories;
    private Float protein;
    private Float carbs;
    private Float fat;


    public MenuListResponse() {
    }

    public Long getId() {
        return id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public Float getCalories() {
        return calories;
    }

    public Float getProtein() {
        return protein;
    }

    public Float getCarbs() {
        return carbs;
    }

    public Float getFat() {
        return fat;
    }
}
