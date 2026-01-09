package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WorkoutDayDetailResponse {
    @SerializedName("dayId")
    private Long dayId;

    @SerializedName("weekNumber")
    private Integer weekNumber;

    @SerializedName("dayOfWeek")
    private Integer dayOfWeek;

    @SerializedName("dayInNumber")
    private Integer dayInNumber;

    @SerializedName("exercises")
    private List<WorkoutDayExerciseResponse> exercises;

    public WorkoutDayDetailResponse() {
    }

    public WorkoutDayDetailResponse(Long dayId, Integer weekNumber, Integer dayOfWeek, Integer dayInNumber, List<WorkoutDayExerciseResponse> exercises) {
        this.dayId = dayId;
        this.weekNumber = weekNumber;
        this.dayOfWeek = dayOfWeek;
        this.dayInNumber = dayInNumber;
        this.exercises = exercises;
    }

    public Long getDayId() {
        return dayId;
    }

    public void setDayId(Long dayId) {
        this.dayId = dayId;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(Integer dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public Integer getDayInNumber() {
        return dayInNumber;
    }

    public void setDayInNumber(Integer dayInNumber) {
        this.dayInNumber = dayInNumber;
    }

    public List<WorkoutDayExerciseResponse> getExercises() {
        return exercises;
    }

    public void setExercises(List<WorkoutDayExerciseResponse> exercises) {
        this.exercises = exercises;
    }
}
