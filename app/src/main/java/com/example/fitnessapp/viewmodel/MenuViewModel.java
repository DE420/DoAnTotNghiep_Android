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
    private Float filterMinCalories = null;
    private Float filterMaxCalories = null;
    private Float filterMinProtein = null;
    private Float filterMaxProtein = null;
    private Float filterMinCarbs = null;
    private Float filterMaxCarbs = null;
    private Float filterMinFat = null;
    private Float filterMaxFat = null;

    // Filters for my menus
    private String mySearchKeyword = "";
    private FitnessGoal myFilterGoal = null;
    private Float myFilterMinCalories = null;
    private Float myFilterMaxCalories = null;
    private Float myFilterMinProtein = null;
    private Float myFilterMaxProtein = null;
    private Float myFilterMinCarbs = null;
    private Float myFilterMaxCarbs = null;
    private Float myFilterMinFat = null;
    private Float myFilterMaxFat = null;

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
        // Don't clear data here - wait for successful response
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
     * Load public menus with all filters
     */
    public void loadPublicMenusWithFilters(FitnessGoal goal, Float minCalories, Float maxCalories,
                                          Float minProtein, Float maxProtein, Float minCarbs, Float maxCarbs,
                                          Float minFat, Float maxFat) {
        this.filterGoal = goal;
        this.filterMinCalories = minCalories;
        this.filterMaxCalories = maxCalories;
        this.filterMinProtein = minProtein;
        this.filterMaxProtein = maxProtein;
        this.filterMinCarbs = minCarbs;
        this.filterMaxCarbs = maxCarbs;
        this.filterMinFat = minFat;
        this.filterMaxFat = maxFat;

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

        if (filterMinCalories != null) {
            params.put("minCalories", String.valueOf(filterMinCalories));
            Log.d(TAG, "Adding minCalories filter: " + filterMinCalories);
        }

        if (filterMaxCalories != null) {
            params.put("maxCalories", String.valueOf(filterMaxCalories));
            Log.d(TAG, "Adding maxCalories filter: " + filterMaxCalories);
        }

        if (filterMinProtein != null) {
            params.put("minProtein", String.valueOf(filterMinProtein));
            Log.d(TAG, "Adding minProtein filter: " + filterMinProtein);
        }

        if (filterMaxProtein != null) {
            params.put("maxProtein", String.valueOf(filterMaxProtein));
            Log.d(TAG, "Adding maxProtein filter: " + filterMaxProtein);
        }

        if (filterMinCarbs != null) {
            params.put("minCarbs", String.valueOf(filterMinCarbs));
            Log.d(TAG, "Adding minCarbs filter: " + filterMinCarbs);
        }

        if (filterMaxCarbs != null) {
            params.put("maxCarbs", String.valueOf(filterMaxCarbs));
            Log.d(TAG, "Adding maxCarbs filter: " + filterMaxCarbs);
        }

        if (filterMinFat != null) {
            params.put("minFat", String.valueOf(filterMinFat));
            Log.d(TAG, "Adding minFat filter: " + filterMinFat);
        }

        if (filterMaxFat != null) {
            params.put("maxFat", String.valueOf(filterMaxFat));
            Log.d(TAG, "Adding maxFat filter: " + filterMaxFat);
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

                        // Clear old data only on successful refresh
                        if (isRefreshing) {
                            allPublicMenus.clear();
                            Log.d(TAG, "Cleared old data for refresh");
                        }

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
        loadMyMenus(false);
    }

    /**
     * Internal method to load my menus
     */
    private void loadMyMenus(boolean isRefreshing) {
        Log.d(TAG, "loadMyMenus() - page: " + myCurrentPage + ", refresh: " + isRefreshing);

        if (myCurrentPage == 0) {
            loadingLiveData.setValue(true);
        } else {
            myIsLoadingMore = true;
        }

        errorLiveData.setValue(null);

        // Build query parameters
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(myCurrentPage));
        params.put("size", String.valueOf(PAGE_SIZE));

        if (mySearchKeyword != null && !mySearchKeyword.trim().isEmpty()) {
            params.put("search", mySearchKeyword);
        }

        if (myFilterGoal != null) {
            params.put("goal", myFilterGoal.name());
        }

        if (myFilterMinCalories != null) {
            params.put("minCalories", String.valueOf(myFilterMinCalories));
        }

        if (myFilterMaxCalories != null) {
            params.put("maxCalories", String.valueOf(myFilterMaxCalories));
        }

        if (myFilterMinProtein != null) {
            params.put("minProtein", String.valueOf(myFilterMinProtein));
        }

        if (myFilterMaxProtein != null) {
            params.put("maxProtein", String.valueOf(myFilterMaxProtein));
        }

        if (myFilterMinCarbs != null) {
            params.put("minCarbs", String.valueOf(myFilterMinCarbs));
        }

        if (myFilterMaxCarbs != null) {
            params.put("maxCarbs", String.valueOf(myFilterMaxCarbs));
        }

        if (myFilterMinFat != null) {
            params.put("minFat", String.valueOf(myFilterMinFat));
        }

        if (myFilterMaxFat != null) {
            params.put("maxFat", String.valueOf(myFilterMaxFat));
        }

        repository.getMyMenus(params, new Callback<ApiResponse<List<MenuResponse>>>() {
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

                        // Clear old data only on successful refresh
                        if (isRefreshing) {
                            allMyMenus.clear();
                            Log.d(TAG, "Cleared old my menus data for refresh");
                        }

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
        // Don't clear data here - wait for successful response
        loadMyMenus(true);
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
     * Filter my menus by fitness goal
     */
    public void filterMyMenusByGoal(FitnessGoal goal) {
        this.myFilterGoal = goal;
        myCurrentPage = 0;
        myHasMorePages = true;
        allMyMenus.clear();
        loadMyMenus(false);
    }

    /**
     * Load my menus with all filters
     */
    public void loadMyMenusWithFilters(FitnessGoal goal, Float minCalories, Float maxCalories,
                                      Float minProtein, Float maxProtein, Float minCarbs, Float maxCarbs,
                                      Float minFat, Float maxFat) {
        this.myFilterGoal = goal;
        this.myFilterMinCalories = minCalories;
        this.myFilterMaxCalories = maxCalories;
        this.myFilterMinProtein = minProtein;
        this.myFilterMaxProtein = maxProtein;
        this.myFilterMinCarbs = minCarbs;
        this.myFilterMaxCarbs = maxCarbs;
        this.myFilterMinFat = minFat;
        this.myFilterMaxFat = maxFat;

        myCurrentPage = 0;
        myHasMorePages = true;
        allMyMenus.clear();
        loadMyMenus(false);
    }

    /**
     * Apply filters locally to my menus (filters in memory after fetching)
     * @deprecated This method is deprecated - use loadMyMenusWithFilters instead
     */
    @Deprecated
    private void applyLocalFiltersToMyMenus() {
        if (myFilterGoal == null) {
            // No filter, show all
            myMenusLiveData.postValue(new ArrayList<>(allMyMenus));
        } else {
            // Filter by goal
            List<MenuResponse> filtered = new ArrayList<>();
            for (MenuResponse menu : allMyMenus) {
                if (menu.getFitnessGoal() == myFilterGoal) {
                    filtered.add(menu);
                }
            }
            myMenusLiveData.postValue(filtered);
        }

        // Update empty state
        List<MenuResponse> currentList = myMenusLiveData.getValue();
        emptyStateLiveData.postValue(currentList == null || currentList.isEmpty());
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
