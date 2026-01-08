package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessapp.R;
import com.example.fitnessapp.viewmodel.OnboardingViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class OnboardingStep2Fragment extends Fragment {
    private OnboardingViewModel viewModel;
    private TextInputEditText etWeight;
    private TextInputEditText etHeight;
    private TextInputLayout tilWeight;
    private TextInputLayout tilHeight;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_step2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        initViews(view);
        setupInputListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        etWeight = view.findViewById(R.id.et_weight);
        etHeight = view.findViewById(R.id.et_height);
        tilWeight = view.findViewById(R.id.til_weight);
        tilHeight = view.findViewById(R.id.til_height);
    }

    private void setupInputListeners() {
        etWeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    try {
                        double weight = Double.parseDouble(s.toString());
                        viewModel.setWeight(weight);
                        tilWeight.setError(null);
                    } catch (NumberFormatException e) {
                        tilWeight.setError(getString(R.string.onboarding_error_weight));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etHeight.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    try {
                        // User inputs in cm, convert to meters
                        double heightCm = Double.parseDouble(s.toString());
                        double heightM = heightCm / 100.0;
                        viewModel.setHeight(heightM);
                        tilHeight.setError(null);
                    } catch (NumberFormatException e) {
                        tilHeight.setError(getString(R.string.onboarding_error_height));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        viewModel.getWeight().observe(getViewLifecycleOwner(), weight -> {
            if (weight != null && etWeight != null && etWeight.getText().toString().isEmpty()) {
                etWeight.setText(String.valueOf(weight));
            }
        });

        viewModel.getHeight().observe(getViewLifecycleOwner(), height -> {
            if (height != null && etHeight != null && etHeight.getText().toString().isEmpty()) {
                // Convert meters back to cm for display
                double heightCm = height * 100.0;
                etHeight.setText(String.valueOf((int) heightCm));
            }
        });
    }
}
