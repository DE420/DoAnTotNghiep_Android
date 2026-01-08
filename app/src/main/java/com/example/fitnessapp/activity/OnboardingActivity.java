package com.example.fitnessapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fitnessapp.MainActivity;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.OnboardingPagerAdapter;
import com.example.fitnessapp.viewmodel.OnboardingViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

public class OnboardingActivity extends AppCompatActivity {
    private OnboardingViewModel viewModel;
    private OnboardingPagerAdapter pagerAdapter;
    private ViewPager2 viewPager;
    private LinearProgressIndicator progressIndicator;
    private TextView tvStepIndicator;
    private MaterialButton btnBack;
    private MaterialButton btnNext;
    private View loadingOverlay;
    private int currentPage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewModel = new ViewModelProvider(this).get(OnboardingViewModel.class);

        initViews();
        setupViewPager();
        setupNavigation();
        observeViewModel();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewpager_onboarding);
        progressIndicator = findViewById(R.id.progress_indicator);
        tvStepIndicator = findViewById(R.id.tv_step_indicator);
        btnBack = findViewById(R.id.btn_back);
        btnNext = findViewById(R.id.btn_next);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }

    private void setupViewPager() {
        pagerAdapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        // FIX: Prevent ViewPager2's internal RecyclerView from stealing focus during IME resize
        // This solves the focus loss issue on Step 2 input fields when keyboard appears with adjustResize
        View child = viewPager.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setFocusable(false);
            child.setFocusableInTouchMode(false);
        }

        // Reduce fragment recreation churn during page changes
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentPage = position;
                updateUI();
            }
        });
    }

    private void setupNavigation() {
        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentPage < 2) {
                    viewPager.setCurrentItem(currentPage + 1, true);
                } else {
                    // Last step, submit
                    viewModel.submitOnboarding();
                }
            }
        });

        btnBack.setOnClickListener(v -> {
            if (currentPage > 0) {
                viewPager.setCurrentItem(currentPage - 1, true);
            }
        });
    }

    private boolean validateCurrentStep() {
        switch (currentPage) {
            case 0:
                if (!viewModel.validateStep1()) {
                    Toast.makeText(this, R.string.onboarding_error_incomplete, Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 1:
                if (!viewModel.validateStep2()) {
                    Toast.makeText(this, R.string.onboarding_error_incomplete, Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
            case 2:
                if (!viewModel.validateStep3()) {
                    Toast.makeText(this, R.string.onboarding_error_incomplete, Toast.LENGTH_SHORT).show();
                    return false;
                }
                break;
        }
        return true;
    }

    private void updateUI() {
        // Update progress indicator
        progressIndicator.setProgress(currentPage + 1);

        // Update step text
        String stepText = getString(R.string.onboarding_step, currentPage + 1, 3);
        tvStepIndicator.setText(stepText);

        // Show/hide back button
        btnBack.setVisibility(currentPage > 0 ? View.VISIBLE : View.GONE);

        // Update next/finish button text
        if (currentPage == 2) {
            btnNext.setText(R.string.onboarding_finish);
        } else {
            btnNext.setText(R.string.onboarding_next);
        }
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            loadingOverlay.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnNext.setEnabled(!isLoading);
            btnBack.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getOnboardingComplete().observe(this, isComplete -> {
            if (isComplete) {
                Toast.makeText(this, R.string.onboarding_success, Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
