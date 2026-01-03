package com.example.fitnessapp.viewmodel;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.NotificationResponse;
import com.example.fitnessapp.repository.NotificationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationViewModel extends AndroidViewModel {

    private static final String TAG = "NotificationViewModel";
    private static final int PAGE_SIZE = 20;

    private final NotificationRepository repository;
    private final ExecutorService executorService;

    private final MutableLiveData<List<NotificationResponse>> notifications = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasMore = new MutableLiveData<>(true);

    private int currentPage = 0;
    private boolean isLoadingMore = false;

    public NotificationViewModel(@NonNull Application application) {
        super(application);
        this.repository = new NotificationRepository();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // LiveData Getters
    public LiveData<List<NotificationResponse>> getNotifications() {
        return notifications;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getHasMore() {
        return hasMore;
    }

    /**
     * Load first page of notifications
     */
    public void loadNotifications() {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }

        currentPage = 0;
        isLoading.postValue(true);
        error.postValue(null);

        executorService.execute(() -> {
            try {
                ApiResponse<List<NotificationResponse>> response =
                        repository.getNotifications(getApplication(), currentPage, PAGE_SIZE);

                if (response != null && response.getData() != null) {
                    notifications.postValue(response.getData());

                    // Update hasMore from pagination metadata
                    if (response.getMeta() != null) {
                        hasMore.postValue(response.getMeta().isHasMore());
                    } else {
                        hasMore.postValue(false);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading notifications", e);
                error.postValue("Failed to load notifications: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Load more notifications (pagination)
     */
    public void loadMoreNotifications() {
        if (isLoadingMore || Boolean.FALSE.equals(hasMore.getValue())) {
            return;
        }

        isLoadingMore = true;
        currentPage++;

        executorService.execute(() -> {
            try {
                ApiResponse<List<NotificationResponse>> response =
                        repository.getNotifications(getApplication(), currentPage, PAGE_SIZE);

                if (response != null && response.getData() != null) {
                    List<NotificationResponse> currentList = notifications.getValue();
                    if (currentList != null) {
                        List<NotificationResponse> newList = new ArrayList<>(currentList);
                        newList.addAll(response.getData());
                        notifications.postValue(newList);
                    }

                    // Update hasMore from pagination metadata
                    if (response.getMeta() != null) {
                        hasMore.postValue(response.getMeta().isHasMore());
                    } else {
                        hasMore.postValue(false);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading more notifications", e);
                error.postValue("Failed to load more notifications: " + e.getMessage());
                currentPage--; // Revert page increment on error
            } finally {
                isLoadingMore = false;
            }
        });
    }

    /**
     * Refresh unread count
     */
    public void refreshUnreadCount() {
        executorService.execute(() -> {
            try {
                int count = repository.getUnreadCount(getApplication());
                unreadCount.postValue(count);
            } catch (Exception e) {
                Log.e(TAG, "Error refreshing unread count", e);
            }
        });
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(Long notificationId) {
        executorService.execute(() -> {
            try {
                boolean success = repository.markAsRead(getApplication(), notificationId);
                if (success) {
                    // Update local notification list
                    List<NotificationResponse> currentList = notifications.getValue();
                    if (currentList != null) {
                        for (NotificationResponse notification : currentList) {
                            if (notification.getId().equals(notificationId)) {
                                notification.setRead(true);
                                break;
                            }
                        }
                        notifications.postValue(currentList);
                    }

                    // Refresh unread count
                    refreshUnreadCount();

                    // Broadcast to update badge in MainActivity
                    Intent intent = new Intent("com.example.fitnessapp.NOTIFICATION_RECEIVED");
                    getApplication().sendBroadcast(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error marking notification as read", e);
                error.postValue("Failed to mark notification as read: " + e.getMessage());
            }
        });
    }

    /**
     * Mark all notifications as read by iterating through unread notifications
     */
    public void markAllAsRead() {
        executorService.execute(() -> {
            try {
                List<NotificationResponse> currentList = notifications.getValue();
                if (currentList == null || currentList.isEmpty()) {
                    Log.d(TAG, "No notifications to mark as read");
                    return;
                }

                // Filter unread notifications
                List<NotificationResponse> unreadNotifications = new ArrayList<>();
                for (NotificationResponse notification : currentList) {
                    if (!notification.isRead()) {
                        unreadNotifications.add(notification);
                    }
                }

                if (unreadNotifications.isEmpty()) {
                    Log.d(TAG, "All notifications already marked as read");
                    return;
                }

                // Mark all unread notifications as read
                int successCount = repository.markAllAsRead(getApplication(), unreadNotifications);

                if (successCount > 0) {
                    // Update local notification list - mark all as read
                    for (NotificationResponse notification : currentList) {
                        notification.setRead(true);
                    }
                    notifications.postValue(currentList);

                    // Reset unread count to 0
                    unreadCount.postValue(0);

                    // Broadcast to update badge in MainActivity
                    Intent intent = new Intent("com.example.fitnessapp.NOTIFICATION_RECEIVED");
                    getApplication().sendBroadcast(intent);

                    Log.d(TAG, "Successfully marked " + successCount + " notifications as read");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error marking all notifications as read", e);
                error.postValue("Failed to mark all as read: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
