package com.example.fitnessapp.enums;

import com.example.fitnessapp.R;

public enum MealType {
    BREAKFAST (R.string.breakfast),
    LUNCH (R.string.lunch),
    DINNER (R.string.dinner),
    EXTRA_MEAL (R.string.extra_meal);

    private final int mResId;

    MealType(int mResId) {
        this.mResId = mResId;
    }

    public int getResId() {
        return mResId;
    }
}
