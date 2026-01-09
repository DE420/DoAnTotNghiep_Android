package com.example.fitnessapp.network;

import com.example.fitnessapp.constants.Constants;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.SuggestionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface HomeApiService {

    @GET("recommendations/{userId}")
    Call<ApiResponse<SuggestionResponse>> getRecommendations(
            @Header(Constants.KEY_AUTHORIZATION) String authorization,
            @Path("userId") Long userId
    );
}
