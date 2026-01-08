package com.example.fitnessapp.fragment.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessapp.R;
import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.viewmodel.OnboardingViewModel;

public class OnboardingStep3Fragment extends Fragment {
    private OnboardingViewModel viewModel;
    private RadioGroup rgFitnessGoal;
    private RadioGroup rgActivityLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_step3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        initViews(view);
        setupFitnessGoalListeners();
        setupActivityLevelListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        rgFitnessGoal = view.findViewById(R.id.rg_fitness_goal);
        rgActivityLevel = view.findViewById(R.id.rg_activity_level);
    }

    private void setupFitnessGoalListeners() {
        rgFitnessGoal.setOnCheckedChangeListener((group, checkedId) -> {
            FitnessGoal goal = null;

            if (checkedId == R.id.rb_lose_weight) {
                goal = FitnessGoal.LOSE_WEIGHT;
            } else if (checkedId == R.id.rb_gain_weight) {
                goal = FitnessGoal.GAIN_WEIGHT;
            } else if (checkedId == R.id.rb_muscle_gain) {
                goal = FitnessGoal.MUSCLE_GAIN;
            } else if (checkedId == R.id.rb_shape_body) {
                goal = FitnessGoal.SHAPE_BODY;
            } else if (checkedId == R.id.rb_others) {
                goal = FitnessGoal.OTHERS;
            }

            viewModel.setFitnessGoal(goal);
        });
    }

    private void setupActivityLevelListeners() {
        rgActivityLevel.setOnCheckedChangeListener((group, checkedId) -> {
            ActivityLevel level = null;

            if (checkedId == R.id.rb_sedentary) {
                level = ActivityLevel.SEDENTARY;
            } else if (checkedId == R.id.rb_lightly) {
                level = ActivityLevel.LIGHTLY_ACTIVE;
            } else if (checkedId == R.id.rb_moderately) {
                level = ActivityLevel.MODERATELY_ACTIVE;
            } else if (checkedId == R.id.rb_very) {
                level = ActivityLevel.VERY_ACTIVE;
            } else if (checkedId == R.id.rb_extra) {
                level = ActivityLevel.EXTRA_ACTIVE;
            }

            viewModel.setActivityLevel(level);
        });
    }

    private void observeViewModel() {
        viewModel.getFitnessGoal().observe(getViewLifecycleOwner(), goal -> {
            if (goal != null && rgFitnessGoal != null) {
                switch (goal) {
                    case LOSE_WEIGHT:
                        rgFitnessGoal.check(R.id.rb_lose_weight);
                        break;
                    case GAIN_WEIGHT:
                        rgFitnessGoal.check(R.id.rb_gain_weight);
                        break;
                    case MUSCLE_GAIN:
                        rgFitnessGoal.check(R.id.rb_muscle_gain);
                        break;
                    case SHAPE_BODY:
                        rgFitnessGoal.check(R.id.rb_shape_body);
                        break;
                    case OTHERS:
                        rgFitnessGoal.check(R.id.rb_others);
                        break;
                }
            }
        });

        viewModel.getActivityLevel().observe(getViewLifecycleOwner(), level -> {
            if (level != null && rgActivityLevel != null) {
                switch (level) {
                    case SEDENTARY:
                        rgActivityLevel.check(R.id.rb_sedentary);
                        break;
                    case LIGHTLY_ACTIVE:
                        rgActivityLevel.check(R.id.rb_lightly);
                        break;
                    case MODERATELY_ACTIVE:
                        rgActivityLevel.check(R.id.rb_moderately);
                        break;
                    case VERY_ACTIVE:
                        rgActivityLevel.check(R.id.rb_very);
                        break;
                    case EXTRA_ACTIVE:
                        rgActivityLevel.check(R.id.rb_extra);
                        break;
                }
            }
        });
    }
}
