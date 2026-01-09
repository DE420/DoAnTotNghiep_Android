package com.example.fitnessapp.repository;

import android.content.Context;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.SuggestionResponse;
import com.example.fitnessapp.network.HomeApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Repository handling API calls related to Home screen.
 *
 * API Endpoints:
 * - GET /api/recommendations/{userId}: Get recommendations for user
 *
 * Response includes:
 * - Health metrics: BMI, TDEE, Target Calories
 * - List of suggested workout plans
 * - List of suggested nutrition menus
 */
public class HomeRepository {
    private final HomeApiService homeApiService;
    private final Gson gson;

    public HomeRepository(Context context) {
        this.homeApiService = RetrofitClient.getHomeApiService(context);
        this.gson = new Gson();
    }

    /**
     * Get recommendations for user
     * @param userId User ID
     * @return SuggestionResponse containing BMI, TDEE, workout and menu recommendations
     * @throws IOException if network error occurs
     * @throws Exception if API returns error
     */
    public SuggestionResponse getRecommendations(Long userId) throws Exception {
        Call<ApiResponse<SuggestionResponse>> call =
            homeApiService.getRecommendations(userId);

        Response<ApiResponse<SuggestionResponse>> response = call.execute();

        if (response.isSuccessful() && response.body() != null) {
            ApiResponse<SuggestionResponse> apiResponse = response.body();

            if (apiResponse.isStatus() && apiResponse.getData() != null) {
                return apiResponse.getData();
            } else {
                throw new Exception("Unable to load recommendation data");
            }
        } else {
            // Parse error body if available
            if (response.errorBody() != null) {
                try {
                    response.errorBody().string(); // Consume error body
                    throw new Exception("Server error: " + response.code());
                } catch (IOException e) {
                    throw new Exception("Connection error: " + response.code());
                }
            }
            throw new Exception("Unknown error: " + response.code());
        }
    }
}
