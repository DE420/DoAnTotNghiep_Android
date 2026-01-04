package com.example.fitnessapp.network;

import com.example.fitnessapp.model.request.RegisterTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.NotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationApi {

    @POST("notifications/register-token")
    Call<ApiResponse<Boolean>> registerDeviceToken(@Body RegisterTokenRequest request);

    @GET("notifications/unread/count")
    Call<ApiResponse<Integer>> getUnreadCount();

    @GET("notifications")
    Call<ApiResponse<List<NotificationResponse>>> getNotifications(
            @Query("page") int page,
            @Query("size") int size
    );

    @PUT("notifications/{id}/read")
    Call<ApiResponse<Boolean>> markAsRead(@Path("id") Long notificationId);
}
