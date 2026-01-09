package com.example.fitnessapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.fitnessapp.LoginActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentConfirmLogoutDialogBinding;
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.AuthApi;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Confirm Logout Dialog Fragment - Material Design
 *
 * Displays a confirmation dialog when user wants to logout with:
 * - Material Design CardView with rounded corners
 * - Red logout icon in circular background
 * - Vietnamese title and message
 * - Cancel (gray) and Logout (red) buttons
 * - Loading state during logout API call
 *
 * Features:
 * - Material Design 3 components
 * - Vietnamese localization
 * - API call to logout endpoint
 * - Session cleanup on logout
 * - Loading overlay during API call
 */
public class ConfirmLogoutDialogFragment extends DialogFragment {

    private FragmentConfirmLogoutDialogBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConfirmLogoutDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup click listeners
        setupClickListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Make dialog background transparent to show rounded corners
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Setup button click listeners
     */
    private void setupClickListeners() {
        // Cancel button - just dismiss dialog
        binding.btnCancelLogout.setOnClickListener(v -> dismiss());

        // Confirm logout button - call logout API
        binding.btnConfirmLogout.setOnClickListener(v -> performLogout());
    }

    /**
     * Perform logout operation
     */
    private void performLogout() {
        // Show loading state
        showLoading();

        // Disable buttons during logout
        binding.btnConfirmLogout.setEnabled(false);
        binding.btnCancelLogout.setEnabled(false);

        String refreshToken = SessionManager.getInstance(requireActivity()).getRefreshToken();

        if (refreshToken == null) {
            Toast.makeText(requireContext(), "No active session found.", Toast.LENGTH_SHORT).show();
            clearUserDataAndNavigateToLogin();
            return;
        }

        // Call logout API
        LogoutRequest logoutRequest = new LogoutRequest(refreshToken);
        AuthApi authApi = RetrofitClient.getAuthApi();

        authApi.logout(logoutRequest).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Even if API fails, still logout locally
                Toast.makeText(requireContext(), "Đã đăng xuất (offline)", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
            }
        });
    }

    /**
     * Show loading overlay
     */
    private void showLoading() {
        if (binding == null) return;

        binding.clContent.setVisibility(View.INVISIBLE);
        binding.rlLoading.setVisibility(View.VISIBLE);
    }

    /**
     * Clear user session data and navigate to login screen
     */
    private void clearUserDataAndNavigateToLogin() {
        // Clear session
        SessionManager.getInstance(requireActivity()).logout();

        // Navigate to login
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish current activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
