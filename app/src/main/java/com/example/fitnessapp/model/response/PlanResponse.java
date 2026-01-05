package com.example.fitnessapp.model.response;

import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.google.gson.annotations.SerializedName;

public class PlanResponse {
    @SerializedName("id")
    private Long id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;
    @SerializedName("durationWeek")
    private Integer durationWeek;
    @SerializedName("daysPerWeek")
    private Integer daysPerWeek;
    @SerializedName("targetGoal")
    private FitnessGoal targetGoal;
    @SerializedName("difficultyLevel")
    private DifficultyLevel difficultyLevel;
    @SerializedName("isDefault")
    private Boolean isDefault;
    @SerializedName("startDate")
    private String startDate;

    public PlanResponse(Long id, String name, String description, Integer durationWeek, Integer daysPerWeek, FitnessGoal targetGoal, DifficultyLevel difficultyLevel, Boolean isDefault, String startDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.durationWeek = durationWeek;
        this.daysPerWeek = daysPerWeek;
        this.targetGoal = targetGoal;
        this.difficultyLevel = difficultyLevel;
        this.isDefault = isDefault;
        this.startDate = startDate;
    }

    public PlanResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDurationWeek() {
        return durationWeek;
    }

    public Integer getDaysPerWeek() {
        return daysPerWeek;
    }

    public FitnessGoal getTargetGoal() {
        return targetGoal;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public String getStartDate() {
        return startDate;
    }
}