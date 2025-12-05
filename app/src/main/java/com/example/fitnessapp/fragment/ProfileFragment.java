package com.example.fitnessapp.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.LoginActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.databinding.FragmentProfileBinding;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.utils.DateUtil;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    public static final String TAG = "com.example.fitnessapp.fragment." + ProfileFragment.class.getSimpleName();

    private FragmentProfileBinding binding;
    private ApiService apiService;
    private int shortAnimationDuration;
    private ProfileResponse profileResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.svContent.setVisibility(View.GONE);
        binding.buttonEditProfile.setVisibility(View.GONE);
        binding.rlLoadingData.setVisibility(View.VISIBLE);
        binding.rlError.setVisibility(View.GONE);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        apiService = RetrofitClient.getApiService();

        loadUserProfileData();

        setupClickListeners();

    }

    private void crossfadeFromLoadingViewToContentView() {

        // Set the content view to 0% opacity but visible, so that it is
        // visible but fully transparent during the animation.
        binding.svContent.setAlpha(0f);
        binding.svContent.setVisibility(View.VISIBLE);


        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view.
        binding.svContent.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);


        binding.buttonEditProfile.setAlpha(0f);
        binding.buttonEditProfile.setVisibility(View.VISIBLE);
        binding.buttonEditProfile.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);


        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.
        binding.rlLoadingData.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.rlLoadingData.setVisibility(View.GONE);
                    }
                });
    }

    private void hideEditProfileButton() {
        binding.buttonEditProfile.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.buttonEditProfile.setVisibility(View.GONE);
                    }
                });
    }

    private void crossfadeFromLoadingViewToErrorView() {

        // Set the content view to 0% opacity but visible, so that it is
        // visible but fully transparent during the animation.
        binding.rlError.setAlpha(0f);
        binding.rlError.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view.
        binding.rlError.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);



        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.
        binding.rlLoadingData.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.rlLoadingData.setVisibility(View.GONE);
                    }
                });
    }

    private void crossfadeFromErrorViewToLoadingView() {

        // Set the content view to 0% opacity but visible, so that it is
        // visible but fully transparent during the animation.
        binding.rlLoadingData.setAlpha(0f);
        binding.rlLoadingData.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view.
        binding.rlLoadingData.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);



        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.
        binding.rlError.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        binding.rlError.setVisibility(View.GONE);
                    }
                });
    }

    private void loadUserProfileData() {
        SessionManager sessionManager = SessionManager.getInstance(getActivity());
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            if (sessionManager.refreshToken()) {
                accessToken = sessionManager.getAccessToken();
            } else {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                // Kết thúc Activity chứa Fragment này (MainActivity)
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return;
            }
        }

        apiService.getUserProfile("Bearer " + accessToken).enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, Response<ApiResponse<ProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        profileResponse = response.body().getData();
                        Log.e(TAG, profileResponse.toString());
                        showContentView();
                    } else {
                        String error = response.body().getData().toString();
                        showErrorView(error);
                        Log.e(TAG, error);
                    }
                } else {
                    String error = response.body() != null ? response.body().getData().toString() : getString(R.string.txt_error_loading_data);
                    showErrorView(error);
                    Log.e(TAG, "code: " + response.code() + "message: +" + (response.errorBody() != null ? response.errorBody().toString() : "error loading data unknown."));

                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                String error = "Sorry, something went wrong.\nPlease check your network.";
                showErrorView(error);
                Log.e(TAG, "error cant loading data: " + t.getMessage());
            }
        });
    }


    private void showContentView() {
        setProfileDataToContentView();
        crossfadeFromLoadingViewToContentView();
    }

    @SuppressLint("DefaultLocale")
    private void setProfileDataToContentView() {
        if (profileResponse == null) {
            showErrorView("No data found.\nPlease retry.");
        } else {
            String noDataStr = getString(R.string.txt_no_data);
            Glide.with(binding.imgAvatar)
                    .load(profileResponse.getAvatar())
                    .error(R.drawable.img_user_default_128)
                    .into(binding.imgAvatar);
            binding.tvFullName.setText(
                    profileResponse.getName() != null ? profileResponse.getName() : noDataStr
            );
            binding.tvEmail.setText(
                    profileResponse.getEmail() != null ? profileResponse.getEmail() : noDataStr
            );

            String birthdayStr = noDataStr;
            if (profileResponse.getDateOfBirth() != null) {
                try {
                    birthdayStr = DateUtil.convertBirthday(
                            profileResponse.getDateOfBirth(),
                            DateUtil.DD_MM_YYYY_DATE_FORMAT
                    );
                } catch (Exception e) {
                    Log.e(TAG, "Fail to convert birthday of user: " + e.getMessage());
                }
            }
            binding.tvBirthday.setText(birthdayStr);
            binding.tvUserWeight.setText(
                    profileResponse.getWeight() != null
                            ? String.format("$.2f", profileResponse.getWeight())
                            : noDataStr
            );
            binding.tvUserHeight.setText(
                    profileResponse.getHeight() != null
                            ? String.format("$.2f", profileResponse.getHeight())
                            : noDataStr
            );

            String fitnessGoalStr = getString(R.string.txt_not_selected_goal);
            if (profileResponse.getFitnessGoal() != null) {
                switch (profileResponse.getFitnessGoal()) {
                    case OTHERS: {
                        fitnessGoalStr = getString(R.string.fitness_goal_others);
                        break;
                    }
                    case SHAPE_BODY: {
                        fitnessGoalStr = getString(R.string.fitness_goal_shape_body);
                        break;
                    }
                    case GAIN_WEIGHT: {
                        fitnessGoalStr = getString(R.string.fitness_goal_gain_weight);
                        break;
                    }
                    case LOSE_WEIGHT: {
                        fitnessGoalStr = getString(R.string.fitness_goal_lose_weight);
                        break;
                    }
                    case MUSCLE_GAIN: {
                        fitnessGoalStr = getString(R.string.fitness_goal_muscle_gain);
                        break;
                    }
                    default:
                        fitnessGoalStr = getString(R.string.txt_not_selected_goal);

                }
            }
            binding.tvFitnessGoal.setText(fitnessGoalStr);

            String activityLevelStr = getString(R.string.txt_not_selected_activity_level);
            if (profileResponse.getActivityLevel() != null) {
                switch (profileResponse.getActivityLevel()) {
                    case SEDENTARY: {
                        activityLevelStr = getString(R.string.acitvity_level_sedentary);
                        break;
                    }
                    case LIGHTLY_ACTIVE: {
                        activityLevelStr = getString(R.string.acitvity_level_lightly_active);
                        break;
                    }
                    case MODERATELY_ACTIVE: {
                        activityLevelStr = getString(R.string.acitvity_level_moderately_active);
                        break;
                    }
                    case VERY_ACTIVE: {
                        activityLevelStr = getString(R.string.acitvity_level_very_active);
                        break;
                    }
                    case EXTRA_ACTIVE: {
                        activityLevelStr = getString(R.string.acitvity_level_extra_active);
                        break;
                    }
                    default:
                        activityLevelStr = getString(R.string.txt_not_selected_goal);

                }
            }
            binding.tvActivityLevel.setText(activityLevelStr);

            binding.tvTotalWorkouts.setText(
                    Integer.toString(profileResponse.getTotalWorkouts())
            );

            binding.tvTotalCalories.setText(
                    String.format("%.2f", profileResponse.getTotalCalories())
            );

            binding.tvTotalHours.setText(
                    String.format("%.2f", profileResponse.getTotalHours())
            );

            String minStr = getString(R.string.txt_min);
            String dayStr = getString(R.string.txt_days);

            if (profileResponse.getMonthlyStats() == null) {
                setVisibilityForMonthlyStatistic(View.GONE);

            } else {
                setVisibilityForMonthlyStatistic(View.VISIBLE);
                ProfileResponse.MonthlyStats monthlyStats = profileResponse.getMonthlyStats();
                binding.tvCurrentMonth.setText(
                        monthlyStats.getMonthName() != null
                                ? monthlyStats.getMonthName()
                                : noDataStr
                );
                binding.tvTotalWorkoutsMonth.setText(
                        getString(R.string.txt_total_workouts)
                        + " " + monthlyStats.getTotalWorkouts()
                );
                binding.tvTotalDurationMonth.setText(
                        String.format("%s %.2f %s",
                                getString(R.string.txt_total_duration),
                                monthlyStats.getTotalDurationMin(),
                                minStr)
                );
                binding.tvActiveDaysMonth.setText(
                        getString(R.string.txt_active_days) + " " + monthlyStats.getActiveDays()
                );
                binding.tvAvgPerWorkoutMonth.setText(
                        String.format("%s %.2f %s",
                                getString(R.string.txt_avg_per_workout),
                                monthlyStats.getAvgDurationMin(),
                                minStr)
                );
                binding.tvCurrentStreakMonth.setText(
                        String.format("%s %d %s",
                                getString(R.string.txt_current_streak),
                                monthlyStats.getCurrentStreak(),
                                dayStr)
                );
                binding.tvTotalCaloriesMonth.setText(
                        String.format("%s %,3.2f",
                                getString(R.string.txt_total_calories),
                                monthlyStats.getTotalCalories())
                );
            }
        }
    }

    private void setVisibilityForMonthlyStatistic(int visibility) {
        binding.tvMonthlyStats.setVisibility(visibility);
        binding.tvCurrentMonth.setVisibility(visibility);
        binding.tvTotalWorkoutsMonth.setVisibility(visibility);
        binding.tvTotalDurationMonth.setVisibility(visibility);
        binding.tvActiveDaysMonth.setVisibility(visibility);
        binding.tvAvgPerWorkoutMonth.setVisibility(visibility);
        binding.tvTotalCaloriesMonth.setVisibility(visibility);
        binding.tvCurrentStreakMonth.setVisibility(visibility);

    }

    private void showErrorView(String error) {
        hideEditProfileButton();
        binding.tvError.setText(error);
        crossfadeFromLoadingViewToErrorView();
    }

    private void setupClickListeners() {
        binding.btnReloadData.setOnClickListener(view -> {
            crossfadeFromErrorViewToLoadingView();
            loadUserProfileData();
        });

        binding.buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to EditProfileFragment or Activity
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in, // enter
                            R.anim.fade_out, // exit
                            R.anim.fade_in, // popEnter
                            R.anim.slide_out // popExit
                    )
                    .replace(R.id.fragment_container, EditProfileFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();

        });

        binding.buttonChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Password Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ChangePasswordFragment or Activity
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in, // enter
                            R.anim.fade_out, // exit
                            R.anim.fade_in, // popEnter
                            R.anim.slide_out // popExit
                    )
                    .replace(R.id.fragment_container, ChangePasswordFragment.class, null)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
        });

        binding.imgChevronLeft.setOnClickListener(view -> {
            getActivity().onBackPressed();
        });

        binding.buttonLogout.setOnClickListener(v -> {
//            handleLogout();

            new ConfirmLogoutDialogFragment().show(getParentFragmentManager(), null);
        });
    }

    private void handleLogout() {
        // Sử dụng requireActivity() để lấy Context một cách an toàn
        String refreshToken = SessionManager.getInstance(requireActivity()).getRefreshToken();

        if (refreshToken == null) {
            Toast.makeText(getContext(), "No active session found.", Toast.LENGTH_SHORT).show();
            clearUserDataAndNavigateToLogin();
            return;
        }

        binding.buttonLogout.setEnabled(false);

        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);

        apiService.logout(logoutRequest).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "Logged out (offline).", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
            }
        });
    }

    private void clearUserDataAndNavigateToLogin() {
        SessionManager.getInstance(requireActivity()).logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Kết thúc Activity chứa Fragment này (MainActivity)
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh rò rỉ bộ nhớ
    }


}