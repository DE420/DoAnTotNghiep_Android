package com.example.fitnessapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker.PoseLandmarkerOptions;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final String TAG = "PoseDetection";

    private PreviewView previewView;
    private OverlayView overlayView;
    private TextView repCounterTextView; // Thêm TextView
    private Spinner exerciseSpinner;            // ← MỚI
    private PoseLandmarker poseLandmarker;
    private ProcessCameraProvider cameraProvider;
    private ExerciseCounter exerciseCounter;    // ← MỚI (thay SquatCounter)

    private long lastProcessedTimestamp = 0;
    private static final long PROCESS_INTERVAL_MS = 33; // ~30 FPS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        overlayView = findViewById(R.id.overlayView);
        repCounterTextView = findViewById(R.id.repCounterTextView);
        Button resetButton = findViewById(R.id.resetButton);
        exerciseSpinner = findViewById(R.id.exerciseSpinner);        // ← MỚI

        // Thông báo cho overlay đang dùng camera trước
        overlayView.setFrontCamera(true);
        exerciseCounter = new ExerciseCounter();                     // ← MỚI

        // Setup Spinner
        setupExerciseSpinner();                                      // ← MỚI

        // Reset button listener
        resetButton.setOnClickListener(v -> resetCounter());

        // Khởi tạo Pose Landmarker
        setupPoseLandmarker();

        // Request camera permission
        requestCameraPermission();
    }

    private void setupPoseLandmarker() {
        try {
            PoseLandmarkerOptions options = PoseLandmarkerOptions.builder()
                    .setBaseOptions(BaseOptions.builder()
                            .setModelAssetPath("pose_landmarker_lite.task")
                            .build())
                    .setRunningMode(RunningMode.LIVE_STREAM)
                    .setNumPoses(1) // Số người tối đa muốn detect
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

    // ← METHOD HOÀN TOÀN MỚI
    private void setupExerciseSpinner() {
        String[] exercises = {"Squat", "Sit-up", "Push-up"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                exercises
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(adapter);

        exerciseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.SQUAT);
                        break;
                    case 1:
                        exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.SITUP);
                        break;
                    case 2:
                        exerciseCounter.setExerciseType(ExerciseCounter.ExerciseType.PUSHUP);
                        break;
                }
                resetCounter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Callback nhận kết quả pose detection
    private void onPoseDetectionResult(PoseLandmarkerResult result, MPImage input) {
        runOnUiThread(() -> {
            if (result != null && !result.landmarks().isEmpty()) {
                // Khai báo biến landmarks rõ ràng
                List<NormalizedLandmark> landmarks = result.landmarks().get(0);

                // Đếm rep
                int reps = exerciseCounter.processLandmarks(landmarks);

                // Cập nhật UI
                repCounterTextView.setText("Reps: " + reps);

                // Cập nhật overlay với pose landmarks
                overlayView.setPoseLandmarks(
                        landmarks,
                        input.getWidth(),
                        input.getHeight()
                );

                Log.d(TAG, "Detected " + result.landmarks().size() + " pose(s)");
            } else {
                overlayView.clear();
            }
        });
    }

    // Thêm method reset (tuỳ chọn)
    public void resetCounter() {
        exerciseCounter.reset();
        repCounterTextView.setText("Reps: 0");
    }

    private void onPoseDetectionError(RuntimeException error) {
        Log.e(TAG, "Pose Detection error: " + error.getMessage());
        runOnUiThread(() -> {
            Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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

        // ImageAnalysis để xử lý frames
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this),
                this::processImageProxy);

        // Camera selector - sử dụng camera trước để dễ test pose
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

//        long currentTimestamp = System.currentTimeMillis();
//
//        // Throttle processing để tránh quá tải
//        if (currentTimestamp - lastProcessedTimestamp < PROCESS_INTERVAL_MS) {
//            imageProxy.close();
//            return;
//        }
//
//        lastProcessedTimestamp = currentTimestamp;

        try {
            // Convert ImageProxy to MPImage
            MPImage mpImage = convertToMPImage(imageProxy);

            if (mpImage != null) {
                // Gửi frame đến MediaPipe với timestamp (milliseconds)
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
            // Sử dụng imageProxy.toBitmap() thay vì xử lý thủ công
            Bitmap bitmap = imageProxy.toBitmap();

            if (bitmap != null) {
                // Xử lý rotation
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }
}