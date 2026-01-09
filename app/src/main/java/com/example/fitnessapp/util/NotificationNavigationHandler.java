package com.example.fitnessapp.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnessapp.R;
import com.example.fitnessapp.fragment.PlanFragment;
import com.example.fitnessapp.fragment.community.PostDetailFragment;
import com.example.fitnessapp.model.response.NotificationResponse;

/**
 * Utility class to handle navigation from notifications to their target content.
 * Parses referenceUrl from notifications and navigates to the appropriate screen.
 */
public class NotificationNavigationHandler {

    private static final String TAG = "NotificationNavHandler";

    /**
     * Handle navigation from a notification click
     * @param context Activity context
     * @param notification The notification that was clicked
     */
    public static void handleNotificationNavigation(Context context, NotificationResponse notification) {
        if (!(context instanceof FragmentActivity)) {
            Log.e(TAG, "Context is not a FragmentActivity");
            return;
        }

        FragmentActivity activity = (FragmentActivity) context;
        String referenceUrl = notification.getReferenceUrl();
        String type = notification.getType();
        Long referenceId = notification.getReferenceId();

        if (referenceUrl == null || referenceUrl.isEmpty()) {
            Log.w(TAG, "No referenceUrl found in notification");
            Toast.makeText(context, "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Handling navigation - Type: " + type + ", URL: " + referenceUrl + ", ID: " + referenceId);

        try {
            if (referenceUrl.startsWith("/posts/")) {
                // Parse post ID from referenceUrl (not referenceId, as it might be comment ID)
                Long postId = parseIdFromUrl(referenceUrl);
                navigateToPost(activity, postId);
            } else if (referenceUrl.startsWith("/workouts/")) {
                // Navigate to workout plan
                navigateToWorkoutPlan(activity);
            } else {
                Log.w(TAG, "Unknown referenceUrl pattern: " + referenceUrl);
                Toast.makeText(context, "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling notification navigation", e);
            Toast.makeText(context, "Đã xảy ra lỗi khi mở nội dung", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Parse ID from URL path (e.g., "/posts/123" -> 123)
     */
    private static Long parseIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            String[] parts = url.split("/");
            // URL format: "/posts/123" or "/workouts/456"
            if (parts.length >= 3) {
                return Long.parseLong(parts[2]);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse ID from URL: " + url, e);
        }

        return null;
    }

    /**
     * Navigate to post detail screen
     * Note: Header and bottom nav visibility is controlled by MainActivity's FragmentLifecycleCallbacks
     */
    private static void navigateToPost(FragmentActivity activity, Long postId) {
        if (postId == null) {
            Log.e(TAG, "Post ID is null");
            Toast.makeText(activity, "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
            return;
        }

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        PostDetailFragment fragment = PostDetailFragment.newInstance(postId);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("PostDetail");
        transaction.commit();

        Log.d(TAG, "Navigated to post: " + postId);
    }

    /**
     * Navigate to workout plan screen
     * Note: Header and bottom nav visibility is controlled by MainActivity's FragmentLifecycleCallbacks
     */
    private static void navigateToWorkoutPlan(FragmentActivity activity) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        PlanFragment fragment = new PlanFragment();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack("WorkoutPlan");
        transaction.commit();

        // Update bottom navigation selection
        activity.findViewById(R.id.bottom_navigation).post(() -> {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    activity.findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_plan);
            }
        });

        Log.d(TAG, "Navigated to workout plan");
    }
}
