package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.model.request.RegisterTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.NotificationResponse;
import com.example.fitnessapp.network.NotificationApi;
import com.example.fitnessapp.network.RetrofitClient;

import java.io.IOException;
import java.util.List;

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

    /**
     * Get unread notification count (synchronous - call from background thread)
     */
    public int getUnreadCount(Context context) throws Exception {
        try {
            NotificationApi api = RetrofitClient.getNotificationApi(context);
            Response<ApiResponse<Integer>> response = api.getUnreadCount().execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                Integer count = response.body().getData();
                return count != null ? count : 0;
            } else {
                throw new Exception("Failed to get unread count: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting unread count", e);
            throw e;
        }
    }

    /**
     * Get paginated notifications (synchronous - call from background thread)
     * Returns ApiResponse which contains List<NotificationResponse> in data field
     * and Pagination metadata in meta field
     */
    public ApiResponse<List<NotificationResponse>> getNotifications(Context context, int page, int size) throws Exception {
        try {
            NotificationApi api = RetrofitClient.getNotificationApi(context);
            Response<ApiResponse<List<NotificationResponse>>> response =
                    api.getNotifications(page, size).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return response.body();
            } else {
                throw new Exception("Failed to get notifications: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting notifications", e);
            throw e;
        }
    }

    /**
     * Mark a notification as read (synchronous - call from background thread)
     */
    public boolean markAsRead(Context context, Long notificationId) throws Exception {
        try {
            NotificationApi api = RetrofitClient.getNotificationApi(context);
            Response<ApiResponse<Boolean>> response = api.markAsRead(notificationId).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return true;
            } else {
                throw new Exception("Failed to mark notification as read: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "Error marking notification as read", e);
            throw e;
        }
    }
}
