package com.example.fitnessapp.network;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.PostResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
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

    @GET("posts/{postId}")
    Call<ApiResponse<PostResponse>> getPostDetail(
            @Path("postId") long postId
    );

    @Multipart
    @POST("posts")
    Call<ApiResponse<PostResponse>> createPost(
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part video
    );

    @Multipart
    @PUT("posts/{postId}")
    Call<ApiResponse<PostResponse>> updatePost(
            @Path("postId") long postId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part video
    );

    @DELETE("posts/{postId}")
    Call<ApiResponse<String>> deletePost(
            @Path("postId") long postId
    );

    @POST("posts/{postId}/post-like")
    Call<ApiResponse<PostResponse>> likePost(
            @Path("postId") long postId
    );

    @DELETE("posts/{postId}/post-like")
    Call<ApiResponse<PostResponse>> unlikePost(
            @Path("postId") long postId
    );


}
