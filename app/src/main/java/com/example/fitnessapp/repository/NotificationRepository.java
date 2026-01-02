package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.model.request.RegisterTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.NotificationApi;
import com.example.fitnessapp.network.RetrofitClient;

import java.io.IOException;

import retrofit2.Response;

public class NotificationRepository {

    private static final String TAG = "NotificationRepository";

    /**
     * Register FCM device token with backend (synchronous - call from background thread)
     */
    public boolean registerDeviceToken(Context context, String token, String deviceType) throws Exception {
        try {
            NotificationApi api = RetrofitClient.getNotificationApi(context);
            RegisterTokenRequest request = new RegisterTokenRequest(token, deviceType);
            Response<ApiResponse<Boolean>> response = api.registerDeviceToken(request).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return true;
            } else {
                throw new Exception("Failed to register token: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error registering token", e);
            throw e;
        }
    }
}
