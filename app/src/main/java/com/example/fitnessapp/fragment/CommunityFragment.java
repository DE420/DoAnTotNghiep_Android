package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.community.CommunityViewPagerAdapter;
import com.example.fitnessapp.databinding.FragmentCommunityBinding;
import com.example.fitnessapp.network.ApiService;
import com.google.android.material.tabs.TabLayoutMediator;

public class CommunityFragment extends Fragment {

    public static final String TAG = CommunityFragment.class.getSimpleName();
    private ApiService apiService;

    private FragmentCommunityBinding binding;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentCommunityBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CommunityViewPagerAdapter adapter = new CommunityViewPagerAdapter(this);

        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("All Posts");
                    } else {
                        tab.setText("My Posts");
                    }
                }
        ).attach();
    }
}