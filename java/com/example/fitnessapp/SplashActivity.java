package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

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
        // Check if the user is logged in by looking for the saved access token
        SharedPreferences sharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String accessToken = sharedPreferences.getString("ACCESS_TOKEN", null);

        Intent intent;
        if (accessToken != null && !accessToken.isEmpty()) {
            // User has a token, so they are logged in. Go to MainActivity.
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // No token found. User is not logged in. Go to LoginActivity.
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        // Start the determined activity
        startActivity(intent);

        // Close this activity so the user can't navigate back to it
        finish();
    }
}