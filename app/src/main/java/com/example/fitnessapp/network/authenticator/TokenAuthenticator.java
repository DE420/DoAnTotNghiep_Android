package com.example.fitnessapp.network.authenticator;

import android.content.Context;
import android.se.omapi.Session;

import androidx.annotation.Nullable;

import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.network.AuthApi;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;


public class TokenAuthenticator implements Authenticator {

    private final SessionManager pref;
    private final AuthApi authApi;

    public TokenAuthenticator(Context context) {
        pref = SessionManager.getInstance(context);
        authApi = RetrofitClient.getAuthApi();
    }

    @Nullable
    @Override
    public Request authenticate(Route route, Response response) throws IOException {

        if (responseCount(response) >= 2) return null;

        String refreshToken = pref.getRefreshToken();
        if (refreshToken == null) return null;

        retrofit2.Response<ApiResponse<LoginResponse>> refreshResp =
                authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();

        if (refreshResp.isSuccessful() && refreshResp.body() != null) {
            LoginResponse loginResponse = refreshResp.body().getData();
            pref.saveTokens(loginResponse.getAccessToken(), loginResponse.getRefreshToken());

            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + loginResponse.getAccessToken())
                    .build();
        }

        return null;
    }

    private int responseCount(Response r) {
        int count = 1;
        while ((r = r.priorResponse()) != null) count++;
        return count;
    }
}
