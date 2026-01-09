package com.example.fitnessapp.fragment.community;

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
import com.example.fitnessapp.adapter.community.CommunityViewPagerAdapter;
import com.example.fitnessapp.databinding.FragmentCommunityBinding;
import com.example.fitnessapp.fragment.nutrition.MenuDetailFragment;
import com.example.fitnessapp.fragment.nutrition.NutritionMainFragment;
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

        // Set offscreen page limit to keep both fragments alive
        // This prevents fragments from being destroyed and recreated when switching tabs
        binding.viewPager.setOffscreenPageLimit(1);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText(R.string.community_tab_all_posts);
                    } else {
                        tab.setText(R.string.community_tab_my_posts);
                    }
                }
        ).attach();

        // Enable nested scrolling for ViewPager2 to work with AppBarLayout
        setupNestedScrolling();

        // Setup FAB click listener
        setupFabClickListener();
    }

    private void setupFabClickListener() {
        binding.fabCreatePost.setOnClickListener(v -> {
            // Pause all videos in currently visible fragment before navigating
            pauseAllVideosInCurrentFragment();

            // Navigate to CreateUpdatePostFragment for creating a new post
            com.example.fitnessapp.fragment.community.CreateUpdatePostFragment createFragment =
                    com.example.fitnessapp.fragment.community.CreateUpdatePostFragment.newInstance(null);

            // Hide this fragment instead of replacing to preserve state and scroll position
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .add(R.id.fragment_container, createFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide MainActivity's app bar when this fragment becomes visible
        showHeaderAndBottomNavigation();
    }

    private void showHeaderAndBottomNavigation() {
        if (getActivity() != null) {
            View appBarLayout = getActivity().findViewById(R.id.app_bar_layout);
            View bottomNavigation = getActivity().findViewById(R.id.bottom_navigation);

            if (appBarLayout != null) {
                appBarLayout.setVisibility(View.VISIBLE);
            }

            if (bottomNavigation != null) {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // Hide/show this fragment's app bar when fragment is hidden/shown
        if (binding != null && binding.appBarLayout != null) {
            binding.appBarLayout.setVisibility(hidden ? View.GONE : View.VISIBLE);
        }

        // When fragment becomes visible again, ensure MainActivity's app bar is hidden
        if (!hidden) {
            showHeaderAndBottomNavigation();
        }
    }


    /**
     * Check if a community-related fragment is in the foreground
     */
    private boolean isCommunityFragmentInForeground() {
        if (getActivity() == null) return false;

        Fragment currentFragment = getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        // Check if current fragment is any nutrition-related fragment
        return currentFragment instanceof CommunityFragment;
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

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                // When user starts swiping, pause all videos in both fragments
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    pauseAllVideosInCurrentFragment();
                }
            }
        });
    }

    /**
     * Pause all videos in the currently visible fragment
     */
    private void pauseAllVideosInCurrentFragment() {
        int currentItem = binding.viewPager.getCurrentItem();
        Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + currentItem);

        if (currentFragment instanceof com.example.fitnessapp.fragment.community.AllPostFragment) {
            com.example.fitnessapp.fragment.community.AllPostFragment allPostFragment =
                    (com.example.fitnessapp.fragment.community.AllPostFragment) currentFragment;
            allPostFragment.stopAndReleaseAllVideos();
        } else if (currentFragment instanceof com.example.fitnessapp.fragment.community.MyPostFragment) {
            com.example.fitnessapp.fragment.community.MyPostFragment myPostFragment =
                    (com.example.fitnessapp.fragment.community.MyPostFragment) currentFragment;
            myPostFragment.stopAndReleaseAllVideos();
        }
    }

    /**
     * Pause all videos in all child fragments when navigating away
     */
    private void pauseAllVideosInAllFragments() {
        // Pause videos in AllPostFragment (position 0)
        Fragment allPostFragment = getChildFragmentManager().findFragmentByTag("f0");
        if (allPostFragment instanceof com.example.fitnessapp.fragment.community.AllPostFragment) {
            ((com.example.fitnessapp.fragment.community.AllPostFragment) allPostFragment).stopAndReleaseAllVideos();
        }

        // Pause videos in MyPostFragment (position 1)
        Fragment myPostFragment = getChildFragmentManager().findFragmentByTag("f1");
        if (myPostFragment instanceof com.example.fitnessapp.fragment.community.MyPostFragment) {
            ((com.example.fitnessapp.fragment.community.MyPostFragment) myPostFragment).stopAndReleaseAllVideos();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop all videos when fragment is paused (user navigates away)
        pauseAllVideosInAllFragments();
        // Only show MainActivity's app bar if we're actually leaving the nutrition flow
        // Check if the next fragment is also a nutrition fragment
    }

    @Override
    public void onStop() {
        super.onStop();
        // Ensure videos are stopped when fragment is no longer visible
        pauseAllVideosInAllFragments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Final cleanup - ensure all videos are stopped and released
        pauseAllVideosInAllFragments();
    }
}