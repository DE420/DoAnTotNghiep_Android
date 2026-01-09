package com.example.fitnessapp.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.request.LogWorkoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.WorkoutDayExerciseResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;

import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.example.fitnessapp.ExerciseCountActivity;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PracticeReadyFragment extends Fragment {

    private static final String TAG = "PracticeReadyFragment";
    private static final String ARG_EXERCISE = "exercise";
    private static final String ARG_WORKOUT_DAY_ID = "workout_day_id";
    private static final String ARG_COMPLETED_SETS = "completed_sets";
    private static final String ARG_POSITION = "position";

    private ActivityResultLauncher<Intent> exerciseCountLauncher;

    private TextView tvExerciseName;
    private TextView tvSetInfo;
    private TextView tvSetRequirement;
    private CheckBox cbManualInput;
    private LinearLayout layoutManualInput;
    private LinearLayout layoutReps;
    private LinearLayout layoutDuration;
    private LinearLayout layoutWeight;
    private EditText etReps;
    private EditText etDuration;
    private EditText etWeight;
    private Button btnAction;
    private ImageView btnBack;
    private ProgressBar progressBar;

    private WorkoutDayExerciseResponse exercise;
    private Long workoutDayId;
    private int completedSets;
    private int totalSets;
    private int position;

    private ApiService apiService;
    private SessionManager sessionManager;

    // Callback interface để thông báo khi hoàn thành set
    public interface OnSetCompletedListener {
        void onSetCompleted(int position, int newCompletedSets);
    }

    private OnSetCompletedListener setCompletedListener;

    public static PracticeReadyFragment newInstance(WorkoutDayExerciseResponse exercise, Long workoutDayId, int completedSets, int position) {
        PracticeReadyFragment fragment = new PracticeReadyFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EXERCISE, exercise);
        args.putLong(ARG_WORKOUT_DAY_ID, workoutDayId);
        args.putInt(ARG_COMPLETED_SETS, completedSets);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnSetCompletedListener(OnSetCompletedListener listener) {
        this.setCompletedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            exercise = (WorkoutDayExerciseResponse) getArguments().getSerializable(ARG_EXERCISE);
            workoutDayId = getArguments().getLong(ARG_WORKOUT_DAY_ID);
            completedSets = getArguments().getInt(ARG_COMPLETED_SETS);
            position = getArguments().getInt(ARG_POSITION, -1);
        }

        // Register activity result launcher
        exerciseCountLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        // Exercise count completed successfully
                        completedSets++;

                        Log.d(TAG, "Exercise count completed. New completed sets: " + completedSets);

                        // Notify parent fragment
                        if (setCompletedListener != null) {
                            setCompletedListener.onSetCompleted(position, completedSets);
                        }

                        // Check if all sets completed
                        if (completedSets >= totalSets) {
                            Toast.makeText(getContext(), "Hoàn thành bài tập!", Toast.LENGTH_SHORT).show();
                        }

                        // Go back to exercise list
                        requireActivity().onBackPressed();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_practice_ready, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupExerciseInfo();
        setupManualInputFields();
        setupListeners();
    }

    private void initViews(View view) {
        tvExerciseName = view.findViewById(R.id.tvExerciseName);
        tvSetInfo = view.findViewById(R.id.tvSetInfo);
        tvSetRequirement = view.findViewById(R.id.tvSetRequirement);
        cbManualInput = view.findViewById(R.id.cbManualInput);
        layoutManualInput = view.findViewById(R.id.layoutManualInput);
        layoutReps = view.findViewById(R.id.layoutReps);
        layoutDuration = view.findViewById(R.id.layoutDuration);
        layoutWeight = view.findViewById(R.id.layoutWeight);
        etReps = view.findViewById(R.id.etReps);
        etDuration = view.findViewById(R.id.etDuration);
        etWeight = view.findViewById(R.id.etWeight);
        btnAction = view.findViewById(R.id.btnAction);
        btnBack = view.findViewById(R.id.btnBack);
        progressBar = view.findViewById(R.id.progressBar);

        apiService = RetrofitClient.getApiService();
        sessionManager = SessionManager.getInstance(requireContext());
    }

    private void setupExerciseInfo() {
        if (exercise == null) return;

        // Set exercise name
        tvExerciseName.setText(exercise.getExerciseName());

        // Set total sets
        totalSets = exercise.getSets() != null ? exercise.getSets() : 0;

        // Set current set info
        updateSetInfo();

        // Build requirement text
        String requirementText = buildRequirementText();
        tvSetRequirement.setText(requirementText);
    }

    private void updateSetInfo() {
        // Hiển thị set tiếp theo sẽ làm
        int nextSet = completedSets + 1;
        String setInfoText = String.format(Locale.getDefault(), "Set: %d / %d", nextSet, totalSets);
        tvSetInfo.setText(setInfoText);
    }

    private String buildRequirementText() {
        StringBuilder text = new StringBuilder();

        boolean hasReps = exercise.getReps() != null && exercise.getReps() > 0;
        boolean hasDuration = exercise.getDuration() != null && exercise.getDuration() > 0;
        boolean hasWeight = exercise.getWeight() != null && exercise.getWeight() > 0;

        if (hasReps) {
            text.append(exercise.getReps()).append(" reps / set");
        } else if (hasDuration) {
            text.append(exercise.getDuration()).append(" s / set");
        }

        if (hasWeight) {
            if (text.length() > 0) {
                text.append(" ");
            }
            text.append("(Tạ ").append(String.format(Locale.getDefault(), "%.0f", exercise.getWeight())).append("kg)");
        }

        return text.toString();
    }

    private void setupManualInputFields() {
        if (exercise == null) return;

        boolean hasReps = exercise.getReps() != null && exercise.getReps() > 0;
        boolean hasDuration = exercise.getDuration() != null && exercise.getDuration() > 0;
        boolean hasWeight = exercise.getWeight() != null && exercise.getWeight() > 0;

        // Show/hide fields based on exercise type
        layoutReps.setVisibility(hasReps ? View.VISIBLE : View.GONE);
        layoutDuration.setVisibility((hasDuration || hasReps) ? View.VISIBLE : View.GONE);
        layoutWeight.setVisibility(hasWeight ? View.VISIBLE : View.GONE);

        // Set default values
        if (hasReps) {
            etReps.setHint(exercise.getReps() + " reps");
        }
        if (hasDuration) {
            etDuration.setHint(exercise.getDuration() + " giây");
        }
        if (hasWeight) {
            etWeight.setHint(String.format(Locale.getDefault(), "%.0f kg", exercise.getWeight()));
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        cbManualInput.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutManualInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            btnAction.setText(isChecked ? "LƯU" : "BẮT ĐẦU");
        });

        btnAction.setOnClickListener(v -> {
            if (cbManualInput.isChecked()) {
                logManualWorkout();
            } else {
                startTracking();
            }
        });
    }

    private void logManualWorkout() {
        if (!validateManualInput()) {
            return;
        }

        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(getContext(), "Token expired.", Toast.LENGTH_LONG).show();
            return;
        }

        // Calculate next set number
        int nextSetNumber = completedSets + 1;

        Log.d(TAG, "Logging workout - Exercise: " + exercise.getExerciseName()
                + ", Exercise ID: " + exercise.getExerciseId()
                + ", Workout Day ID: " + workoutDayId
                + ", Current completed sets: " + completedSets
                + ", Next set number: " + nextSetNumber);

        // Build request
        LogWorkoutRequest request = new LogWorkoutRequest();
        request.setExerciseId(exercise.getExerciseId());
        request.setWorkoutDayId(workoutDayId);
        request.setSetNumber(nextSetNumber);

        // Set values based on input
        boolean hasReps = layoutReps.getVisibility() == View.VISIBLE;
        boolean hasDuration = layoutDuration.getVisibility() == View.VISIBLE;
        boolean hasWeight = layoutWeight.getVisibility() == View.VISIBLE;

        if (hasReps && !TextUtils.isEmpty(etReps.getText())) {
            int reps = Integer.parseInt(etReps.getText().toString());
            request.setReps(reps);
            Log.d(TAG, "Setting reps: " + reps);
        }

        if (hasDuration && !TextUtils.isEmpty(etDuration.getText())) {
            int duration = Integer.parseInt(etDuration.getText().toString());
            request.setDuration(duration);
            Log.d(TAG, "Setting duration: " + duration);
        }

        if (hasWeight && !TextUtils.isEmpty(etWeight.getText())) {
            double weight = Double.parseDouble(etWeight.getText().toString());
            request.setWeight(weight);
            Log.d(TAG, "Setting weight: " + weight);
        }

        showLoading(true);

        String authHeader = "Bearer " + accessToken;
        apiService.logWorkoutSet(authHeader, request)
                .enqueue(new Callback<ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Response<ApiResponse<Boolean>> response) {
                        showLoading(false);

                        Log.d(TAG, "API Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Boolean> apiResponse = response.body();
                            Log.d(TAG, "API Response - Status: " + apiResponse.isStatus()
                                    + ", Data: " + apiResponse.getData()
                                    + ", Message: " + apiResponse.getData());

                            if (apiResponse.isStatus() && apiResponse.getData()) {
                                // Tăng completed sets sau khi log thành công
                                completedSets++;

                                Log.d(TAG, "Set logged successfully. New completed sets: " + completedSets
                                        + ", Position: " + position);

                                Toast.makeText(getContext(), "Đã lưu set " + completedSets, Toast.LENGTH_SHORT).show();

                                // Thông báo cho fragment cha về việc hoàn thành set
                                if (setCompletedListener != null) {
                                    setCompletedListener.onSetCompleted(position, completedSets);
                                    Log.d(TAG, "Callback invoked for position: " + position);
                                } else {
                                    Log.w(TAG, "setCompletedListener is null!");
                                }

                                // Check if all sets completed
                                if (completedSets >= totalSets) {
                                    Toast.makeText(getContext(), "Hoàn thành bài tập!", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Exercise completed! " + completedSets + "/" + totalSets);
                                }

                                // Quay lại màn hình danh sách
                                requireActivity().onBackPressed();
                            } else {
                                Toast.makeText(getContext(), "Lỗi: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "API Error: " + apiResponse.getData());
                            }
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body", e);
                            }

                            Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Server error: " + response.code() + ", Body: " + errorBody);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<Boolean>> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Connection error: " + t.getMessage(), t);
                    }
                });
    }

    private boolean validateManualInput() {
        boolean hasReps = layoutReps.getVisibility() == View.VISIBLE;
        boolean hasDuration = layoutDuration.getVisibility() == View.VISIBLE;

        // Check if at least one required field is filled
        boolean hasInput = false;

        if (hasReps && !TextUtils.isEmpty(etReps.getText())) {
            hasInput = true;
        }

        if (hasDuration && !TextUtils.isEmpty(etDuration.getText())) {
            hasInput = true;
        }

        if (!hasInput) {
            Toast.makeText(getContext(), "Vui lòng nhập ít nhất một giá trị", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void startTracking() {
        Log.d(TAG, "Starting exercise count activity for: " + exercise.getExerciseName()
                + ", Set: " + (completedSets + 1));

        Intent intent = new Intent(getActivity(), ExerciseCountActivity.class);
        intent.putExtra(ExerciseCountActivity.EXTRA_EXERCISE, exercise);
        intent.putExtra(ExerciseCountActivity.EXTRA_WORKOUT_DAY_ID, workoutDayId);
        intent.putExtra(ExerciseCountActivity.EXTRA_COMPLETED_SETS, completedSets);
        intent.putExtra(ExerciseCountActivity.EXTRA_POSITION, position);

        exerciseCountLauncher.launch(intent);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnAction.setEnabled(!isLoading);
        cbManualInput.setEnabled(!isLoading);
    }
}