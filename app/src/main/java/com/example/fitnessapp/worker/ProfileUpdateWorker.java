package com.example.fitnessapp.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.fitnessapp.MainActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.repository.ProfileRepository;
import com.example.fitnessapp.session.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Worker for updating user profile in the background
 * Shows notifications on success/failure
 */
public class ProfileUpdateWorker extends Worker {

    private static final String TAG = "ProfileUpdateWorker";
    private static final String CHANNEL_ID = "profile_updates";
    private static final String CHANNEL_NAME = "Profile Updates";
    private static final int NOTIFICATION_ID = 1001;
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    // Input data keys
    public static final String KEY_NAME = "name";
    public static final String KEY_WEIGHT = "weight";
    public static final String KEY_HEIGHT = "height";
    public static final String KEY_DATE_OF_BIRTH = "dateOfBirth";
    public static final String KEY_ACTIVITY_LEVEL = "activityLevel";
    public static final String KEY_FITNESS_GOAL = "fitnessGoal";
    public static final String KEY_AVATAR_URI = "avatarUri";
    public static final String KEY_AVATAR_URL = "avatarUrl";
    public static final String KEY_HAS_NEW_AVATAR = "hasNewAvatar";

    private ProfileRepository repository;

    public ProfileUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        repository = new ProfileRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting profile update in background");

        // Show progress notification
        showProgressNotification();

        try {
            // Prepare field map
            Map<String, RequestBody> fields = new HashMap<>();

            // Add all fields from input data
            Data inputData = getInputData();

            String name = inputData.getString(KEY_NAME);
            if (name != null) {
                fields.put(ProfileRepository.KEY_NAME, createRequestBody(name));
            }

            String weight = inputData.getString(KEY_WEIGHT);
            if (weight != null && !weight.isEmpty()) {
                fields.put(ProfileRepository.KEY_WEIGHT, createRequestBody(weight));
            }

            String height = inputData.getString(KEY_HEIGHT);
            if (height != null && !height.isEmpty()) {
                fields.put(ProfileRepository.KEY_HEIGHT, createRequestBody(height));
            }

            String dateOfBirth = inputData.getString(KEY_DATE_OF_BIRTH);
            if (dateOfBirth != null) {
                fields.put(ProfileRepository.KEY_DATE_OF_BIRTH, createRequestBody(dateOfBirth));
            }

            String activityLevel = inputData.getString(KEY_ACTIVITY_LEVEL);
            if (activityLevel != null) {
                fields.put(ProfileRepository.KEY_ACTIVITY_LEVEL, createRequestBody(activityLevel));
            }

            String fitnessGoal = inputData.getString(KEY_FITNESS_GOAL);
            if (fitnessGoal != null) {
                fields.put(ProfileRepository.KEY_FITNESS_GOAL, createRequestBody(fitnessGoal));
            }

            // Handle avatar
            boolean hasNewAvatar = inputData.getBoolean(KEY_HAS_NEW_AVATAR, false);
            boolean success;

            if (hasNewAvatar) {
                // Upload with new avatar file
                String avatarUriString = inputData.getString(KEY_AVATAR_URI);
                if (avatarUriString != null) {
                    Uri avatarUri = Uri.parse(avatarUriString);
                    MultipartBody.Part avatarPart = prepareAvatarPart(avatarUri);
                    success = repository.updateProfile(getApplicationContext(), avatarPart, fields);
                } else {
                    Log.e(TAG, "Avatar URI is null");
                    showErrorNotification("Lỗi khi tải ảnh lên");
                    return Result.failure();
                }
            } else {
                // Keep existing avatar or no avatar
                String avatarUrl = inputData.getString(KEY_AVATAR_URL);
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    fields.put(ProfileRepository.KEY_AVATAR, createRequestBody(avatarUrl));
                    success = repository.updateProfileWithExistingAvatar(getApplicationContext(), fields);
                } else {
                    success = repository.updateProfileNoAvatar(getApplicationContext(), fields);
                }
            }

            if (success) {
                Log.d(TAG, "Profile updated successfully");

                // Fetch updated profile and save to SharedPreferences
                try {
                    ProfileResponse updatedProfile = repository.getUserProfile(getApplicationContext());
                    if (updatedProfile != null) {
                        SessionManager sessionManager = SessionManager.getInstance(getApplicationContext());
                        sessionManager.saveUserProfile(
                                updatedProfile.getId(),
                                updatedProfile.getName(),
                                updatedProfile.getUsername(),
                                updatedProfile.getEmail(),
                                updatedProfile.getAvatar(),
                                updatedProfile.getSex(),
                                updatedProfile.getWeight(),
                                updatedProfile.getHeight(),
                                updatedProfile.getDateOfBirth()
                        );
                        Log.d(TAG, "Profile data updated in SharedPreferences");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to update profile in SharedPreferences", e);
                    // Continue with success notification even if SharedPreferences update fails
                }

                showSuccessNotification();

                // Broadcast to update avatar in MainActivity
                Intent profileUpdateIntent = new Intent("com.example.fitnessapp.PROFILE_UPDATED");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(profileUpdateIntent);

                return Result.success();
            } else {
                Log.e(TAG, "Profile update failed");
                showErrorNotification("Không thể cập nhật hồ sơ");
                return Result.failure();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error updating profile", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            showErrorNotification(errorMessage);
            return Result.failure();
        }
    }

    private MultipartBody.Part prepareAvatarPart(Uri uri) throws Exception {
        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new FileNotFoundException("Could not open image");
        }

        // Read and compress image
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Check size
        if (imageBytes.length > MAX_IMAGE_SIZE) {
            throw new Exception("Ảnh quá lớn. Kích thước tối đa 5MB");
        }

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageBytes
        );

        return MultipartBody.Part.createFormData("avatarFile", "avatar.jpg", requestFile);
    }

    private RequestBody createRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    private void showProgressNotification() {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Đang cập nhật hồ sơ")
                .setContentText("Vui lòng chờ...")
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(getApplicationContext().getResources().getColor(R.color.yellow, null));

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showSuccessNotification() {
        createNotificationChannel();

        // Create intent to open profile screen
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction("com.example.fitnessapp.OPEN_PROFILE");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cập nhật thành công")
                .setContentText("Hồ sơ của bạn đã được cập nhật")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(getApplicationContext().getResources().getColor(R.color.green_500, null));

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void showErrorNotification(String errorMessage) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cập nhật thất bại")
                .setContentText(errorMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(getApplicationContext().getResources().getColor(R.color.red_400, null));

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo cập nhật hồ sơ");

            NotificationManager notificationManager =
                    getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
