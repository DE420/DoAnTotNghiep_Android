package com.example.fitnessapp.network.interceptor;


import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager pref;

    public AuthInterceptor(Context context) {
        this.pref = SessionManager.getInstance(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request req = chain.request();
        String accessToken = pref.getAccessToken();

        if (accessToken != null) {
            req = req.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
            Log.e("AuthInterceptor", "Access token: " + accessToken);
        }
        return chain.proceed(req);
    }
}
