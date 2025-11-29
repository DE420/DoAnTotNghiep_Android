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
        // Lấy refresh token đã lưu. Backend cần token này để vô hiệu hóa nó.
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String refreshToken = sharedPreferences.getString("refresh_token", null);

        if (refreshToken == null) {
            Toast.makeText(getContext(), "No active session found.", Toast.LENGTH_SHORT).show();
            // Nếu không có token, vẫn nên xóa dữ liệu và quay về login
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
                // Dù thành công hay thất bại, chúng ta đều đăng xuất người dùng ở client
                Toast.makeText(getContext(), "Logged out successfully.", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                // Nếu có lỗi mạng, vẫn đăng xuất người dùng ở client
                Toast.makeText(getContext(), "Logged out (offline).", Toast.LENGTH_SHORT).show();
                clearUserDataAndNavigateToLogin();
            }
        });
    }

    private void clearUserDataAndNavigateToLogin() {
        // Xóa toàn bộ dữ liệu trong SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Tạo Intent để quay về LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);

        // Các flags này rất quan trọng:
        // FLAG_ACTIVITY_NEW_TASK: Bắt đầu một task mới.
        // FLAG_ACTIVITY_CLEAR_TASK: Xóa tất cả các activity trong task cũ (bao gồm cả MainActivity).
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