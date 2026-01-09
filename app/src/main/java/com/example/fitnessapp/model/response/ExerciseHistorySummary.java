package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

public class ExerciseHistorySummary {
    @SerializedName("exerciseId")
    private Long exerciseId;

    @SerializedName("exerciseName")
    private String exerciseName;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("totalSets")
    private int totalSets;

    @SerializedName("totalReps")
    private int totalReps;

    @SerializedName("totalDurations")
    private int totalDurations;

    @SerializedName("totalCalories")
    private float totalCalories;

    public ExerciseHistorySummary() {
    }

    public ExerciseHistorySummary(Long exerciseId, String exerciseName, String thumbnail, int totalSets, int totalReps, int totalDurations, float totalCalories) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.thumbnail = thumbnail;
        this.totalSets = totalSets;
        this.totalReps = totalReps;
        this.totalDurations = totalDurations;
        this.totalCalories = totalCalories;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getTotalSets() {
        return totalSets;
    }

    public void setTotalSets(int totalSets) {
        this.totalSets = totalSets;
    }

    public int getTotalReps() {
        return totalReps;
    }

    public void setTotalReps(int totalReps) {
        this.totalReps = totalReps;
    }

    public int getTotalDurations() {
        return totalDurations;
    }

    public void setTotalDurations(int totalDurations) {
        this.totalDurations = totalDurations;
    }

    public float getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(float totalCalories) {
        this.totalCalories = totalCalories;
    }
}
