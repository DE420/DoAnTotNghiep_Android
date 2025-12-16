package com.example.fitnessapp;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.model.constants.Constants;
import com.example.fitnessapp.model.request.RegisterRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.RegisterResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {

    public static final String TAG = RegisterActivity.class.getSimpleName();

    private ImageView imageViewShowPassword, imageViewShowConfirmPassword;
    private View relativeLayoutLoading, containerLayout;
    private TextView textViewLoginNow;
    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPassword, editTextConfirmPassword;

    private TextView textViewEmailWarning;
    private TextView textViewUsernameWarning;
    private TextView textViewPasswordWarning;
    private TextView textViewConfirmPasswordWarning;
    private TextView textViewErrorRegister;

    private AppCompatButton btnRegister;

    private ApiService apiService;

    private ImageView imgBackground;

    private OnBackPressedCallback backPressedCallback;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing → back button disabled
                Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
            }
        };

        setContentView(R.layout.activity_register);
        initViews();
        apiService = RetrofitClient.getApiService();
    }

    private void initViews() {
        containerLayout = findViewById(R.id.main);
        textViewEmailWarning = findViewById(R.id.tv_email_warning);
        textViewUsernameWarning = findViewById(R.id.tv_full_name_warning);
        textViewPasswordWarning = findViewById(R.id.tv_password_warning);
        textViewConfirmPasswordWarning = findViewById(R.id.tv_confirm_password_warning);
        textViewErrorRegister = findViewById(R.id.tv_error_register);
        imgBackground = findViewById(R.id.img_background);

        Glide.with(this)
                .load(R.drawable.login_register_background_compress)
                .into(imgBackground);

        imageViewShowPassword = findViewById(R.id.img_show_password);
        imageViewShowConfirmPassword = findViewById(R.id.img_show_confirm_password);

        relativeLayoutLoading = findViewById(R.id.rl_loading);
        textViewLoginNow = findViewById(R.id.tv_login_now);
        textViewLoginNow.setOnClickListener(this);

        editTextEmail = findViewById(R.id.et_email);
        editTextEmail.setOnFocusChangeListener(this);


        editTextUsername = findViewById(R.id.et_username);
        editTextUsername.setOnFocusChangeListener(this);

        editTextPassword = findViewById(R.id.et_password);
        editTextPassword.setOnFocusChangeListener(this);

        editTextConfirmPassword = findViewById(R.id.et_confirm_password);
        editTextConfirmPassword.setOnFocusChangeListener(this);
        editTextConfirmPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                checkValidConfirmPassword();
                return false;
            }
        });

        btnRegister = findViewById(R.id.btn_register);

        imageViewShowPassword.setOnClickListener(this);
        imageViewShowConfirmPassword.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        relativeLayoutLoading.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.img_show_password) {
            processClickShowPassword();
        } else if (view.getId() == R.id.img_show_confirm_password) {
            processClickShowConfirmPassword();
        } else if (view.getId() == R.id.btn_register) {
            checkValidEmail();
            checkValidUsername();
            checkValidPassword();
            checkValidConfirmPassword();
            if (checkValidEmail() && checkValidUsername()
                && checkValidPassword() && checkValidConfirmPassword()) {
                registerAccount();
            } else {
                Toast.makeText(this, "Input invalid", Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.tv_login_now) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else if(view.getId() == R.id.rl_loading) {

        }
    }

    private void registerAccount() {
        backPressedCallback.setEnabled(true);
        enableLoading();
        clearWarning(textViewErrorRegister);

        RegisterRequest registerRequest = new RegisterRequest.Builder()
                .email(editTextEmail.getText().toString())
                .username(editTextUsername.getText().toString())
                .password(editTextPassword.getText().toString())
                .confirmPassword(editTextConfirmPassword.getText().toString())
                .build();

        apiService.register(registerRequest).enqueue(new Callback<ApiResponse<RegisterResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RegisterResponse>> call, Response<ApiResponse<RegisterResponse>> response) {
                backPressedCallback.setEnabled(false);
                disableLoading();
                if (response.isSuccessful()) {
                    ApiResponse<RegisterResponse> apiResponse = response.body();
                    RegisterResponse registeredUser = apiResponse.getData();
                    Snackbar.make(containerLayout, "Register success\nReturn to login", Snackbar.LENGTH_SHORT)
                            .setBackgroundTint(
                                    getResources()
                                            .getColor(R.color.green_500, null)
                            ).show();
                    new Handler().postDelayed(() -> {
                        RegisterActivity.this.getOnBackPressedDispatcher().onBackPressed();
                    }, 2000);
                    Log.d(TAG, "User: " + registeredUser.getUsername());
                } else {


                    try {
                        String json = response.errorBody().string();

                        Gson gson = new Gson();
                        ApiResponse<String> errorResponse = gson.fromJson(json, ApiResponse.class);

                        String errorMessage = errorResponse.getData();
                        Snackbar.make(containerLayout, errorMessage, Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(
                                        getResources()
                                                .getColor(R.color.red_400, null)
                                ).show();
                        setWarning(textViewErrorRegister, errorMessage);
                        Log.e(TAG, "Lỗi API: " + errorMessage);

                    } catch (Exception e) {
                        Snackbar.make(containerLayout, "Register fail", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(
                                        getResources()
                                                .getColor(R.color.red_400, null)
                                ).show();
                        e.printStackTrace();
                        Log.e(TAG, "Không đọc được lỗi từ server");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RegisterResponse>> call, Throwable t) {
                backPressedCallback.setEnabled(false);
                disableLoading();
                Snackbar.make(containerLayout, "Register fail", Snackbar.LENGTH_SHORT)
                                .setBackgroundTint(
                                        getResources()
                                        .getColor(R.color.red_400, null)
                                ).show();

                Log.e(TAG, Objects.requireNonNull(t.getMessage()));
            }
        });
    }

    public void enableLoading() {
        btnRegister.setEnabled(false);
        relativeLayoutLoading.setVisibility(View.VISIBLE);
    }

    public void disableLoading() {
        btnRegister.setEnabled(true);
        relativeLayoutLoading.setVisibility(View.GONE);
    }

    private void processClickShowConfirmPassword() {
        if (editTextConfirmPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editTextConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageViewShowConfirmPassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            editTextConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageViewShowConfirmPassword.setImageResource(R.drawable.ic_eye_on);
        }
    }

    private void processClickShowPassword() {
        if (editTextPassword.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
            editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            imageViewShowPassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            imageViewShowPassword.setImageResource(R.drawable.ic_eye_on);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (view.getId() == R.id.et_email) {
            if (!hasFocus) {
                checkValidEmail();
            }
        } else if (view.getId() == R.id.et_username) {
            if (!hasFocus) {
                checkValidUsername();
            }
        } else if (view.getId() == R.id.et_password) {
            if (!hasFocus) {
                checkValidPassword();
            }

        } else if (view.getId() == R.id.et_confirm_password) {
            if (!hasFocus) {
                checkValidConfirmPassword();
            }
        }
    }




    private void clearWarning(TextView textViewWarning) {
        textViewWarning.setText("");
        textViewWarning.setVisibility(View.GONE);
    }

    private void setWarning(TextView textViewWarning, String warning) {
        textViewWarning.setText(warning);
        textViewWarning.setVisibility(View.VISIBLE);
    }

    private boolean checkValidEmail() {
        String email = editTextEmail.getText().toString();
        String warningStr = "";
        if (email.isBlank()) {
            warningStr += "Email can't blank.";
        }

        if (!Constants.patternMatches(email, Constants.EMAIL_PATTERN)) {
            if (!warningStr.isEmpty()) {
                warningStr += "\n";
            }
            warningStr += "Email is invalid.";
        }
        if (warningStr.isEmpty()) {
            clearWarning(textViewEmailWarning);
            return true;
        } else {
            setWarning(textViewEmailWarning, warningStr);
            return false;
        }
    }

    private boolean checkValidUsername() {
        String username = editTextUsername.getText().toString();
        String warningStr = "";
        if (username.isBlank()) {
            warningStr += "Full name can't blank.";
        }

        if (username.length() < 3) {
            if (!warningStr.isEmpty()) {
                warningStr += "\n";
            }
            warningStr += "Use at least 3 characters.";
        }

        if (warningStr.isEmpty()) {
            clearWarning(textViewUsernameWarning);
            return true;
        } else {
            setWarning(textViewUsernameWarning, warningStr);
            return false;
        }
    }

    private boolean checkValidPassword() {
        String password = editTextPassword.getText().toString();
        String warningStr = "";
        if (password.isBlank()) {
            warningStr += "Password can't blank.";
        }

        if (!Constants.patternMatches(password, Constants.PASSWORD_PATTERN)) {
            if (!warningStr.isEmpty()) {
                warningStr += "\n";
            }
            warningStr += "Password must be at least 8 characters, including letters, numbers and special characters.";
        }

        if (warningStr.isEmpty()) {
            clearWarning(textViewPasswordWarning);
            return true;
        } else {
            setWarning(textViewPasswordWarning, warningStr);
            return false;
        }
    }

    private boolean checkValidConfirmPassword() {
        String password = editTextPassword.getText().toString();
        String confirmPassword = editTextConfirmPassword.getText().toString();
        String warningStr = "";
        if (confirmPassword.isBlank()) {
            warningStr += "Confirm password can't blank.";
        }

        if (!confirmPassword.equals(password)) {
            if (!warningStr.isEmpty()) {
                warningStr += "\n";
            }
            warningStr += "Password and Confirm Password does not mat.";
        }

        if (warningStr.isEmpty()) {
            clearWarning(textViewConfirmPasswordWarning);
            return true;
        } else {
            setWarning(textViewConfirmPasswordWarning, warningStr);
            return false;
        }
    }
}
