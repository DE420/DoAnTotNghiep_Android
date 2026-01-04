package com.example.fitnessapp.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.LoginActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentProfileBinding;
import com.example.fitnessapp.model.request.UpdateProfileRequest;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.util.DateUtil;
import com.example.fitnessapp.viewmodel.ProfileViewModel;

/**
 * Profile Fragment - MVVM Architecture
 *
 * Displays user profile information with following sections:
 * - Profile Header: Avatar, Name, Email, Birthday
 * - Body Metrics: Weight, Height
 * - Fitness Info: Goal, Activity Level
 * - Overall Statistics: Total workouts, calories, hours
 * - Monthly Statistics: Current month stats
 *
 * Features:
 * - Pull to refresh
 * - Edit profile navigation
 * - Change password navigation
 * - Logout functionality
 * - Loading/Error states
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private ProfileResponse currentProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Setup UI
        setupClickListeners();
        setupObservers();

        // Load profile data
        viewModel.loadUserProfile();
    }

    /**
     * Setup LiveData observers for ViewModel
     */
    private void setupObservers() {
        // Observe profile data
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                currentProfile = profile;
                updateUI(profile);
                showContentState();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding != null) {
                if (Boolean.TRUE.equals(isLoading)) {
                    // Only show loading spinner if we don't have data yet
                    if (currentProfile == null) {
                        showLoadingState();
                    }
                } else {
                    // Stop refresh animation
                    binding.srlProfile.setRefreshing(false);
                }
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Handle specific error cases
                if (errorMessage.contains("Unauthorized") || errorMessage.contains("Token expired")) {
                    handleUnauthorized();
                } else {
                    showErrorState(errorMessage);
                }
                viewModel.clearError();
            }
        });
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Back button
        binding.ibBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Edit button
        binding.ibEdit.setOnClickListener(v -> navigateToEditProfile());

        // Change Password button
        binding.ibChangePassword.setOnClickListener(v -> navigateToChangePassword());

        // Logout button
        binding.ibLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Retry button (in error state)
        binding.btnRetry.setOnClickListener(v -> {
            viewModel.loadUserProfile();
        });

        // Pull to refresh
        binding.srlProfile.setOnRefreshListener(() -> {
            viewModel.loadUserProfile();
        });
    }

    /**
     * Update UI with profile data
     */
    @SuppressLint("DefaultLocale")
    private void updateUI(ProfileResponse profile) {
        if (binding == null || profile == null) {
            return;
        }

        String noDataStr = getString(R.string.profile_no_data);

        // Avatar
        Glide.with(this)
                .load(profile.getAvatar())
                .error(R.drawable.img_user_default_128)
                .placeholder(R.drawable.img_user_default_128)
                .into(binding.ivAvatar);

        // Basic Info
        binding.tvFullName.setText(
                profile.getName() != null ? profile.getName() : noDataStr
        );
        binding.tvEmail.setText(
                profile.getEmail() != null ? profile.getEmail() : noDataStr
        );

        // Birthday
        String birthdayStr = noDataStr;
        if (profile.getDateOfBirth() != null) {
            try {
                birthdayStr = DateUtil.convertToBirthday(
                        profile.getDateOfBirth(),
                        DateUtil.DD_MM_YYYY_DATE_FORMAT
                );
            } catch (Exception e) {
                Log.e(TAG, "Failed to format birthday: " + e.getMessage());
            }
        }
        binding.tvBirthday.setText(birthdayStr);

        // Body Metrics
        binding.tvWeightValue.setText(
                profile.getWeight() != null
                        ? String.format("%.1f", profile.getWeight())
                        : noDataStr
        );
        binding.tvHeightValue.setText(
                profile.getHeight() != null
                        ? String.format("%.2f", profile.getHeight())
                        : noDataStr
        );

        // Fitness Goal
        String fitnessGoalStr = profile.getFitnessGoal() != null
                ? getString(profile.getFitnessGoal().getResId())
                : noDataStr;
        binding.tvGoalValue.setText(fitnessGoalStr);

        // Activity Level
        String activityLevelStr = profile.getActivityLevel() != null
                ? getString(profile.getActivityLevel().getResId())
                : noDataStr;
        binding.tvActivityLevelValue.setText(activityLevelStr);

        // Overall Statistics
        binding.tvTotalWorkouts.setText(
                String.valueOf(profile.getTotalWorkouts())
        );
        binding.tvTotalCalories.setText(
                String.format("%.0f", profile.getTotalCalories())
        );
        binding.tvTotalHours.setText(
                String.format("%.1f", profile.getTotalHours())
        );

        // Monthly Statistics
        if (profile.getMonthlyStats() == null) {
            binding.cvMonthlyStats.setVisibility(View.GONE);
        } else {
            binding.cvMonthlyStats.setVisibility(View.VISIBLE);
            ProfileResponse.MonthlyStats monthlyStats = profile.getMonthlyStats();

            // Update month title
            binding.tvCurrentMonth.setText(
                    monthlyStats.getMonthName() != null
                            ? monthlyStats.getMonthName()
                            : noDataStr
            );

            // Update monthly stats values
            binding.tvMonthlyWorkouts.setText(
                    String.format("%s %d",
                            getString(R.string.profile_total_workouts),
                            monthlyStats.getTotalWorkouts())
            );
            binding.tvMonthlyDuration.setText(
                    String.format("%s %.0f %s",
                            getString(R.string.profile_total_duration),
                            monthlyStats.getTotalDurationMin(),
                            getString(R.string.profile_unit_min))
            );
            binding.tvMonthlyActiveDays.setText(
                    String.format("%s %d %s",
                            getString(R.string.profile_active_days),
                            monthlyStats.getActiveDays(),
                            getString(R.string.profile_unit_days))
            );
            binding.tvMonthlyAvg.setText(
                    String.format("%s %.0f %s",
                            getString(R.string.profile_avg_per_workout),
                            monthlyStats.getAvgDurationMin(),
                            getString(R.string.profile_unit_min))
            );
            binding.tvMonthlyStreak.setText(
                    String.format("%s %d %s",
                            getString(R.string.profile_current_streak),
                            monthlyStats.getCurrentStreak(),
                            getString(R.string.profile_unit_days))
            );
            binding.tvMonthlyCalories.setText(
                    String.format("%s %.0f",
                            getString(R.string.profile_total_calories),
                            monthlyStats.getTotalCalories())
            );
        }
    }

    /**
     * Show loading state
     */
    private void showLoadingState() {
        if (binding == null) return;

        binding.pbLoading.setVisibility(View.VISIBLE);
        binding.nsvContent.setVisibility(View.GONE);
        binding.llErrorState.setVisibility(View.GONE);
    }

    /**
     * Show content state
     */
    private void showContentState() {
        if (binding == null) return;

        binding.pbLoading.setVisibility(View.GONE);
        binding.nsvContent.setVisibility(View.VISIBLE);
        binding.llErrorState.setVisibility(View.GONE);
    }

    /**
     * Show error state
     */
    private void showErrorState(String errorMessage) {
        if (binding == null) return;

        binding.pbLoading.setVisibility(View.GONE);
        binding.nsvContent.setVisibility(View.GONE);
        binding.llErrorState.setVisibility(View.VISIBLE);

        String message = errorMessage != null && !errorMessage.isEmpty()
                ? errorMessage
                : getString(R.string.profile_error_load);
        binding.tvErrorMessage.setText(message);
    }

    /**
     * Navigate to Edit Profile screen
     */
    private void navigateToEditProfile() {
        if (currentProfile == null) {
            Toast.makeText(requireContext(),
                    R.string.profile_error_load,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest.Builder()
                .avatar(currentProfile.getAvatar())
                .name(currentProfile.getName())
                .height(currentProfile.getHeight())
                .weight(currentProfile.getWeight())
                .activityLevel(currentProfile.getActivityLevel())
                .fitnessGoal(currentProfile.getFitnessGoal())
                .dateOfBirth(currentProfile.getDateOfBirth())
                .build();

        Bundle bundle = new Bundle();
        bundle.putSerializable(UpdateProfileRequest.KEY_UPDATE_PROFILE_REQUEST, updateProfileRequest);

        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                )
                .replace(R.id.fragment_container, EditProfileFragment.class, bundle)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Navigate to Change Password screen
     */
    private void navigateToChangePassword() {
        getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                )
                .replace(R.id.fragment_container, ChangePasswordFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Show logout confirmation dialog
     */
    private void showLogoutConfirmation() {
        new ConfirmLogoutDialogFragment().show(getParentFragmentManager(), "logout_dialog");
    }

    /**
     * Handle unauthorized error (token expired)
     */
    private void handleUnauthorized() {
        Toast.makeText(requireContext(),
                R.string.profile_error_unauthorized,
                Toast.LENGTH_LONG).show();

        // Clear session and redirect to login
        SessionManager.getInstance(requireActivity()).logout();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when returning from edit screen
        if (currentProfile != null) {
            viewModel.loadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
