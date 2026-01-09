package com.example.fitnessapp.repository;

import android.content.Context;

import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.network.AuthApi;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi api;
    private final SessionManager pref;

    public AuthRepository(Context context) {
        this.api = RetrofitClient.getAuthApi();
        this.pref = SessionManager.getInstance(context);
    }

    public void login(LoginRequest request,
                      Callback<ApiResponse<LoginResponse>> cb) {
        api.login(request).enqueue(cb);
    }

    public void saveToken(LoginResponse response) {
        pref.saveTokens(response.getAccessToken(), response.getRefreshToken());
    }
}

