package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.FitnessGoalAdapter;
import com.example.fitnessapp.adapter.nutrition.MenuAdapter;
import com.example.fitnessapp.databinding.FragmentMyMenusBinding;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.viewmodel.MenuViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class MyMenusFragment extends Fragment {

    public static final String TAG = MyMenusFragment.class.getSimpleName();

    private FragmentMyMenusBinding binding;
    private MenuViewModel viewModel;
    private MenuAdapter adapter;
    private boolean isLoadingMore = false;

    // Store current filter state
    private FitnessGoal currentFilterGoal = null;
    private Float currentMinCalories = null;
    private Float currentMaxCalories = null;
    private Float currentMinProtein = null;
    private Float currentMaxProtein = null;
    private Float currentMinCarbs = null;
    private Float currentMaxCarbs = null;
    private Float currentMinFat = null;
    private Float currentMaxFat = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyMenusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel (shared with parent activity/fragment)
        viewModel = new ViewModelProvider(requireActivity()).get(MenuViewModel.class);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Search
        setupSearch();

        // Setup Filters
        setupFilters();

        // Setup Swipe Refresh
        setupSwipeRefresh();

        // Observe ViewModel
        observeViewModel();

        // Load initial data only if not already loaded
        if (viewModel.getMyMenus().getValue() == null || viewModel.getMyMenus().getValue().isEmpty()) {
            viewModel.loadMyMenus();
        }
    }

    private void setupRecyclerView() {
        adapter = new MenuAdapter(requireContext());

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        binding.rvMenuList.setLayoutManager(layoutManager);
        binding.rvMenuList.setAdapter(adapter);

        // Setup click listeners
        adapter.setOnMenuClickListener(new MenuAdapter.OnMenuClickListener() {
            @Override
            public void onMenuClick(MenuResponse menu) {
                // Navigate to MenuDetailFragment
                // Hide parent NutritionMainFragment instead of this child fragment to preserve state
                MenuDetailFragment detailFragment = MenuDetailFragment.newInstance(menu.getId());

                Fragment parentFragment = getParentFragment();
                if (parentFragment != null) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .hide(parentFragment)
                            .add(R.id.fragment_container, detailFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }

            @Override
            public void onCloneClick(MenuResponse menu) {
                // Should not happen on My Menus (clone button hidden for owned menus)
                Toast.makeText(requireContext(), R.string.this_is_your_menu, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup pagination
        binding.rvMenuList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // Scrolling down
                    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                        if (!isLoadingMore && viewModel.hasMoreMyPages()) {
                            if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 2) {
                                loadMore();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupSearch() {
        // Set SearchView text color to white
        int searchTextId = getResources().getIdentifier("android:id/search_src_text", null, null);
        android.widget.TextView searchText = binding.svSearch.findViewById(searchTextId);
        if (searchText != null) {
            searchText.setTextColor(getResources().getColor(R.color.white, null));
            searchText.setHintTextColor(getResources().getColor(R.color.gray_450, null));
        }

        // Set SearchView icons to white
        int searchIconId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        android.widget.ImageView searchIcon = binding.svSearch.findViewById(searchIconId);
        if (searchIcon != null) {
            searchIcon.setColorFilter(getResources().getColor(R.color.white, null));
        }

        int closeIconId = getResources().getIdentifier("android:id/search_close_btn", null, null);
        android.widget.ImageView closeIcon = binding.svSearch.findViewById(closeIconId);
        if (closeIcon != null) {
            closeIcon.setColorFilter(getResources().getColor(R.color.white, null));
        }

        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchMyMenus(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewModel.searchMyMenus("");
                }
                return true;
            }
        });
    }

    private void setupFilters() {
        // Hide chip-based filters since we're using dialog now
        binding.hsvFilters.setVisibility(View.GONE);

        // Setup filter button click listener
        binding.ibFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshMyMenus();
        });

        binding.swipeRefresh.setColorSchemeResources(
                R.color.yellow,
                R.color.green_500,
                R.color.red_400
        );
    }

    private void observeViewModel() {
        // Observe menus
        viewModel.getMyMenus().observe(getViewLifecycleOwner(), menus -> {
            Log.d(TAG, "My Menus updated: " + (menus != null ? menus.size() : 0));
            adapter.setMenuList(menus);
        });

        // Observe loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        // Observe error
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.clearError();
            }
        });

        // Observe empty state
        viewModel.getEmptyState().observe(getViewLifecycleOwner(), isEmpty -> {
            binding.llEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
    }

    private void loadMore() {
        if (!isLoadingMore) {
            isLoadingMore = true;
            viewModel.loadMoreMyMenus();

            // Reset flag after a delay
            binding.rvMenuList.postDelayed(() -> {
                isLoadingMore = false;
            }, 1000);
        }
    }

    /**
     * Show filter dialog for menus
     */
    private void showFilterDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_menu_filter, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Enable scrolling when keyboard appears
        dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        // Get filter views
        Spinner spFitnessGoal = dialogView.findViewById(R.id.sp_fitness_goal);
        TextInputEditText etMinCalories = dialogView.findViewById(R.id.et_min_calories);
        TextInputEditText etMaxCalories = dialogView.findViewById(R.id.et_max_calories);
        TextInputEditText etMinProtein = dialogView.findViewById(R.id.et_min_protein);
        TextInputEditText etMaxProtein = dialogView.findViewById(R.id.et_max_protein);
        TextInputEditText etMinCarbs = dialogView.findViewById(R.id.et_min_carbs);
        TextInputEditText etMaxCarbs = dialogView.findViewById(R.id.et_max_carbs);
        TextInputEditText etMinFat = dialogView.findViewById(R.id.et_min_fat);
        TextInputEditText etMaxFat = dialogView.findViewById(R.id.et_max_fat);
        android.widget.ImageButton ibClose = dialogView.findViewById(R.id.ib_close);
        MaterialButton btnClear = dialogView.findViewById(R.id.btn_clear_filter);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply_filter);

        // Setup fitness goal spinner with FitnessGoalAdapter
        List<FitnessGoal> goalsList = Arrays.asList(FitnessGoal.values());
        FitnessGoalAdapter goalAdapter = new FitnessGoalAdapter(requireContext(), goalsList);
        spFitnessGoal.setAdapter(goalAdapter);

        // Restore previous filter selections
        if (currentFilterGoal != null) {
            for (int i = 0; i < FitnessGoal.values().length; i++) {
                if (FitnessGoal.values()[i] == currentFilterGoal) {
                    spFitnessGoal.setSelection(i + 1);
                    break;
                }
            }
        } else {
            spFitnessGoal.setSelection(0);
        }

        // Set selection listener for spinner
        spFitnessGoal.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                goalAdapter.setSelectedPosition(position);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        if (currentMinCalories != null) {
            etMinCalories.setText(String.valueOf(currentMinCalories));
        }
        if (currentMaxCalories != null) {
            etMaxCalories.setText(String.valueOf(currentMaxCalories));
        }
        if (currentMinProtein != null) {
            etMinProtein.setText(String.valueOf(currentMinProtein));
        }
        if (currentMaxProtein != null) {
            etMaxProtein.setText(String.valueOf(currentMaxProtein));
        }
        if (currentMinCarbs != null) {
            etMinCarbs.setText(String.valueOf(currentMinCarbs));
        }
        if (currentMaxCarbs != null) {
            etMaxCarbs.setText(String.valueOf(currentMaxCarbs));
        }
        if (currentMinFat != null) {
            etMinFat.setText(String.valueOf(currentMinFat));
        }
        if (currentMaxFat != null) {
            etMaxFat.setText(String.valueOf(currentMaxFat));
        }

        // Close button
        ibClose.setOnClickListener(v -> dialog.dismiss());

        // Clear filter button
        btnClear.setOnClickListener(v -> {
            spFitnessGoal.setSelection(0);
            etMinCalories.setText("");
            etMaxCalories.setText("");
            etMinProtein.setText("");
            etMaxProtein.setText("");
            etMinCarbs.setText("");
            etMaxCarbs.setText("");
            etMinFat.setText("");
            etMaxFat.setText("");

            // Apply default filter (no filters)
            applyFilters(null, null, null, null, null, null, null, null, null);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.menu_clear_filters, Toast.LENGTH_SHORT).show();
        });

        // Apply filter button
        btnApply.setOnClickListener(v -> {
            // Get selected fitness goal
            FitnessGoal selectedGoal = null;
            int goalPosition = spFitnessGoal.getSelectedItemPosition();
            if (goalPosition > 0) {
                selectedGoal = FitnessGoal.values()[goalPosition - 1];
            }

            // Parse nutrition values
            Float minCal = parseFloatFromEditText(etMinCalories);
            Float maxCal = parseFloatFromEditText(etMaxCalories);
            Float minProt = parseFloatFromEditText(etMinProtein);
            Float maxProt = parseFloatFromEditText(etMaxProtein);
            Float minCarb = parseFloatFromEditText(etMinCarbs);
            Float maxCarb = parseFloatFromEditText(etMaxCarbs);
            Float minFatVal = parseFloatFromEditText(etMinFat);
            Float maxFatVal = parseFloatFromEditText(etMaxFat);

            // Apply filters
            applyFilters(selectedGoal, minCal, maxCal, minProt, maxProt, minCarb, maxCarb, minFatVal, maxFatVal);
            dialog.dismiss();
            Toast.makeText(requireContext(), R.string.menu_apply_filter, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    /**
     * Helper method to parse float from TextInputEditText
     */
    private Float parseFloatFromEditText(TextInputEditText editText) {
        String text = editText.getText() != null ? editText.getText().toString().trim() : "";
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing float: " + text, e);
            return null;
        }
    }

    /**
     * Apply filters to menu list
     */
    private void applyFilters(FitnessGoal goal, Float minCalories, Float maxCalories,
                             Float minProtein, Float maxProtein, Float minCarbs, Float maxCarbs,
                             Float minFat, Float maxFat) {
        Log.d(TAG, "Applying filters - goal: " + goal +
                ", calories: " + minCalories + "-" + maxCalories +
                ", protein: " + minProtein + "-" + maxProtein +
                ", carbs: " + minCarbs + "-" + maxCarbs +
                ", fat: " + minFat + "-" + maxFat);

        // Save current filter state
        currentFilterGoal = goal;
        currentMinCalories = minCalories;
        currentMaxCalories = maxCalories;
        currentMinProtein = minProtein;
        currentMaxProtein = maxProtein;
        currentMinCarbs = minCarbs;
        currentMaxCarbs = maxCarbs;
        currentMinFat = minFat;
        currentMaxFat = maxFat;

        // Apply filters through ViewModel
        viewModel.loadMyMenusWithFilters(goal, minCalories, maxCalories, minProtein, maxProtein,
                minCarbs, maxCarbs, minFat, maxFat);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
