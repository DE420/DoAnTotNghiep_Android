package com.example.fitnessapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.OnboardingRequest;
import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.model.response.user.UserResponse;
import com.example.fitnessapp.repository.OnboardingRepository;
import com.example.fitnessapp.session.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OnboardingViewModel extends AndroidViewModel {
    private final OnboardingRepository repository;
    private final SessionManager sessionManager;

    // Data fields
    private final MutableLiveData<String> sex = new MutableLiveData<>();
    private final MutableLiveData<Date> dateOfBirth = new MutableLiveData<>();
    private final MutableLiveData<Double> weight = new MutableLiveData<>();
    private final MutableLiveData<Double> height = new MutableLiveData<>();
    private final MutableLiveData<FitnessGoal> fitnessGoal = new MutableLiveData<>();
    private final MutableLiveData<ActivityLevel> activityLevel = new MutableLiveData<>();

    // UI State
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> onboardingComplete = new MutableLiveData<>(false);

    public OnboardingViewModel(@NonNull Application application) {
        super(application);
        this.repository = OnboardingRepository.getInstance();
        this.sessionManager = SessionManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<String> getSex() {
        return sex;
    }

    public LiveData<Date> getDateOfBirth() {
        return dateOfBirth;
    }

    public LiveData<Double> getWeight() {
        return weight;
    }

    public LiveData<Double> getHeight() {
        return height;
    }

    public LiveData<FitnessGoal> getFitnessGoal() {
        return fitnessGoal;
    }

    public LiveData<ActivityLevel> getActivityLevel() {
        return activityLevel;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getOnboardingComplete() {
        return onboardingComplete;
    }

    // Setters - Save to local storage when data changes
    public void setSex(String sex) {
        this.sex.setValue(sex);
        sessionManager.saveOnboardingDraftSex(sex);
    }

    public void setDateOfBirth(Date date) {
        this.dateOfBirth.setValue(date);
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sessionManager.saveOnboardingDraftDateOfBirth(sdf.format(date));
        }
    }

    public void setWeight(Double weight) {
        this.weight.setValue(weight);
        sessionManager.saveOnboardingDraftWeight(weight);
    }

    public void setHeight(Double height) {
        this.height.setValue(height);
        sessionManager.saveOnboardingDraftHeight(height);
    }

    public void setFitnessGoal(FitnessGoal goal) {
        this.fitnessGoal.setValue(goal);
        if (goal != null) {
            sessionManager.saveOnboardingDraftFitnessGoal(goal.name());
        }
    }

    public void setActivityLevel(ActivityLevel level) {
        this.activityLevel.setValue(level);
        if (level != null) {
            sessionManager.saveOnboardingDraftActivityLevel(level.name());
        }
    }

    // Validation methods
    public boolean validateStep1() {
        return sex.getValue() != null && dateOfBirth.getValue() != null;
    }

    public boolean validateStep2() {
        return weight.getValue() != null && weight.getValue() > 0 &&
               height.getValue() != null && height.getValue() > 0;
    }

    public boolean validateStep3() {
        return fitnessGoal.getValue() != null && activityLevel.getValue() != null;
    }

    // Submit onboarding data
    public void submitOnboarding() {
        if (!validateStep1() || !validateStep2() || !validateStep3()) {
            errorMessage.setValue("Vui lòng điền đầy đủ thông tin");
            return;
        }

        isLoading.setValue(true);

        OnboardingRequest request = new OnboardingRequest();
        request.setSex(sex.getValue());

        // Format date to dd/MM/yyyy
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        request.setDateOfBirth(sdf.format(dateOfBirth.getValue()));

        request.setWeight(weight.getValue());
        request.setHeight(height.getValue());
        request.setFitnessGoal(fitnessGoal.getValue());
        request.setActivityLevel(activityLevel.getValue());

        repository.submitOnboarding(getApplication(), request, new OnboardingRepository.OnboardingSubmitCallback() {
            @Override
            public void onSuccess(UserResponse user) {
                isLoading.postValue(false);
                onboardingComplete.postValue(true);
                // Clear draft data after successful submission
                sessionManager.clearOnboardingDraft();
                Log.d("OnboardingViewModel", "Onboarding submitted successfully, draft data cleared");
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    /**
     * Load existing profile data to populate onboarding screens
     * Hybrid approach: 1) Load from local draft, 2) Fill missing fields from API
     * Called when user wants to edit their onboarding information
     */
    public void loadExistingProfileData() {
        Log.d("OnboardingViewModel", "Loading existing profile data...");

        // First, load any available data from local draft storage
        boolean hasLocalDraft = loadFromLocalDraft();
        if (hasLocalDraft) {
            Log.d("OnboardingViewModel", "Loaded some data from local draft storage");
        }

        // Always call API to fill in missing fields (fields not in local draft)
        Log.d("OnboardingViewModel", "Loading profile from API to fill missing fields...");
        isLoading.setValue(true);

        repository.getUserProfileData(getApplication(), new OnboardingRepository.ProfileDataCallback() {
            @Override
            public void onSuccess(ProfileResponse profile) {
                Log.d("OnboardingViewModel", "Profile data loaded from API successfully");

                // Only populate fields that are NOT already set from local draft
                // Populate sex (only if not in draft)
                if (sex.getValue() == null && profile.getSex() != null) {
                    sex.postValue(profile.getSex());
                    Log.d("OnboardingViewModel", "API filled sex: " + profile.getSex());
                }

                // Populate date of birth (only if not in draft)
                if (dateOfBirth.getValue() == null && profile.getDateOfBirth() != null && !profile.getDateOfBirth().isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date dob = sdf.parse(profile.getDateOfBirth());
                        dateOfBirth.postValue(dob);
                        Log.d("OnboardingViewModel", "API filled date of birth: " + profile.getDateOfBirth());
                    } catch (ParseException e) {
                        Log.e("OnboardingViewModel", "Failed to parse date: " + profile.getDateOfBirth(), e);
                    }
                }

                // Populate weight (only if not in draft)
                if (weight.getValue() == null && profile.getWeight() != null) {
                    weight.postValue(profile.getWeight());
                    Log.d("OnboardingViewModel", "API filled weight: " + profile.getWeight());
                }

                // Populate height (only if not in draft)
                if (height.getValue() == null && profile.getHeight() != null) {
                    height.postValue(profile.getHeight());
                    Log.d("OnboardingViewModel", "API filled height: " + profile.getHeight());
                }

                // Populate fitness goal (only if not in draft)
                if (fitnessGoal.getValue() == null && profile.getFitnessGoal() != null) {
                    fitnessGoal.postValue(profile.getFitnessGoal());
                    Log.d("OnboardingViewModel", "API filled fitness goal: " + profile.getFitnessGoal());
                }

                // Populate activity level (only if not in draft)
                if (activityLevel.getValue() == null && profile.getActivityLevel() != null) {
                    activityLevel.postValue(profile.getActivityLevel());
                    Log.d("OnboardingViewModel", "API filled activity level: " + profile.getActivityLevel());
                }

                isLoading.postValue(false);
                Log.d("OnboardingViewModel", "Profile data loading complete (draft + API)");
            }

            @Override
            public void onError(String error) {
                Log.e("OnboardingViewModel", "Failed to load profile data from API: " + error);
                // Even if API fails, we still have draft data loaded
                isLoading.postValue(false);
                // Only show error if we don't have any draft data
                if (!hasLocalDraft) {
                    errorMessage.postValue(error);
                }
            }
        });
    }

    /**
     * Load data from local draft storage (SharedPreferences)
     * @return true if any draft data was found and loaded
     */
    private boolean loadFromLocalDraft() {
        boolean hasData = false;

        // Load sex
        String draftSex = sessionManager.getOnboardingDraftSex();
        if (draftSex != null) {
            sex.setValue(draftSex);
            Log.d("OnboardingViewModel", "Loaded draft sex: " + draftSex);
            hasData = true;
        }

        // Load date of birth
        String draftDob = sessionManager.getOnboardingDraftDateOfBirth();
        if (draftDob != null && !draftDob.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date dob = sdf.parse(draftDob);
                dateOfBirth.setValue(dob);
                Log.d("OnboardingViewModel", "Loaded draft DOB: " + draftDob);
                hasData = true;
            } catch (ParseException e) {
                Log.e("OnboardingViewModel", "Failed to parse draft DOB: " + draftDob, e);
            }
        }

        // Load weight
        Double draftWeight = sessionManager.getOnboardingDraftWeight();
        if (draftWeight != null) {
            weight.setValue(draftWeight);
            Log.d("OnboardingViewModel", "Loaded draft weight: " + draftWeight);
            hasData = true;
        }

        // Load height
        Double draftHeight = sessionManager.getOnboardingDraftHeight();
        if (draftHeight != null) {
            height.setValue(draftHeight);
            Log.d("OnboardingViewModel", "Loaded draft height: " + draftHeight);
            hasData = true;
        }

        // Load fitness goal
        String draftGoal = sessionManager.getOnboardingDraftFitnessGoal();
        if (draftGoal != null) {
            try {
                FitnessGoal goal = FitnessGoal.valueOf(draftGoal);
                fitnessGoal.setValue(goal);
                Log.d("OnboardingViewModel", "Loaded draft fitness goal: " + draftGoal);
                hasData = true;
            } catch (IllegalArgumentException e) {
                Log.e("OnboardingViewModel", "Invalid fitness goal: " + draftGoal, e);
            }
        }

        // Load activity level
        String draftLevel = sessionManager.getOnboardingDraftActivityLevel();
        if (draftLevel != null) {
            try {
                ActivityLevel level = ActivityLevel.valueOf(draftLevel);
                activityLevel.setValue(level);
                Log.d("OnboardingViewModel", "Loaded draft activity level: " + draftLevel);
                hasData = true;
            } catch (IllegalArgumentException e) {
                Log.e("OnboardingViewModel", "Invalid activity level: " + draftLevel, e);
            }
        }

        return hasData;
    }
}
