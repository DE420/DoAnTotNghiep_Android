package com.example.fitnessapp.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.network.UserApi;
import com.example.fitnessapp.session.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class for updating user profile with avatar
 * Usage example:
 *
 * UserProfileHelper helper = new UserProfileHelper(context);
 * helper.updateProfileWithAvatar(
 *     imageUri,
 *     "John Doe",
 *     70.5,
 *     175.0,
 *     ActivityLevel.MODERATE,
 *     FitnessGoal.LOSE_WEIGHT,
 *     "1990-01-01",
 *     new UserProfileHelper.ProfileUpdateCallback() {
 *         @Override
 *         public void onSuccess() {
 *             // Handle success
 *         }
 *         @Override
 *         public void onFailure(String error) {
 *             // Handle failure
 *         }
 *     }
 * );
 */
public class UserProfileHelper {

    private static final String TAG = "UserProfileHelper";
    private final Context context;
    private final UserApi userApi;
    private final SessionManager sessionManager;

    public UserProfileHelper(Context context) {
        this.context = context.getApplicationContext();
        this.userApi = RetrofitClient.getUserApi(context);
        this.sessionManager = SessionManager.getInstance(context);
    }

    /**
     * Update user profile with avatar image
     */
    public void updateProfileWithAvatar(
            Uri imageUri,
            String name,
            Double weight,
            Double height,
            ActivityLevel activityLevel,
            FitnessGoal fitnessGoal,
            String dateOfBirth,
            ProfileUpdateCallback callback
    ) {
        try {
            // Prepare multipart body parts
            MultipartBody.Part avatarPart = prepareAvatarPart(imageUri);
            Map<String, RequestBody> fields = prepareProfileFields(
                    name, weight, height, activityLevel, fitnessGoal, dateOfBirth
            );

            // Make API call
            userApi.updateUserProfile(avatarPart, fields).enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call,
                                      Response<ApiResponse<Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isStatus()) {
                        Log.d(TAG, "Profile updated successfully");

                        // Update local session data
                        updateLocalSession(name, weight, height, dateOfBirth);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        String error = "Failed to update profile: " + response.message();
                        Log.e(TAG, error);
                        if (callback != null) {
                            callback.onFailure(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    String error = "Error updating profile: " + t.getMessage();
                    Log.e(TAG, error, t);
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception preparing profile update", e);
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    /**
     * Update user profile without changing avatar
     */
    public void updateProfile(
            String name,
            Double weight,
            Double height,
            ActivityLevel activityLevel,
            FitnessGoal fitnessGoal,
            String dateOfBirth,
            ProfileUpdateCallback callback
    ) {
        try {
            Map<String, RequestBody> fields = prepareProfileFields(
                    name, weight, height, activityLevel, fitnessGoal, dateOfBirth
            );

            userApi.updateUserProfile(fields).enqueue(new Callback<ApiResponse<Boolean>>() {
                @Override
                public void onResponse(Call<ApiResponse<Boolean>> call,
                                      Response<ApiResponse<Boolean>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isStatus()) {
                        Log.d(TAG, "Profile updated successfully");

                        updateLocalSession(name, weight, height, dateOfBirth);

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } else {
                        String error = "Failed to update profile: " + response.message();
                        Log.e(TAG, error);
                        if (callback != null) {
                            callback.onFailure(error);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                    String error = "Error updating profile: " + t.getMessage();
                    Log.e(TAG, error, t);
                    if (callback != null) {
                        callback.onFailure(error);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Exception preparing profile update", e);
            if (callback != null) {
                callback.onFailure(e.getMessage());
            }
        }
    }

    /**
     * Prepare avatar multipart body part from URI
     */
    private MultipartBody.Part prepareAvatarPart(Uri imageUri) throws Exception {
        File file = uriToFile(imageUri);
        String mimeType = getMimeType(imageUri);

        RequestBody requestFile = RequestBody.create(
                MediaType.parse(mimeType != null ? mimeType : "image/*"),
                file
        );

        return MultipartBody.Part.createFormData("avatarFile", file.getName(), requestFile);
    }

    /**
     * Prepare profile fields as RequestBody map
     */
    private Map<String, RequestBody> prepareProfileFields(
            String name,
            Double weight,
            Double height,
            ActivityLevel activityLevel,
            FitnessGoal fitnessGoal,
            String dateOfBirth
    ) {
        Map<String, RequestBody> fields = new HashMap<>();

        if (name != null) {
            fields.put("name", createPartFromString(name));
        }
        if (weight != null) {
            fields.put("weight", createPartFromString(String.valueOf(weight)));
        }
        if (height != null) {
            fields.put("height", createPartFromString(String.valueOf(height)));
        }
        if (activityLevel != null) {
            fields.put("activityLevel", createPartFromString(activityLevel.name()));
        }
        if (fitnessGoal != null) {
            fields.put("fitnessGoal", createPartFromString(fitnessGoal.name()));
        }
        if (dateOfBirth != null) {
            fields.put("dateOfBirth", createPartFromString(dateOfBirth));
        }

        return fields;
    }

    /**
     * Create RequestBody from string
     */
    private RequestBody createPartFromString(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    /**
     * Convert URI to File
     */
    private File uriToFile(Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Cannot open input stream from URI");
        }

        File tempFile = File.createTempFile("avatar", ".jpg", context.getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();

        return tempFile;
    }

    /**
     * Get MIME type from URI
     */
    private String getMimeType(Uri uri) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return null;
    }

    /**
     * Update local session with new profile data
     */
    private void updateLocalSession(String name, Double weight, Double height, String dateOfBirth) {
        // Save updated profile data to session
        sessionManager.saveUserProfile(
                sessionManager.getUserId(),
                name != null ? name : sessionManager.getUserName(),
                sessionManager.getUsername(),
                sessionManager.getEmail(),
                sessionManager.getAvatar(),
                sessionManager.getSex(),
                weight != null ? weight : sessionManager.getWeight(),
                height != null ? height : sessionManager.getHeight(),
                dateOfBirth != null ? dateOfBirth : sessionManager.getDateOfBirth()
        );
    }

    /**
     * Callback interface for profile update
     */
    public interface ProfileUpdateCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
