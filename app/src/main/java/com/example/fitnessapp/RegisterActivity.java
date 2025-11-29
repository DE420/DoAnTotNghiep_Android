package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.databinding.ActivityRegisterBinding;
import com.example.fitnessapp.model.response.ApiResponse; // Make sure this is the generic one
import com.example.fitnessapp.model.request.RegisterRequest;
import com.example.fitnessapp.model.response.RegisterResponse; // Import the new model
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService();

        binding.buttonRegister.setOnClickListener(v -> handleRegister());

        binding.textViewLogin.setOnClickListener(v -> {
            // Tạo Intent để mở LoginActivity
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegister() {
        String email = binding.editTextEmail.getText().toString().trim();
        String username = binding.editTextUsername.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString().trim();

        if (!validateInput(email, username, password, confirmPassword)) {
            return;
        }

        binding.buttonRegister.setEnabled(false);
        binding.buttonRegister.setText("Registering...");

        RegisterRequest registerRequest = new RegisterRequest(email, username, password, confirmPassword);

        // --- The generic type here is now Call<ApiResponse<Object>> ---
        apiService.registerUser(registerRequest).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                resetButtonState();

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.isStatus()) {
                        // --- SUCCESS ---
                        // The data is an object (likely a LinkedTreeMap), so we convert it to our model
                        Gson gson = new Gson();
                        RegisterResponse registeredUser = gson.fromJson(gson.toJson(apiResponse.getData()), RegisterResponse.class);

                        String successMessage = "Welcome, " + registeredUser.getUsername() + "! Please log in.";
                        Toast.makeText(RegisterActivity.this, successMessage, Toast.LENGTH_LONG).show();

                        // Navigate to Login screen
                        // Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        // startActivity(intent);
                        // finish();

                    } else {
                        // --- BACKEND VALIDATION ERROR ---
                        // Here, we know the data is a String.
                        String errorMessage = apiResponse.getData().toString();
                        Toast.makeText(RegisterActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // --- API ERROR (e.g., 400 Bad Request) ---
                    try {
                        String errorBody = response.errorBody().string();
                        ApiResponse<?> errorResponse = new Gson().fromJson(errorBody, ApiResponse.class);
                        Toast.makeText(RegisterActivity.this, "Error: " + errorResponse.getData().toString(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "An error occurred: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                // --- NETWORK ERROR ---
                resetButtonState();
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private boolean validateInput(String email, String username, String password, String confirmPassword) {
        // (Validation logic remains the same as before)
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Please enter a valid email");
            binding.editTextEmail.requestFocus();
            return false;
        }
        if (username.length() < 3) {
            binding.editTextUsername.setError("Username must be at least 3 characters");
            binding.editTextUsername.requestFocus();
            return false;
        }
        if (password.length() < 8) {
            binding.editTextPassword.setError("Password must be at least 8 characters");
            binding.editTextPassword.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.editTextConfirmPassword.setError("Passwords do not match");
            binding.editTextConfirmPassword.requestFocus();
            return false;
        }
        return true;
    }

    private void resetButtonState() {
        binding.buttonRegister.setEnabled(true);
        binding.buttonRegister.setText("Register");
    }
}