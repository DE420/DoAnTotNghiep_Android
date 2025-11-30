package com.example.fitnessapp.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // ip address emulator
    private static final String BASE_URL = "http://192.168.1.169:8080/api/";

    // ip address real device
//    private static final String BASE_URL = "http://192.168.1.61:8080/api/v1/";


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
}