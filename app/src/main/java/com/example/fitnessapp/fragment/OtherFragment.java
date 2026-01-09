package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.FragmentOtherBinding;

public class OtherFragment extends Fragment {
    private FragmentOtherBinding binding;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonChallenge.setOnClickListener(v -> Toast.makeText(getContext(), "Challenge", Toast.LENGTH_SHORT).show());

        binding.buttonNutrition.setOnClickListener(v -> {
            loadFragmentOther(new com.example.fitnessapp.fragment.nutrition.NutritionMainFragment());
        });

        binding.buttonExercises.setOnClickListener(v -> {
            loadFragmentOther(new ExercisesFragment());
        });

        binding.buttonStatistics.setOnClickListener(v -> {
            loadFragmentOther(new StatisticsFragment());
        });

        binding.buttonLeaderboard.setOnClickListener(v -> Toast.makeText(getContext(), "Leaderboard", Toast.LENGTH_SHORT).show());
    }

    private void loadFragmentOther(Fragment fragment) {
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}