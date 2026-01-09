package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlanDayRequest {
    @SerializedName("weekNumber")
    private Integer weekNumber;

    @SerializedName("dayOfWeek")
    private Integer dayOfWeek;

    @SerializedName("exercises")
    private List<PlanExerciseRequest> exercises;

    public PlanDayRequest() {
    }

    public PlanDayRequest(Integer weekNumber, Integer dayOfWeek, List<PlanExerciseRequest> exercises) {
        this.weekNumber = weekNumber;
        this.dayOfWeek = dayOfWeek;
        this.exercises = exercises;
    }

    public Integer getWeekNumber() { return weekNumber; }
    public void setWeekNumber(Integer weekNumber) { this.weekNumber = weekNumber; }

    // Existing Getters and Setters
    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public List<PlanExerciseRequest> getExercises() { return exercises; }
    public void setExercises(List<PlanExerciseRequest> exercises) { this.exercises = exercises; }
}