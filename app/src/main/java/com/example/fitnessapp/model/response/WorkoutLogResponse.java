package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

public class WorkoutLogResponse {
    @SerializedName("id")
    private Long id;
    @SerializedName("exerciseId")
    private Long exerciseId;
    @SerializedName("exerciseName")
    private String exerciseName;
    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("setNumber")
    private Integer setNumber;
    @SerializedName("reps")
    private Integer reps;
    @SerializedName("weight")
    private Double weight;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("caloriesBurned")
    private Float caloriesBurned;

    public WorkoutLogResponse() {
    }

    public WorkoutLogResponse(Long id, Long exerciseId, String exerciseName, String thumbnail, Integer setNumber, Integer reps, Double weight, Integer duration, Float caloriesBurned) {
        this.id = id;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.thumbnail = thumbnail;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.duration = duration;
        this.caloriesBurned = caloriesBurned;
    }

    public Long getId() {
        return id;
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

    public Integer getSetNumber() {
        return setNumber;
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

    public Float getCaloriesBurned() {
        return caloriesBurned;
    }
}
