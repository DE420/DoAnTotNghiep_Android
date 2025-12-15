package com.example.fitnessapp.fragment;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
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
import com.example.fitnessapp.constants.Constants;
import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.RefreshTokenRequest;
import com.example.fitnessapp.model.request.UpdateProfileRequest;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.databinding.FragmentProfileBinding;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.utils.DateUtil;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    public static final String TAG = ProfileFragment.class.getSimpleName();

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


        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        apiService = RetrofitClient.getApiService();


        binding.buttonEditProfile.setVisibility(View.GONE);
        binding.svContent.setVisibility(View.GONE);
        binding.rlLoadingData.setVisibility(View.VISIBLE);
        setupClickListeners();

        loadUserProfileData();

    }



    private void loadUserProfileData() {
        SessionManager sessionManager = SessionManager.getInstance(getActivity());
        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null) {
            Snackbar.make(binding.fragmentProfile, "Redirect to login", Snackbar.LENGTH_SHORT).show();
            Log.e(TAG, "accessToken null");
            return;
//            Log.e(TAG, "access tokens null");
//            if (sessionManager.refreshToken()) {
//                accessToken = sessionManager.getAccessToken();
//            } else {
//                Intent intent = new Intent(getActivity(), LoginActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                startActivity(intent);
//
//                // Kết thúc Activity chứa Fragment này (MainActivity)
//                if (getActivity() != null) {
//                    getActivity().finish();
//                }
//                return;
//            }
        }

        Log.e(TAG, accessToken);

        apiService.getUserProfile(Constants.PREFIX_JWT + " " + accessToken).enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, Response<ApiResponse<ProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isStatus()) {
                        profileResponse = response.body().getData();
                        Log.e(TAG, profileResponse.toString());
                        if (binding != null) {
                            showContentView(binding.rlLoadingData);
                        }
                    } else {
                        String error = response.body().getData().toString();
                        showErrorView(binding.rlLoadingData, error);
                        Log.e(TAG, error);
                    }
                } else {
                    if (response.code() == 401) {
                        if (binding != null) {
                            Snackbar.make(binding.fragmentProfile, "Redirect to login", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        String error = response.body() != null ? response.body().getData().toString() : getString(R.string.txt_error_loading_data);
                        if (binding != null) {
                            showErrorView(binding.rlLoadingData, error);
                        }
                        try {
                            Log.e(TAG, "code: " + response.code() + "message: +" + (response.errorBody() != null ? response.errorBody().string() : "error loading data unknown."));
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                String error = "Sorry, something went wrong.\nPlease check your network.";
                if (binding != null) {
                    showErrorView(binding.rlLoadingData, error);
                }
                Log.e(TAG, "error cant loading data: " + t.getMessage());
            }
        });
    }



    @SuppressLint("DefaultLocale")
    private void setProfileDataToContentView() {
        if (profileResponse == null) {
            showErrorView(binding.svContent, "No data found.\nPlease retry.");
        } else {
            String noDataStr = getString(R.string.txt_no_data);
            Glide.with(requireActivity())
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
                    birthdayStr = DateUtil.convertToBirthday(
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
                            ? String.format("%.2f kg", profileResponse.getWeight())
                            : noDataStr
            );
            binding.tvUserHeight.setText(
                    profileResponse.getHeight() != null
                            ? String.format("%.2f m", profileResponse.getHeight())
                            : noDataStr
            );

            String fitnessGoalStr = profileResponse.getFitnessGoal() != null
                    ? getString(profileResponse.getFitnessGoal().getResId())
                    :getString(R.string.txt_not_selected_goal);

            binding.tvFitnessGoal.setText(fitnessGoalStr);

            String activityLevelStr = profileResponse.getActivityLevel() != null
                    ? getString(profileResponse.getActivityLevel().getResId())
                    : getString(R.string.txt_not_selected_activity_level);
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


    private void setupClickListeners() {
        binding.btnReloadData.setOnClickListener(view -> {
//            crossfadeFromErrorViewToLoadingView();
            showLoadingView(binding.rlError);
            loadUserProfileData();
        });

        binding.buttonEditProfile.setOnClickListener(v -> {
            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest.Builder()
                    .avatar(profileResponse.getAvatar())
                    .name(profileResponse.getName())
                    .height(profileResponse.getHeight())
                    .weight(profileResponse.getWeight())
                    .activityLevel(profileResponse.getActivityLevel())
                    .fitnessGoal(profileResponse.getFitnessGoal())
                    .dateOfBirth(profileResponse.getDateOfBirth())
                    .build();


            Bundle bundle = new Bundle();
            bundle.putSerializable(UpdateProfileRequest.KEY_UPDATE_PROFILE_REQUEST, updateProfileRequest);
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in, // enter
                            R.anim.fade_out, // exit
                            R.anim.fade_in, // popEnter
                            R.anim.slide_out // popExit
                    )
                    .replace(R.id.fragment_container, EditProfileFragment.class, bundle)
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();

        });

        binding.buttonChangePassword.setOnClickListener(v -> {
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

    private void showLoadingView(View from) {
        if (binding != null) {
            binding.buttonEditProfile.setVisibility(View.GONE);
            fade(from, binding.rlLoadingData);
        }
    }

    private void showContentView(View from) {
        if (binding != null) {
            setProfileDataToContentView();
            fade(from, binding.svContent);
            binding.buttonEditProfile.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorView(View from, String error) {
        if (binding != null) {
            binding.buttonEditProfile.setVisibility(View.GONE);
            binding.tvError.setText(error);
            fade(from, binding.rlError);
        }
    }



    private void fade(final View from, final View to) {

        // Set the content view to 0% opacity but visible, so that it is
        // visible but fully transparent during the animation.
        to.setAlpha(0f);
        to.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity and clear any animation
        // listener set on the view.
        to.animate()
                .alpha(1f)
                .setDuration(shortAnimationDuration)
                .setListener(null);

        // Animate the loading view to 0% opacity. After the animation ends,
        // set its visibility to GONE as an optimization step so it doesn't
        // participate in layout passes.
        from.animate()
                .alpha(0f)
                .setDuration(shortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        from.setVisibility(View.GONE);
                    }
                });

    }

}