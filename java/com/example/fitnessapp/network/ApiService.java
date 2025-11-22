package com.example.fitnessapp.network;

import com.example.fitnessapp.model.request.GoogleLoginRequest;
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    @POST("auth/google")
    Call<ApiResponse<LoginResponse>> loginWithGoogle(@Body GoogleLoginRequest googleLoginRequest);

}