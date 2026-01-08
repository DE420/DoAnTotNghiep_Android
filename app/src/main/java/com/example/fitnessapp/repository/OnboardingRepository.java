package com.example.fitnessapp.repository;

import com.example.fitnessapp.model.request.OnboardingRequest;
import com.example.fitnessapp.model.response.BasicInfoResponse;
import com.example.fitnessapp.model.response.user.UserResponse;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingRepository {
    private static OnboardingRepository instance;

    private OnboardingRepository() {
    }

    public static synchronized OnboardingRepository getInstance() {
        if (instance == null) {
            instance = new OnboardingRepository();
        }
        return instance;
    }

    public interface OnboardingStatusCallback {
        void onSuccess(BasicInfoResponse response);
        void onError(String errorMessage);
    }

    public interface OnboardingSubmitCallback {
        void onSuccess(UserResponse user);
        void onError(String errorMessage);
    }

    /**
     * Check if user has completed onboarding
     * Calls GET /auth/me via AuthApi
     */
    public void checkOnboardingStatus(OnboardingStatusCallback callback) {
        RetrofitClient.getAuthApi().getBasicInfo().enqueue(new Callback<ApiResponse<BasicInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BasicInfoResponse>> call, Response<ApiResponse<BasicInfoResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Không thể kiểm tra trạng thái onboarding");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BasicInfoResponse>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Submit onboarding information
     * Calls PUT /user/onboarding via UserApi
     */
    public void submitOnboarding(OnboardingRequest request, OnboardingSubmitCallback callback) {
        RetrofitClient.getUserApi().updateOnboarding(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserResponse>> call, Response<ApiResponse<UserResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    callback.onError("Không thể hoàn tất onboarding");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserResponse>> call, Throwable t) {
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
