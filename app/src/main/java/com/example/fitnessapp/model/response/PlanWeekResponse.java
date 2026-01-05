package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlanWeekResponse {
    @SerializedName("weekNumber")
    private Integer weekNumber;
    @SerializedName("days")
    private List<PlanDayResponse> days;

    public PlanWeekResponse() {
    }

    public PlanWeekResponse(Integer weekNumber, List<PlanDayResponse> days) {
        this.weekNumber = weekNumber;
        this.days = days;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public List<PlanDayResponse> getDays() {
        return days;
    }
}