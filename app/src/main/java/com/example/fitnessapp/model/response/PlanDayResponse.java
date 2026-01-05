package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlanDayResponse {
    @SerializedName("id")
    private Long id;
    @SerializedName("dayOfWeek")
    private Integer dayOfWeek;
    @SerializedName("dayInNumber")
    private Integer dayInNumber;

    @SerializedName("exercises")
    private List<PlanExerciseDetailResponse> exercises;

    // Default constructor cho Gson
    public PlanDayResponse() {
    }

    // Constructor với tất cả các trường
    public PlanDayResponse(Long id, Integer dayOfWeek, Integer dayInNumber, List<PlanExerciseDetailResponse> exercises) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.dayInNumber = dayInNumber;
        this.exercises = exercises;
    }

    public Long getId() {
        return id;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public Integer getDayInNumber() {
        return dayInNumber;
    }

    public List<PlanExerciseDetailResponse> getExercises() {
        return exercises;
    }
}