package com.example.fitnessapp.fragment.community;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.community.PostAdapter;
import com.example.fitnessapp.databinding.FragmentMyPostBinding;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.viewmodel.UserPostViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MyPostFragment extends Fragment {

    private static final String TAG = "MyPostFragment";

    private FragmentMyPostBinding binding;
    private UserPostViewModel viewModel;
    private PostAdapter adapter;
    private LinearLayoutManager layoutManager;

    // Search debounce
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Store current filter state
    private String currentSortBy = "newest";
    private String currentStartDate = null;
    private String currentEndDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMyPostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UserPostViewModel.class);
        Log.d(TAG, "ViewModel initialized");

        // Setup UI
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupObservers();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Load/reload data when fragment becomes visible
        // This ensures posts are refreshed when coming back from CreateUpdatePost or PostDetail
        Log.d(TAG, "onStart called - loading user posts...");
        viewModel.loadPosts();
    }

    /**
     * Setup RecyclerView with adapter and pagination
     */
    private void setupRecyclerView() {
        adapter = new PostAdapter(requireContext());
        layoutManager = new LinearLayoutManager(requireContext());

        binding.rvPostList.setLayoutManager(layoutManager);
        binding.rvPostList.setAdapter(adapter);

        // Add divider
        DividerItemDecoration divider = new DividerItemDecoration(requireContext(),
                layoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.post_list_divider, null));
        binding.rvPostList.addItemDecoration(divider);

        // Setup infinite scroll and video pause on scroll
        binding.rvPostList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Pause all videos when user starts scrolling
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    adapter.pauseAllVideos(recyclerView);
                    Log.d(TAG, "User started scrolling - pausing all videos");
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if scrolled to bottom
                if (dy > 0) { // Scrolling down
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    // Load more when 3 items from bottom
                    if (!viewModel.isLoadingMore() && viewModel.hasMorePages()) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                            viewModel.loadMorePosts();
                        }
                    }
                }
            }
        });

        // Setup adapter click listeners
        adapter.setItemListener(new PostAdapter.PostItemListener() {
            @Override
            public void onItemClick(View view, int position) {
                PostResponse post = adapter.getItem(position);
                // Stop all videos before navigating
                adapter.pauseAllVideos();
                navigateToPostDetail(post.getId());
            }

            @Override
            public void onEditClick(View view, int position) {
                PostResponse post = adapter.getItem(position);
                // Stop and release all video players before navigating
                adapter.pauseAllVideos();
                adapter.releaseAllPlayers();
                navigateToEditPost(post);
            }

            @Override
            public void onCommentClick(View view, int position) {
                PostResponse post = adapter.getItem(position);
                // Stop all videos before navigating
                adapter.pauseAllVideos();
                navigateToPostDetail(post.getId());
            }

            @Override
            public void onDeleteClick(View view, int position) {
                PostResponse post = adapter.getItem(position);
                showDeleteConfirmation(post, position);
            }

            @Override
            public void onImageClick(String imageUrl) {
                showFullImageDialog(imageUrl);
            }
        });
    }

    /**
     * Setup SwipeRefreshLayout for pull-to-refresh
     */
    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refreshPosts();
        });

        // Set refresh colors
        binding.swipeRefresh.setColorSchemeResources(
                R.color.yellow,
                R.color.green_500,
                R.color.red_400
        );
    }

    /**
     * Setup search functionality with debounce
     */
    private void setupSearch() {
        // Change SearchView text color to white
        int searchTextId = binding.svKeyword.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        android.widget.TextView searchText = binding.svKeyword.findViewById(searchTextId);
        if (searchText != null) {
            searchText.setTextColor(getResources().getColor(R.color.white, null));
            searchText.setHintTextColor(getResources().getColor(R.color.gray_450, null));
        }

        // Change search icon color to white
        int searchIconId = binding.svKeyword.getContext().getResources()
                .getIdentifier("android:id/search_mag_icon", null, null);
        android.widget.ImageView searchIcon = binding.svKeyword.findViewById(searchIconId);
        if (searchIcon != null) {
            searchIcon.setColorFilter(getResources().getColor(R.color.white, null));
        }

        // Change close/clear icon color to white
        int closeIconId = binding.svKeyword.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        android.widget.ImageView closeIcon = binding.svKeyword.findViewById(closeIconId);
        if (closeIcon != null) {
            closeIcon.setColorFilter(getResources().getColor(R.color.white, null));
        }

        binding.svKeyword.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.searchPosts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Debounce search - wait 500ms after user stops typing
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    if (newText.length() >= 2 || newText.isEmpty()) {
                        viewModel.searchPosts(newText);
                    }
                };

                searchHandler.postDelayed(searchRunnable, 500);
                return true;
            }
        });

        // Setup filter button (reuse same filter dialog)
        binding.ibSearchCriteria.setOnClickListener(v -> {
            showFilterDialog();
        });
    }

    /**
     * Show filter dialog for posts (same as AllPostFragment)
     */
    private void showFilterDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_post_filter, null);
        builder.setView(dialogView);

        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Get filter views
        android.widget.RadioGroup rgSortBy = dialogView.findViewById(R.id.rg_sort_by);
        android.widget.LinearLayout llStartDate = dialogView.findViewById(R.id.tv_start_date).getParent() instanceof android.widget.LinearLayout
            ? (android.widget.LinearLayout) dialogView.findViewById(R.id.tv_start_date).getParent() : null;
        android.widget.LinearLayout llEndDate = dialogView.findViewById(R.id.tv_end_date).getParent() instanceof android.widget.LinearLayout
            ? (android.widget.LinearLayout) dialogView.findViewById(R.id.tv_end_date).getParent() : null;
        android.widget.TextView tvStartDate = dialogView.findViewById(R.id.tv_start_date);
        android.widget.TextView tvEndDate = dialogView.findViewById(R.id.tv_end_date);
        com.google.android.material.button.MaterialButton btnClear = dialogView.findViewById(R.id.btn_clear_filter);
        com.google.android.material.button.MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply_filter);

        // Store selected dates (initialize with current values)
        final String[] startDate = {currentStartDate};
        final String[] endDate = {currentEndDate};

        // Restore previous filter selections
        if ("oldest".equals(currentSortBy)) {
            rgSortBy.check(R.id.rb_oldest);
        } else {
            rgSortBy.check(R.id.rb_newest);
        }

        if (currentStartDate != null) {
            tvStartDate.setText(currentStartDate);
            tvStartDate.setTextColor(getResources().getColor(R.color.white, null));
        }

        if (currentEndDate != null) {
            tvEndDate.setText(currentEndDate);
            tvEndDate.setTextColor(getResources().getColor(R.color.white, null));
        }

        // Start date picker
        if (llStartDate != null) {
            llStartDate.setOnClickListener(v -> {
                showDatePicker((year, month, day) -> {
                    startDate[0] = String.format("%02d/%02d/%d", day, month + 1, year);
                    tvStartDate.setText(startDate[0]);
                    tvStartDate.setTextColor(getResources().getColor(R.color.white, null));
                });
            });
        }

        // End date picker
        if (llEndDate != null) {
            llEndDate.setOnClickListener(v -> {
                showDatePicker((year, month, day) -> {
                    endDate[0] = String.format("%02d/%02d/%d", day, month + 1, year);
                    tvEndDate.setText(endDate[0]);
                    tvEndDate.setTextColor(getResources().getColor(R.color.white, null));
                });
            });
        }

        // Clear filter button
        btnClear.setOnClickListener(v -> {
            rgSortBy.check(R.id.rb_newest);
            startDate[0] = null;
            endDate[0] = null;
            tvStartDate.setText("Select start date");
            tvStartDate.setTextColor(getResources().getColor(R.color.gray_450, null));
            tvEndDate.setText("Select end date");
            tvEndDate.setTextColor(getResources().getColor(R.color.gray_450, null));

            // Apply default filter
            applyFilters("newest", null, null);
            dialog.dismiss();
            Toast.makeText(requireContext(), "Filters cleared", Toast.LENGTH_SHORT).show();
        });

        // Apply filter button
        btnApply.setOnClickListener(v -> {
            // Get selected sort option
            String sortBy = "newest";
            int selectedId = rgSortBy.getCheckedRadioButtonId();
            if (selectedId == R.id.rb_oldest) {
                sortBy = "oldest";
            }

            // Apply filters
            applyFilters(sortBy, startDate[0], endDate[0]);
            dialog.dismiss();
            Toast.makeText(requireContext(), "Filters applied", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    /**
     * Show date picker dialog
     */
    private void showDatePicker(OnDateSelectedListener listener) {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    listener.onDateSelected(selectedYear, selectedMonth, selectedDay);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    /**
     * Interface for date selection callback
     */
    private interface OnDateSelectedListener {
        void onDateSelected(int year, int month, int day);
    }

    /**
     * Apply filters to post list
     */
    private void applyFilters(String sortBy, String startDate, String endDate) {
        Log.d(TAG, "Applying filters - sortBy: " + sortBy +
                ", startDate: " + startDate +
                ", endDate: " + endDate);

        // Save current filter state
        currentSortBy = sortBy;
        currentStartDate = startDate;
        currentEndDate = endDate;

        // Apply filters through ViewModel
        viewModel.loadPostsWithFilters(sortBy, startDate, endDate);
    }

    /**
     * Setup ViewModel observers
     */
    private void setupObservers() {
        Log.d(TAG, "Setting up observers");

        // Observe posts data
        viewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                Log.d(TAG, "Posts observer triggered - received " + posts.size() + " posts");
                updatePostsList(posts);
            } else {
                Log.d(TAG, "Posts observer triggered - received null");
            }
        });

        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                Log.d(TAG, "Loading state changed: " + isLoading);
                binding.swipeRefresh.setRefreshing(isLoading);

                // Show/hide progress bar for initial load
                if (isLoading && adapter.getItemCount() == 0) {
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.rvPostList.setVisibility(View.GONE);
                    binding.llEmptyState.setVisibility(View.GONE);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                }
            }
        });

        // Observe error state
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error occurred: " + error);
                showError(error);
                viewModel.clearError();
            }
        });

        // Observe empty state
        viewModel.getEmptyState().observe(getViewLifecycleOwner(), isEmpty -> {
            Log.d(TAG, "Empty state changed: " + isEmpty);
            if (isEmpty != null && isEmpty) {
                binding.llEmptyState.setVisibility(View.VISIBLE);
                binding.rvPostList.setVisibility(View.GONE);
            } else {
                binding.llEmptyState.setVisibility(View.GONE);
                binding.rvPostList.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Update posts list in adapter
     */
    private void updatePostsList(List<PostResponse> posts) {
        Log.d(TAG, "Updating adapter with " + posts.size() + " posts");
        adapter.setPosts(posts);
    }

    /**
     * Show error message
     */
    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setAction("Retry", v -> viewModel.loadPosts())
                .setBackgroundTint(getResources().getColor(R.color.red_400, null))
                .show();
    }

    /**
     * Show delete confirmation dialog
     */
    private void showDeleteConfirmation(PostResponse post, int position) {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePost(post, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Delete post
     */
    private void deletePost(PostResponse post, int position) {
        Log.d(TAG, "Deleting post: " + post.getId());

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);

        // Call repository to delete
        com.example.fitnessapp.repository.PostRepository repository =
                new com.example.fitnessapp.repository.PostRepository(requireContext());

        repository.deletePost(post.getId(), new retrofit2.Callback<com.example.fitnessapp.model.response.ApiResponse<String>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.fitnessapp.model.response.ApiResponse<String>> call,
                                   retrofit2.Response<com.example.fitnessapp.model.response.ApiResponse<String>> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Log.d(TAG, "Post deleted successfully");
                    adapter.removePost(position);
                    Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to delete post");
                    Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.fitnessapp.model.response.ApiResponse<String>> call,
                                  Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error deleting post: " + t.getMessage());
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigate to post detail screen
     */
    private void navigateToPostDetail(long postId) {
        Log.d(TAG, "Navigating to post detail: " + postId);

        // Create PostDetailFragment with postId argument
        PostDetailFragment detailFragment = PostDetailFragment.newInstance(postId);

        // Navigate using fragment transaction
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Navigate to edit post screen
     */
    private void navigateToEditPost(PostResponse post) {
        Log.d(TAG, "Navigating to edit post: " + post.getId());

        // Create CreateUpdatePostFragment with post data for editing
        CreateUpdatePostFragment editFragment = CreateUpdatePostFragment.newInstance(post);

        // Navigate using fragment transaction
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause all videos when fragment is paused
        if (adapter != null) {
            adapter.pauseAllVideos();
        }
    }

    /**
     * Public method to stop and release all video players
     * Called from parent CommunityFragment when navigating away
     */
    public void stopAndReleaseAllVideos() {
        if (adapter != null) {
            adapter.pauseAllVideos();
            adapter.releaseAllPlayers();
            Log.d(TAG, "All videos stopped and released");
        }
    }

    private void showFullImageDialog(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;

        // Create dialog
        android.app.Dialog dialog = new android.app.Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        // Get views
        android.widget.ImageView imageView = dialog.findViewById(R.id.iv_full_image);
        android.widget.ImageButton closeButton = dialog.findViewById(R.id.ib_close);

        // Load image
        com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .placeholder(R.color.gray_450)
                .into(imageView);

        // Close button
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Close on image click
        imageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        // Release all video players
        if (adapter != null) {
            adapter.releaseAllPlayers();
        }
        binding = null;
    }
}
