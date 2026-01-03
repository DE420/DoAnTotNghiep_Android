package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessapp.databinding.ActivityMainBinding;
import com.example.fitnessapp.fragment.community.CommunityFragment;
import com.example.fitnessapp.fragment.HomeFragment;
import com.example.fitnessapp.fragment.NotificationFragment;
import com.example.fitnessapp.fragment.OtherFragment;
import com.example.fitnessapp.fragment.PlanFragment;
import com.example.fitnessapp.fragment.ProfileFragment; // <-- Import ProfileFragment
import com.example.fitnessapp.fragment.community.CreateUpdatePostFragment;
import com.example.fitnessapp.fragment.community.PostDetailFragment;
import com.example.fitnessapp.fragment.nutrition.NutritionMainFragment;
import com.example.fitnessapp.fragment.nutrition.MenuDetailFragment;
import com.example.fitnessapp.fragment.nutrition.CreateEditMenuFragment;
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.network.UserApi;
import com.example.fitnessapp.repository.AuthRepository;
import com.example.fitnessapp.repository.NotificationRepository;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.viewmodel.NotificationViewModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.bumptech.glide.Glide;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int preSelectedItemIditem = 0;

    private AppBarLayout appBarLayout;
    private NotificationViewModel notificationViewModel;

    // BroadcastReceiver for notification badge updates
    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Refresh badge when new notification is received
            if (notificationViewModel != null) {
                notificationViewModel.refreshUnreadCount();
            }
        }
    };

    /**
     * Setup fragment lifecycle callbacks to automatically control header and bottom nav visibility
     */
    private void setupFragmentLifecycleCallbacks() {
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {

                    private void updateVisibility(Fragment f) {
                        // Control header and bottom nav visibility based on fragment type
                        if (f instanceof PostDetailFragment || f instanceof NotificationFragment || f instanceof CreateUpdatePostFragment
                                || f instanceof MenuDetailFragment || f instanceof CreateEditMenuFragment) {
                            // Hide header for PostDetailFragment, NotificationFragment, CreateUpdatePostFragment,
                            // MenuDetailFragment, and CreateEditMenuFragment
                            appBarLayout.setVisibility(View.GONE);

                            if (f instanceof PostDetailFragment || f instanceof CreateUpdatePostFragment
                                    || f instanceof MenuDetailFragment || f instanceof CreateEditMenuFragment) {
                                // Hide bottom nav for PostDetailFragment, CreateUpdatePostFragment,
                                // MenuDetailFragment, and CreateEditMenuFragment
                                binding.bottomNavigation.setVisibility(View.GONE);
                            } else if (f instanceof NotificationFragment) {
                                // Show bottom nav for NotificationFragment
                                binding.bottomNavigation.setVisibility(View.VISIBLE);
                            }
                        } else if (f instanceof ProfileFragment || f instanceof NutritionMainFragment) {
                            // Hide header for ProfileFragment and NutritionMainFragment
                            appBarLayout.setVisibility(View.GONE);
                            // Keep bottom nav visible and maintain selection
                            binding.bottomNavigation.setVisibility(View.VISIBLE);
                            if (f instanceof ProfileFragment) {
                                // Keep bottom nav selection but uncheck it for ProfileFragment
                                binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(false);
                            }
                        } else {
                            // Show header and bottom nav for other fragments (including CommunityFragment)
                            appBarLayout.setVisibility(View.VISIBLE);
                            binding.bottomNavigation.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        super.onFragmentResumed(fm, f);
                        updateVisibility(f);
                    }

                    @Override
                    public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        super.onFragmentStarted(fm, f);
                        // Also update visibility when fragment is started (handles show/hide scenarios)
                        updateVisibility(f);
                    }

                    @Override
                    public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
                        super.onFragmentStopped(fm, f);

                        // When PostDetailFragment, CreateUpdatePostFragment, MenuDetailFragment,
                        // or CreateEditMenuFragment is stopped (being removed),
                        // explicitly show header and bottom nav so that whatever fragment is shown next
                        // will have them visible by default
                        if (f instanceof PostDetailFragment || f instanceof CreateUpdatePostFragment
                                || f instanceof MenuDetailFragment || f instanceof CreateEditMenuFragment) {
                            // Use post() to ensure this happens after the fragment transaction completes
                            binding.getRoot().post(() -> {
                                // Check if there's another special fragment still visible
                                Fragment currentFragment = fm.findFragmentById(R.id.fragment_container);
                                if (currentFragment != null) {
                                    updateVisibility(currentFragment);
                                } else {
                                    // No fragment found, default to showing header and bottom nav
                                    appBarLayout.setVisibility(View.VISIBLE);
                                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                }, true
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appBarLayout = findViewById(R.id.app_bar_layout);

        Log.d("MainActivity", "onCreate called");
        Log.d("MainActivity", "Intent action: " + getIntent().getAction());
        Log.d("MainActivity", "Intent extras: " + getIntent().getExtras());
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Log.d("MainActivity", "  Extra key: " + key + " = " + getIntent().getExtras().get(key));
            }
        }

        // Register fragment lifecycle callbacks to control header visibility
//        setupFragmentLifecycleCallbacks();

        // Register BroadcastReceiver for notification badge updates
        IntentFilter filter = new IntentFilter("com.example.fitnessapp.NOTIFICATION_RECEIVED");
        registerReceiver(notificationReceiver, filter);

        // Set up notification badge observer FIRST before handling deep links
        setupNotificationBadge();

        if (savedInstanceState == null) {
            // load default home fragment
            loadFragment(new HomeFragment(), "Home", false);
        }

        // Load user profile and display avatar
        loadUserProfile();

        // Register FCM token if logged in
        registerFcmToken();

        binding.imageAvatar.setOnClickListener(v -> {
            loadFragment(new ProfileFragment(), "Profile", true); // addToBackStack là true
        });

        // Notification icon click listener
        binding.iconNotification.setOnClickListener(v -> {
            loadFragment(new com.example.fitnessapp.fragment.NotificationFragment(), "Thông báo", true);
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = getString(R.string.app_name);
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = getString(R.string.nav_title_home);
            } else if (itemId == R.id.nav_plan) {
                selectedFragment = new PlanFragment();
                title = getString(R.string.nav_title_plan);
            } else if (itemId == R.id.nav_practice) {
                startActivity(new Intent(this, ExerciseCountActivity.class));
                return false;
            } else if (itemId == R.id.nav_community) {
                selectedFragment = new CommunityFragment();
                title = getString(R.string.nav_title_community);
            } else if (itemId == R.id.nav_other) {
                selectedFragment = new OtherFragment();
                title = getString(R.string.nav_title_other);
            }

            if (selectedFragment != null) {
                // Các fragment từ bottom nav không cần thêm vào back stack
                loadFragment(selectedFragment, title, false);
                return true;
            }

            return false;
        });

        // Handle notification deep link if opened from notification (AFTER ViewModel is initialized)
        handleNotificationDeepLink(getIntent());

//        fakeLogin();
    }

    private void fakeLogin() {

        String username = "vanan";
        String password = "Password123!";


        AuthRepository authRepository = new AuthRepository(this);
        authRepository.login(new LoginRequest(username, password),
                new Callback<ApiResponse<LoginResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isStatus()) {
                            SessionManager.getInstance(getApplicationContext()).saveTokens(
                                    response.body().getData().getAccessToken(),
                                    response.body().getData().getRefreshToken()
                            );
                            Snackbar.make(
                                    binding.getRoot(),
                                    "Login success",
                                    Snackbar.LENGTH_SHORT
                            ).setBackgroundTint(getColor(R.color.green_500))
                                    .show();
                        } else {
                            Snackbar.make(
                                            binding.getRoot(),
                                            "Login fail (onResponse)",
                                            Snackbar.LENGTH_SHORT
                                    ).setBackgroundTint(getColor(R.color.red_400))
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                        Snackbar.make(
                                        binding.getRoot(),
                                        "Login fail (onFailure)",
                                        Snackbar.LENGTH_SHORT
                                ).setBackgroundTint(getColor(R.color.red_400))
                                .show();
                    }
                });

    }

    // Cập nhật hàm loadFragment để xử lý back stack
    private void loadFragment(Fragment fragment, String title, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (!addToBackStack) {
            fragmentManager.popBackStack(title, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in, // enter
                R.anim.fade_out, // exit
                R.anim.fade_in, // popEnter
                R.anim.slide_out // popExit
        );
        transaction.replace(R.id.fragment_container, fragment);

        // Chỉ thêm vào back stack khi được yêu cầu (ví dụ: khi mở Profile)
        if (addToBackStack) {
            transaction.addToBackStack(title);
        }

        transaction.commit();

        preSelectedItemIditem = binding.bottomNavigation.getSelectedItemId();
        if (title.equals("Profile") || title.equals("Thông báo")) {
            binding.appBarLayout.setVisibility(View.GONE);
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(false);
        } else {
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(true);
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.toolbar.setTitle(title);
        }
        // Cập nhật title của toolbar only for non-Profile and non-Notification fragments
        if (!title.equals("Profile") && !title.equals("Thông báo")) {
            binding.toolbar.setTitle(title);
        }
    }

    public void setAppBarVisible(boolean visible) {

        if (appBarLayout != null) {
            appBarLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        Fragment current = fm.findFragmentById(R.id.fragment_container);
        if (current instanceof ProfileFragment || current instanceof NotificationFragment) {
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(true);
        }
        super.onBackPressed();
//        if (fm.getBackStackEntryCount() > 0) {
//            fm.popBackStack();
//        } else {
//            super.onBackPressed();
//        }
    }

    /**
     * Load user profile from API and save to local storage
     */
    private void loadUserProfile() {
        SessionManager sessionManager = SessionManager.getInstance(this);

        // First, load avatar from local storage if available
        String cachedAvatar = sessionManager.getAvatar();
        if (cachedAvatar != null && !cachedAvatar.isEmpty()) {
            loadAvatarImage(cachedAvatar);
        }

        // Then fetch fresh data from API
        if (sessionManager.isLoggedIn()) {
            UserApi userApi = RetrofitClient.getUserApi(this);
            userApi.getUserProfile().enqueue(new Callback<ApiResponse<ProfileResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<ProfileResponse>> call,
                                      Response<ApiResponse<ProfileResponse>> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().isStatus()) {
                        ProfileResponse profile = response.body().getData();

                        // Save profile data to local storage
                        sessionManager.saveUserProfile(
                                profile.getId(),
                                profile.getName(),
                                profile.getUsername(),
                                profile.getEmail(),
                                profile.getAvatar(),
                                profile.getSex(),
                                profile.getWeight(),
                                profile.getHeight(),
                                profile.getDateOfBirth()
                        );

                        // Update avatar in UI
                        loadAvatarImage(profile.getAvatar());

                        Log.d("MainActivity", "Profile loaded: " + profile.getName());
                    } else {
                        Log.e("MainActivity", "Failed to load profile: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                    Log.e("MainActivity", "Error loading profile", t);
                }
            });
        }
    }

    /**
     * Load avatar image using Glide
     * @param avatarUrl URL of the avatar image
     */
    private void loadAvatarImage(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.img_user_default_128)
                    .error(R.drawable.img_user_default_128)
                    .into(binding.imageAvatar);
        } else {
            binding.imageAvatar.setImageResource(R.drawable.img_user_default_128);
        }
    }

    /**
     * Public method to refresh avatar - can be called from ProfileFragment after avatar update
     */
    public void refreshAvatar() {
        String avatarUrl = SessionManager.getInstance(this).getAvatar();
        loadAvatarImage(avatarUrl);
    }

    /**
     * Set up notification badge observer to display unread count
     */
    private void setupNotificationBadge() {
        notificationViewModel = new ViewModelProvider(this).get(NotificationViewModel.class);

        // Observe unread count and update badge
        notificationViewModel.getUnreadCount().observe(this, unreadCount -> {
            if (unreadCount != null && unreadCount > 0) {
                binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                // Show "9+" for counts greater than 9
                if (unreadCount > 9) {
                    binding.tvNotificationBadge.setText("9+");
                } else {
                    binding.tvNotificationBadge.setText(String.valueOf(unreadCount));
                }
            } else {
                binding.tvNotificationBadge.setVisibility(View.GONE);
            }
        });

        // Initial load of unread count
        notificationViewModel.refreshUnreadCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh badge count when returning to MainActivity
        if (notificationViewModel != null) {
            notificationViewModel.refreshUnreadCount();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Handle notification deep link when app is already running
        handleNotificationDeepLink(intent);
    }

    /**
     * Handle deep link from notification when app is opened from background/killed
     */
    private void handleNotificationDeepLink(Intent intent) {
        Log.d("MainActivity", "handleNotificationDeepLink called");

        if (intent == null) {
            Log.d("MainActivity", "Intent is null, skipping deep link handling");
            return;
        }

        String link = intent.getStringExtra("notification_link");
        String type = intent.getStringExtra("notification_type");
        String notificationIdStr = intent.getStringExtra("notification_id");

        Log.d("MainActivity", "Deep link data - Link: " + link + ", Type: " + type + ", ID: " + notificationIdStr);

        if (link != null && !link.isEmpty()) {
            Log.d("MainActivity", "Handling notification deep link - Type: " + type + ", Link: " + link);

            // Mark notification as read if we have the notification ID
            if (notificationIdStr != null && !notificationIdStr.isEmpty()) {
                try {
                    Long notificationId = Long.parseLong(notificationIdStr);
                    if (notificationViewModel != null) {
                        notificationViewModel.markAsRead(notificationId);
                        Log.d("MainActivity", "Marked notification as read: " + notificationId);
                    } else {
                        Log.w("MainActivity", "NotificationViewModel is null, cannot mark as read");
                    }
                } catch (NumberFormatException e) {
                    Log.w("MainActivity", "Invalid notification ID: " + notificationIdStr, e);
                }
            }

            // Wait for the fragment manager to be ready
            binding.getRoot().post(() -> {
                Log.d("MainActivity", "Post runnable executing for deep link navigation");
                try {
                    if (link.startsWith("/posts/")) {
                        // Extract post ID from link and navigate to post detail
                        String[] parts = link.split("/");
                        Log.d("MainActivity", "Post link parts: " + java.util.Arrays.toString(parts));
                        if (parts.length >= 3) {
                            long postId = Long.parseLong(parts[2]);
                            Log.d("MainActivity", "Navigating to post detail: " + postId);
                            navigateToPostDetail(postId);
                        } else {
                            Log.w("MainActivity", "Invalid post link format: " + link);
                        }
                    } else if (link.startsWith("/workouts/")) {
                        Log.d("MainActivity", "Navigating to workout plan");
                        // Navigate to workout plan
                        navigateToWorkoutPlan();
                    } else {
                        Log.w("MainActivity", "Unknown link format: " + link);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Error handling deep link", e);
                    Toast.makeText(this, "Không thể mở nội dung này", Toast.LENGTH_SHORT).show();
                }
            });

            // Clear the intent extras so we don't handle it again
            intent.removeExtra("notification_link");
            intent.removeExtra("notification_type");
            intent.removeExtra("notification_id");
            Log.d("MainActivity", "Cleared intent extras");
        } else {
            Log.d("MainActivity", "No notification link found in intent");
        }
    }

    /**
     * Navigate to post detail from deep link
     */
    private void navigateToPostDetail(long postId) {
        PostDetailFragment fragment = PostDetailFragment.newInstance(postId);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                )
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("PostDetail")
                .commit();
        Log.d("MainActivity", "Navigated to post: " + postId);
    }

    /**
     * Navigate to workout plan from deep link
     */
    private void navigateToWorkoutPlan() {
        // Switch to plan tab in bottom navigation
        binding.bottomNavigation.setSelectedItemId(R.id.nav_plan);
        Log.d("MainActivity", "Navigated to workout plan");
    }

    /**
     * Register FCM token with backend if user is logged in
     */
    private void registerFcmToken() {
        SessionManager sessionManager = SessionManager.getInstance(this);

        // Only register if user is logged in
        if (!sessionManager.isLoggedIn()) {
            Log.d("MainActivity", "User not logged in, skipping FCM token registration");
            return;
        }

        // Get FCM token from Firebase
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("MainActivity", "Failed to get FCM token", task.getException());
                        return;
                    }

                    // Get new FCM token
                    String token = task.getResult();
                    if (token == null || token.isEmpty()) {
                        Log.w("MainActivity", "FCM token is null or empty");
                        return;
                    }

                    Log.d("MainActivity", "FCM token retrieved: " + token);

                    // Save token locally
                    sessionManager.saveFcmToken(token);

                    // Register token with backend in background thread
                    new Thread(() -> {
                        try {
                            NotificationRepository repository = new NotificationRepository();
                            repository.registerDeviceToken(getApplicationContext(), token, "ANDROID");
                            Log.d("MainActivity", "FCM token registered with backend successfully");
                        } catch (Exception e) {
                            Log.e("MainActivity", "Failed to register FCM token with backend", e);
                        }
                    }).start();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister BroadcastReceiver to prevent memory leaks
        try {
            unregisterReceiver(notificationReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered, ignore
            Log.w("MainActivity", "Receiver not registered: " + e.getMessage());
        }
    }

}