package com.example.fitnessapp.utils;

import java.util.HashMap;
import java.util.Map;

public class ExerciseFieldConfig {

    public enum FieldDisplay {
        SETS_REPS,           // Sets + Reps (e.g., Push-ups, Pull-ups)
        SETS_DURATION,       // Sets + Duration (e.g., Plank, Wall Sit)
        SETS_REPS_WEIGHT     // Sets + Reps + Weight (e.g., Bench Press, Squats with weight)
    }

    // Map exercise names to their field display configuration
    private static final Map<String, FieldDisplay> EXERCISE_CONFIG = new HashMap<>();

    static {
        // Sets, reps
        EXERCISE_CONFIG.put("Pull up", FieldDisplay.SETS_REPS);
        EXERCISE_CONFIG.put("Push up", FieldDisplay.SETS_REPS);
        EXERCISE_CONFIG.put("Squat", FieldDisplay.SETS_REPS);

        // Sets, durations
        EXERCISE_CONFIG.put("Plank", FieldDisplay.SETS_DURATION);
        EXERCISE_CONFIG.put("Back lever", FieldDisplay.SETS_DURATION);

        // Sets, reps, weight
        EXERCISE_CONFIG.put("Biceps Curl", FieldDisplay.SETS_REPS_WEIGHT);
        EXERCISE_CONFIG.put("Deadlift", FieldDisplay.SETS_REPS_WEIGHT);
    }

    /**
     * Get the field display configuration for a specific exercise
     * @param exerciseName The name of the exercise
     * @return FieldDisplay configuration, defaults to SETS_REPS if not found
     */
    public static FieldDisplay getFieldDisplay(String exerciseName) {
        if (exerciseName == null || exerciseName.trim().isEmpty()) {
            return FieldDisplay.SETS_REPS; // Default
        }

        // Try exact match first
        FieldDisplay config = EXERCISE_CONFIG.get(exerciseName);
        if (config != null) {
            return config;
        }

        // Try case-insensitive partial match
        String lowerName = exerciseName.toLowerCase();
        for (Map.Entry<String, FieldDisplay> entry : EXERCISE_CONFIG.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lowerName) ||
                    lowerName.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }

        // Default to SETS_REPS if no match found
        return FieldDisplay.SETS_REPS;
    }

    /**
     * Add a custom exercise configuration at runtime
     * @param exerciseName Name of the exercise
     * @param fieldDisplay Field display configuration
     */
    public static void addExerciseConfig(String exerciseName, FieldDisplay fieldDisplay) {
        EXERCISE_CONFIG.put(exerciseName, fieldDisplay);
    }

    /**
     * Check if an exercise requires weight input
     * @param exerciseName Name of the exercise
     * @return true if weight field should be shown
     */
    public static boolean requiresWeight(String exerciseName) {
        return getFieldDisplay(exerciseName) == FieldDisplay.SETS_REPS_WEIGHT;
    }

    /**
     * Check if an exercise uses duration instead of reps
     * @param exerciseName Name of the exercise
     * @return true if duration field should be shown instead of reps
     */
    public static boolean usesDuration(String exerciseName) {
        return getFieldDisplay(exerciseName) == FieldDisplay.SETS_DURATION;
    }

    /**
     * Get all exercises that use a specific field display type
     * @param fieldDisplay The field display type
     * @return Map of exercise names and their configurations
     */
    public static Map<String, FieldDisplay> getExercisesByType(FieldDisplay fieldDisplay) {
        Map<String, FieldDisplay> result = new HashMap<>();
        for (Map.Entry<String, FieldDisplay> entry : EXERCISE_CONFIG.entrySet()) {
            if (entry.getValue() == fieldDisplay) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}