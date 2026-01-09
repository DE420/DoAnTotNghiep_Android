package com.example.fitnessapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.model.response.SuggestionResponse;
import com.example.fitnessapp.repository.HomeRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel managing state and business logic for Home screen.
 *
 * Features:
 * - Load recommendation data from API (BMI, TDEE, workout plans, menus)
 * - Manage loading, error, and success states
 * - Support pull-to-refresh
 *
 * LiveData:
 * - suggestionData: Recommendation data from server
 * - isLoading: Loading state
 * - errorMessage: Error message if any
 */
public class HomeViewModel extends AndroidViewModel {

    private final HomeRepository repository;
    private final ExecutorService executorService;

    // LiveData for UI state
    private final MutableLiveData<SuggestionResponse> suggestionData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.repository = new HomeRepository(application.getApplicationContext());
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Getters for LiveData
    public LiveData<SuggestionResponse> getSuggestionData() {
        return suggestionData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Load recommendation data from server
     * @param userId User ID
     */
    public void loadRecommendations(Long userId) {
        isLoading.postValue(true);
        errorMessage.postValue(null);

        executorService.execute(() -> {
            try {
                SuggestionResponse data = repository.getRecommendations(userId);
                suggestionData.postValue(data);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue(e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Refresh data
     * @param userId User ID
     */
    public void refreshData(Long userId) {
        loadRecommendations(userId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
