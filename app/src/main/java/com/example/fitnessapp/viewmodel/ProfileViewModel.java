package com.example.fitnessapp.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitnessapp.model.response.user.ProfileResponse;
import com.example.fitnessapp.repository.ProfileRepository;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * ViewModel for Profile feature
 *
 * Responsibilities:
 * - Load user profile data from repository
 * - Update user profile with new data
 * - Manage loading/error states
 * - Provide LiveData for UI observation
 *
 * Usage:
 * <pre>
 * ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
 * viewModel.getProfileData().observe(this, profile -> {
 *     // Update UI with profile data
 * });
 * viewModel.loadUserProfile();
 * </pre>
 */
public class ProfileViewModel extends AndroidViewModel {

    private static final String TAG = "ProfileViewModel";

    private final ProfileRepository repository;
    private final ExecutorService executorService;

    // LiveData for UI observation
    private final MutableLiveData<ProfileResponse> profileData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> passwordChangeSuccess = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProfileRepository();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // LiveData Getters
    public LiveData<ProfileResponse> getProfileData() {
        return profileData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Boolean> getPasswordChangeSuccess() {
        return passwordChangeSuccess;
    }

    /**
     * Load user profile from server
     */
    public void loadUserProfile() {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return; // Already loading
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);

        executorService.execute(() -> {
            try {
                ProfileResponse profile = repository.getUserProfile(getApplication());
                profileData.postValue(profile);
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile", e);
                errorMessage.postValue(e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Update profile with new avatar file
     *
     * @param avatarFile MultipartBody.Part for new avatar
     * @param fields Map of profile fields (name, weight, height, etc.)
     */
    public void updateProfileWithNewAvatar(MultipartBody.Part avatarFile,
                                          Map<String, RequestBody> fields) {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);
        updateSuccess.postValue(false);

        executorService.execute(() -> {
            try {
                boolean success = repository.updateProfile(
                    getApplication(),
                    avatarFile,
                    fields
                );

                if (success) {
                    updateSuccess.postValue(true);
                    // Reload profile to get updated data
                    loadUserProfile();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating profile with new avatar", e);
                errorMessage.postValue(e.getMessage());
                updateSuccess.postValue(false);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Update profile with existing avatar URL (no new file upload)
     *
     * @param fields Map of profile fields including avatar URL
     */
    public void updateProfileWithExistingAvatar(Map<String, RequestBody> fields) {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);
        updateSuccess.postValue(false);

        executorService.execute(() -> {
            try {
                boolean success = repository.updateProfileWithExistingAvatar(
                    getApplication(),
                    fields
                );

                if (success) {
                    updateSuccess.postValue(true);
                    loadUserProfile();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating profile with existing avatar", e);
                errorMessage.postValue(e.getMessage());
                updateSuccess.postValue(false);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Update profile without avatar
     *
     * @param fields Map of profile fields (without avatar)
     */
    public void updateProfileNoAvatar(Map<String, RequestBody> fields) {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);
        updateSuccess.postValue(false);

        executorService.execute(() -> {
            try {
                boolean success = repository.updateProfileNoAvatar(
                    getApplication(),
                    fields
                );

                if (success) {
                    updateSuccess.postValue(true);
                    loadUserProfile();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating profile without avatar", e);
                errorMessage.postValue(e.getMessage());
                updateSuccess.postValue(false);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Clear error message after showing to user
     */
    public void clearError() {
        errorMessage.postValue(null);
    }

    /**
     * Clear update success flag after handling
     */
    public void clearUpdateSuccess() {
        updateSuccess.postValue(false);
    }

    /**
     * Clear password change success flag after handling
     */
    public void clearPasswordChangeSuccess() {
        passwordChangeSuccess.postValue(false);
    }

    /**
     * Change user password
     *
     * @param currentPassword User's current password
     * @param newPassword New password to set
     * @param confirmPassword Confirmation of new password
     */
    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }

        isLoading.postValue(true);
        errorMessage.postValue(null);
        passwordChangeSuccess.postValue(false);

        executorService.execute(() -> {
            try {
                boolean success = repository.changePassword(
                    getApplication(),
                    currentPassword,
                    newPassword,
                    confirmPassword
                );

                if (success) {
                    passwordChangeSuccess.postValue(true);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error changing password", e);
                errorMessage.postValue(e.getMessage());
                passwordChangeSuccess.postValue(false);
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
