package com.example.fitnessapp.repository;

import android.content.Context;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.CommentResponse;
import com.example.fitnessapp.network.CommentApi;
import com.example.fitnessapp.network.RetrofitClient;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;

public class CommentRepository {

    private final CommentApi commentApi;
    private final Context context;

    public CommentRepository(Context context) {
        this.context = context;
        this.commentApi = RetrofitClient.getCommentApi(context);
    }

    /**
     * Get comments for a post
     */
    public void getComments(long postId, int page, int size, Callback<ApiResponse<List<CommentResponse>>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));

        commentApi.getComments(postId, params).enqueue(callback);
    }

    /**
     * Get comment detail by ID
     */
    public void getCommentDetail(long commentId, Callback<ApiResponse<CommentResponse>> callback) {
        commentApi.getCommentDetail(commentId).enqueue(callback);
    }

    /**
     * Create a new comment with optional image
     */
    public void createComment(long postId, String content, File imageFile, Callback<ApiResponse<CommentResponse>> callback) {
        RequestBody postIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(postId));
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

        MultipartBody.Part imagePart = null;
        if (imageFile != null) {
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageBody);
        }

        commentApi.createComment(postIdBody, contentBody, imagePart).enqueue(callback);
    }

    /**
     * Update a comment with optional image
     */
    public void updateComment(long commentId, String content, File imageFile, Callback<ApiResponse<CommentResponse>> callback) {
        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

        MultipartBody.Part imagePart = null;
        if (imageFile != null) {
            RequestBody imageBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageBody);
        }

        commentApi.updateComment(commentId, contentBody, imagePart).enqueue(callback);
    }

    /**
     * Update a comment (text only - backward compatibility)
     */
    public void updateComment(long commentId, String content, Callback<ApiResponse<CommentResponse>> callback) {
        updateComment(commentId, content, null, callback);
    }

    /**
     * Delete a comment
     */
    public void deleteComment(long commentId, Callback<ApiResponse<String>> callback) {
        commentApi.deleteComment(commentId).enqueue(callback);
    }

    /**
     * Like a comment
     */
    public void likeComment(long commentId, Callback<ApiResponse<CommentResponse>> callback) {
        commentApi.likeComment(commentId).enqueue(callback);
    }

    /**
     * Unlike a comment
     */
    public void unlikeComment(long commentId, Callback<ApiResponse<CommentResponse>> callback) {
        commentApi.unlikeComment(commentId).enqueue(callback);
    }
}
