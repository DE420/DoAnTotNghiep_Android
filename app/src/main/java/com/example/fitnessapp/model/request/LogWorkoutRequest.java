package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;

public class LogWorkoutRequest {
    @SerializedName("exerciseId")
    private Long exerciseId;

    @SerializedName("workoutDayId")
    private Long workoutDayId;

    @SerializedName("setNumber")
    private Integer setNumber;

    @SerializedName("reps")
    private Integer reps;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("duration")
    private Integer duration;

    public LogWorkoutRequest() {
    }

    public LogWorkoutRequest(Long exerciseId, Long workoutDayId, Integer setNumber, Integer reps, Double weight, Integer duration) {
        this.exerciseId = exerciseId;
        this.workoutDayId = workoutDayId;
        this.setNumber = setNumber;
        this.reps = reps;
        this.weight = weight;
        this.duration = duration;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public Long getWorkoutDayId() {
        return workoutDayId;
    }

    public void setWorkoutDayId(Long workoutDayId) {
        this.workoutDayId = workoutDayId;
    }

    public Integer getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(Integer setNumber) {
        this.setNumber = setNumber;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "LogWorkoutRequest{" +
                "exerciseId=" + exerciseId +
                ", workoutDayId=" + workoutDayId +
                ", setNumber=" + setNumber +
                ", reps=" + reps +
                ", weight=" + weight +
                ", duration=" + duration +
                '}';
    }
}
