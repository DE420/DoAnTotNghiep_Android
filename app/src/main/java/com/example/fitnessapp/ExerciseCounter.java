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
        BICEPS_CURL,
        PULLUP,
        BACK_LEVER,
        DEADLIFT
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
        HOLDING,
        UNKNOWN
    }

    private ExerciseType currentExercise = ExerciseType.SQUAT;
    private State currentState = State.UNKNOWN;
    private int repCount = 0;

    // Duration tracking - Chuẩn hóa cho tất cả bài tập duration
    private boolean isDurationHolding = false;
    private long durationHoldStartTime = 0;
    private int durationHoldTime = 0; // in seconds
    private int durationFramesCorrect = 0;
    private static final int DURATION_FRAMES_THRESHOLD = 3;

    // Biceps curl tracking
    private boolean leftArmCurled = false;
    private boolean rightArmCurled = false;
    private boolean bothArmsDown = true;

    // Squat thresholds
    private static final double SQUAT_STANDING_ANGLE = 160.0;
    private static final double SQUAT_SQUATTING_ANGLE = 90.0;

    // Sit-up thresholds
    private static final double SITUP_UP_ANGLE = 45.0;
    private static final double SITUP_DOWN_ANGLE = 100.0;

    // Push-up thresholds
    private static final double PUSHUP_UP_ANGLE = 160.0;
    private static final double PUSHUP_DOWN_ANGLE = 90.0;

    // Pull-up thresholds
    private static final double PULLUP_DOWN_ANGLE = 160.0;
    private static final double PULLUP_UP_ANGLE = 50.0;

    // Plank thresholds
    private static final double PLANK_MIN_BODY_ANGLE = 150.0;
    private static final double PLANK_MAX_BODY_ANGLE = 200.0;
    private static final double PLANK_MIN_ELBOW_ANGLE = 70.0;
    private static final double PLANK_MAX_ELBOW_ANGLE = 110.0;
    private static final double PLANK_MAX_SHOULDER_HIP_DIFF = 0.15;

    // Back Lever thresholds
    private static final double BACK_LEVER_MIN_BODY_ANGLE = 160.0;
    private static final double BACK_LEVER_MAX_BODY_ANGLE = 200.0;
    private static final double BACK_LEVER_MIN_HIP_HEIGHT = 0.35;
    private static final double BACK_LEVER_MAX_HIP_HEIGHT = 0.65;
    private static final double BACK_LEVER_MAX_SHOULDER_HIP_DIFF = 0.1;

    // Biceps curl thresholds
    private static final double BICEPS_CURL_UP_ANGLE = 50.0;
    private static final double BICEPS_CURL_DOWN_ANGLE = 150.0;
    private static final double BICEPS_TOLERANCE = 20.0;

    // Deadlift thresholds
    private static final double DEADLIFT_STANDING_ANGLE = 160.0;  // Đứng thẳng (hip-knee-ankle)
    private static final double DEADLIFT_BENT_ANGLE = 70.0;       // Cúi xuống
    private static final double DEADLIFT_HIP_ANGLE_MIN = 130.0;   // Góc hông khi đứng (shoulder-hip-knee)
    private static final double DEADLIFT_HIP_ANGLE_BENT = 60.0;   // Góc hông khi cúi

    public void setExerciseType(ExerciseType type) {
        if (this.currentExercise != type) {
            this.currentExercise = type;
            reset();
        }
    }

    public ExerciseType getExerciseType() {
        return currentExercise;
    }

    /**
     * Kiểm tra xem bài tập hiện tại có phải dạng duration không
     */
    public boolean isDurationBasedExercise() {
        return currentExercise == ExerciseType.PLANK ||
                currentExercise == ExerciseType.BACK_LEVER;
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
            case PULLUP:
                processPullup(landmarks);
                break;
            case PLANK:
                processPlank(landmarks);
                break;
            case BACK_LEVER:
                processBackLever(landmarks);
                break;
            case BICEPS_CURL:
                processBicepsCurl(landmarks);
                break;
            case DEADLIFT:
                processDeadlift(landmarks);
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

        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
            Log.d(TAG, "Push-up rep completed: " + repCount);
        }
    }

    // PULL-UP LOGIC
    private void processPullup(List<NormalizedLandmark> landmarks) {
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

        if (avgElbowAngle > PULLUP_DOWN_ANGLE) {
            currentState = State.DOWN;
        } else if (avgElbowAngle < PULLUP_UP_ANGLE) {
            currentState = State.UP;
        }

        if (previousState == State.UP && currentState == State.DOWN) {
            repCount++;
            Log.d(TAG, "Pull-up rep completed: " + repCount);
        }
    }

    // PLANK LOGIC
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

        double bodyAngle = calculateAngle(midShoulder, midHip, midAnkle);

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

        double shoulderY = midShoulder.y();
        double hipY = midHip.y();
        double shoulderHipDiff = Math.abs(shoulderY - hipY);

        boolean hipNotTooHigh = hipY >= shoulderY - 0.05;
        boolean hipNotTooLow = hipY <= shoulderY + 0.2;

        boolean bodyAligned = bodyAngle >= PLANK_MIN_BODY_ANGLE &&
                bodyAngle <= PLANK_MAX_BODY_ANGLE;
        boolean elbowsCorrect = avgElbowAngle >= PLANK_MIN_ELBOW_ANGLE &&
                avgElbowAngle <= PLANK_MAX_ELBOW_ANGLE;
        boolean heightCorrect = shoulderHipDiff < PLANK_MAX_SHOULDER_HIP_DIFF &&
                hipNotTooHigh && hipNotTooLow;

        boolean isCorrectPose = bodyAligned && elbowsCorrect && heightCorrect;

        processDurationExercise(isCorrectPose, "Plank");
    }

    // BACK LEVER LOGIC
    private void processBackLever(List<NormalizedLandmark> landmarks) {
        NormalizedLandmark leftShoulder = landmarks.get(LEFT_SHOULDER);
        NormalizedLandmark rightShoulder = landmarks.get(RIGHT_SHOULDER);
        NormalizedLandmark leftHip = landmarks.get(LEFT_HIP);
        NormalizedLandmark rightHip = landmarks.get(RIGHT_HIP);
        NormalizedLandmark leftKnee = landmarks.get(LEFT_KNEE);
        NormalizedLandmark rightKnee = landmarks.get(RIGHT_KNEE);

        NormalizedLandmark midShoulder = midpoint(leftShoulder, rightShoulder);
        NormalizedLandmark midHip = midpoint(leftHip, rightHip);
        NormalizedLandmark midKnee = midpoint(leftKnee, rightKnee);

        double bodyAngle = calculateAngle(midShoulder, midHip, midKnee);

        double shoulderY = midShoulder.y();
        double hipY = midHip.y();
        double shoulderHipDiff = Math.abs(shoulderY - hipY);

        boolean hipHeightCorrect = hipY >= BACK_LEVER_MIN_HIP_HEIGHT &&
                hipY <= BACK_LEVER_MAX_HIP_HEIGHT;

        boolean bodyAligned = bodyAngle >= BACK_LEVER_MIN_BODY_ANGLE &&
                bodyAngle <= BACK_LEVER_MAX_BODY_ANGLE;
        boolean heightCorrect = shoulderHipDiff < BACK_LEVER_MAX_SHOULDER_HIP_DIFF &&
                hipHeightCorrect;

        boolean isCorrectPose = bodyAligned && heightCorrect;

        processDurationExercise(isCorrectPose, "Back Lever");
    }

    // BICEPS CURL LOGIC
    private void processBicepsCurl(List<NormalizedLandmark> landmarks) {
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

        double angleDifference = Math.abs(leftElbowAngle - rightElbowAngle);
        boolean armsInSync = angleDifference < BICEPS_TOLERANCE;

        boolean bothArmsCurled = leftElbowAngle < BICEPS_CURL_UP_ANGLE &&
                rightElbowAngle < BICEPS_CURL_UP_ANGLE &&
                armsInSync;

        boolean bothArmsExtended = leftElbowAngle > BICEPS_CURL_DOWN_ANGLE &&
                rightElbowAngle > BICEPS_CURL_DOWN_ANGLE &&
                armsInSync;

        if (bothArmsExtended && !bothArmsDown) {
            bothArmsDown = true;
            leftArmCurled = false;
            rightArmCurled = false;
            currentState = State.DOWN;
            Log.d(TAG, "Biceps Curl: Both arms down");
        }
        else if (bothArmsCurled && bothArmsDown) {
            bothArmsDown = false;
            leftArmCurled = true;
            rightArmCurled = true;
            currentState = State.UP;
            repCount++;
            Log.d(TAG, "Biceps Curl rep completed: " + repCount);
        }

        if (!armsInSync && (leftElbowAngle < BICEPS_CURL_DOWN_ANGLE || rightElbowAngle < BICEPS_CURL_DOWN_ANGLE)) {
            Log.w(TAG, "Biceps Curl: Arms not in sync!");
        }
    }

    private void processDeadlift(List<NormalizedLandmark> landmarks) {
        NormalizedLandmark leftShoulder = landmarks.get(LEFT_SHOULDER);
        NormalizedLandmark rightShoulder = landmarks.get(RIGHT_SHOULDER);
        NormalizedLandmark leftHip = landmarks.get(LEFT_HIP);
        NormalizedLandmark rightHip = landmarks.get(RIGHT_HIP);
        NormalizedLandmark leftKnee = landmarks.get(LEFT_KNEE);
        NormalizedLandmark rightKnee = landmarks.get(RIGHT_KNEE);
        NormalizedLandmark leftAnkle = landmarks.get(LEFT_ANKLE);
        NormalizedLandmark rightAnkle = landmarks.get(RIGHT_ANKLE);

        NormalizedLandmark midShoulder = midpoint(leftShoulder, rightShoulder);
        NormalizedLandmark midHip = midpoint(leftHip, rightHip);
        NormalizedLandmark midKnee = midpoint(leftKnee, rightKnee);
        NormalizedLandmark midAnkle = midpoint(leftAnkle, rightAnkle);

        // 1. Góc hip-knee-ankle (chân)
        double legAngle = calculateAngle(midHip, midKnee, midAnkle);

        // 2. Góc shoulder-hip-knee (thân người)
        double hipAngle = calculateAngle(midShoulder, midHip, midKnee);

        Log.d(TAG, String.format("Deadlift - Leg angle: %.1f°, Hip angle: %.1f°",
                legAngle, hipAngle));

        State previousState = currentState;

        // Tư thế đứng thẳng: chân duỗi, thân thẳng
        if (legAngle > DEADLIFT_STANDING_ANGLE && hipAngle > DEADLIFT_HIP_ANGLE_MIN) {
            currentState = State.UP;
        }
        // Tư thế cúi xuống: chân hơi gập, thân cúi
        else if (legAngle < DEADLIFT_BENT_ANGLE && hipAngle < DEADLIFT_HIP_ANGLE_BENT) {
            currentState = State.DOWN;
        }

        // Hoàn thành 1 rep: DOWN (cúi) -> UP (đứng)
        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
            Log.d(TAG, "Deadlift rep completed: " + repCount);
        }
    }

    /**
     * Xử lý chung cho tất cả bài tập duration
     */
    private void processDurationExercise(boolean isCorrectPose, String exerciseName) {
        long currentTime = System.currentTimeMillis();

        if (isCorrectPose) {
            durationFramesCorrect++;

            if (durationFramesCorrect >= DURATION_FRAMES_THRESHOLD) {
                if (!isDurationHolding) {
                    isDurationHolding = true;
                    durationHoldStartTime = currentTime;
                    currentState = State.HOLDING;
                    Log.d(TAG, exerciseName + " STARTED - holding...");
                } else {
                    long elapsedTime = currentTime - durationHoldStartTime;
                    durationHoldTime = (int) (elapsedTime / 1000);
                }
            }
        } else {
            if (durationFramesCorrect > 0) {
                Log.d(TAG, exerciseName + " pose incorrect, resetting frame counter");
            }
            durationFramesCorrect = 0;

            if (isDurationHolding) {
                isDurationHolding = false;
                currentState = State.UNKNOWN;
                Log.d(TAG, exerciseName + " ENDED. Total duration: " + durationHoldTime + "s");
            }
        }
    }

    /**
     * Lấy thời gian giữ cho bài tập duration (tính bằng giây)
     */
    public int getDurationTime() {
        if (isDurationHolding) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - durationHoldStartTime;
            return (int) (elapsedTime / 1000);
        }
        return durationHoldTime;
    }

    /**
     * Kiểm tra xem có đang giữ tư thế duration không
     */
    public boolean isDurationHolding() {
        return isDurationHolding;
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

        // Reset duration tracking
        isDurationHolding = false;
        durationHoldStartTime = 0;
        durationHoldTime = 0;
        durationFramesCorrect = 0;

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