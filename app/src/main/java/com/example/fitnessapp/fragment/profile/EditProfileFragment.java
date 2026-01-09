package com.example.fitnessapp.fragment.profile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.ActivityLevelAdapter;
import com.example.fitnessapp.adapter.FitnessGoalAdapter;
import com.example.fitnessapp.databinding.FragmentEditProfileBinding;
import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.UpdateProfileRequest;
import com.example.fitnessapp.util.DateUtil;
import com.example.fitnessapp.viewmodel.ProfileViewModel;
import com.example.fitnessapp.worker.ProfileUpdateWorker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Edit Profile Fragment - MVVM Architecture
 *
 * Allows users to edit their profile information including:
 * - Avatar photo
 * - Personal details (name, weight, height, birthday)
 * - Fitness preferences (goal, activity level)
 *
 * Features:
 * - MVVM architecture with ProfileViewModel
 * - Material Design inputs
 * - Form validation
 * - Avatar upload from gallery
 * - Loading states
 */
public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    private FragmentEditProfileBinding binding;
    private ProfileViewModel viewModel;
    private UpdateProfileRequest currentProfile;

    // Avatar handling
    private Uri selectedAvatarUri;
    private boolean avatarChanged = false;

    // Date picker
    private DatePickerDialog datePickerDialog;
    private Calendar selectedDate;

    // Spinners
    private List<ActivityLevel> activityLevelList;
    private List<FitnessGoal> fitnessGoalList;
    private ActivityLevel selectedActivityLevel;
    private FitnessGoal selectedFitnessGoal;

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            if (uri != null) {
                                handleImageSelected(uri);
                            }
                        }
                    }
            );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get profile data from arguments
        if (getArguments() != null) {
            currentProfile = (UpdateProfileRequest) getArguments()
                    .getSerializable(UpdateProfileRequest.KEY_UPDATE_PROFILE_REQUEST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Setup UI
        setupViews();
        setupTextWatchers();
        setupClickListeners();

        // Load current profile data
        populateFields();
    }

    /**
     * Setup all views and adapters
     */
    private void setupViews() {
        // Setup Activity Level Spinner
        activityLevelList = new ArrayList<>(Arrays.asList(ActivityLevel.values()));
        ActivityLevelAdapter activityLevelAdapter =
                new ActivityLevelAdapter(requireContext(), activityLevelList);
        binding.spinnerActivityLevel.setAdapter(activityLevelAdapter);

        binding.spinnerActivityLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedActivityLevel = activityLevelList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup Fitness Goal Spinner
        fitnessGoalList = new ArrayList<>(Arrays.asList(FitnessGoal.values()));
        FitnessGoalAdapter fitnessGoalAdapter =
                new FitnessGoalAdapter(requireContext(), fitnessGoalList);
        binding.spinnerGoal.setAdapter(fitnessGoalAdapter);

        binding.spinnerGoal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFitnessGoal = fitnessGoalList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup Date Picker
        selectedDate = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    updateBirthdayField();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        // Set max date to today (can't be born in future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // FIX: Force white hint color for TextInputLayouts (both focused and unfocused states)
        int whiteColor = ContextCompat.getColor(requireContext(), R.color.white);
        ColorStateList whiteColorStateList = ColorStateList.valueOf(whiteColor);

        binding.tilName.setDefaultHintTextColor(whiteColorStateList);
        binding.tilName.setHintTextColor(whiteColorStateList);

        binding.tilWeight.setDefaultHintTextColor(whiteColorStateList);
        binding.tilWeight.setHintTextColor(whiteColorStateList);

        binding.tilHeight.setDefaultHintTextColor(whiteColorStateList);
        binding.tilHeight.setHintTextColor(whiteColorStateList);

        binding.tilBirthday.setDefaultHintTextColor(whiteColorStateList);
        binding.tilBirthday.setHintTextColor(whiteColorStateList);
    }

    /**
     * Setup real-time validation with TextWatchers
     */
    private void setupTextWatchers() {
        // Name TextWatcher
        binding.etName.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String name = s.toString().trim();
                if (name.isEmpty()) {
                    binding.tilName.setError(getString(R.string.profile_name_required));
                } else if (name.length() < 2) {
                    binding.tilName.setError("Tên phải có ít nhất 2 ký tự");
                } else if (name.length() > 100) {
                    binding.tilName.setError("Tên quá dài (tối đa 100 ký tự)");
                } else {
                    binding.tilName.setError(null);
                }
            }
        });

        // Weight TextWatcher
        binding.etWeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String weightStr = s.toString().trim();
                if (!weightStr.isEmpty()) {
                    try {
                        double weight = Double.parseDouble(weightStr);
                        if (weight <= 0 || weight > 500) {
                            binding.tilWeight.setError(getString(R.string.profile_weight_invalid));
                        } else {
                            binding.tilWeight.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        binding.tilWeight.setError(getString(R.string.profile_weight_invalid));
                    }
                } else {
                    binding.tilWeight.setError(null);
                }
            }
        });

        // Height TextWatcher
        binding.etHeight.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String heightStr = s.toString().trim();
                if (!heightStr.isEmpty()) {
                    try {
                        double heightCm = Double.parseDouble(heightStr);
                        if (heightCm <= 0 || heightCm > 300) {
                            binding.tilHeight.setError(getString(R.string.profile_height_invalid));
                        } else {
                            binding.tilHeight.setError(null);
                        }
                    } catch (NumberFormatException e) {
                        binding.tilHeight.setError(getString(R.string.profile_height_invalid));
                    }
                } else {
                    binding.tilHeight.setError(null);
                }
            }
        });

        // Birthday TextWatcher (validates when text changes from DatePicker)
        binding.etBirthday.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                String birthdayStr = s.toString().trim();
                if (!birthdayStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        sdf.setLenient(false);
                        Date birthday = sdf.parse(birthdayStr);

                        if (birthday != null) {
                            // Check if birthday is not in the future
                            Date today = new Date();
                            if (birthday.after(today)) {
                                binding.tilBirthday.setError("Ngày sinh không thể là ngày trong tương lai");
                            } else {
                                // Check age range (5-150 years)
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(birthday);
                                int birthYear = cal.get(Calendar.YEAR);
                                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                                int age = currentYear - birthYear;

                                if (age > 150 || age < 0) {
                                    binding.tilBirthday.setError("Ngày sinh không hợp lệ");
                                } else {
                                    binding.tilBirthday.setError(null);
                                }
                            }
                        }
                    } catch (ParseException e) {
                        binding.tilBirthday.setError("Định dạng ngày sinh không hợp lệ (dd/MM/yyyy)");
                    }
                } else {
                    binding.tilBirthday.setError(null);
                }
            }
        });
    }

    /**
     * Setup click listeners
     */
    private void setupClickListeners() {
        // Back button
        binding.ibBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> saveProfile());

        // Change avatar button
        binding.btnChangeAvatar.setOnClickListener(v -> showAvatarOptions());

        // Birthday field (opens date picker)
        binding.etBirthday.setOnClickListener(v -> datePickerDialog.show());
        binding.tilBirthday.setEndIconOnClickListener(v -> datePickerDialog.show());
    }

    /**
     * Populate fields with current profile data
     */
    @SuppressLint("SetTextI18n")
    private void populateFields() {
        if (currentProfile == null) {
            return;
        }

        // Load avatar
        if (currentProfile.getAvatar() != null) {
            Glide.with(this)
                    .load(currentProfile.getAvatar())
                    .error(R.drawable.img_user_default_128)
                    .into(binding.ivAvatar);
        }

        // Name
        if (currentProfile.getName() != null) {
            binding.etName.setText(currentProfile.getName());
        }

        // Weight
        if (currentProfile.getWeight() != null) {
            // Use Locale.US to ensure period decimal separator (70.50 not 70,50)
            binding.etWeight.setText(String.format(Locale.US, "%.2f", currentProfile.getWeight()));
        }

        // Height (in centimeters)
        if (currentProfile.getHeight() != null) {
            // Display height in centimeters
            binding.etHeight.setText(String.format(Locale.US, "%.0f", currentProfile.getHeight()));
        }

        // Birthday
        if (currentProfile.getDateOfBirth() != null) {
            try {
                // Try parsing with dd/MM/yyyy format first (server format)
                SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DD_MM_YYYY_DATE_FORMAT, Locale.getDefault());
                Date date = sdf.parse(currentProfile.getDateOfBirth());
                if (date != null) {
                    selectedDate.setTimeInMillis(date.getTime());
                    updateBirthdayField();
                }
            } catch (ParseException e) {
                // If that fails, try yyyy-MM-dd format
                try {
                    SimpleDateFormat sdf2 = new SimpleDateFormat(DateUtil.YYYY_MM_DD_DATE_FORMAT, Locale.getDefault());
                    Date date = sdf2.parse(currentProfile.getDateOfBirth());
                    if (date != null) {
                        selectedDate.setTimeInMillis(date.getTime());
                        updateBirthdayField();
                    }
                } catch (ParseException e2) {
                    Log.e(TAG, "Error parsing birthday: " + e.getMessage());
                }
            }
        }

        // Activity Level
        if (currentProfile.getActivityLevel() != null) {
            selectedActivityLevel = currentProfile.getActivityLevel();
            int position = activityLevelList.indexOf(currentProfile.getActivityLevel());
            if (position >= 0) {
                binding.spinnerActivityLevel.setSelection(position);
            }
        }

        // Fitness Goal
        if (currentProfile.getFitnessGoal() != null) {
            selectedFitnessGoal = currentProfile.getFitnessGoal();
            int position = fitnessGoalList.indexOf(currentProfile.getFitnessGoal());
            if (position >= 0) {
                binding.spinnerGoal.setSelection(position);
            }
        }
    }

    /**
     * Update birthday field display
     */
    private void updateBirthdayField() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DD_MM_YYYY_DATE_FORMAT, Locale.getDefault());
        String formatted = sdf.format(selectedDate.getTime());
        binding.etBirthday.setText(formatted);
    }

    /**
     * Show avatar selection options
     */
    private void showAvatarOptions() {
        String[] options = {
                getString(R.string.profile_select_photo)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.profile_change_avatar)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openGallery();
                    }
                })
                .show();
    }

    /**
     * Open gallery to select image
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * Handle image selection from gallery
     */
    private void handleImageSelected(Uri uri) {
        // Persist URI permission for API < 33
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            try {
                requireContext().getContentResolver()
                        .takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
            } catch (SecurityException e) {
                Log.w(TAG, "Could not persist URI permission: " + e.getMessage());
            }
        }

        selectedAvatarUri = uri;
        avatarChanged = true;

        // Display selected image
        Glide.with(this)
                .load(uri)
                .error(R.drawable.img_user_default_128)
                .into(binding.ivAvatar);
    }

    /**
     * Validate form inputs
     */
    private boolean validateForm() {
        boolean isValid = true;

        // Clear previous errors
        binding.tilName.setError(null);
        binding.tilWeight.setError(null);
        binding.tilHeight.setError(null);
        binding.tilBirthday.setError(null);

        // Validate name
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.profile_name_required));
            isValid = false;
        } else if (name.length() < 2) {
            binding.tilName.setError("Tên phải có ít nhất 2 ký tự");
            isValid = false;
        } else if (name.length() > 100) {
            binding.tilName.setError("Tên quá dài (tối đa 100 ký tự)");
            isValid = false;
        }

        // Validate weight
        String weightStr = binding.etWeight.getText().toString().trim();
        if (!weightStr.isEmpty()) {
            try {
                double weight = Double.parseDouble(weightStr);
                if (weight <= 0 || weight > 500) {
                    binding.tilWeight.setError(getString(R.string.profile_weight_invalid));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.tilWeight.setError(getString(R.string.profile_weight_invalid));
                isValid = false;
            }
        }

        // Validate height (in centimeters)
        String heightStr = binding.etHeight.getText().toString().trim();
        if (!heightStr.isEmpty()) {
            try {
                double heightCm = Double.parseDouble(heightStr);
                if (heightCm <= 0 || heightCm > 300) {
                    binding.tilHeight.setError(getString(R.string.profile_height_invalid));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                binding.tilHeight.setError(getString(R.string.profile_height_invalid));
                isValid = false;
            }
        }

        // Validate birthday
        String birthdayStr = binding.etBirthday.getText().toString().trim();
        if (!birthdayStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                sdf.setLenient(false);
                Date birthday = sdf.parse(birthdayStr);

                if (birthday != null) {
                    // Check if birthday is not in the future
                    Date today = new Date();
                    if (birthday.after(today)) {
                        binding.tilBirthday.setError("Ngày sinh không thể là ngày trong tương lai");
                        isValid = false;
                    }

                    // Check if age is reasonable (between 5 and 150 years old)
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(birthday);
                    int birthYear = cal.get(Calendar.YEAR);
                    int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                    int age = currentYear - birthYear;

                    if (age > 150 || age < 0) {
                        binding.tilBirthday.setError("Ngày sinh không hợp lệ");
                        isValid = false;
                    }
                }
            } catch (ParseException e) {
                binding.tilBirthday.setError("Định dạng ngày sinh không hợp lệ (dd/MM/yyyy)");
                isValid = false;
            }
        }

        return isValid;
    }

    /**
     * Save profile in background using WorkManager
     */
    private void saveProfile() {
        if (!validateForm()) {
            return;
        }

        try {
            // Prepare data for WorkManager
            Data.Builder dataBuilder = new Data.Builder();

            // Name
            String name = binding.etName.getText().toString().trim();
            dataBuilder.putString(ProfileUpdateWorker.KEY_NAME, name);

            // Weight
            String weightStr = binding.etWeight.getText().toString().trim();
            if (!weightStr.isEmpty()) {
                dataBuilder.putString(ProfileUpdateWorker.KEY_WEIGHT, weightStr);
            }

            // Height (in centimeters, send as-is to backend)
            String heightStr = binding.etHeight.getText().toString().trim();
            if (!heightStr.isEmpty()) {
                dataBuilder.putString(ProfileUpdateWorker.KEY_HEIGHT, heightStr);
            }

            // Birthday
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String birthday = sdf.format(selectedDate.getTime());
            dataBuilder.putString(ProfileUpdateWorker.KEY_DATE_OF_BIRTH, birthday);

            // Activity Level
            if (selectedActivityLevel != null) {
                dataBuilder.putString(ProfileUpdateWorker.KEY_ACTIVITY_LEVEL, selectedActivityLevel.name());
            }

            // Fitness Goal
            if (selectedFitnessGoal != null) {
                dataBuilder.putString(ProfileUpdateWorker.KEY_FITNESS_GOAL, selectedFitnessGoal.name());
            }

            // Handle avatar
            if (avatarChanged && selectedAvatarUri != null) {
                // Upload with new avatar file
                dataBuilder.putString(ProfileUpdateWorker.KEY_AVATAR_URI, selectedAvatarUri.toString());
                dataBuilder.putBoolean(ProfileUpdateWorker.KEY_HAS_NEW_AVATAR, true);
            } else if (currentProfile != null && currentProfile.getAvatar() != null) {
                // Keep existing avatar URL
                dataBuilder.putString(ProfileUpdateWorker.KEY_AVATAR_URL, currentProfile.getAvatar());
                dataBuilder.putBoolean(ProfileUpdateWorker.KEY_HAS_NEW_AVATAR, false);
            } else {
                // No avatar
                dataBuilder.putBoolean(ProfileUpdateWorker.KEY_HAS_NEW_AVATAR, false);
            }

            // Create constraints (require network)
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Create work request
            OneTimeWorkRequest updateRequest = new OneTimeWorkRequest.Builder(ProfileUpdateWorker.class)
                    .setInputData(dataBuilder.build())
                    .setConstraints(constraints)
                    .build();

            // Enqueue work
            WorkManager.getInstance(requireContext()).enqueue(updateRequest);

            // Show confirmation toast and navigate back
            Toast.makeText(requireContext(),
                    "Đang cập nhật hồ sơ trong nền...",
                    Toast.LENGTH_SHORT).show();

            // Navigate back to profile screen
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error preparing profile update: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                    "Lỗi khi chuẩn bị cập nhật hồ sơ",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
