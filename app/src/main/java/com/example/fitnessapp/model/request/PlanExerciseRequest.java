package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;

public class PlanExerciseRequest {
    @SerializedName("exerciseId")
    private Long exerciseId;
    @SerializedName("sets")
    private Integer sets;
    @SerializedName("reps")
    private Integer reps;
    @SerializedName("weight")
    private Double weight;
    @SerializedName("duration")
    private Integer duration;

    public PlanExerciseRequest() {
    }

    public PlanExerciseRequest(Long exerciseId, Integer sets, Integer reps, Double weight, Integer duration) {
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.duration = duration;
    }

    // Getters and Setters
    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    public Integer getSets() { return sets; }
    public void setSets(Integer sets) { this.sets = sets; }
    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}