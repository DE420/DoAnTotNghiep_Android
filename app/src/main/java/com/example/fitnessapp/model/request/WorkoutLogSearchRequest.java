package com.example.fitnessapp.model.request;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

public class WorkoutLogSearchRequest {
    @SerializedName("fromDate")
    private LocalDate fromDate;

    @SerializedName("toDate")
    private LocalDate toDate;

    @SerializedName("exerciseId")
    private Long exerciseId;

    public WorkoutLogSearchRequest() {
    }

    public WorkoutLogSearchRequest(LocalDate fromDate, LocalDate toDate, Long exerciseId) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.exerciseId = exerciseId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(Long exerciseId) {
        this.exerciseId = exerciseId;
    }
}
