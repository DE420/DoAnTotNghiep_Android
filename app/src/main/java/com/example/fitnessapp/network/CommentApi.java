package com.example.fitnessapp.network;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.CommentResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface CommentApi {

    /**
     * Get comments for a specific post
     * Path: GET /posts/{postId}/comments
     */
    @GET("posts/{postId}/comments")
    Call<ApiResponse<List<CommentResponse>>> getComments(
            @Path("postId") long postId,
            @QueryMap Map<String, String> params
    );

    /**
     * Get comment detail by ID
     * Path: GET /comments/{id}
     */
    @GET("comments/{id}")
    Call<ApiResponse<CommentResponse>> getCommentDetail(
            @Path("id") long commentId
    );

    /**
     * Create a new comment
     * Path: POST /comments (with multipart form data)
     */
    @Multipart
    @POST("comments")
    Call<ApiResponse<CommentResponse>> createComment(
            @Part("postId") RequestBody postId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image
    );

    /**
     * Update comment
     * Path: PUT /comments/{id} (with multipart form data)
     */
    @Multipart
    @PUT("comments/{id}")
    Call<ApiResponse<CommentResponse>> updateComment(
            @Path("id") long commentId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part image
    );

    /**
     * Delete comment by ID
     * Path: DELETE /comments/{id}
     */
    @DELETE("comments/{id}")
    Call<ApiResponse<String>> deleteComment(
            @Path("id") long commentId
    );

    /**
     * Like a comment
     * Path: POST /comments/{commentId}/comment-like
     */
    @POST("comments/{commentId}/comment-like")
    Call<ApiResponse<CommentResponse>> likeComment(
            @Path("commentId") long commentId
    );

    /**
     * Unlike a comment
     * Path: DELETE /comments/{commentId}/comment-like
     */
    @DELETE("comments/{commentId}/comment-like")
    Call<ApiResponse<CommentResponse>> unlikeComment(
            @Path("commentId") long commentId
    );
}
