package com.example.fitnessapp.enums;

import com.example.fitnessapp.R;

public enum DifficultyLevel {
    BEGINNER(R.string.difficulty_level_beginner),
    INTERMEDIATE(R.string.difficulty_level_intermediate),
    ADVANCED(R.string.difficulty_level_advanced);

    private final int mResId;

    DifficultyLevel(int mResId) {
        this.mResId = mResId;
    }

    public int getResId() {
        return mResId;
    }
}