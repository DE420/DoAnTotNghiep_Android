package com.example.fitnessapp.adapter.community;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.fitnessapp.fragment.HomeFragment;
import com.example.fitnessapp.fragment.community.AllPostFragment;
import com.example.fitnessapp.fragment.community.MyPostFragment;

public class CommunityViewPagerAdapter extends FragmentStateAdapter {


    public CommunityViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new AllPostFragment();
        }
        else {
            return new MyPostFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
