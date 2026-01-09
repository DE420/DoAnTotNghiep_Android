package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ExerciseResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    @SerializedName("id")
    private Long id;
    @SerializedName("name")
    private String name;
    @SerializedName("level")
    private String level;
    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("videoUrl")
    private String videoUrl;
    @SerializedName("description")
    private String description;
    @SerializedName("benefit")
    private String benefit;
    @SerializedName("trainingType")
    private String trainingType;
    @SerializedName("muscleGroups")
    private List<String> muscleGroups;
    @SerializedName("equipments")
    private List<String> equipments;
    @SerializedName("primaryMuscles")
    private List<String> primaryMuscles;
    @SerializedName("secondaryMuscles")
    private List<String> secondaryMuscles;
    @SerializedName("steps")
    private List<String> steps;
    @SerializedName("tips")
    private List<String> tips;
    @SerializedName("mistakes")
    private List<String> mistakes;
    @SerializedName("benefits")
    private List<String> benefits;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getLevel() { return level; }
    public String getThumbnail() { return thumbnail; }
    public String getVideoUrl() { return videoUrl; }
    public String getDescription() { return description; }
    public String getBenefit() { return benefit; }
    public String getTrainingType() { return trainingType; }
    public List<String> getMuscleGroups() { return muscleGroups; }
    public List<String> getEquipments() { return equipments; }
    public List<String> getPrimaryMuscles() { return primaryMuscles; }
    public List<String> getSecondaryMuscles() { return secondaryMuscles; }
    public List<String> getSteps() { return steps; }
    public List<String> getTips() { return tips; }
    public List<String> getMistakes() { return mistakes; }
    public List<String> getBenefits() { return benefits; }
}