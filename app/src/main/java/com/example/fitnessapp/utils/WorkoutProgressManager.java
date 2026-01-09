package com.example.fitnessapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorkoutProgressManager {
    private static final String TAG = "WorkoutProgressManager";
    private static final String PREF_NAME = "workout_progress";
    private static final String KEY_COMPLETED_SETS = "completed_sets_";

    private static WorkoutProgressManager instance;
    private SharedPreferences preferences;
    private Gson gson;

    private WorkoutProgressManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized WorkoutProgressManager getInstance(Context context) {
        if (instance == null) {
            instance = new WorkoutProgressManager(context);
        }
        return instance;
    }

    // Lưu số set đã hoàn thành cho một exercise trong workout day cụ thể
    public void saveCompletedSets(Long workoutDayId, Long exerciseId, int completedSets) {
        String key = getKey(workoutDayId, exerciseId);
        preferences.edit().putInt(key, completedSets).apply();
        Log.d(TAG, "Saved completed sets: " + completedSets + " for key: " + key);
    }

    // Lấy số set đã hoàn thành
    public int getCompletedSets(Long workoutDayId, Long exerciseId) {
        String key = getKey(workoutDayId, exerciseId);
        int completedSets = preferences.getInt(key, 0);
        Log.d(TAG, "Retrieved completed sets: " + completedSets + " for key: " + key);
        return completedSets;
    }

    // Xóa progress của một workout day (khi hoàn thành hoặc reset)
    public void clearWorkoutDayProgress(Long workoutDayId) {
        SharedPreferences.Editor editor = preferences.edit();
        Map<String, ?> allEntries = preferences.getAll();
        String prefix = KEY_COMPLETED_SETS + workoutDayId + "_";

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                editor.remove(entry.getKey());
            }
        }
        editor.apply();
        Log.d(TAG, "Cleared progress for workout day: " + workoutDayId);
    }

    // Xóa tất cả progress (khi logout hoặc reset app)
    public void clearAllProgress() {
        preferences.edit().clear().apply();
        Log.d(TAG, "Cleared all workout progress");
    }

    private String getKey(Long workoutDayId, Long exerciseId) {
        return KEY_COMPLETED_SETS + workoutDayId + "_" + exerciseId;
    }

    // Helper: Check xem có progress nào cho workout day không
    public boolean hasProgress(Long workoutDayId) {
        Map<String, ?> allEntries = preferences.getAll();
        String prefix = KEY_COMPLETED_SETS + workoutDayId + "_";

        for (String key : allEntries.keySet()) {
            if (key.startsWith(prefix)) {
                int sets = preferences.getInt(key, 0);
                if (sets > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}