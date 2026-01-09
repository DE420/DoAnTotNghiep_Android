package com.example.fitnessapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.Pagination;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.repository.PostRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostViewModel extends AndroidViewModel {

    private static final String TAG = "PostViewModel";
    private final PostRepository repository;

    // LiveData for posts
    private final MutableLiveData<List<PostResponse>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyStateLiveData = new MutableLiveData<>(false);

    // Pagination
    private int currentPage = 0;
    private boolean hasMorePages = true;
    private boolean isLoadingMore = false;

    // Current posts list
    private final List<PostResponse> allPosts = new ArrayList<>();

    // Search/Filter
    private String searchKeyword = "";
    private String sortOrder = "DESC";
    private String filterStartDate = null;
    private String filterEndDate = null;

    public PostViewModel(@NonNull Application application) {
        super(application);
        repository = new PostRepository(application);
        Log.d(TAG, "PostViewModel created");
    }

    // Getters for LiveData
    public LiveData<List<PostResponse>> getPosts() {
        return postsLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public LiveData<Boolean> getEmptyState() {
        return emptyStateLiveData;
    }

    public boolean hasMorePages() {
        return hasMorePages;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    /**
     * Load posts - called initially and on refresh
     */
    public void loadPosts() {
        loadPosts(false);
    }

    /**
     * Refresh posts - clears existing and loads from page 0
     */
    public void refreshPosts() {
        currentPage = 0;
        hasMorePages = true;
        allPosts.clear();
        loadPosts(true);
    }

    /**
     * Load more posts - for pagination
     */
    public void loadMorePosts() {
        if (isLoadingMore || !hasMorePages) {
            return;
        }

        currentPage++;
        loadPosts(false);
    }

    /**
     * Search posts by keyword
     */
    public void searchPosts(String keyword) {
        this.searchKeyword = keyword;
        currentPage = 0;
        hasMorePages = true;
        allPosts.clear();
        loadPosts(false);
    }

    /**
     * Sort posts
     */
    public void setSortOrder(String order) {
        this.sortOrder = order;
        currentPage = 0;
        hasMorePages = true;
        allPosts.clear();
        loadPosts(false);
    }

    /**
     * Load posts with filters - supports sort order and date range
     */
    public void loadPostsWithFilters(String sortBy, String startDate, String endDate) {
        Log.d(TAG, "loadPostsWithFilters - sortBy: " + sortBy +
                ", startDate: " + startDate +
                ", endDate: " + endDate);

        // Store filter settings
        this.filterStartDate = startDate;
        this.filterEndDate = endDate;

        // Convert sort option to backend order parameter (DESC/ASC)
        if ("oldest".equals(sortBy)) {
            this.sortOrder = "ASC";
        } else {
            // "newest" or default
            this.sortOrder = "DESC";
        }

        // Reset pagination and reload
        currentPage = 0;
        hasMorePages = true;
        allPosts.clear();
        loadPosts(false);
    }

    /**
     * Internal method to load posts
     */
    private void loadPosts(boolean isRefreshing) {
        Log.d(TAG, "loadPosts() called - page: " + currentPage + ", isRefreshing: " + isRefreshing);

        // Set loading state
        if (currentPage == 0) {
            loadingLiveData.setValue(true);
            Log.d(TAG, "Setting loading state to TRUE (initial load)");
        } else {
            isLoadingMore = true;
            Log.d(TAG, "Setting isLoadingMore to TRUE (pagination)");
        }

        // Clear previous error
        errorLiveData.setValue(null);

        // Build query parameters
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(currentPage));
        params.put("size", "5");  // Load 5 posts per page
        params.put("order", sortOrder);

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            params.put("key", searchKeyword);
        }

        // Add date filters if set
        if (filterStartDate != null && !filterStartDate.isEmpty()) {
            params.put("startDate", filterStartDate);
            Log.d(TAG, "Adding startDate filter: " + filterStartDate);
        }

        if (filterEndDate != null && !filterEndDate.isEmpty()) {
            params.put("endDate", filterEndDate);
            Log.d(TAG, "Adding endDate filter: " + filterEndDate);
        }

        Log.d(TAG, "API params: " + params.toString());

        // Make API call
        repository.getAllPosts(params, new Callback<ApiResponse<List<PostResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<PostResponse>>> call,
                                   Response<ApiResponse<List<PostResponse>>> response) {
                Log.d(TAG, "onResponse - HTTP code: " + response.code() + ", isSuccessful: " + response.isSuccessful());

                // Clear loading states
                loadingLiveData.postValue(false);
                isLoadingMore = false;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<PostResponse>> apiResponse = response.body();
                    Log.d(TAG, "Response body received - status: " + apiResponse.isStatus());

                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        List<PostResponse> newPosts = apiResponse.getData();
                        Log.d(TAG, "SUCCESS - Received " + newPosts.size() + " posts");

                        // Add new posts to the list
                        allPosts.addAll(newPosts);
                        Log.d(TAG, "Total posts now: " + allPosts.size());

                        // Update LiveData
                        postsLiveData.postValue(new ArrayList<>(allPosts));

                        // Check pagination
                        Pagination pagination = apiResponse.getMeta();
                        if (pagination != null) {
                            hasMorePages = pagination.isHasMore();
                            Log.d(TAG, "Pagination info - hasMore: " + hasMorePages);
                        } else {
                            // If no pagination info, check if we got posts (5 per page)
                            hasMorePages = newPosts.size() >= 5;
                            Log.d(TAG, "No pagination metadata - calculated hasMore: " + hasMorePages);
                        }

                        // Update empty state
                        boolean isEmpty = allPosts.isEmpty();
                        emptyStateLiveData.postValue(isEmpty);
                        Log.d(TAG, "Empty state: " + isEmpty);

                    } else {
                        // API returned error
                        String errorMsg = apiResponse.getData() != null ?
                            apiResponse.getData().toString() : "Failed to load posts";
                        Log.e(TAG, "API ERROR - status false: " + errorMsg);
                        errorLiveData.postValue(errorMsg);

                        // If first page and no posts, show empty state
                        if (currentPage == 0 && allPosts.isEmpty()) {
                            emptyStateLiveData.postValue(true);
                        }
                    }
                } else {
                    // HTTP error
                    String errorMsg = "Error: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "HTTP ERROR - Code: " + response.code() + ", Body: " + errorBody);
                            errorMsg = errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                        e.printStackTrace();
                    }
                    errorLiveData.postValue(errorMsg);

                    // If first page and no posts, show empty state
                    if (currentPage == 0 && allPosts.isEmpty()) {
                        emptyStateLiveData.postValue(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<PostResponse>>> call, Throwable t) {
                Log.e(TAG, "NETWORK FAILURE: " + t.getMessage(), t);

                // Clear loading states
                loadingLiveData.postValue(false);
                isLoadingMore = false;

                // Set error message
                String errorMsg = t.getMessage() != null ? t.getMessage() : "Network error";
                errorLiveData.postValue(errorMsg);

                // If first page and no posts, show empty state
                if (currentPage == 0 && allPosts.isEmpty()) {
                    emptyStateLiveData.postValue(true);
                }

                // Rollback page if load more failed
                if (currentPage > 0) {
                    currentPage--;
                    Log.d(TAG, "Rolled back page to: " + currentPage);
                }
            }
        });
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorLiveData.setValue(null);
    }
}
