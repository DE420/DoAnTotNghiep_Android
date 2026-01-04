package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentChangePasswordBinding;
import com.example.fitnessapp.viewmodel.ProfileViewModel;

/**
 * Change Password Fragment - MVVM Architecture
 *
 * Allows users to change their password with the following features:
 * - Current password validation
 * - New password validation (8+ characters, letters, numbers, special chars)
 * - Password confirmation matching
 * - Material Design 3 TextInputLayout with password toggle
 * - Loading states
 * - Vietnamese localization
 *
 * Features:
 * - MVVM architecture with ProfileViewModel
 * - Material Design inputs with built-in password toggle
 * - Real-time validation with error display
 * - Loading states
 */
public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    private FragmentChangePasswordBinding binding;
    private ProfileViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
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
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        // Back button
        binding.ibBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> changePassword());
    }

    /**
     * Setup ViewModel observers
     */
    private void setupObservers() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;

            if (Boolean.TRUE.equals(isLoading)) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
                binding.ibBack.setEnabled(false);
            } else {
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
                binding.ibBack.setEnabled(true);
            }
        });

        // Observe password change success
        viewModel.getPasswordChangeSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(),
                        R.string.profile_change_password_success,
                        Toast.LENGTH_SHORT).show();
                viewModel.clearPasswordChangeSuccess();

                // Clear fields
                binding.etCurrentPassword.setText("");
                binding.etNewPassword.setText("");
                binding.etConfirmPassword.setText("");

                // Navigate back after delay
                new Handler().postDelayed(() -> {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }, 1500);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Check for specific error messages
                if (errorMessage.contains("Current password is incorrect") ||
                        errorMessage.contains("Mật khẩu hiện tại không đúng")) {
                    binding.tilCurrentPassword.setError(getString(R.string.profile_current_password_incorrect));
                } else {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                viewModel.clearError();
            }
        });
    }

    /**
     * Validate and change password
     */
    private void changePassword() {
        if (!validateForm()) {
            return;
        }

        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        viewModel.changePassword(currentPassword, newPassword, confirmPassword);
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Clear previous errors
        binding.tilCurrentPassword.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        // Validate current password
        String currentPassword = binding.etCurrentPassword.getText().toString().trim();
        if (currentPassword.isEmpty()) {
            binding.tilCurrentPassword.setError(getString(R.string.profile_name_required));
            isValid = false;
        }

        // Validate new password
        String newPassword = binding.etNewPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError(getString(R.string.profile_name_required));
            isValid = false;
        } else if (newPassword.length() < 8) {
            binding.tilNewPassword.setError(getString(R.string.profile_password_too_short));
            isValid = false;
        } else if (!isPasswordStrong(newPassword)) {
            binding.tilNewPassword.setError(getString(R.string.profile_password_weak));
            isValid = false;
        }

        // Validate confirm password
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(getString(R.string.profile_name_required));
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.profile_password_mismatch));
            isValid = false;
        }

        return isValid;
    }

    /**
     * Check if password is strong enough
     * Must contain at least one letter, one number, and one special character
     */
    private boolean isPasswordStrong(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isWhitespace(c)) {
                hasSpecial = true;
            }
        }

        return hasLetter && hasDigit && hasSpecial;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
