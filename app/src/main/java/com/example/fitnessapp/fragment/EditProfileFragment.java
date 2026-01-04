package com.example.fitnessapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.adapter.ActivityLevelAdapter;
import com.example.fitnessapp.adapter.FitnessGoalAdapter;
import com.example.fitnessapp.databinding.FragmentEditProfileBinding;
import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.UpdateProfileRequest;
import com.example.fitnessapp.repository.ProfileRepository;
import com.example.fitnessapp.util.DateUtil;
import com.example.fitnessapp.viewmodel.ProfileViewModel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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
        setupClickListeners();
        setupObservers();

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
     * Setup ViewModel observers
     */
    private void setupObservers() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null) return;

            if (Boolean.TRUE.equals(isLoading)) {
                binding.pbLoading.setVisibility(View.VISIBLE);
                binding.btnSave.setEnabled(false);
            } else {
                binding.pbLoading.setVisibility(View.GONE);
                binding.btnSave.setEnabled(true);
            }
        });

        // Observe update success
        viewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(),
                        R.string.profile_update_success,
                        Toast.LENGTH_SHORT).show();
                viewModel.clearUpdateSuccess();

                // Navigate back to profile
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(),
                        errorMessage,
                        Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
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
            binding.etWeight.setText(String.valueOf(currentProfile.getWeight()));
        }

        // Height
        if (currentProfile.getHeight() != null) {
            // Convert meters to centimeters for display
            double heightInCm = currentProfile.getHeight() * 100;
            binding.etHeight.setText(String.format(Locale.getDefault(), "%.0f", heightInCm));
        }

        // Birthday
        if (currentProfile.getDateOfBirth() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdf.parse(currentProfile.getDateOfBirth());
                if (date != null) {
                    selectedDate.setTimeInMillis(date.getTime());
                    updateBirthdayField();
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing birthday: " + e.getMessage());
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

        // Validate name
        String name = binding.etName.getText().toString().trim();
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.profile_name_required));
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

        return isValid;
    }

    /**
     * Save profile to server
     */
    private void saveProfile() {
        if (!validateForm()) {
            return;
        }

        try {
            // Prepare field map
            Map<String, RequestBody> fields = new HashMap<>();

            // Name
            String name = binding.etName.getText().toString().trim();
            fields.put(ProfileRepository.KEY_NAME, createRequestBody(name));

            // Weight
            String weightStr = binding.etWeight.getText().toString().trim();
            if (!weightStr.isEmpty()) {
                fields.put(ProfileRepository.KEY_WEIGHT, createRequestBody(weightStr));
            }

            // Height (convert cm to meters for backend)
            String heightStr = binding.etHeight.getText().toString().trim();
            if (!heightStr.isEmpty()) {
                double heightCm = Double.parseDouble(heightStr);
                double heightM = heightCm / 100.0;
                fields.put(ProfileRepository.KEY_HEIGHT, createRequestBody(String.valueOf(heightM)));
            }

            // Birthday
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String birthday = sdf.format(selectedDate.getTime());
            fields.put(ProfileRepository.KEY_DATE_OF_BIRTH, createRequestBody(birthday));

            // Activity Level
            if (selectedActivityLevel != null) {
                fields.put(ProfileRepository.KEY_ACTIVITY_LEVEL,
                        createRequestBody(selectedActivityLevel.name()));
            }

            // Fitness Goal
            if (selectedFitnessGoal != null) {
                fields.put(ProfileRepository.KEY_FITNESS_GOAL,
                        createRequestBody(selectedFitnessGoal.name()));
            }

            // Handle avatar
            if (avatarChanged && selectedAvatarUri != null) {
                // Upload with new avatar file
                MultipartBody.Part avatarPart = prepareAvatarPart(selectedAvatarUri);
                viewModel.updateProfileWithNewAvatar(avatarPart, fields);
            } else if (currentProfile != null && currentProfile.getAvatar() != null) {
                // Keep existing avatar URL
                fields.put(ProfileRepository.KEY_AVATAR,
                        createRequestBody(currentProfile.getAvatar()));
                viewModel.updateProfileWithExistingAvatar(fields);
            } else {
                // No avatar
                viewModel.updateProfileNoAvatar(fields);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error saving profile: " + e.getMessage(), e);
            Toast.makeText(requireContext(),
                    R.string.profile_error_update,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prepare avatar file part for multipart upload
     */
    private MultipartBody.Part prepareAvatarPart(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new FileNotFoundException("Could not open image");
        }

        // Read and compress image
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Check size
        if (imageBytes.length > MAX_IMAGE_SIZE) {
            throw new IOException("Image too large. Maximum size is 5MB");
        }

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/jpeg"),
                imageBytes
        );

        return MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile);
    }

    /**
     * Create RequestBody from string
     */
    private RequestBody createRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
