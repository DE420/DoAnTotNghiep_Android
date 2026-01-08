package com.example.fitnessapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitnessapp.fragment.OnboardingStep1Fragment;
import com.example.fitnessapp.fragment.OnboardingStep2Fragment;
import com.example.fitnessapp.fragment.OnboardingStep3Fragment;

public class OnboardingPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 3;

    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new OnboardingStep1Fragment();
            case 1:
                return new OnboardingStep2Fragment();
            case 2:
                return new OnboardingStep3Fragment();
            default:
                return new OnboardingStep1Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}
