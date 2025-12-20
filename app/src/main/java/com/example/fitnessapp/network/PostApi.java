package com.example.fitnessapp.network;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.PostResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface PostApi {

    @GET("posts")
    Call<ApiResponse<List<PostResponse>>> getAllPosts(
            @QueryMap Map<String, String> params
            );

    @GET("user/posts")
    Call<ApiResponse<List<PostResponse>>> getUserPosts(
            @QueryMap Map<String, String> params
    );



}
