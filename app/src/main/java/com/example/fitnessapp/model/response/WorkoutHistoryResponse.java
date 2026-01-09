package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WorkoutHistoryResponse {
    @SerializedName("date")
    private String date;

    @SerializedName("totalExercises")
    private int totalExercises;

    @SerializedName("totalCalories")
    private float totalCalories;

    @SerializedName("exercises")
    private List<ExerciseHistorySummary> exercises;

    public String getDate() {
        return date;
    }

    public int getTotalExercises() {
        return totalExercises;
    }

    public float getTotalCalories() {
        return totalCalories;
    }

    public List<ExerciseHistorySummary> getExercises() {
        return exercises;
    }

    public static class ExerciseHistorySummary {
        @SerializedName("exerciseId")
        private Long exerciseId;

        @SerializedName("exerciseName")
        private String exerciseName;

        @SerializedName("totalSets")
        private int totalSets;

        @SerializedName("totalReps")
        private int totalReps;

        @SerializedName("totalCalories")
        private float totalCalories;

        public Long getExerciseId() {
            return exerciseId;
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public int getTotalSets() {
            return totalSets;
        }

        public int getTotalReps() {
            return totalReps;
        }

        public float getTotalCalories() {
            return totalCalories;
        }
    }
}