package com.example.fitnessapp.repository;

import android.content.Context;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.network.PostApi;
import com.example.fitnessapp.network.RetrofitClient;

import java.util.List;
import java.util.Map;

import retrofit2.Callback;

public class PostRepository {

    private final PostApi api;

    public PostRepository(Context ctx) {
        api = RetrofitClient.getPostApi(ctx);
    }

    public void getAllPosts(Map<String, String> params,
                         Callback<ApiResponse<List<PostResponse>>> cb) {
        api.getAllPosts(params).enqueue(cb);
    }

    public void getUserPosts(Map<String, String> params,
                             Callback<ApiResponse<List<PostResponse>>> cb) {
        api.getUserPosts(params).enqueue(cb);
    }

}
