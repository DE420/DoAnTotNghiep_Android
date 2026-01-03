package com.example.fitnessapp.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.fitnessapp.MainActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.repository.NotificationRepository;
import com.example.fitnessapp.session.SessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "fitness_notifications";
    private static final String CHANNEL_NAME = "Fitness Notifications";

    private SessionManager sessionManager;
    private NotificationRepository notificationRepository;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        sessionManager = SessionManager.getInstance(getApplicationContext());
        notificationRepository = new NotificationRepository();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        // Save token locally
        sessionManager.saveFcmToken(token);

        // Send to backend if user is logged in
        if (sessionManager.isLoggedIn()) {
            executorService.execute(() -> {
                try {
                    notificationRepository.registerDeviceToken(getApplicationContext(), token, "ANDROID");
                    Log.d(TAG, "Token registered with backend");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to register token: " + e.getMessage(), e);
                }
            });
        }
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "Message received from: " + message.getFrom());

        // Handle notification payload
        if (message.getNotification() != null) {
            Log.d(TAG, "Notification: " + message.getNotification().getTitle());
        }

        // Handle data payload (contains custom data from backend)
        if (!message.getData().isEmpty()) {
            Log.d(TAG, "Message data: " + message.getData());
            handleDataPayload(message.getData());
        }
    }

    private void handleDataPayload(Map<String, String> data) {
        String title = data.getOrDefault("title", "Fitness App");
        String body = data.getOrDefault("body", "");
        String link = data.getOrDefault("link", "");
        String type = data.getOrDefault("type", "SYSTEM");
        String id = data.get("id");

        // Create and show notification
        showNotification(title, body, link, type, id);
    }

    private void showNotification(String title, String body, String link, String type, String id) {
        createNotificationChannel();

        // Create intent for notification tap
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("notification_link", link);
        intent.putExtra("notification_type", type);
        intent.putExtra("notification_id", id);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.yellow, null));

        // Show notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());

        // Broadcast to update badge count in MainActivity
        Intent badgeIntent = new Intent("com.example.fitnessapp.NOTIFICATION_RECEIVED");
        sendBroadcast(badgeIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for workout reminders and social interactions");
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
