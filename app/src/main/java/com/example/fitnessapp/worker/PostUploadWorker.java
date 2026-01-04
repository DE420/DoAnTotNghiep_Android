package com.example.fitnessapp.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import com.example.fitnessapp.model.response.community.PostResponse;
import com.example.fitnessapp.repository.PostRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Worker for creating/updating posts in the background
 * Shows notifications on progress/success/failure
 */
public class PostUploadWorker extends Worker {

    private static final String TAG = "PostUploadWorker";
    private static final String CHANNEL_ID = "post_uploads";
    private static final String CHANNEL_NAME = "Post Uploads";
    private static final int NOTIFICATION_ID_UPLOAD = 2001;

    // Input data keys
    public static final String KEY_IS_EDITING = "isEditing";
    public static final String KEY_POST_ID = "postId";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_IMAGE_URI = "imageUri";
    public static final String KEY_VIDEO_URI = "videoUri";

    // Broadcast action
    public static final String ACTION_POST_UPLOADED = "com.example.fitnessapp.POST_UPLOADED";
    public static final String EXTRA_POST_ID = "postId";

    private PostRepository repository;

    public PostUploadWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        repository = new PostRepository();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Starting post upload in background");

        // Get input data
        Data inputData = getInputData();
        boolean isEditing = inputData.getBoolean(KEY_IS_EDITING, false);
        long postId = inputData.getLong(KEY_POST_ID, 0L);
        String content = inputData.getString(KEY_CONTENT);
        String imageUriString = inputData.getString(KEY_IMAGE_URI);
        String videoUriString = inputData.getString(KEY_VIDEO_URI);

        // Validate content
        if (content == null || content.trim().isEmpty()) {
            showErrorNotification("Nội dung bài viết không được để trống");
            return Result.failure();
        }

        // Show progress notification
        if (isEditing) {
            showProgressNotification("Đang cập nhật bài viết...");
        } else {
            showProgressNotification("Đang đăng bài viết...");
        }

        try {
            // Prepare files
            File imageFile = null;
            File videoFile = null;

            if (imageUriString != null && !imageUriString.isEmpty()) {
                Uri imageUri = Uri.parse(imageUriString);
                imageFile = getFileFromUri(imageUri, "image");
            }

            if (videoUriString != null && !videoUriString.isEmpty()) {
                Uri videoUri = Uri.parse(videoUriString);
                videoFile = getFileFromUri(videoUri, "video");
            }

            // Call repository
            boolean success;
            PostResponse postResponse = null;

            if (isEditing) {
                success = repository.updatePostSync(getApplicationContext(), postId, content, imageFile, videoFile);
            } else {
                postResponse = repository.createPostSync(getApplicationContext(), content, imageFile, videoFile);
                success = postResponse != null;
            }

            // Clean up temp files
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete();
            }
            if (videoFile != null && videoFile.exists()) {
                videoFile.delete();
            }

            if (success) {
                Log.d(TAG, "Post uploaded successfully");

                if (isEditing) {
                    showSuccessNotification("Cập nhật bài viết thành công");
                } else {
                    showSuccessNotification("Đăng bài viết thành công");
                }

                // Broadcast to refresh feed
                Intent refreshIntent = new Intent(ACTION_POST_UPLOADED);
                if (!isEditing && postResponse != null) {
                    refreshIntent.putExtra(EXTRA_POST_ID, postResponse.getId());
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(refreshIntent);

                return Result.success();
            } else {
                Log.e(TAG, "Post upload failed");
                if (isEditing) {
                    showErrorNotification("Không thể cập nhật bài viết");
                } else {
                    showErrorNotification("Không thể đăng bài viết");
                }
                return Result.failure();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error uploading post", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Lỗi không xác định";
            showErrorNotification(errorMessage);
            return Result.failure();
        }
    }

    /**
     * Convert URI to File for multipart upload
     */
    private File getFileFromUri(Uri uri, String prefix) throws Exception {
        Context context = getApplicationContext();
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Không thể đọc file");
        }

        // Create temp file
        String extension = getFileExtension(uri);
        File tempFile = File.createTempFile(prefix, extension, context.getCacheDir());

        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } finally {
            inputStream.close();
        }

        return tempFile;
    }

    /**
     * Get file extension from URI
     */
    private String getFileExtension(Uri uri) {
        String mimeType = getApplicationContext().getContentResolver().getType(uri);
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) {
                return ".jpg";
            } else if (mimeType.startsWith("video/")) {
                return ".mp4";
            }
        }
        return ".tmp";
    }

    private void showProgressNotification(String message) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(message)
                .setContentText("Vui lòng chờ...")
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(getApplicationContext().getResources().getColor(R.color.yellow, null));

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_UPLOAD, builder.build());
        }
    }

    private void showSuccessNotification(String message) {
        createNotificationChannel();

        // Create intent to open community feed
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setAction("com.example.fitnessapp.OPEN_COMMUNITY");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(message)
                .setContentText("Nhấn để xem bài viết")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(getApplicationContext().getResources().getColor(R.color.green_500, null));

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_UPLOAD, builder.build());
        }
    }

    private void showErrorNotification(String errorMessage) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Đăng bài thất bại")
                .setContentText(errorMessage)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(getApplicationContext().getResources().getColor(R.color.red_400, null));

        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID_UPLOAD, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo đăng bài viết");

            NotificationManager notificationManager =
                    getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
