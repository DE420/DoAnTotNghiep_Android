package com.example.fitnessapp.model.request;

import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.google.gson.annotations.SerializedName;

public class PlanSearchRequest {
    @SerializedName("keyword")
    private String keyword;
    @SerializedName("goal")
    private FitnessGoal goal;
    @SerializedName("level")
    private DifficultyLevel level;
    @SerializedName("duration")
    private Integer duration;
    @SerializedName("page")
    private Integer page;
    @SerializedName("limit")
    private Integer limit;

    public PlanSearchRequest(String keyword, FitnessGoal goal, DifficultyLevel level, Integer duration, Integer page, Integer limit) {
        this.keyword = keyword;
        this.goal = goal;
        this.level = level;
        this.duration = duration;
        this.page = page;
        this.limit = limit;
    }

    // Getters and Setters (you can generate these in Android Studio: Alt+Insert -> Getter and Setter)
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public FitnessGoal getGoal() {
        return goal;
    }

    public void setGoal(FitnessGoal goal) {
        this.goal = goal;
    }

    public DifficultyLevel getLevel() {
        return level;
    }

    public void setLevel(DifficultyLevel level) {
        this.level = level;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}