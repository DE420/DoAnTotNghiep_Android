package com.example.fitnessapp.enums;


import com.example.fitnessapp.R;

public enum ActivityLevel {
    SEDENTARY (R.string.acitvity_level_sedentary),          // Ít vận động
    LIGHTLY_ACTIVE (R.string.acitvity_level_lightly_active),     // Vận động nhẹ (1-3 ngày/tuần)
    MODERATELY_ACTIVE (R.string.acitvity_level_moderately_active),  // Vận động vừa (3-5 ngày/tuần)
    VERY_ACTIVE (R.string.acitvity_level_very_active),        // Vận động nhiều (6-7 ngày/tuần)
    EXTRA_ACTIVE (R.string.acitvity_level_extra_active)        // Vận động rất nhiều (2 lần/ngày)
    ;

    private final int mResId;

    ActivityLevel(int activityLevel) {
        mResId = activityLevel;
    }

    public int getResId() {
        return mResId;
    }
}

