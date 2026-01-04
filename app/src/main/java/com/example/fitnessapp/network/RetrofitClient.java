package com.example.fitnessapp.network;

import android.content.Context;

import com.example.fitnessapp.network.authenticator.TokenAuthenticator;
import com.example.fitnessapp.network.interceptor.AuthInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // ip address emulator
//    private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";

    // ip address real device
    private static final String BASE_URL = "http://192.168.1.168:8080/api/";
    private static Retrofit retrofitPlain;
    private static Retrofit retrofitAuth;
    private static AuthApi authApi;
    private static PostApi postApi;
    private static CommentApi commentApi;
    private static UserApi userApi;
    private static NutritionApi nutritionApi;
    private static NotificationApi notificationApi;

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static Retrofit getPlainRetrofit() {
        if (retrofitPlain == null) {
            retrofitPlain = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitPlain;
    }

    private static Retrofit getAuthRetrofit(Context ctx) {
        if (retrofitAuth == null) {

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor(ctx.getApplicationContext()))
                    .authenticator(new TokenAuthenticator(ctx.getApplicationContext()))
                    .build();

            retrofitAuth = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofitAuth;
    }

    public static AuthApi getAuthApi() {
        if (authApi == null) {
            authApi = getPlainRetrofit().create(AuthApi.class);
        }
        return authApi;
    }

    public static PostApi getPostApi(Context ctx) {
        if (postApi == null) {
            postApi = getAuthRetrofit(ctx.getApplicationContext())
                    .create(PostApi.class);
        }
        return postApi;
    }

    public static CommentApi getCommentApi(Context ctx) {
        if (commentApi == null) {
            commentApi = getAuthRetrofit(ctx.getApplicationContext())
                    .create(CommentApi.class);
        }
        return commentApi;
    }

    public static UserApi getUserApi(Context ctx) {
        if (userApi == null) {
            userApi = getAuthRetrofit(ctx.getApplicationContext())
                    .create(UserApi.class);
        }
        return userApi;
    }

    public static NutritionApi getNutritionApi(Context ctx) {
        if (nutritionApi == null ) {
            nutritionApi = getAuthRetrofit(ctx.getApplicationContext())
                    .create(NutritionApi.class);
        }
        return nutritionApi;
    }

    public static NotificationApi getNotificationApi(Context context) {
        if (notificationApi == null) {
            notificationApi = getAuthRetrofit(context).create(NotificationApi.class);
        }
        return notificationApi;
    }

}