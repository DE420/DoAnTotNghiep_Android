package com.example.fitnessapp.adapter.nutrition;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitnessapp.fragment.nutrition.PublicMenusFragment;
import com.example.fitnessapp.fragment.nutrition.MyMenusFragment;

public class NutritionViewPagerAdapter extends FragmentStateAdapter {

    public NutritionViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new PublicMenusFragment();
        } else {
            return new MyMenusFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
