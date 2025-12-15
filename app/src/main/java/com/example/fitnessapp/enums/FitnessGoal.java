package com.example.fitnessapp.enums;

import com.example.fitnessapp.R;

public enum FitnessGoal {
    LOSE_WEIGHT (R.string.fitness_goal_lose_weight),        // Giảm cân
    GAIN_WEIGHT (R.string.fitness_goal_gain_weight),        // Tăng cân
    MUSCLE_GAIN (R.string.fitness_goal_muscle_gain),   // Tăng cơ
    SHAPE_BODY (R.string.fitness_goal_shape_body),         // Giữ dáng/Săn chắc
    OTHERS  (R.string.fitness_goal_others)            // Khác
    ;

    private final int mResId;

    FitnessGoal(int mResId) {
        this.mResId = mResId;
    }

    public int getResId() {
        return mResId;
    }
}
