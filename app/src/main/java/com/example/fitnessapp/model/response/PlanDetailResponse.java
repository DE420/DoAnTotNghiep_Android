package com.example.fitnessapp.model.response;

import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PlanDetailResponse {
    @SerializedName("id")
    private Long id;
    @SerializedName("name")
    private String name;
    @SerializedName("description")
    private String description;

    @SerializedName("createdAt")
    private LocalDateTime createdAt;
    @SerializedName("startDate")
    private LocalDate startDate;

    @SerializedName("durationWeek")
    private Integer durationWeek;
    @SerializedName("daysPerWeek")
    private Integer daysPerWeek;
    @SerializedName("totalUsers")
    private Long totalUsers;

    @SerializedName("targetGoal")
    private FitnessGoal targetGoal;
    @SerializedName("difficultyLevel")
    private DifficultyLevel difficultyLevel;
    @SerializedName("isDefault")
    private Boolean isDefault;

    @SerializedName("weeks")
    private List<PlanWeekResponse> weeks;

    // Default constructor cho Gson
    public PlanDetailResponse() {
    }

    // Constructor với tất cả các trường (có thể tạo tự động bằng Alt+Insert -> Constructor trong Android Studio)
    public PlanDetailResponse(Long id, String name, String description, LocalDateTime createdAt, LocalDate startDate, Integer durationWeek, Integer daysPerWeek, Long totalUsers, FitnessGoal targetGoal, DifficultyLevel difficultyLevel, Boolean isDefault, List<PlanWeekResponse> weeks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.startDate = startDate;
        this.durationWeek = durationWeek;
        this.daysPerWeek = daysPerWeek;
        this.totalUsers = totalUsers;
        this.targetGoal = targetGoal;
        this.difficultyLevel = difficultyLevel;
        this.isDefault = isDefault;
        this.weeks = weeks;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public Integer getDurationWeek() {
        return durationWeek;
    }

    public Integer getDaysPerWeek() {
        return daysPerWeek;
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public FitnessGoal getTargetGoal() {
        return targetGoal;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public List<PlanWeekResponse> getWeeks() {
        return weeks;
    }
}
