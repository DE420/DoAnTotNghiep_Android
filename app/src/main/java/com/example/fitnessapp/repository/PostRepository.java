package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.network.PostApi;
import com.example.fitnessapp.network.RetrofitClient;

import java.io.File;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRepository {

    private static final String TAG = "PostRepository";
    private PostApi api;

    public PostRepository(Context ctx) {
        api = RetrofitClient.getPostApi(ctx);
        Log.d(TAG, "PostRepository initialized");
    }

    /**
     * Default constructor for Workers (they will call initApi separately)
     */
    public PostRepository() {
        Log.d(TAG, "PostRepository initialized without API");
    }

    /**
     * Initialize API (for use in Workers)
     */
    private void initApi(Context ctx) {
        if (api == null) {
            api = RetrofitClient.getPostApi(ctx);
        }
    }

    public void getAllPosts(Map<String, String> params,
                         Callback<ApiResponse<List<PostResponse>>> cb) {
        Log.d(TAG, "getAllPosts called with params: " + params.toString());
        api.getAllPosts(params).enqueue(cb);
    }

    public void getUserPosts(Map<String, String> params,
                             Callback<ApiResponse<List<PostResponse>>> cb) {
        Log.d(TAG, "getUserPosts called with params: " + params.toString());
        api.getUserPosts(params).enqueue(cb);
    }

    public void likePost(long postId, Callback<ApiResponse<PostResponse>> cb) {
        Log.d(TAG, "likePost called for postId: " + postId);
        api.likePost(postId).enqueue(cb);
    }

    public void unlikePost(long postId, Callback<ApiResponse<PostResponse>> cb) {
        Log.d(TAG, "unlikePost called for postId: " + postId);
        api.unlikePost(postId).enqueue(cb);
    }

    public void deletePost(long postId, Callback<ApiResponse<String>> cb) {
        Log.d(TAG, "deletePost called for postId: " + postId);
        api.deletePost(postId).enqueue(cb);
    }

    public void getPostDetail(long postId, Callback<ApiResponse<PostResponse>> cb) {
        Log.d(TAG, "getPostDetail called for postId: " + postId);
        api.getPostDetail(postId).enqueue(cb);
    }

    /**
     * Create a new post
     */
    public void createPost(String content, File imageFile, File videoFile, Callback<ApiResponse<PostResponse>> callback) {
        Log.d(TAG, "createPost called - content: " + content);
        Log.d(TAG, "Image file: " + (imageFile != null ? imageFile.getPath() : "null"));
        Log.d(TAG, "Video file: " + (videoFile != null ? videoFile.getPath() : "null"));

        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

        MultipartBody.Part imagePart = null;
        if (imageFile != null) {
            RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageRequestBody);
            Log.d(TAG, "Image part created: " + imageFile.getName());
        }

        MultipartBody.Part videoPart = null;
        if (videoFile != null) {
            RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
            videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestBody);
            Log.d(TAG, "Video part created: " + videoFile.getName());
        }

        api.createPost(contentBody, imagePart, videoPart).enqueue(callback);
    }

    /**
     * Update an existing post
     */
    public void updatePost(long postId, String content, File imageFile, File videoFile,
                          boolean deleteImage, boolean deleteVideo,
                          Callback<ApiResponse<PostResponse>> callback) {
        Log.d(TAG, "updatePost called for postId: " + postId + ", content: " + content);
        Log.d(TAG, "Image file: " + (imageFile != null ? imageFile.getPath() : "null"));
        Log.d(TAG, "Video file: " + (videoFile != null ? videoFile.getPath() : "null"));
        Log.d(TAG, "Delete image: " + deleteImage + ", Delete video: " + deleteVideo);

        RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

        MultipartBody.Part imagePart = null;
        if (imageFile != null) {
            RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageRequestBody);
            Log.d(TAG, "Image part created: " + imageFile.getName());
        }

        MultipartBody.Part videoPart = null;
        if (videoFile != null) {
            RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
            videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestBody);
            Log.d(TAG, "Video part created: " + videoFile.getName());
        }

        RequestBody deleteImageBody = RequestBody.create(MediaType.parse("text/plain"),
                String.valueOf(deleteImage));
        RequestBody deleteVideoBody = RequestBody.create(MediaType.parse("text/plain"),
                String.valueOf(deleteVideo));

        api.updatePost(postId, contentBody, imagePart, videoPart, deleteImageBody, deleteVideoBody).enqueue(callback);
    }

    /**
     * Create a new post synchronously (for use in Workers)
     * @return PostResponse if successful, null otherwise
     */
    public PostResponse createPostSync(Context context, String content, File imageFile, File videoFile) {
        initApi(context);
        Log.d(TAG, "createPostSync called - content: " + content);
        Log.d(TAG, "Image file: " + (imageFile != null ? imageFile.getPath() : "null"));
        Log.d(TAG, "Video file: " + (videoFile != null ? videoFile.getPath() : "null"));

        try {
            RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

            MultipartBody.Part imagePart = null;
            if (imageFile != null && imageFile.exists()) {
                RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageRequestBody);
                Log.d(TAG, "Image part created: " + imageFile.getName());
            }

            MultipartBody.Part videoPart = null;
            if (videoFile != null && videoFile.exists()) {
                RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
                videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestBody);
                Log.d(TAG, "Video part created: " + videoFile.getName());
            }

            Response<ApiResponse<PostResponse>> response = api.createPost(contentBody, imagePart, videoPart).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                Log.d(TAG, "Post created successfully");
                return response.body().getData();
            } else {
                Log.e(TAG, "Failed to create post: " + response.message());
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating post", e);
            return null;
        }
    }

    /**
     * Update an existing post synchronously (for use in Workers)
     * @return true if successful, false otherwise
     */
    public boolean updatePostSync(Context context, long postId, String content, File imageFile, File videoFile,
                                 boolean deleteImage, boolean deleteVideo) {
        initApi(context);
        Log.d(TAG, "updatePostSync called for postId: " + postId + ", content: " + content);
        Log.d(TAG, "Image file: " + (imageFile != null ? imageFile.getPath() : "null"));
        Log.d(TAG, "Video file: " + (videoFile != null ? videoFile.getPath() : "null"));
        Log.d(TAG, "Delete image: " + deleteImage + ", Delete video: " + deleteVideo);

        try {
            RequestBody contentBody = RequestBody.create(MediaType.parse("text/plain"), content);

            MultipartBody.Part imagePart = null;
            if (imageFile != null && imageFile.exists()) {
                RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
                imagePart = MultipartBody.Part.createFormData("image", imageFile.getName(), imageRequestBody);
                Log.d(TAG, "Image part created: " + imageFile.getName());
            }

            MultipartBody.Part videoPart = null;
            if (videoFile != null && videoFile.exists()) {
                RequestBody videoRequestBody = RequestBody.create(MediaType.parse("video/*"), videoFile);
                videoPart = MultipartBody.Part.createFormData("video", videoFile.getName(), videoRequestBody);
                Log.d(TAG, "Video part created: " + videoFile.getName());
            }

            RequestBody deleteImageBody = RequestBody.create(MediaType.parse("text/plain"),
                    String.valueOf(deleteImage));
            RequestBody deleteVideoBody = RequestBody.create(MediaType.parse("text/plain"),
                    String.valueOf(deleteVideo));

            Response<ApiResponse<PostResponse>> response = api.updatePost(postId, contentBody, imagePart, videoPart,
                    deleteImageBody, deleteVideoBody).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                Log.d(TAG, "Post updated successfully");
                return true;
            } else {
                Log.e(TAG, "Failed to update post: " + response.message());
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating post", e);
            return false;
        }
    }

}
