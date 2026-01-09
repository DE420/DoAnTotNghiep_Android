package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.util.List;

public class WorkoutHistoryResponse {
    @SerializedName("date")
    private LocalDate date;

    @SerializedName("totalExercises")
    private int totalExercises;

    @SerializedName("totalCalories")
    private float totalCalories;

    @SerializedName("exercises")
    private List<ExerciseHistorySummary> exercises;
}
