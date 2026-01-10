package com.example.fitnessapp;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.databinding.ActivityForgotPasswordBinding;
import com.example.fitnessapp.model.request.ForgotPasswordRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService();

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonSendCode.setOnClickListener(v -> handleSendCode());
        binding.textViewBackToLogin.setOnClickListener(v -> finish()); // Đóng activity hiện tại để quay lại
    }

    private void handleSendCode() {
        String email = binding.editTextEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Vui lòng nhập email hợp lệ");
            binding.editTextEmail.requestFocus();
            return;
        }

        setLoading(true);

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        apiService.forgotPassword(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    // Hiển thị thông báo từ API (thành công hoặc lỗi)
                    Toast.makeText(ForgotPasswordActivity.this, apiResponse.getData(), Toast.LENGTH_LONG).show();

                    if (apiResponse.isStatus()) {
                        // Nếu thành công, có thể quay lại màn hình login sau vài giây
                        // Hoặc chỉ cần hiển thị thông báo là đủ
                    }

                } else {
                    // Xử lý lỗi từ server (ví dụ: email không tồn tại)
                    String errorMessage = "Đã xảy ra lỗi. Vui lòng thử lại.";
                    if (response.errorBody() != null) {
                        try {
                            ApiResponse<?> errorResponse = new Gson().fromJson(response.errorBody().charStream(), ApiResponse.class);
                            errorMessage = errorResponse.getData().toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ForgotPasswordActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.buttonSendCode.setEnabled(false);
            binding.buttonSendCode.setText("ĐANG GỬI...");
        } else {
            binding.buttonSendCode.setEnabled(true);
            binding.buttonSendCode.setText("GỬI MÃ");
        }
    }
}