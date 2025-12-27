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
import com.example.fitnessapp.databinding.FragmentMyMenusBinding;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.viewmodel.MenuViewModel;

public class MyMenusFragment extends Fragment {

    public static final String TAG = MyMenusFragment.class.getSimpleName();

    private FragmentMyMenusBinding binding;
    private MenuViewModel viewModel;
    private MenuAdapter adapter;
    private boolean isLoadingMore = false;

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
                MenuDetailFragment detailFragment = MenuDetailFragment.newInstance(menu.getId());
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onCloneClick(MenuResponse menu) {
                // Should not happen on My Menus (clone button hidden for owned menus)
                Toast.makeText(requireContext(), "This is your menu", Toast.LENGTH_SHORT).show();
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

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshMyMenus();
        });

        binding.swipeRefresh.setColorSchemeResources(R.color.yellow);
    }

    private void observeViewModel() {
        // Observe menus
        viewModel.getMyMenus().observe(getViewLifecycleOwner(), menus -> {
            Log.d(TAG, "My Menus updated: " + (menus != null ? menus.size() : 0));
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
            viewModel.loadMoreMyMenus();

            // Reset flag after a delay
            binding.rvMenuList.postDelayed(() -> {
                isLoadingMore = false;
            }, 1000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
