package com.example.fitnessapp.repository;

import android.content.Context;
import android.util.Log;

import com.example.fitnessapp.model.request.ChangePasswordRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.network.UserApi;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

/**
 * Repository for Profile data operations
 *
 * All methods are synchronous and should be called from background thread.
 * Use ProfileViewModel to handle threading via ExecutorService.
 *
 * Available operations:
 * - getUserProfile(): Fetch user profile from server
 * - updateProfile(): Update profile with flexible parameter map
 * - changePassword(): Change user password
 */
public class ProfileRepository {

    private static final String TAG = "ProfileRepository";

    // Field keys for update profile map
    public static final String KEY_NAME = "name";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_ACTIVITY_LEVEL = "activityLevel";
    public static final String KEY_FITNESS_GOAL = "fitnessGoal";
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";
    public static final String KEY_AVATAR = "avatar";

    /**
     * Get user profile from server
     * @param context Application context
     * @return ProfileResponse object
     * @throws Exception if request fails
     */
    public ProfileResponse getUserProfile(Context context) throws Exception {
        try {
            UserApi api = RetrofitClient.getUserApi(context);

            Response<ApiResponse<ProfileResponse>> response = api.getUserProfile().execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return response.body().getData();
            } else if (response.code() == 401) {
                throw new Exception("Unauthorized - Token expired");
            } else {
                throw new Exception("Failed to get profile");
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error getting profile", e);
            throw new Exception("Network error: " + e.getMessage());
        }
    }

    /**
     * Update user profile with new avatar file
     *
     * @param context Application context
     * @param avatarFile MultipartBody.Part for avatar image (optional)
     * @param fields Map of profile fields (name, weight, height, activityLevel, fitnessGoal, dateOfBirth)
     * @return true if update successful
     * @throws Exception if update fails
     *
     * Example usage:
     * <pre>
     * Map&lt;String, RequestBody&gt; fields = new HashMap&lt;&gt;();
     * fields.put(KEY_NAME, createRequestBody("John Doe"));
     * fields.put(KEY_WEIGHT, createRequestBody("70.5"));
     * updateProfile(context, avatarFilePart, fields);
     * </pre>
     */
    public boolean updateProfile(Context context,
                                 MultipartBody.Part avatarFile,
                                 Map<String, RequestBody> fields) throws Exception {
        try {
            UserApi api = RetrofitClient.getUserApi(context);

            Response<ApiResponse<Boolean>> response = api.updateUserProfile(
                avatarFile,
                fields
            ).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return true;
            } else if (response.code() == 401) {
                throw new Exception("Unauthorized - Token expired");
            } else {
                // Extract error message from error body
                String errorMessage = "Failed to update profile";
                if (response.errorBody() != null) {
                    try {
                        ApiResponse<String> errorResponse = new Gson().fromJson(
                                response.errorBody().charStream(),
                                ApiResponse.class
                        );
                        if (errorResponse != null && errorResponse.getData() != null) {
                            errorMessage = errorResponse.getData().toString();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
                Log.e(TAG, "Update profile failed - Code: " + response.code() + ", Message: " + errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error updating profile", e);
            throw new Exception("Network error: " + e.getMessage());
        }
    }

    /**
     * Update user profile with existing avatar URL (no new file upload)
     *
     * @param context Application context
     * @param fields Map of profile fields including avatar URL
     * @return true if update successful
     * @throws Exception if update fails
     */
    public boolean updateProfileWithExistingAvatar(Context context,
                                                   Map<String, RequestBody> fields) throws Exception {
        try {
            UserApi api = RetrofitClient.getUserApi(context);

            Response<ApiResponse<Boolean>> response = api.updateUserProfile(fields).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return true;
            } else if (response.code() == 401) {
                throw new Exception("Unauthorized - Token expired");
            } else {
                // Extract error message from error body
                String errorMessage = "Failed to update profile";
                if (response.errorBody() != null) {
                    try {
                        ApiResponse<String> errorResponse = new Gson().fromJson(
                                response.errorBody().charStream(),
                                ApiResponse.class
                        );
                        if (errorResponse != null && errorResponse.getData() != null) {
                            errorMessage = errorResponse.getData().toString();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
                Log.e(TAG, "Update profile failed - Code: " + response.code() + ", Message: " + errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error updating profile", e);
            throw new Exception("Network error: " + e.getMessage());
        }
    }

    /**
     * Update user profile without avatar (for cases where avatar is not changed)
     *
     * @param context Application context
     * @param fields Map of profile fields (without avatar)
     * @return true if update successful
     * @throws Exception if update fails
     */
    public boolean updateProfileNoAvatar(Context context,
                                        Map<String, RequestBody> fields) throws Exception {
        try {
            UserApi api = RetrofitClient.getUserApi(context);

            Response<ApiResponse<Boolean>> response = api.updateUserProfile(fields).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return true;
            } else if (response.code() == 401) {
                throw new Exception("Unauthorized - Token expired");
            } else {
                // Extract error message from error body
                String errorMessage = "Failed to update profile";
                if (response.errorBody() != null) {
                    try {
                        ApiResponse<String> errorResponse = new Gson().fromJson(
                                response.errorBody().charStream(),
                                ApiResponse.class
                        );
                        if (errorResponse != null && errorResponse.getData() != null) {
                            errorMessage = errorResponse.getData().toString();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
                Log.e(TAG, "Update profile failed - Code: " + response.code() + ", Message: " + errorMessage);
                throw new Exception(errorMessage);
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error updating profile", e);
            throw new Exception("Network error: " + e.getMessage());
        }
    }

    /**
     * Change user password
     *
     * @param context Application context
     * @param currentPassword Current password
     * @param newPassword New password
     * @param confirmPassword Confirm new password
     * @return true if password change successful
     * @throws Exception if change fails
     */
    public boolean changePassword(Context context,
                                  String currentPassword,
                                  String newPassword,
                                  String confirmPassword) throws Exception {
        try {
            UserApi api = RetrofitClient.getUserApi(context);

            ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,
                newPassword,
                confirmPassword
            );

            Response<ApiResponse<String>> response = api.changePassword(request).execute();

            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                return true;
            } else if (response.code() == 401) {
                throw new Exception("Unauthorized - Token expired");
            } else if (response.body() != null && !response.body().isStatus()) {
                // Server returned error message
                String message = response.body().getData();
                throw new Exception(message != null ? message : "Failed to change password");
            } else {
                throw new Exception("Failed to change password");
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error changing password", e);
            throw new Exception("Network error: " + e.getMessage());
        }
    }

    /**
     * Helper method to create RequestBody from string
     * @param value String value
     * @return RequestBody for multipart form data
     */
    public static RequestBody createRequestBody(String value) {
        return RequestBody.create(okhttp3.MediaType.parse("text/plain"), value);
    }
}
