package com.example.fitnessapp.model;

import com.example.fitnessapp.model.response.WorkoutDayExerciseResponse;

public class PracticeExerciseItem {
    private WorkoutDayExerciseResponse exercise;
    private int completedSets;
    private boolean isCompleted;

    public PracticeExerciseItem(WorkoutDayExerciseResponse exercise) {
        this.exercise = exercise;
        this.completedSets = 0;
        this.isCompleted = false;
    }

    public WorkoutDayExerciseResponse getExercise() {
        return exercise;
    }

    public void setExercise(WorkoutDayExerciseResponse exercise) {
        this.exercise = exercise;
    }

    public int getCompletedSets() {
        return completedSets;
    }

    public void setCompletedSets(int completedSets) {
        this.completedSets = completedSets;
        updateCompletionStatus();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    private void updateCompletionStatus() {
        if (exercise != null && exercise.getSets() != null) {
            this.isCompleted = completedSets >= exercise.getSets();
        }
    }

    public void incrementCompletedSets() {
        this.completedSets++;
        updateCompletionStatus();
    }

    // Thêm method để check xem còn set nào chưa hoàn thành không
    public boolean hasRemainingSet() {
        if (exercise != null && exercise.getSets() != null) {
            return completedSets < exercise.getSets();
        }
        return false;
    }
}