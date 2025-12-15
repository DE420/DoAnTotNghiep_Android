package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.session.SessionManager;

// Using SuppressLint for a known visual lint issue with splash screens in recent IDEs.
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::decideNextActivity, SPLASH_TIME_OUT);
    }

    private void decideNextActivity() {
        // Lấy instance của SessionManager
        SessionManager sessionManager = SessionManager.getInstance(this);

        Intent intent;
        if (sessionManager.isLoggedIn()) { // <-- Logic kiểm tra đơn giản hơn nhiều
            // User has a token, so they are logged in. Go to MainActivity.
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // No token found. User is not logged in. Go to LoginActivity.
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}