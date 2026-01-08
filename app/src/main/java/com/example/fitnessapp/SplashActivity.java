package com.example.fitnessapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitnessapp.activity.OnboardingActivity;
import com.example.fitnessapp.repository.OnboardingRepository;
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
        SessionManager sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            // Not logged in -> LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Logged in -> Check onboarding status
        OnboardingRepository.getInstance().checkOnboardingStatus(this, new OnboardingRepository.OnboardingStatusCallback() {
            @Override
            public void onSuccess(com.example.fitnessapp.model.response.BasicInfoResponse response) {
                Intent intent;
                if (!response.isOnboardingCompleted()) {
                    // Onboarding not completed -> OnboardingActivity
                    intent = new Intent(SplashActivity.this, OnboardingActivity.class);
                } else {
                    // Onboarding completed -> MainActivity
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                // On error, assume onboarding not completed or go to MainActivity
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}