package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlanExerciseDetailResponse {
    @SerializedName("exerciseId")
    private Long exerciseId;
    @SerializedName("exerciseName")
    private String exerciseName;
    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("sets")
    private Integer sets;
    @SerializedName("reps")
    private Integer reps;
    @SerializedName("weight")
    private Double weight;
    @SerializedName("duration")
    private Integer duration;

    @SerializedName("logs")
    private List<WorkoutLogResponse> logs;

    // Default constructor cho Gson
    public PlanExerciseDetailResponse() {
    }

    // Constructor với tất cả các trường
    public PlanExerciseDetailResponse(Long exerciseId, String exerciseName, String thumbnail, Integer sets, Integer reps, Double weight, Integer duration, List<WorkoutLogResponse> logs) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.thumbnail = thumbnail;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.duration = duration;
        this.logs = logs;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Integer getSets() {
        return sets;
    }

    public Integer getReps() {
        return reps;
    }

    public Double getWeight() {
        return weight;
    }

    public Integer getDuration() {
        return duration;
    }

    public List<WorkoutLogResponse> getLogs() {
        return logs;
    }
}