package com.example.fitnessapp.model.request;

import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.PlanDayRequest;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.List;

public class CreatePlanRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("goal")
    private FitnessGoal goal;

    @SerializedName("startDate")
    private LocalDate startDate;

    @SerializedName("durationWeek")
    private Integer durationWeek;

    @SerializedName("daysPerWeek")
    private Integer daysPerWeek;

    @SerializedName("level")
    private DifficultyLevel level;

    @SerializedName("description")
    private String description;

    @SerializedName("schedule")
    private List<PlanDayRequest> schedule;

    public CreatePlanRequest() {
    }

    public CreatePlanRequest(String name, FitnessGoal goal, LocalDate startDate, Integer durationWeek, Integer daysPerWeek, DifficultyLevel level, String description, List<PlanDayRequest> schedule) {
        this.name = name;
        this.goal = goal;
        this.startDate = startDate;
        this.durationWeek = durationWeek;
        this.daysPerWeek = daysPerWeek;
        this.level = level;
        this.description = description;
        this.schedule = schedule;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public FitnessGoal getGoal() { return goal; }
    public void setGoal(FitnessGoal goal) { this.goal = goal; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public Integer getDurationWeek() { return durationWeek; }
    public void setDurationWeek(Integer durationWeek) { this.durationWeek = durationWeek; }
    public Integer getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(Integer daysPerWeek) { this.daysPerWeek = daysPerWeek; }
    public DifficultyLevel getLevel() { return level; }
    public void setLevel(DifficultyLevel level) { this.level = level; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<PlanDayRequest> getSchedule() { return schedule; }
    public void setSchedule(List<PlanDayRequest> schedule) { this.schedule = schedule; }
}