package com.example.fitnessapp;

import android.util.Log;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

public class ExerciseCounter {

    private static final String TAG = "ExerciseCounter";

    // Exercise types
    public enum ExerciseType {
        SQUAT,
        SITUP,
        PUSHUP,
        PLANK,
        BICEPS_CURL  // Thêm mới
    }

    // Pose landmark indices
    private static final int NOSE = 0;
    private static final int LEFT_SHOULDER = 11;
    private static final int RIGHT_SHOULDER = 12;
    private static final int LEFT_ELBOW = 13;
    private static final int RIGHT_ELBOW = 14;
    private static final int LEFT_WRIST = 15;
    private static final int RIGHT_WRIST = 16;
    private static final int LEFT_HIP = 23;
    private static final int RIGHT_HIP = 24;
    private static final int LEFT_KNEE = 25;
    private static final int RIGHT_KNEE = 26;
    private static final int LEFT_ANKLE = 27;
    private static final int RIGHT_ANKLE = 28;

    // Exercise states
    private enum State {
        UP,
        DOWN,
        HOLDING,  // For plank
        UNKNOWN
    }

    private ExerciseType currentExercise = ExerciseType.SQUAT;
    private State currentState = State.UNKNOWN;
    private int repCount = 0;

    // Plank tracking
    private boolean isPlankHolding = false;
    private long plankHoldStartTime = 0;
    private int plankHoldDuration = 0; // in seconds

    // Biceps curl tracking - cả 2 tay phải đồng bộ
    private boolean leftArmCurled = false;
    private boolean rightArmCurled = false;
    private boolean bothArmsDown = true; // Bắt đầu ở trạng thái xuống

    // Squat thresholds
    private static final double SQUAT_STANDING_ANGLE = 160.0;
    private static final double SQUAT_SQUATTING_ANGLE = 90.0;

    // Sit-up thresholds
    private static final double SITUP_UP_ANGLE = 45.0;
    private static final double SITUP_DOWN_ANGLE = 100.0;

    // Push-up thresholds
    private static final double PUSHUP_UP_ANGLE = 160.0;
    private static final double PUSHUP_DOWN_ANGLE = 90.0;

    // Plank thresholds
    private static final double PLANK_MIN_BACK_ANGLE = 160.0;
    private static final double PLANK_MAX_BACK_ANGLE = 200.0;
    private static final double PLANK_MIN_HIP_HEIGHT = 0.3;
    private static final double PLANK_MAX_HIP_HEIGHT = 0.7;

    // Biceps curl thresholds
    private static final double BICEPS_CURL_UP_ANGLE = 50.0;    // Tay nâng lên (góc nhỏ)
    private static final double BICEPS_CURL_DOWN_ANGLE = 150.0; // Tay duỗi thẳng (góc lớn)
    private static final double BICEPS_TOLERANCE = 20.0;        // Dung sai cho 2 tay

    public void setExerciseType(ExerciseType type) {
        if (this.currentExercise != type) {
            this.currentExercise = type;
            reset();
        }
    }

    public ExerciseType getExerciseType() {
        return currentExercise;
    }

    public int processLandmarks(List<NormalizedLandmark> landmarks) {
        if (landmarks == null || landmarks.size() < 33) {
            return repCount;
        }

        switch (currentExercise) {
            case SQUAT:
                processSquat(landmarks);
                break;
            case SITUP:
                processSitup(landmarks);
                break;
            case PUSHUP:
                processPushup(landmarks);
                break;
            case PLANK:
                processPlank(landmarks);
                break;
            case BICEPS_CURL:
                processBicepsCurl(landmarks);
                break;
        }

        return repCount;
    }

    // SQUAT LOGIC
    private void processSquat(List<NormalizedLandmark> landmarks) {
        double leftKneeAngle = calculateAngle(
                landmarks.get(LEFT_HIP),
                landmarks.get(LEFT_KNEE),
                landmarks.get(LEFT_ANKLE)
        );

        double rightKneeAngle = calculateAngle(
                landmarks.get(RIGHT_HIP),
                landmarks.get(RIGHT_KNEE),
                landmarks.get(RIGHT_ANKLE)
        );

        double avgKneeAngle = (leftKneeAngle + rightKneeAngle) / 2.0;

        State previousState = currentState;

        if (avgKneeAngle > SQUAT_STANDING_ANGLE) {
            currentState = State.UP;
        } else if (avgKneeAngle < SQUAT_SQUATTING_ANGLE) {
            currentState = State.DOWN;
        }

        // Hoàn thành 1 rep: DOWN → UP
        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
            Log.d(TAG, "Squat rep completed: " + repCount);
        }
    }

    // SIT-UP LOGIC
    private void processSitup(List<NormalizedLandmark> landmarks) {
        NormalizedLandmark leftShoulder = landmarks.get(LEFT_SHOULDER);
        NormalizedLandmark rightShoulder = landmarks.get(RIGHT_SHOULDER);
        NormalizedLandmark leftHip = landmarks.get(LEFT_HIP);
        NormalizedLandmark rightHip = landmarks.get(RIGHT_HIP);
        NormalizedLandmark leftKnee = landmarks.get(LEFT_KNEE);
        NormalizedLandmark rightKnee = landmarks.get(RIGHT_KNEE);

        NormalizedLandmark midShoulder = midpoint(leftShoulder, rightShoulder);
        NormalizedLandmark midHip = midpoint(leftHip, rightHip);
        NormalizedLandmark midKnee = midpoint(leftKnee, rightKnee);

        double torsoHipAngle = calculateAngle(midShoulder, midHip, midKnee);

        State previousState = currentState;

        if (torsoHipAngle < SITUP_UP_ANGLE) {
            currentState = State.UP;
        } else if (torsoHipAngle > SITUP_DOWN_ANGLE) {
            currentState = State.DOWN;
        }

        // Hoàn thành 1 rep: DOWN → UP
        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
            Log.d(TAG, "Sit-up rep completed: " + repCount);
        }
    }

    // PUSH-UP LOGIC
    private void processPushup(List<NormalizedLandmark> landmarks) {
        double leftElbowAngle = calculateAngle(
                landmarks.get(LEFT_SHOULDER),
                landmarks.get(LEFT_ELBOW),
                landmarks.get(LEFT_WRIST)
        );

        double rightElbowAngle = calculateAngle(
                landmarks.get(RIGHT_SHOULDER),
                landmarks.get(RIGHT_ELBOW),
                landmarks.get(RIGHT_WRIST)
        );

        double avgElbowAngle = (leftElbowAngle + rightElbowAngle) / 2.0;

        State previousState = currentState;

        if (avgElbowAngle > PUSHUP_UP_ANGLE) {
            currentState = State.UP;
        } else if (avgElbowAngle < PUSHUP_DOWN_ANGLE) {
            currentState = State.DOWN;
        }

        // Hoàn thành 1 rep: DOWN → UP
        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
            Log.d(TAG, "Push-up rep completed: " + repCount);
        }
    }

    // PLANK LOGIC - Đo thời gian giữ tư thế
    private void processPlank(List<NormalizedLandmark> landmarks) {
        NormalizedLandmark leftShoulder = landmarks.get(LEFT_SHOULDER);
        NormalizedLandmark rightShoulder = landmarks.get(RIGHT_SHOULDER);
        NormalizedLandmark leftHip = landmarks.get(LEFT_HIP);
        NormalizedLandmark rightHip = landmarks.get(RIGHT_HIP);
        NormalizedLandmark leftAnkle = landmarks.get(LEFT_ANKLE);
        NormalizedLandmark rightAnkle = landmarks.get(RIGHT_ANKLE);

        NormalizedLandmark midShoulder = midpoint(leftShoulder, rightShoulder);
        NormalizedLandmark midHip = midpoint(leftHip, rightHip);
        NormalizedLandmark midAnkle = midpoint(leftAnkle, rightAnkle);

        // Tính góc lưng (shoulder-hip-ankle)
        double backAngle = calculateAngle(midShoulder, midHip, midAnkle);

        // Tính độ cao tương đối của hông
        double hipHeight = midHip.y();

        // Kiểm tra tư thế plank đúng
        boolean isCorrectPlankPose =
                backAngle >= PLANK_MIN_BACK_ANGLE &&
                        backAngle <= PLANK_MAX_BACK_ANGLE &&
                        hipHeight >= PLANK_MIN_HIP_HEIGHT &&
                        hipHeight <= PLANK_MAX_HIP_HEIGHT;

        long currentTime = System.currentTimeMillis();

        if (isCorrectPlankPose) {
            if (!isPlankHolding) {
                // Bắt đầu giữ plank
                isPlankHolding = true;
                plankHoldStartTime = currentTime;
                currentState = State.HOLDING;
                Log.d(TAG, "Plank started");
            } else {
                // Đang giữ plank - tính thời gian
                long elapsedTime = currentTime - plankHoldStartTime;
                plankHoldDuration = (int) (elapsedTime / 1000);
            }
        } else {
            if (isPlankHolding) {
                // Mất tư thế - kết thúc
                isPlankHolding = false;
                currentState = State.UNKNOWN;
                Log.d(TAG, "Plank ended. Duration: " + plankHoldDuration + "s");
            }
        }
    }

    // BICEPS CURL LOGIC - Yêu cầu cả 2 tay nâng lên
    private void processBicepsCurl(List<NormalizedLandmark> landmarks) {
        // Tính góc khuỷu tay trái (shoulder-elbow-wrist)
        double leftElbowAngle = calculateAngle(
                landmarks.get(LEFT_SHOULDER),
                landmarks.get(LEFT_ELBOW),
                landmarks.get(LEFT_WRIST)
        );

        // Tính góc khuỷu tay phải (shoulder-elbow-wrist)
        double rightElbowAngle = calculateAngle(
                landmarks.get(RIGHT_SHOULDER),
                landmarks.get(RIGHT_ELBOW),
                landmarks.get(RIGHT_WRIST)
        );

        // Kiểm tra độ lệch giữa 2 tay (để đảm bảo đồng bộ)
        double angleDifference = Math.abs(leftElbowAngle - rightElbowAngle);
        boolean armsInSync = angleDifference < BICEPS_TOLERANCE;

        // Debug log
        Log.d(TAG, String.format("Biceps Curl - Left: %.1f°, Right: %.1f°, Diff: %.1f°, Sync: %b",
                leftElbowAngle, rightElbowAngle, angleDifference, armsInSync));

        // Kiểm tra trạng thái curl (cả 2 tay phải nâng lên cùng lúc)
        boolean bothArmsCurled = leftElbowAngle < BICEPS_CURL_UP_ANGLE &&
                rightElbowAngle < BICEPS_CURL_UP_ANGLE &&
                armsInSync;

        // Kiểm tra trạng thái duỗi (cả 2 tay phải duỗi thẳng)
        boolean bothArmsExtended = leftElbowAngle > BICEPS_CURL_DOWN_ANGLE &&
                rightElbowAngle > BICEPS_CURL_DOWN_ANGLE &&
                armsInSync;

        // State machine để đếm rep
        if (bothArmsExtended && !bothArmsDown) {
            // Cả 2 tay đã duỗi thẳng xuống
            bothArmsDown = true;
            leftArmCurled = false;
            rightArmCurled = false;
            currentState = State.DOWN;
            Log.d(TAG, "Biceps Curl: Both arms down");
        }
        else if (bothArmsCurled && bothArmsDown) {
            // Cả 2 tay đã nâng lên từ trạng thái xuống → Hoàn thành 1 rep
            bothArmsDown = false;
            leftArmCurled = true;
            rightArmCurled = true;
            currentState = State.UP;
            repCount++;
            Log.d(TAG, "Biceps Curl rep completed: " + repCount);
        }

        // Cảnh báo nếu 2 tay không đồng bộ
        if (!armsInSync && (leftElbowAngle < BICEPS_CURL_DOWN_ANGLE || rightElbowAngle < BICEPS_CURL_DOWN_ANGLE)) {
            Log.w(TAG, "Biceps Curl: Arms not in sync! Left: " + leftElbowAngle + "°, Right: " + rightElbowAngle + "°");
        }
    }

    /**
     * Lấy thời gian giữ plank hiện tại (tính bằng giây)
     */
    public int getPlankDuration() {
        if (isPlankHolding) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - plankHoldStartTime;
            return (int) (elapsedTime / 1000);
        }
        return plankHoldDuration;
    }

    /**
     * Kiểm tra xem có đang giữ plank không
     */
    public boolean isPlankHolding() {
        return isPlankHolding;
    }

    /**
     * Tính góc giữa 3 điểm (A-B-C), với B là đỉnh
     */
    private double calculateAngle(NormalizedLandmark pointA,
                                  NormalizedLandmark pointB,
                                  NormalizedLandmark pointC) {
        double ba_x = pointA.x() - pointB.x();
        double ba_y = pointA.y() - pointB.y();

        double bc_x = pointC.x() - pointB.x();
        double bc_y = pointC.y() - pointB.y();

        double dotProduct = ba_x * bc_x + ba_y * bc_y;

        double magnitudeBA = Math.sqrt(ba_x * ba_x + ba_y * ba_y);
        double magnitudeBC = Math.sqrt(bc_x * bc_x + bc_y * bc_y);

        if (magnitudeBA == 0 || magnitudeBC == 0) {
            return 0;
        }

        double cosAngle = dotProduct / (magnitudeBA * magnitudeBC);
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));
        double angleRad = Math.acos(cosAngle);

        return Math.toDegrees(angleRad);
    }

    /**
     * Tính điểm giữa 2 landmark
     */
    private NormalizedLandmark midpoint(NormalizedLandmark p1, NormalizedLandmark p2) {
        float midX = (p1.x() + p2.x()) / 2.0f;
        float midY = (p1.y() + p2.y()) / 2.0f;
        float midZ = (p1.z() + p2.z()) / 2.0f;

        return NormalizedLandmark.create(midX, midY, midZ);
    }

    public int getRepCount() {
        return repCount;
    }

    public void reset() {
        repCount = 0;
        currentState = State.UNKNOWN;
        isPlankHolding = false;
        plankHoldStartTime = 0;
        plankHoldDuration = 0;

        // Reset biceps curl states
        leftArmCurled = false;
        rightArmCurled = false;
        bothArmsDown = true;
    }

    public ExerciseType getCurrentExercise() {
        return currentExercise;
    }

    public State getCurrentState() {
        return currentState;
    }
}