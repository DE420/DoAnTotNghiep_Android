package com.example.fitnessapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class WorkoutProgressManager {

    private static final String PREF_NAME = "workout_progress";
    private static final String KEY_COMPLETED_SETS = "completed_sets_";
    private static final String KEY_WORKOUT_DAY_COMPLETED = "workout_day_completed_";

    private static WorkoutProgressManager instance;
    private final SharedPreferences preferences;

    private WorkoutProgressManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized WorkoutProgressManager getInstance(Context context) {
        if (instance == null) {
            instance = new WorkoutProgressManager(context.getApplicationContext());
        }
        return instance;
    }

    // Existing methods
    public void saveCompletedSets(Long workoutDayId, Long workoutDayExerciseId, int completedSets) {
        String key = KEY_COMPLETED_SETS + workoutDayId + "_" + workoutDayExerciseId;
        preferences.edit().putInt(key, completedSets).apply();
    }

    public int getCompletedSets(Long workoutDayId, Long workoutDayExerciseId) {
        String key = KEY_COMPLETED_SETS + workoutDayId + "_" + workoutDayExerciseId;
        return preferences.getInt(key, 0);
    }

    public void clearWorkoutDayProgress(Long workoutDayId) {
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith(KEY_COMPLETED_SETS + workoutDayId + "_")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    // NEW: Đánh dấu workout day hoàn thành
    public void markWorkoutDayCompleted(Long workoutDayId) {
        String key = KEY_WORKOUT_DAY_COMPLETED + workoutDayId;
        preferences.edit().putBoolean(key, true).apply();
    }

    public boolean isWorkoutDayCompleted(Long workoutDayId) {
        String key = KEY_WORKOUT_DAY_COMPLETED + workoutDayId;
        return preferences.getBoolean(key, false);
    }

    public void clearWorkoutDayCompletion(Long workoutDayId) {
        String key = KEY_WORKOUT_DAY_COMPLETED + workoutDayId;
        preferences.edit().remove(key).apply();
    }
}