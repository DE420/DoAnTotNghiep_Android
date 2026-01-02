package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessapp.databinding.ActivityMainBinding;
import com.example.fitnessapp.fragment.CommunityFragment;
import com.example.fitnessapp.fragment.HomeFragment;
import com.example.fitnessapp.fragment.NotificationFragment;
import com.example.fitnessapp.fragment.OtherFragment;
import com.example.fitnessapp.fragment.PlanFragment;
import com.example.fitnessapp.fragment.ProfileFragment; // <-- Import ProfileFragment
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.AuthApi;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.network.UserApi;
import com.example.fitnessapp.repository.AuthRepository;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.viewmodel.NotificationViewModel;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.bumptech.glide.Glide;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int preSelectedItemIditem = 0;

    private AppBarLayout appBarLayout;
    private NotificationViewModel notificationViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appBarLayout = findViewById(R.id.app_bar_layout);

        if (savedInstanceState == null) {
            // load default home fragment
            loadFragment(new HomeFragment(), "Home", false);
        }

        // Load user profile and display avatar
        loadUserProfile();

        binding.imageAvatar.setOnClickListener(v -> {
            loadFragment(new ProfileFragment(), "Profile", true); // addToBackStack là true
        });

        // Notification icon click listener
        binding.iconNotification.setOnClickListener(v -> {
            loadFragment(new com.example.fitnessapp.fragment.NotificationFragment(), "Thông báo", true);
        });

        // Set up notification badge observer
        setupNotificationBadge();

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
            fragmentManager.popBackStack("Profile", FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
        if (title.equals("Profile")) {
            binding.appBarLayout.setVisibility(View.GONE);
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(false);
        } else {
            binding.bottomNavigation.getMenu().findItem(preSelectedItemIditem).setChecked(true);
            binding.appBarLayout.setVisibility(View.VISIBLE);
            binding.toolbar.setTitle(title);
        }
        // Cập nhật title của toolbar
        if (!title.equals("Profile")) {
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
        if (current instanceof ProfileFragment) {
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


}