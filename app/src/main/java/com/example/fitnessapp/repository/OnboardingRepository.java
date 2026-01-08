package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

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
     * Calls GET /auth/me via authenticated AuthApi
     */
    public void checkOnboardingStatus(Context context, OnboardingStatusCallback callback) {
        Log.d("OnboardingRepository", "Calling GET /auth/me...");
        RetrofitClient.getAuthApiAuthenticated(context).getBasicInfo().enqueue(new Callback<ApiResponse<BasicInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BasicInfoResponse>> call, Response<ApiResponse<BasicInfoResponse>> response) {
                Log.d("OnboardingRepository", "API Response Code: " + response.code());
                Log.d("OnboardingRepository", "Is Successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("OnboardingRepository", "Response Body: " + response.body());
                    Log.d("OnboardingRepository", "Response Status: " + response.body().isStatus());

                    if (response.body().isStatus()) {
                        BasicInfoResponse data = response.body().getData();
                        Log.d("OnboardingRepository", "BasicInfoResponse Data: " + data);
                        callback.onSuccess(data);
                    } else {
                        Log.e("OnboardingRepository", "Response status is false");
                        callback.onError("Không thể kiểm tra trạng thái onboarding");
                    }
                } else {
                    Log.e("OnboardingRepository", "Response unsuccessful or body null");
                    if (response.errorBody() != null) {
                        try {
                            Log.e("OnboardingRepository", "Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("OnboardingRepository", "Cannot read error body", e);
                        }
                    }
                    callback.onError("Không thể kiểm tra trạng thái onboarding");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BasicInfoResponse>> call, Throwable t) {
                Log.e("OnboardingRepository", "API Call Failed", t);
                Log.e("OnboardingRepository", "Error Message: " + t.getMessage());
                callback.onError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Submit onboarding information
     * Calls PUT /user/onboarding via UserApi
     */
    public void submitOnboarding(Context context, OnboardingRequest request, OnboardingSubmitCallback callback) {
        RetrofitClient.getUserApi(context).updateOnboarding(request).enqueue(new Callback<ApiResponse<UserResponse>>() {
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
