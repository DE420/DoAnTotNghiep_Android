package com.example.fitnessapp.fragment.nutrition;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.nutrition.MenuAdapter;
import com.example.fitnessapp.databinding.FragmentPublicMenusBinding;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.viewmodel.MenuViewModel;
import com.google.android.material.chip.Chip;

public class PublicMenusFragment extends Fragment {

    public static final String TAG = PublicMenusFragment.class.getSimpleName();

    private FragmentPublicMenusBinding binding;
    private MenuViewModel viewModel;
    private MenuAdapter adapter;
    private boolean isLoadingMore = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPublicMenusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MenuViewModel.class);

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

        // Load initial data
        viewModel.loadPublicMenus();
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
                MenuDetailFragment detailFragment = MenuDetailFragment.newInstance(menu.getId());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCloneClick(MenuResponse menu) {
                cloneMenu(menu);
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

                        if (!isLoadingMore && viewModel.hasMorePublicPages()) {
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
        binding.svSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchPublicMenus(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    viewModel.searchPublicMenus("");
                }
                return true;
            }
        });
    }

    private void setupFilters() {
        binding.chipAll.setOnClickListener(v -> {
            if (((Chip) v).isChecked()) {
                viewModel.filterByGoal(null);
            }
        });

        binding.chipLoseWeight.setOnClickListener(v -> {
            if (((Chip) v).isChecked()) {
                viewModel.filterByGoal(FitnessGoal.LOSE_WEIGHT);
            }
        });

        binding.chipGainWeight.setOnClickListener(v -> {
            if (((Chip) v).isChecked()) {
                viewModel.filterByGoal(FitnessGoal.GAIN_WEIGHT);
            }
        });

        binding.chipMuscleGain.setOnClickListener(v -> {
            if (((Chip) v).isChecked()) {
                viewModel.filterByGoal(FitnessGoal.MUSCLE_GAIN);
            }
        });

        binding.chipShapeBody.setOnClickListener(v -> {
            if (((Chip) v).isChecked()) {
                viewModel.filterByGoal(FitnessGoal.SHAPE_BODY);
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshPublicMenus();
        });

        binding.swipeRefresh.setColorSchemeResources(R.color.yellow);
    }

    private void observeViewModel() {
        // Observe menus
        viewModel.getPublicMenus().observe(getViewLifecycleOwner(), menus -> {
            Log.d(TAG, "Menus updated: " + (menus != null ? menus.size() : 0));
            adapter.setMenuList(menus);
        });

        // Observe loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.swipeRefresh.setRefreshing(false);
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
            viewModel.loadMorePublicMenus();

            // Reset flag after a delay
            binding.rvMenuList.postDelayed(() -> {
                isLoadingMore = false;
            }, 1000);
        }
    }

    private void cloneMenu(MenuResponse menu) {
        viewModel.cloneMenu(menu.getId(), new MenuViewModel.OnMenuClonedListener() {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
