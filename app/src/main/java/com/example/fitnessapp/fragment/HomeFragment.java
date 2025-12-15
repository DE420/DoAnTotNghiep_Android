package com.example.fitnessapp.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessapp.LoginActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentHomeBinding; // Import ViewBinding
import com.example.fitnessapp.model.request.LogoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding; // Sử dụng ViewBinding
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = RetrofitClient.getApiService();

        binding.buttonLogout.setOnClickListener(v -> {
            handleLogout();
        });
    }

    private void handleLogout() {
        // Lấy refresh token từ SessionManager
        String refreshToken = SessionManager.getInstance(requireActivity()).getRefreshToken();

        if (refreshToken == null) {
            Toast.makeText(getContext(), "No active session found.", Toast.LENGTH_SHORT).show();
            clearUserDataAndNavigateToLogin();
            return;
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        binding.buttonLogout.setEnabled(false);
        binding.buttonLogout.setText("Logging out...");

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
        // Chỉ cần gọi một dòng duy nhất để logout
        SessionManager.getInstance(requireActivity()).logout();

        // Tạo Intent để quay về LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Kết thúc MainActivity
        getActivity().finish();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}