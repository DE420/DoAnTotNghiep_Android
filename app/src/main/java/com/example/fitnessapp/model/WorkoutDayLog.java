package com.example.fitnessapp.model;

import java.util.List;

public class WorkoutDayLog {
    private String date;
    private List<WorkoutExerciseLog> exercises;

    public WorkoutDayLog(String date, List<WorkoutExerciseLog> exercises) {
        this.date = date;
        this.exercises = exercises;
    }

    public String getDate() {
        return date;
    }

    public List<WorkoutExerciseLog> getExercises() {
        return exercises;
    }

    public static class WorkoutExerciseLog {
        private String exerciseName;
        private int reps;
        private int sets;
        private Integer duration;
        private Double weight;
        private int calories;

        public WorkoutExerciseLog(String exerciseName, int reps, int sets, int calories) {
            this.exerciseName = exerciseName;
            this.reps = reps;
            this.sets = sets;
            this.calories = calories;
        }

        public WorkoutExerciseLog(String exerciseName, int reps, int sets, Integer duration, Double weight, int calories) {
            this.exerciseName = exerciseName;
            this.reps = reps;
            this.sets = sets;
            this.duration = duration;
            this.weight = weight;
            this.calories = calories;
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public int getReps() {
            return reps;
        }

        public int getSets() {
            return sets;
        }

        public Integer getDuration() {
            return duration;
        }

        public Double getWeight() {
            return weight;
        }

        public int getCalories() {
            return calories;
        }
    }
}