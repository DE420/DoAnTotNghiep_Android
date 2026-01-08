package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.nutrition.MealAdapter;
import com.example.fitnessapp.databinding.FragmentMenuDetailBinding;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.viewmodel.MenuDetailViewModel;

import java.util.Locale;

public class MenuDetailFragment extends Fragment {

    public static final String TAG = MenuDetailFragment.class.getSimpleName();
    private static final String ARG_MENU_ID = "menu_id";

    private FragmentMenuDetailBinding binding;
    private MenuDetailViewModel viewModel;
    private MealAdapter mealAdapter;
    private Long menuId;
    private Menu toolbarMenu;
    private MenuResponse currentMenu;

    private long currentUserId;

    public static MenuDetailFragment newInstance(Long menuId) {
        MenuDetailFragment fragment = new MenuDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_MENU_ID, menuId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            menuId = getArguments().getLong(ARG_MENU_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMenuDetailBinding.inflate(inflater, container, false);

        // Enable options menu for this fragment
        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = SessionManager.getInstance(requireActivity().getApplicationContext());

        currentUserId = sessionManager.getUserId();

        // Setup Toolbar
        setupToolbar();

        // Setup SwipeRefreshLayout
        setupSwipeRefresh();

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MenuDetailViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Observe ViewModel
        observeViewModel();

        // Load menu detail
        if (menuId != null) {
            viewModel.loadMenuDetail(menuId);
        }
    }

    private void setupRecyclerView() {
        mealAdapter = new MealAdapter(requireContext());
        binding.rvMeals.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvMeals.setAdapter(mealAdapter);
        binding.rvMeals.setNestedScrollingEnabled(false);

        // Setup click listener for dishes
        mealAdapter.setOnDishClickListener(new MealAdapter.OnDishClickListener() {
            @Override
            public void onDishClick(MealDishResponse dish) {
                navigateToDishDetail(dish);
            }

            @Override
            public void onViewRecipeClick(MealDishResponse dish) {
                navigateToDishDetail(dish);
            }
        });
    }

    /**
     * Setup SwipeRefreshLayout
     */
    private void setupSwipeRefresh() {
        // Set refresh colors
        binding.swipeRefresh.setColorSchemeResources(
                R.color.yellow,
                R.color.white,
                R.color.gray_450
        );

        // Set background color
        binding.swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.black_370);

        // Set refresh listener
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (menuId != null) {
                viewModel.loadMenuDetail(menuId);
            }
        });
    }

    private void observeViewModel() {
        // Observe menu
        viewModel.getMenu().observe(getViewLifecycleOwner(), menu -> {
            if (menu != null) {
                Log.d(TAG, "===== MENU DATA RECEIVED =====");
                Log.d(TAG, "Menu ID: " + menu.getId());
                Log.d(TAG, "Menu Name: " + menu.getName());
                Log.d(TAG, "Description: " + menu.getDescription());
                Log.d(TAG, "Image: " + menu.getImage());
                Log.d(TAG, "Fitness Goal: " + menu.getFitnessGoal());
                Log.d(TAG, "IsOwner: " + menu.getIsOwner());
                Log.d(TAG, "IsPrivate: " + menu.getIsPrivate());
                Log.d(TAG, "Creator ID: " + menu.getCreatorId());
                Log.d(TAG, "Creator Name: " + menu.getCreatorName());
                Log.d(TAG, "Creator Avatar: " + menu.getCreatorAvatar());
                Log.d(TAG, "Calories: " + menu.getCalories());
                Log.d(TAG, "Protein: " + menu.getProtein());
                Log.d(TAG, "Carbs: " + menu.getCarbs());
                Log.d(TAG, "Fat: " + menu.getFat());
                Log.d(TAG, "Meals count: " + (menu.getMeals() != null ? menu.getMeals().size() : 0));
                Log.d(TAG, "==============================");
                displayMenu(menu);
            }
        });

        // Observe loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            // Stop swipe refresh animation when loading completes
            if (!isLoading && binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // Observe error
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });
    }

    private void displayMenu(MenuResponse menu) {
        Log.d(TAG, "Displaying menu: " + menu.getName());

        // Save current menu and update toolbar
        currentMenu = menu;
        updateToolbarMenu();

        // Set menu name
        binding.tvMenuName.setText(menu.getName());

        // Set description
        if (menu.getDescription() != null && !menu.getDescription().isEmpty()) {
            binding.tvMenuDescription.setText(menu.getDescription());
            binding.tvMenuDescription.setVisibility(View.VISIBLE);
        } else {
            binding.tvMenuDescription.setVisibility(View.GONE);
        }

        // Set creator info (only show for menus you don't own)
        boolean isOwner = menu.getCreatorId() != null && menu.getCreatorId() == currentUserId;
        Log.d(TAG, "Creator Info Check - isOwner: " + isOwner);
        Log.d(TAG, "Creator Info Check - creatorName: " + menu.getCreatorName());
        Log.d(TAG, "Creator Info Check - creatorAvatar: " + menu.getCreatorAvatar());

        if (!isOwner) {
            Log.d(TAG, "SHOWING creator info");
            binding.llCreatorInfo.setVisibility(View.VISIBLE);

            // Set creator name - show "Unknown User" if null/empty
            if (menu.getCreatorName() != null && !menu.getCreatorName().isEmpty()) {
                binding.tvCreatorName.setText(menu.getCreatorName());
            } else {
                binding.tvCreatorName.setText(R.string.unknown_user);
            }

            // Load creator avatar - use default if null/empty
            if (menu.getCreatorAvatar() != null && !menu.getCreatorAvatar().isEmpty()) {
                Glide.with(this)
                        .load(menu.getCreatorAvatar())
                        .placeholder(R.drawable.img_user_default_128)
                        .error(R.drawable.img_user_default_128)
                        .centerCrop()
                        .into(binding.civCreatorAvatar);
            } else {
                binding.civCreatorAvatar.setImageResource(R.drawable.img_user_default_128);
            }
        } else {
            Log.d(TAG, "HIDING creator info - Reason: User is owner");
            binding.llCreatorInfo.setVisibility(View.GONE);
        }

        // Set nutrition summary
        if (menu.getCalories() != null) {
            binding.tvCalories.setText(String.format(Locale.getDefault(), "%.0f", menu.getCalories()));
        }
        if (menu.getProtein() != null) {
            binding.tvProtein.setText(String.format(Locale.getDefault(), "%.0fg", menu.getProtein()));
        }
        if (menu.getCarbs() != null) {
            binding.tvCarbs.setText(String.format(Locale.getDefault(), "%.0fg", menu.getCarbs()));
        }
        if (menu.getFat() != null) {
            binding.tvFat.setText(String.format(Locale.getDefault(), "%.0fg", menu.getFat()));
        }

        // Set fitness goal
        if (menu.getFitnessGoal() != null) {
            String goalText = getString(R.string.fitness_goal) + ": " +
                    getString(menu.getFitnessGoal().getResId());
            binding.tvFitnessGoal.setText(goalText);
        }

        // Set meals
        if (menu.getMeals() != null && !menu.getMeals().isEmpty()) {
            mealAdapter.setMealList(menu.getMeals());
        }

        // Setup action buttons
        setupActionButtons(menu);
    }

    private void setupActionButtons(MenuResponse menu) {
        // Check if user is owner
        boolean isOwner = menu.getCreatorId() != null && menu.getCreatorId() == currentUserId;

        if (isOwner) {
            // Show edit and delete buttons, hide clone
            binding.btnClone.setVisibility(View.GONE);
            binding.btnEdit.setVisibility(View.VISIBLE);
            binding.btnDelete.setVisibility(View.VISIBLE);

            // Edit button
            binding.btnEdit.setOnClickListener(v -> {
                navigateToEditMenu(menu);
            });

            // Delete button
            binding.btnDelete.setOnClickListener(v -> {
                showDeleteConfirmation(menu);
            });
        } else {
            // Show clone button, hide edit and delete
            binding.btnClone.setVisibility(View.VISIBLE);
            binding.btnEdit.setVisibility(View.GONE);
            binding.btnDelete.setVisibility(View.GONE);

            // Clone button
            binding.btnClone.setOnClickListener(v -> {
                cloneMenu(menu);
            });
        }
    }

    private void cloneMenu(MenuResponse menu) {
        viewModel.cloneMenu(menu.getId(), new MenuDetailViewModel.OnMenuClonedListener() {
            @Override
            public void onSuccess(MenuResponse clonedMenu) {
                Toast.makeText(requireContext(), R.string.menu_cloned_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(MenuResponse menu) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_menu)
                .setMessage(R.string.confirm_delete_menu)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteMenu(menu);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteMenu(MenuResponse menu) {
        viewModel.deleteMenu(menu.getId(), new MenuDetailViewModel.OnMenuDeletedListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), R.string.menu_deleted_success, Toast.LENGTH_SHORT).show();
                // Go back
                requireActivity().onBackPressed();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Inflate menu
        binding.toolbar.inflateMenu(R.menu.menu_menu_detail);
        toolbarMenu = binding.toolbar.getMenu();

        // Handle menu item clicks
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_clone_menu) {
                if (currentMenu != null) {
                    cloneMenuFromToolbar(currentMenu);
                }
                return true;
            }
            return false;
        });

        // Initially hide clone button until we know if user owns the menu
        updateToolbarMenu();
    }

    /**
     * Update toolbar menu based on menu ownership
     */
    private void updateToolbarMenu() {
        if (toolbarMenu != null) {
            MenuItem cloneMenuItem = toolbarMenu.findItem(R.id.action_clone_menu);
            if (cloneMenuItem != null) {
                if (currentMenu != null) {
                    // Show clone button only if user doesn't own this menu
                    boolean isOwner = currentMenu.getCreatorId() != null
                            && currentMenu.getCreatorId() == currentUserId;
                    cloneMenuItem.setVisible(!isOwner);
                } else {
                    // Hide clone button until we know menu ownership
                    cloneMenuItem.setVisible(false);
                }
            }
        }
    }

    /**
     * Clone menu from toolbar
     */
    private void cloneMenuFromToolbar(MenuResponse menu) {
        viewModel.cloneMenu(menu.getId(), new MenuDetailViewModel.OnMenuClonedListener() {
            @Override
            public void onSuccess(MenuResponse clonedMenu) {
                Toast.makeText(requireContext(), R.string.menu_cloned_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Hide MainActivity's app bar when showing menu detail
     */
    private void hideMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(false);
        }
    }

    /**
     * Show MainActivity's app bar when leaving menu detail
     */
    private void showMainAppBar() {
        if (getActivity() instanceof com.example.fitnessapp.MainActivity) {
            ((com.example.fitnessapp.MainActivity) getActivity()).setAppBarVisible(true);
        }
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
     * Navigate to DishDetailFragment
     */
    private void navigateToDishDetail(MealDishResponse dish) {
        if (dish == null || dish.getDishId() == null) {
            Log.e(TAG, "Cannot navigate to dish detail: dish or dishId is null");
            return;
        }

        Log.d(TAG, "Navigating to dish detail: " + dish.getName() + " (ID: " + dish.getDishId() + ")");

        DishDetailFragment fragment = DishDetailFragment.newInstance(dish.getDishId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Navigate to CreateEditMenuFragment for editing
     * Passes the MenuResponse for instant display, then fetches updates in background
     */
    private void navigateToEditMenu(MenuResponse menu) {
        if (menu == null || menu.getId() == null) {
            Log.e(TAG, "Cannot navigate to edit menu: menu or menuId is null");
            return;
        }

        Log.d(TAG, "Navigating to edit menu with data: " + menu.getName() + " (ID: " + menu.getId() + ")");

        // Pass MenuResponse for instant display
        CreateEditMenuFragment fragment = CreateEditMenuFragment.newInstance(menu.getId(), menu);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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
               currentFragment instanceof MenuDetailFragment ||
               currentFragment instanceof DishDetailFragment;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
