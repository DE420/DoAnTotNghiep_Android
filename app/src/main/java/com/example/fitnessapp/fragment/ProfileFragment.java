package com.example.fitnessapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessapp.LoginActivity;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.databinding.FragmentProfileBinding;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getApiService();

        // TODO: Load user data from API and set to views
        // binding.textName.setText(...);
        // Glide.with(this).load(user.getAvatarUrl()).into(binding.imageAvatar);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to EditProfileFragment or Activity
        });

        binding.buttonChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Password Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ChangePasswordFragment or Activity
        });

        binding.buttonLogout.setOnClickListener(v -> {
            handleLogout();
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