package com.example.fitnessapp.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitnessapp.R;
import com.example.fitnessapp.viewmodel.OnboardingViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OnboardingStep1Fragment extends Fragment {
    private OnboardingViewModel viewModel;
    private MaterialCardView cardMale;
    private MaterialCardView cardFemale;
    private TextInputEditText etDob;
    private String selectedSex = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_step1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        initViews(view);
        setupGenderSelection();
        setupDatePicker();
        observeViewModel();
    }

    private void initViews(View view) {
        cardMale = view.findViewById(R.id.card_male);
        cardFemale = view.findViewById(R.id.card_female);
        etDob = view.findViewById(R.id.et_dob);
    }

    private void setupGenderSelection() {
        cardMale.setOnClickListener(v -> selectGender("MALE", cardMale, cardFemale));
        cardFemale.setOnClickListener(v -> selectGender("FEMALE", cardFemale, cardMale));
    }

    private void selectGender(String sex, MaterialCardView selected, MaterialCardView unselected) {
        selectedSex = sex;
        viewModel.setSex(sex);

        // Highlight selected card
        selected.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.yellow));
        selected.setStrokeWidth(4);

        // Unhighlight unselected card
        unselected.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.gray_400));
        unselected.setStrokeWidth(2);
    }

    private void setupDatePicker() {
        etDob.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -18); // Default to 18 years ago

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, year, month, dayOfMonth) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, dayOfMonth);
                Date date = selected.getTime();

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDob.setText(sdf.format(date));

                viewModel.setDateOfBirth(date);
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void observeViewModel() {
        viewModel.getSex().observe(getViewLifecycleOwner(), sex -> {
            if (sex != null && !sex.equals(selectedSex)) {
                // Only update UI if sex changed from outside (prevents infinite loop)
                selectedSex = sex;
                updateGenderUI(sex);
            }
        });

        viewModel.getDateOfBirth().observe(getViewLifecycleOwner(), date -> {
            if (date != null && etDob != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDob.setText(sdf.format(date));
            }
        });
    }

    private void updateGenderUI(String sex) {
        if (sex.equals("MALE")) {
            cardMale.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.yellow));
            cardMale.setStrokeWidth(4);
            cardFemale.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.gray_400));
            cardFemale.setStrokeWidth(2);
        } else {
            cardFemale.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.yellow));
            cardFemale.setStrokeWidth(4);
            cardMale.setStrokeColor(ContextCompat.getColor(requireContext(), R.color.gray_400));
            cardMale.setStrokeWidth(2);
        }
    }
}
