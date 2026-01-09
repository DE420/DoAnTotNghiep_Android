package com.example.fitnessapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.fitnessapp.model.request.LogWorkoutRequest;
import com.example.fitnessapp.model.response.ApiResponse;
import com.example.fitnessapp.model.response.WorkoutDayExerciseResponse;
import com.example.fitnessapp.network.ApiService;
import com.example.fitnessapp.network.RetrofitClient;
import com.example.fitnessapp.session.SessionManager;
import com.example.fitnessapp.utils.WorkoutProgressManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker.PoseLandmarkerOptions;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExerciseCountActivity extends AppCompatActivity {

    private static final String TAG = "ExerciseCountActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    public static final String EXTRA_EXERCISE = "extra_exercise";
    public static final String EXTRA_WORKOUT_DAY_ID = "extra_workout_day_id";
    public static final String EXTRA_COMPLETED_SETS = "extra_completed_sets";
    public static final String EXTRA_POSITION = "extra_position";

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView tvExerciseName;
    private TextView repCounterTextView;
    private Button btnFinish;
    private ProgressBar progressBar;

    private WorkoutDayExerciseResponse exercise;
    private Long workoutDayId;
    private int completedSets;
    private int position;
    private int currentCount = 0;
    private int targetCount = 0;
    private boolean isRepBased = true;

    private PoseLandmarker poseLandmarker;
    private ProcessCameraProvider cameraProvider;
    private ExerciseCounter exerciseCounter;

    private ApiService apiService;
    private SessionManager sessionManager;
    private WorkoutProgressManager progressManager;

    private boolean isProcessing = false;

    // Duration tracking
    private long exerciseStartTime = 0;
    private int elapsedDuration = 0; // in seconds
    private Thread durationTrackingThread;
    private volatile boolean isTrackingDuration = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_count);

        initViews();
        getIntentData();
        setupExerciseInfo();

        apiService = RetrofitClient.getApiService();
        sessionManager = SessionManager.getInstance(this);
        progressManager = WorkoutProgressManager.getInstance(this);

        overlayView.setFrontCamera(true);

        setupPoseLandmarker();
        requestCameraPermission();
        setupListeners();

        // Bắt đầu đo thời gian
        exerciseStartTime = System.currentTimeMillis();
        startDurationTracking();
    }

    private void initViews() {
        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        tvExerciseName = findViewById(R.id.tvExerciseName);
        repCounterTextView = findViewById(R.id.repCounterTextView);
        btnFinish = findViewById(R.id.btnFinish);
        progressBar = findViewById(R.id.progressBar);
    }

    private void getIntentData() {
        exercise = (WorkoutDayExerciseResponse) getIntent().getSerializableExtra(EXTRA_EXERCISE);
        workoutDayId = getIntent().getLongExtra(EXTRA_WORKOUT_DAY_ID, -1);
        completedSets = getIntent().getIntExtra(EXTRA_COMPLETED_SETS, 0);
        position = getIntent().getIntExtra(EXTRA_POSITION, -1);

        Log.d(TAG, "Exercise: " + (exercise != null ? exercise.getExerciseName() : "null")
                + ", Workout Day ID: " + workoutDayId
                + ", Completed Sets: " + completedSets
                + ", Position: " + position);
    }

    private void setupExerciseInfo() {
        if (exercise == null) {
            Toast.makeText(this, "Invalid exercise data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set exercise name
        tvExerciseName.setText(exercise.getExerciseName());

        // Initialize exercise counter TRƯỚC KHI xác định rep/duration
        exerciseCounter = new ExerciseCounter();
        String exerciseName = exercise.getExerciseName();

        // Map exercise name to type
        if (exerciseName.equalsIgnoreCase("pull up")) {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.PULLUP);
        } else if (exerciseName.equalsIgnoreCase("push up")) {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.PUSHUP);
        } else if (exerciseName.equalsIgnoreCase("squat")) {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.SQUAT);
        } else if (exerciseName.equalsIgnoreCase("back lever")) {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.BACK_LEVER);
        } else if (exerciseName.equalsIgnoreCase("plank")) {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.PLANK);
        } else if (exerciseName.equalsIgnoreCase("deadlift")) {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.DEADLIFT);
        } else {
            exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.SQUAT); // Default
        }

        // Determine if rep-based or duration-based
        boolean hasReps = exercise.getReps() != null && exercise.getReps() > 0;
        boolean hasDuration = exercise.getDuration() != null && exercise.getDuration() > 0;

        if (hasReps) {
            isRepBased = true;
            targetCount = exercise.getReps();
            updateCountDisplay();
        } else if (hasDuration) {
            isRepBased = false;
            targetCount = exercise.getDuration();
            updateDurationDisplay();
        } else {
            Toast.makeText(this, "Invalid exercise configuration", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Exercise setup - Type: " + (isRepBased ? "Reps" : "Duration")
                + ", Target: " + targetCount
                + ", Counter type: " + exerciseCounter.getExerciseType());
    }

    private void startDurationTracking() {
        isTrackingDuration = true;

        durationTrackingThread = new Thread(() -> {
            while (isTrackingDuration && !isProcessing) {
                try {
                    Thread.sleep(100);

                    runOnUiThread(() -> {
                        if (isRepBased) {
                            long currentTime = System.currentTimeMillis();
                            elapsedDuration = (int) ((currentTime - exerciseStartTime) / 1000);
                            updateCountDisplay();
                        } else {
                            if (exerciseCounter.isDurationBasedExercise()) {
                                currentCount = exerciseCounter.getDurationTime();
                                long currentTime = System.currentTimeMillis();
                                elapsedDuration = (int) ((currentTime - exerciseStartTime) / 1000);
                            } else {
                                long currentTime = System.currentTimeMillis();
                                currentCount = (int) ((currentTime - exerciseStartTime) / 1000);
                                elapsedDuration = currentCount;
                            }

                            updateDurationDisplay();

                            if (currentCount >= targetCount) {
                                isTrackingDuration = false;
                                onTargetReached();
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    Log.d(TAG, "Duration tracking thread interrupted");
                    break;
                }
            }
            Log.d(TAG, "Duration tracking thread stopped");
        });

        durationTrackingThread.start();
    }

    private void updateCountDisplay() {
        if (isRepBased) {
            // Show reps with elapsed time
            String displayText = String.format(Locale.getDefault(),
                    "Rep: %d / %d (%ds)",
                    currentCount, targetCount, elapsedDuration);
            repCounterTextView.setText(displayText);
        } else {
            updateDurationDisplay();
        }
    }

    private void updateDurationDisplay() {
        String displayText;

        if (exerciseCounter.isDurationBasedExercise()) {
            if (exerciseCounter.isDurationHolding()) {
                displayText = String.format(Locale.getDefault(),
                        "Thời gian: %d/%d s",
                        currentCount, targetCount);
            } else {
                displayText = String.format(Locale.getDefault(),
                        "Chưa vào tư thế",
                        currentCount, targetCount);
            }
        } else {
            displayText = String.format(Locale.getDefault(),
                    "Thời gian: %d/%d s",
                    currentCount, targetCount);
        }

        repCounterTextView.setText(displayText);
    }

    private void setupPoseLandmarker() {
        try {
            PoseLandmarkerOptions options = PoseLandmarkerOptions.builder()
                    .setBaseOptions(BaseOptions.builder()
                            .setModelAssetPath("pose_landmarker_lite.task")
                            .build())
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setNumPoses(1)
                    .setMinPoseDetectionConfidence(0.5f)
                    .setMinPosePresenceConfidence(0.5f)
                    .setMinTrackingConfidence(0.5f)
                    .setResultListener(this::onPoseDetectionResult)
                    .setErrorListener(this::onPoseDetectionError)
                    .build();

            poseLandmarker = PoseLandmarker.createFromOptions(this, options);
            Log.d(TAG, "Pose Landmarker initialized successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error initializing Pose Landmarker", e);
            Toast.makeText(this, "Lỗi khởi tạo Pose Detection", Toast.LENGTH_SHORT).show();
        }
    }

    private void onPoseDetectionResult(PoseLandmarkerResult result, MPImage input) {
        runOnUiThread(() -> {
            if (result != null && !result.landmarks().isEmpty()) {
                List<NormalizedLandmark> landmarks = result.landmarks().get(0);

                if (isRepBased) {
                    int reps = exerciseCounter.processLandmarks(landmarks);

                    if (reps > currentCount) {
                        currentCount = reps;
                        updateCountDisplay();

                        if (currentCount >= targetCount) {
                            onTargetReached();
                        }
                    }
                } else {
                    exerciseCounter.processLandmarks(landmarks);

                    if (exerciseCounter.isDurationBasedExercise()) {
                        currentCount = exerciseCounter.getDurationTime();
                    }
                }

                overlayView.setPoseLandmarks(
                        landmarks,
                        input.getWidth(),
                        input.getHeight()
                );

            } else {
                overlayView.clear();
            }
        });
    }

    private void onPoseDetectionError(RuntimeException error) {
        Log.e(TAG, "Pose Detection error: " + error.getMessage());
        runOnUiThread(() -> {
            Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void onTargetReached() {
        if (isProcessing) {
            return;
        }

        Log.d(TAG, "Target reached! Logging workout...");
        Toast.makeText(this, "Hoàn thành set!", Toast.LENGTH_SHORT).show();
        logWorkoutAndFinish();
    }

    private void setupListeners() {
        btnFinish.setOnClickListener(v -> {
            logWorkoutAndFinish();
        });
    }

    private void logWorkoutAndFinish() {
        if (isProcessing) {
            return;
        }

        isProcessing = true;
        isTrackingDuration = false;

        String accessToken = sessionManager.getAccessToken();
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Token expired.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        LogWorkoutRequest request = new LogWorkoutRequest();
        request.setExerciseId(exercise.getExerciseId());
        request.setWorkoutDayId(workoutDayId);
        request.setSetNumber(completedSets + 1);

        if (isRepBased) {
            request.setReps(currentCount);
            request.setDuration(elapsedDuration);
            Log.d(TAG, "Logging rep-based workout - Reps: " + currentCount
                    + ", Duration: " + elapsedDuration + "s");
        } else {
            if (exerciseCounter.isDurationBasedExercise()) {
                int duration = exerciseCounter.getDurationTime();
                request.setDuration(duration);
                Log.d(TAG, "Logging duration-based workout - Duration: " + duration + "s (correct pose time)");
            } else {
                request.setDuration(currentCount);
                Log.d(TAG, "Logging duration-based workout - Duration: " + currentCount + "s");
            }
        }

        if (exercise.getWeight() != null && exercise.getWeight() > 0) {
            request.setWeight(exercise.getWeight());
            Log.d(TAG, "Weight: " + exercise.getWeight() + "kg");
        }

        showLoading(true);

        String authHeader = "Bearer " + accessToken;
        apiService.logWorkoutSet(authHeader, request)
                .enqueue(new Callback<ApiResponse<Boolean>>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Boolean> apiResponse = response.body();
                            if (apiResponse.isStatus() && apiResponse.getData()) {
                                int newCompletedSets = completedSets + 1;
                                progressManager.saveCompletedSets(workoutDayId, exercise.getId(), newCompletedSets);

                                Log.d(TAG, "Workout logged successfully. New completed sets: " + newCompletedSets);

                                String message;
                                if (isRepBased) {
                                    message = String.format("Da luu set %d (%d reps, %ds)",
                                            newCompletedSets, currentCount, elapsedDuration);
                                } else {
                                    message = String.format("Da luu set %d (%ds)",
                                            newCompletedSets, request.getDuration());
                                }
                                Toast.makeText(ExerciseCountActivity.this, message, Toast.LENGTH_SHORT).show();

                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(ExerciseCountActivity.this, "Loi: " + apiResponse.getData(), Toast.LENGTH_SHORT).show();
                                isProcessing = false;
                                isTrackingDuration = true;
                                startDurationTracking();
                            }
                        } else {
                            Toast.makeText(ExerciseCountActivity.this, "Loi server: " + response.code(), Toast.LENGTH_SHORT).show();
                            isProcessing = false;
                            isTrackingDuration = true;
                            startDurationTracking();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                        showLoading(false);
                        Toast.makeText(ExerciseCountActivity.this, "Loi ket noi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Connection error", t);
                        isProcessing = false;
                        isTrackingDuration = true;
                        startDurationTracking();
                    }
                });
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Cần cấp quyền camera để sử dụng app",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCamera();
            } catch (Exception e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, "Lỗi khởi động camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera() {
        // Preview
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // ImageAnalysis for pose detection
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                this::processImageProxy);

        // Camera selector - front camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        // Bind to lifecycle
        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalysis);

            Log.d(TAG, "Camera bound successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera", e);
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void processImageProxy(ImageProxy imageProxy) {
        if (poseLandmarker == null) {
            imageProxy.close();
            return;
        }

        try {
            // Convert ImageProxy to MPImage
            MPImage mpImage = convertToMPImage(imageProxy);

            if (mpImage != null) {
                // Send frame to MediaPipe with timestamp
                long timestampMs = SystemClock.uptimeMillis();
                poseLandmarker.detectAsync(mpImage, timestampMs);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing image", e);
        } finally {
            imageProxy.close();
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private MPImage convertToMPImage(ImageProxy imageProxy) {
        try {
            Bitmap bitmap = imageProxy.toBitmap();

            if (bitmap != null) {
                // Handle rotation
                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                if (rotationDegrees != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationDegrees);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }

                return new BitmapImageBuilder(bitmap).build();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error converting to MPImage", e);
        }
        return null;
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? android.view.View.VISIBLE : android.view.View.GONE);
        btnFinish.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isProcessing = true;
        isTrackingDuration = false; // Stop duration tracking thread

        // Wait for thread to finish
        if (durationTrackingThread != null) {
            try {
                durationTrackingThread.interrupt();
                durationTrackingThread.join(1000); // Wait max 1 second
            } catch (InterruptedException e) {
                Log.e(TAG, "Error stopping duration tracking thread", e);
            }
        }

        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Tạm dừng tracking khi app vào background
        isTrackingDuration = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume tracking khi app quay lại
        if (!isProcessing && durationTrackingThread != null && !durationTrackingThread.isAlive()) {
            startDurationTracking();
        }
    }
}