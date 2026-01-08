package com.example.fitnessapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.enums.ActivityLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.request.OnboardingRequest;
import com.example.fitnessapp.model.response.user.UserResponse;
import com.example.fitnessapp.repository.OnboardingRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OnboardingViewModel extends AndroidViewModel {
    private final OnboardingRepository repository;

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

    // Setters
    public void setSex(String sex) {
        this.sex.setValue(sex);
    }

    public void setDateOfBirth(Date date) {
        this.dateOfBirth.setValue(date);
    }

    public void setWeight(Double weight) {
        this.weight.setValue(weight);
    }

    public void setHeight(Double height) {
        this.height.setValue(height);
    }

    public void setFitnessGoal(FitnessGoal goal) {
        this.fitnessGoal.setValue(goal);
    }

    public void setActivityLevel(ActivityLevel level) {
        this.activityLevel.setValue(level);
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
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }
}
