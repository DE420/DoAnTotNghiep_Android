package com.example.fitnessapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.nutrition.MenuResponse;
import com.example.fitnessapp.repository.NutritionRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuDetailViewModel extends AndroidViewModel {

    private static final String TAG = "MenuDetailViewModel";
    private final NutritionRepository repository;

    // LiveData
    private final MutableLiveData<MenuResponse> menuLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public MenuDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new NutritionRepository(application);
        Log.d(TAG, "MenuDetailViewModel created");
    }

    // Getters
    public LiveData<MenuResponse> getMenu() {
        return menuLiveData;
    }

    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Load menu detail by ID
     */
    public void loadMenuDetail(Long menuId) {
        if (menuId == null) {
            errorLiveData.setValue("Invalid menu ID");
            return;
        }

        Log.d(TAG, "Loading menu detail for ID: " + menuId);
        loadingLiveData.setValue(true);
        errorLiveData.setValue(null);

        repository.getMenuDetail(menuId, new Callback<ApiResponse<MenuResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MenuResponse>> call,
                                 Response<ApiResponse<MenuResponse>> response) {
                loadingLiveData.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<MenuResponse> apiResponse = response.body();

                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        MenuResponse menu = apiResponse.getData();
                        Log.d(TAG, "SUCCESS - Menu loaded: " + menu.getName());
                        menuLiveData.postValue(menu);
                    } else {
                        String errorMsg = "Failed to load menu details";
                        Log.e(TAG, "API ERROR - " + errorMsg);
                        errorLiveData.postValue(errorMsg);
                    }
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<MenuResponse>> call, Throwable t) {
                Log.e(TAG, "NETWORK FAILURE: " + t.getMessage(), t);
                loadingLiveData.postValue(false);

                String errorMsg = t.getMessage() != null ? t.getMessage() : "Network error";
                errorLiveData.postValue(errorMsg);
            }
        });
    }

    /**
     * Clone menu
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
     * Delete menu
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
