package com.example.fitnessapp.model.response;

import com.example.fitnessapp.model.response.ExerciseResponse;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WorkoutDayExerciseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("sets")
    private Integer sets;

    @SerializedName("reps")
    private Integer reps;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("duration")
    private Integer duration;

    @SerializedName("exercise")
    private ExerciseResponse exercise;

    public WorkoutDayExerciseResponse() {
    }

    public WorkoutDayExerciseResponse(Long id, String name, Integer sets, Integer reps, Double weight, Integer duration, ExerciseResponse exercise) {
        this.id = id;
        this.name = name;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
        this.duration = duration;
        this.exercise = exercise;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSets() {
        return sets;
    }

    public void setSets(Integer sets) {
        this.sets = sets;
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

    public ExerciseResponse getExercise() {
        return exercise;
    }

    public void setExercise(ExerciseResponse exercise) {
        this.exercise = exercise;
    }

    // Helper methods
    public String getExerciseName() {
        if (exercise != null && exercise.getName() != null) {
            return exercise.getName();
        }
        return name != null ? name : "Unknown Exercise";
    }

    public Long getExerciseId() {
        if (exercise != null && exercise.getId() != null) {
            return exercise.getId();
        }
        return null;
    }

    public String getThumbnail() {
        if (exercise != null) {
            return exercise.getThumbnail();
        }
        return null;
    }

    public String getVideoUrl() {
        if (exercise != null) {
            return exercise.getVideoUrl();
        }
        return null;
    }

    public String getDescription() {
        if (exercise != null) {
            return exercise.getDescription();
        }
        return null;
    }
}