package com.example.fitnessapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.databinding.ActivityLoginBinding;
import com.example.fitnessapp.model.request.GoogleLoginRequest;
import com.example.fitnessapp.model.request.LoginRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.LoginResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson; // For parsing error bodies

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private ApiService apiService;
    private GoogleSignInClient mGoogleSignInClient;
    private boolean isPasswordVisible = false;

    // ActivityResultLauncher for the Google Sign In intent
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleGoogleSignInResult(task);
                } else {
                    Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService();

//        setupGoogleSignIn();
        setupClickListeners();
    }

//    private void setupGoogleSignIn() {
//        // Configure Google Sign-In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                // You must request an ID token for backend authentication.
//                // Your web client ID from Google Cloud Console.
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//    }

    private void setupClickListeners() {
        binding.buttonLogin.setOnClickListener(v -> handleEmailPasswordLogin());
        binding.buttonGoogle.setOnClickListener(v -> handleGoogleSignIn());
        binding.textViewSignUp.setOnClickListener(v -> {
            // Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Navigate to Sign Up Screen", Toast.LENGTH_SHORT).show();
        });
        binding.textViewForgotPassword.setOnClickListener(v -> {
            // Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Navigate to Forgot Password Screen", Toast.LENGTH_SHORT).show();
        });

        // Password visibility toggle
        binding.editTextPassword.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.editTextPassword.getRight() - binding.editTextPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    togglePasswordVisibility();
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            binding.editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_off, 0);
        } else {
            binding.editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.editTextPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_eye_on, 0); // you need to add an 'ic_eye_on' drawable
        }
        binding.editTextPassword.setSelection(binding.editTextPassword.length());
        isPasswordVisible = !isPasswordVisible;
    }


    private void handleEmailPasswordLogin() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        LoginRequest loginRequest = new LoginRequest(email, password);
        apiService.login(loginRequest).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    LoginResponse loginResponse = response.body().getData();
                    // SUCCESS: Save tokens and navigate to the main screen
                    saveTokens(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
                    navigateToMainApp();
                } else {
                    // Handle API error (e.g., wrong password, user not found)
                    String errorMessage = "Login failed. Please try again.";
                    if (response.errorBody() != null) {
                        try {
                            // Assuming error response is also in ApiResponse format but with a String data
                            ApiResponse<String> errorResponse = new Gson().fromJson(
                                    response.errorBody().charStream(),
                                    ApiResponse.class
                            );
                            if (errorResponse != null && errorResponse.getData() != null) {
                                errorMessage = errorResponse.getData();
                            }
                        } catch (Exception e) {
                            Log.e("LoginActivity", "Error parsing error body", e);
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Log.e("LoginActivity", "Network request failed", t);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleGoogleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            if (idToken != null) {
                // Send ID token to your backend
                sendGoogleTokenToBackend(idToken);
            } else {
                Toast.makeText(this, "Failed to get Google ID Token.", Toast.LENGTH_SHORT).show();
            }

        } catch (ApiException e) {
            Log.w("LoginActivity", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google Sign-In failed. Code: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendGoogleTokenToBackend(String idToken) {
        setLoading(true);
        GoogleLoginRequest request = new GoogleLoginRequest(idToken);
        apiService.loginWithGoogle(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    LoginResponse loginResponse = response.body().getData();
                    saveTokens(loginResponse.getAccessToken(), loginResponse.getRefreshToken());
                    navigateToMainApp();
                } else {
                    Toast.makeText(LoginActivity.this, "Google authentication with backend failed.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                setLoading(false);
                Log.e("LoginActivity", "Google Auth Network request failed", t);
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.buttonLogin.setEnabled(false);
            binding.buttonGoogle.setEnabled(false);
            // Optionally, show a ProgressBar
        } else {
            binding.buttonLogin.setEnabled(true);
            binding.buttonGoogle.setEnabled(true);
            // Optionally, hide the ProgressBar
        }
    }

    private void saveTokens(String accessToken, String refreshToken) {
        // Use SharedPreferences to store tokens securely
        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .edit()
                .putString("ACCESS_TOKEN", accessToken)
                .putString("REFRESH_TOKEN", refreshToken)
                .apply();
        Log.d("LoginActivity", "Tokens saved successfully.");
    }

    private void navigateToMainApp() {
        // Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // startActivity(intent);
        Toast.makeText(this, "Login Successful! Navigating to main app...", Toast.LENGTH_SHORT).show();
        finish(); // Finish LoginActivity so user can't go back to it
    }
}