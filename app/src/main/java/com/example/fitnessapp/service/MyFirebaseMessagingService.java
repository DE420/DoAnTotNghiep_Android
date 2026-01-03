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
        Log.d(TAG, "Message ID: " + message.getMessageId());
        Log.d(TAG, "Message sent time: " + message.getSentTime());

        // Handle notification payload
        if (message.getNotification() != null) {
            Log.d(TAG, "Notification title: " + message.getNotification().getTitle());
            Log.d(TAG, "Notification body: " + message.getNotification().getBody());
            Log.d(TAG, "Notification click action: " + message.getNotification().getClickAction());
        }

        // Handle data payload (contains custom data from backend)
        if (!message.getData().isEmpty()) {
            Log.d(TAG, "Message data: " + message.getData());

            // When app is in foreground, we handle the notification ourselves
            // When app is in background/killed, system handles it and this might not be called
            handleDataPayload(message.getData());
        } else {
            Log.w(TAG, "No data payload found in notification");
        }
    }

    private void handleDataPayload(Map<String, String> data) {
        String title = data.getOrDefault("title", "Fitness App");
        String body = data.getOrDefault("body", "");
        String link = data.getOrDefault("link", "");
        String type = data.getOrDefault("type", "SYSTEM");
        String id = data.get("id");

        Log.d(TAG, "Notification data - Title: " + title);
        Log.d(TAG, "Notification data - Body: " + body);
        Log.d(TAG, "Notification data - Link: " + link);
        Log.d(TAG, "Notification data - Type: " + type);
        Log.d(TAG, "Notification data - ID: " + id);

        // Create and show notification
        showNotification(title, body, link, type, id);
    }

    private void showNotification(String title, String body, String link, String type, String id) {
        createNotificationChannel();

        Log.d(TAG, "Creating notification with PendingIntent");
        Log.d(TAG, "Intent extras - Link: " + link + ", Type: " + type + ", ID: " + id);

        // Create intent for notification tap
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction("com.example.fitnessapp.NOTIFICATION_CLICK");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("notification_link", link);
        intent.putExtra("notification_type", type);
        intent.putExtra("notification_id", id);

        // Use notification ID as request code to make each notification unique
        int requestCode = id != null ? id.hashCode() : (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d(TAG, "PendingIntent created with requestCode: " + requestCode);

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
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());

        Log.d(TAG, "Notification displayed with ID: " + notificationId);

        // Broadcast to update badge count in MainActivity
        Intent badgeIntent = new Intent("com.example.fitnessapp.NOTIFICATION_RECEIVED");
        sendBroadcast(badgeIntent);
        Log.d(TAG, "Badge update broadcast sent");
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
