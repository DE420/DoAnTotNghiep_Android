package com.example.fitnessapp.network;

import com.example.fitnessapp.model.request.RegisterTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotificationApi {

    @POST("notifications/register-token")
    Call<ApiResponse<Boolean>> registerDeviceToken(@Body RegisterTokenRequest request);
}
