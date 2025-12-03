package com.example.fitnessapp.network;

import com.example.fitnessapp.model.request.ForgotPasswordRequest;
import com.example.fitnessapp.model.request.GoogleLoginRequest;
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.request.RegisterRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.model.response.RegisterResponse;
import com.example.fitnessapp.model.response.user.ProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<ApiResponse<LoginResponse>> login(@Body LoginRequest loginRequest);

    @POST("auth/google")
    Call<ApiResponse<LoginResponse>> loginWithGoogle(@Body GoogleLoginRequest googleLoginRequest);

    @POST("auth/register")
    Call<ApiResponse<Object>> registerUser(@Body RegisterRequest registerRequest);

    @POST("auth/forgot-password")
    Call<ApiResponse<String>> forgotPassword(@Body ForgotPasswordRequest forgotPasswordRequest);

    @POST("auth/logout")
    Call<ApiResponse<String>> logout(@Body LogoutRequest logoutRequest);

    @POST("auth/refresh-token")
    Call<ApiResponse<LoginResponse>> refreshToken(@Body RefreshTokenRequest refreshTokenRequest);

    @GET("user/profile")
    Call<ApiResponse<ProfileResponse>> getUserProfile(@Header("Authorization") String authorization);
}