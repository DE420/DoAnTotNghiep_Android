package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
    private Menu toolbarMenu;
    private int currentTabPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNutritionMainBinding.inflate(inflater, container, false);

        // Enable options menu for this fragment
        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Toolbar
        setupToolbar();

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
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Inflate menu
        binding.toolbar.inflateMenu(R.menu.menu_nutrition_main);
        toolbarMenu = binding.toolbar.getMenu();

        // Handle menu item clicks
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_add_menu) {
                onCreateMenuClicked();
                return true;
            }
            return false;
        });

        // Initially hide the add button (show only on My Menus tab)
        updateToolbarMenu();
    }

    /**
     * Handle create menu button click
     */
    private void onCreateMenuClicked() {
        CreateEditMenuFragment fragment = CreateEditMenuFragment.newInstance(null);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Update toolbar menu visibility based on current tab
     */
    private void updateToolbarMenu() {
        if (toolbarMenu != null) {
            MenuItem addMenuItem = toolbarMenu.findItem(R.id.action_add_menu);
            if (addMenuItem != null) {
                // Show add button on both tabs
                addMenuItem.setVisible(true);
            }
        }
    }

    /**
     * Hide MainActivity's app bar when showing nutrition screens
     */
    private void hideMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(false);
        }
    }

    /**
     * Show MainActivity's app bar when leaving nutrition screens
     */
    private void showMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(true);
        }
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

                // Update current tab position and toolbar menu
                currentTabPosition = position;
                updateToolbarMenu();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hide MainActivity's app bar when this fragment becomes visible
        hideMainAppBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Only show MainActivity's app bar if we're actually leaving the nutrition flow
        // Check if the next fragment is also a nutrition fragment
        if (!isNutritionFragmentInForeground()) {
            showMainAppBar();
        }
    }

    /**
     * Check if a nutrition-related fragment is in the foreground
     */
    private boolean isNutritionFragmentInForeground() {
        if (getActivity() == null) return false;

        Fragment currentFragment = getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);

        // Check if current fragment is any nutrition-related fragment
        return currentFragment instanceof NutritionMainFragment ||
               currentFragment instanceof MenuDetailFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
