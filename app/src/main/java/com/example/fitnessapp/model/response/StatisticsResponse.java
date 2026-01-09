package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatisticsResponse {
    @SerializedName("totalWorkouts")
    private int totalWorkouts;

    @SerializedName("totalCalories")
    private double totalCalories;

    @SerializedName("currentStreak")
    private int currentStreak;

    @SerializedName("longestStreak")
    private int longestStreak;

    @SerializedName("caloriesChart")
    private List<ChartDataResponseByDate> caloriesChart;

    @SerializedName("intensityChart")
    private List<ChartDataResponseByDate> intensityChart;

    public int getTotalWorkouts() {
        return totalWorkouts;
    }

    public double getTotalCalories() {
        return totalCalories;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public List<ChartDataResponseByDate> getCaloriesChart() {
        return caloriesChart;
    }

    public List<ChartDataResponseByDate> getIntensityChart() {
        return intensityChart;
    }

    public static class ChartDataResponseByDate {
        @SerializedName("date")
        private String date;

        @SerializedName("value")
        private double value;

        public String getDate() {
            return date;
        }

        public double getValue() {
            return value;
        }
    }
}