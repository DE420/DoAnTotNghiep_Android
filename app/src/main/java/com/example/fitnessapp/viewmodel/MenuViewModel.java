package com.example.fitnessapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.Pagination;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.repository.NutritionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuViewModel extends AndroidViewModel {

    private static final String TAG = "MenuViewModel";
    private static final int PAGE_SIZE = 10;

    private final NutritionRepository repository;

    // LiveData for public menus
    private final MutableLiveData<List<MenuResponse>> publicMenusLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<MenuResponse>> myMenusLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emptyStateLiveData = new MutableLiveData<>(false);

    // Pagination for public menus
    private int publicCurrentPage = 0;
    private boolean publicHasMorePages = true;
    private boolean publicIsLoadingMore = false;
    private final List<MenuResponse> allPublicMenus = new ArrayList<>();

    // Pagination for my menus
    private int myCurrentPage = 0;
    private boolean myHasMorePages = true;
    private boolean myIsLoadingMore = false;
    private final List<MenuResponse> allMyMenus = new ArrayList<>();

    // Filters for public menus
    private String searchKeyword = "";
    private FitnessGoal filterGoal = null;

    // Filters for my menus
    private String mySearchKeyword = "";

    public MenuViewModel(@NonNull Application application) {
        super(application);
        repository = new NutritionRepository(application);
        Log.d(TAG, "MenuViewModel created");
    }

    // Getters for LiveData
    public LiveData<List<MenuResponse>> getPublicMenus() {
        return publicMenusLiveData;
    }

    public LiveData<List<MenuResponse>> getMyMenus() {
        return myMenusLiveData;
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

    public boolean hasMorePublicPages() {
        return publicHasMorePages;
    }

    public boolean isLoadingMorePublic() {
        return publicIsLoadingMore;
    }

    public boolean hasMoreMyPages() {
        return myHasMorePages;
    }

    public boolean isLoadingMoreMy() {
        return myIsLoadingMore;
    }

    /**
     * Load public menus - called initially
     */
    public void loadPublicMenus() {
        loadPublicMenusInternal(false);
    }

    /**
     * Refresh public menus - clears existing and loads from page 0
     */
    public void refreshPublicMenus() {
        publicCurrentPage = 0;
        publicHasMorePages = true;
        allPublicMenus.clear();
        loadPublicMenusInternal(true);
    }

    /**
     * Load more public menus - for pagination
     */
    public void loadMorePublicMenus() {
        if (publicIsLoadingMore || !publicHasMorePages) {
            Log.d(TAG, "Skip load more - isLoading: " + publicIsLoadingMore + ", hasMore: " + publicHasMorePages);
            return;
        }

        publicCurrentPage++;
        loadPublicMenusInternal(false);
    }

    /**
     * Search public menus by keyword
     */
    public void searchPublicMenus(String keyword) {
        this.searchKeyword = keyword;
        publicCurrentPage = 0;
        publicHasMorePages = true;
        allPublicMenus.clear();
        loadPublicMenusInternal(false);
    }

    /**
     * Filter public menus by fitness goal
     */
    public void filterByGoal(FitnessGoal goal) {
        this.filterGoal = goal;
        publicCurrentPage = 0;
        publicHasMorePages = true;
        allPublicMenus.clear();
        loadPublicMenusInternal(false);
    }

    /**
     * Internal method to load public menus
     */
    private void loadPublicMenusInternal(boolean isRefreshing) {
        Log.d(TAG, "loadPublicMenus() - page: " + publicCurrentPage + ", refresh: " + isRefreshing);

        // Set loading state
        if (publicCurrentPage == 0) {
            loadingLiveData.setValue(true);
            Log.d(TAG, "Setting loading state to TRUE (initial load)");
        } else {
            publicIsLoadingMore = true;
            Log.d(TAG, "Setting isLoadingMore to TRUE (pagination)");
        }

        // Clear previous error
        errorLiveData.setValue(null);

        // Build query parameters
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(publicCurrentPage));
        params.put("size", String.valueOf(PAGE_SIZE));

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            params.put("search", searchKeyword);
            Log.d(TAG, "Adding search param: " + searchKeyword);
        }

        if (filterGoal != null) {
            params.put("goal", filterGoal.name());
            Log.d(TAG, "Adding goal filter: " + filterGoal.name());
        }

        Log.d(TAG, "API params: " + params.toString());

        // Make API call
        repository.getPublicMenus(params, new Callback<ApiResponse<List<MenuResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MenuResponse>>> call,
                                 Response<ApiResponse<List<MenuResponse>>> response) {
                Log.d(TAG, "onResponse - HTTP code: " + response.code());

                // Clear loading states
                loadingLiveData.postValue(false);
                publicIsLoadingMore = false;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<MenuResponse>> apiResponse = response.body();
                    Log.d(TAG, "Response received - status: " + apiResponse.isStatus());

                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        List<MenuResponse> newMenus = apiResponse.getData();
                        Log.d(TAG, "SUCCESS - Received " + newMenus.size() + " menus");

                        // Add new menus to the list
                        allPublicMenus.addAll(newMenus);
                        Log.d(TAG, "Total menus now: " + allPublicMenus.size());

                        // Update LiveData
                        publicMenusLiveData.postValue(new ArrayList<>(allPublicMenus));

                        // Check pagination
                        Pagination pagination = apiResponse.getMeta();
                        if (pagination != null) {
                            publicHasMorePages = pagination.isHasMore();
                            Log.d(TAG, "Pagination - hasMore: " + publicHasMorePages);
                        } else {
                            publicHasMorePages = newMenus.size() >= PAGE_SIZE;
                            Log.d(TAG, "No pagination - calculated hasMore: " + publicHasMorePages);
                        }

                        // Update empty state
                        boolean isEmpty = allPublicMenus.isEmpty();
                        emptyStateLiveData.postValue(isEmpty);
                        Log.d(TAG, "Empty state: " + isEmpty);

                    } else {
                        String errorMsg = "Failed to load menus";
                        Log.e(TAG, "API ERROR - status false");
                        errorLiveData.postValue(errorMsg);

                        if (publicCurrentPage == 0 && allPublicMenus.isEmpty()) {
                            emptyStateLiveData.postValue(true);
                        }
                    }
                } else {
                    handleError(response);
                    if (publicCurrentPage == 0 && allPublicMenus.isEmpty()) {
                        emptyStateLiveData.postValue(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MenuResponse>>> call, Throwable t) {
                Log.e(TAG, "NETWORK FAILURE: " + t.getMessage(), t);

                loadingLiveData.postValue(false);
                publicIsLoadingMore = false;

                String errorMsg = t.getMessage() != null ? t.getMessage() : "Network error";
                errorLiveData.postValue(errorMsg);

                if (publicCurrentPage == 0 && allPublicMenus.isEmpty()) {
                    emptyStateLiveData.postValue(true);
                }

                // Rollback page if load more failed
                if (publicCurrentPage > 0) {
                    publicCurrentPage--;
                    Log.d(TAG, "Rolled back page to: " + publicCurrentPage);
                }
            }
        });
    }

    /**
     * Load my menus
     */
    public void loadMyMenus() {
        Log.d(TAG, "loadMyMenus() - page: " + myCurrentPage);

        if (myCurrentPage == 0) {
            loadingLiveData.setValue(true);
        } else {
            myIsLoadingMore = true;
        }

        errorLiveData.setValue(null);

        repository.getMyMenus(myCurrentPage, PAGE_SIZE, mySearchKeyword, new Callback<ApiResponse<List<MenuResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MenuResponse>>> call,
                                 Response<ApiResponse<List<MenuResponse>>> response) {
                loadingLiveData.postValue(false);
                myIsLoadingMore = false;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<MenuResponse>> apiResponse = response.body();

                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        List<MenuResponse> newMenus = apiResponse.getData();
                        Log.d(TAG, "SUCCESS - Received " + newMenus.size() + " my menus");

                        allMyMenus.addAll(newMenus);
                        myMenusLiveData.postValue(new ArrayList<>(allMyMenus));

                        Pagination pagination = apiResponse.getMeta();
                        if (pagination != null) {
                            myHasMorePages = pagination.isHasMore();
                        } else {
                            myHasMorePages = newMenus.size() >= PAGE_SIZE;
                        }

                        emptyStateLiveData.postValue(allMyMenus.isEmpty());

                    } else {
                        errorLiveData.postValue("Failed to load my menus");
                        if (myCurrentPage == 0 && allMyMenus.isEmpty()) {
                            emptyStateLiveData.postValue(true);
                        }
                    }
                } else {
                    handleError(response);
                    if (myCurrentPage == 0 && allMyMenus.isEmpty()) {
                        emptyStateLiveData.postValue(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MenuResponse>>> call, Throwable t) {
                Log.e(TAG, "NETWORK FAILURE: " + t.getMessage(), t);

                loadingLiveData.postValue(false);
                myIsLoadingMore = false;

                String errorMsg = t.getMessage() != null ? t.getMessage() : "Network error";
                errorLiveData.postValue(errorMsg);

                if (myCurrentPage == 0 && allMyMenus.isEmpty()) {
                    emptyStateLiveData.postValue(true);
                }

                if (myCurrentPage > 0) {
                    myCurrentPage--;
                }
            }
        });
    }

    /**
     * Refresh my menus
     */
    public void refreshMyMenus() {
        myCurrentPage = 0;
        myHasMorePages = true;
        allMyMenus.clear();
        loadMyMenus();
    }

    /**
     * Load more my menus
     */
    public void loadMoreMyMenus() {
        if (myIsLoadingMore || !myHasMorePages) {
            return;
        }

        myCurrentPage++;
        loadMyMenus();
    }

    /**
     * Search my menus by keyword
     */
    public void searchMyMenus(String keyword) {
        this.mySearchKeyword = keyword != null ? keyword : "";
        myCurrentPage = 0;
        myHasMorePages = true;
        allMyMenus.clear();
        loadMyMenus();
    }

    /**
     * Clone a menu
     */
    public void cloneMenu(Long menuId, OnMenuClonedListener listener) {
        repository.cloneMenu(menuId, new Callback<ApiResponse<MenuResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MenuResponse>> call,
                                 Response<ApiResponse<MenuResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        listener.onSuccess(response.body().getData());
                    } else {
                        listener.onError("Failed to clone menu");
                    }
                } else {
                    listener.onError("Failed to clone menu");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MenuResponse>> call, Throwable t) {
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Delete a menu
     */
    public void deleteMenu(Long menuId, OnMenuDeletedListener listener) {
        repository.deleteMenu(menuId, new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call,
                                 Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        listener.onSuccess();
                    } else {
                        listener.onError("Failed to delete menu");
                    }
                } else {
                    listener.onError("Failed to delete menu");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                listener.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleError(Response<?> response) {
        String errorMsg = "Error: " + response.code();
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e(TAG, "HTTP ERROR - Code: " + response.code() + ", Body: " + errorBody);
                errorMsg = errorBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading error body", e);
        }
        errorLiveData.postValue(errorMsg);
    }

    public void clearError() {
        errorLiveData.setValue(null);
    }

    // Listener interfaces
    public interface OnMenuClonedListener {
        void onSuccess(MenuResponse menu);
        void onError(String message);
    }

    public interface OnMenuDeletedListener {
        void onSuccess();
        void onError(String message);
    }
}
