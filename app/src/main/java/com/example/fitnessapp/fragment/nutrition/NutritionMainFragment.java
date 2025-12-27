package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.nutrition.NutritionViewPagerAdapter;
import com.example.fitnessapp.databinding.FragmentNutritionMainBinding;
import com.google.android.material.tabs.TabLayoutMediator;

public class NutritionMainFragment extends Fragment {

    public static final String TAG = NutritionMainFragment.class.getSimpleName();

    private FragmentNutritionMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNutritionMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup ViewPager adapter
        NutritionViewPagerAdapter adapter = new NutritionViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        // Set offscreen page limit to keep both fragments alive
        // This prevents fragments from being destroyed and recreated when switching tabs
        binding.viewPager.setOffscreenPageLimit(1);

        // Setup TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.public_menus);
                    } else {
                        tab.setText(R.string.my_menus);
                    }
                }
        ).attach();

        // Enable nested scrolling for ViewPager2 to work with AppBarLayout
        setupNestedScrolling();
    }

    /**
     * Setup nested scrolling between ViewPager2 children and AppBarLayout
     * This allows the TabLayout to hide/show when scrolling in child fragments
     */
    private void setupNestedScrolling() {
        // Get the internal RecyclerView from ViewPager2
        View recyclerView = binding.viewPager.getChildAt(0);
        if (recyclerView instanceof RecyclerView) {
            recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            ((RecyclerView) recyclerView).setNestedScrollingEnabled(true);
        }

        // Listen to page changes and update scroll behavior
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Expand AppBarLayout when switching tabs
                binding.appBarLayout.setExpanded(true, true);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
