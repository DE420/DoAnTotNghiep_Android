package com.example.posedetection;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

public class ExerciseCounter {

    // Exercise types
    public enum ExerciseType {
        SQUAT,
        SITUP,
        PUSHUP
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
        UNKNOWN
    }

    private ExerciseType currentExercise = ExerciseType.SQUAT;
    private State currentState = State.UNKNOWN;
    private int repCount = 0;

    // Squat thresholds
    private static final double SQUAT_STANDING_ANGLE = 160.0;
    private static final double SQUAT_SQUATTING_ANGLE = 90.0;

    // Sit-up thresholds - dùng góc hip (hip-shoulder-knee)
    private static final double SITUP_UP_ANGLE = 45.0;      // Góc hip khi ngồi dậy (nhỏ)
    private static final double SITUP_DOWN_ANGLE = 100.0;   // Góc hip khi nằm xuống (lớn)

    // Push-up thresholds
    private static final double PUSHUP_UP_ANGLE = 160.0;    // Góc khuỷu tay khi đẩy lên
    private static final double PUSHUP_DOWN_ANGLE = 90.0;   // Góc khuỷu tay khi hạ xuống

    public void setExerciseType(ExerciseType type) {
        if (this.currentExercise != type) {
            this.currentExercise = type;
            reset();
        }
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
        }
    }

    // SIT-UP LOGIC
    private void processSitup(List<NormalizedLandmark> landmarks) {
        // Tính góc giữa thân (shoulder-hip) và đùi (hip-knee)
        NormalizedLandmark leftShoulder = landmarks.get(LEFT_SHOULDER);
        NormalizedLandmark rightShoulder = landmarks.get(RIGHT_SHOULDER);
        NormalizedLandmark leftHip = landmarks.get(LEFT_HIP);
        NormalizedLandmark rightHip = landmarks.get(RIGHT_HIP);
        NormalizedLandmark leftKnee = landmarks.get(LEFT_KNEE);
        NormalizedLandmark rightKnee = landmarks.get(RIGHT_KNEE);

        // Tạo điểm trung tâm
        NormalizedLandmark midShoulder = midpoint(leftShoulder, rightShoulder);
        NormalizedLandmark midHip = midpoint(leftHip, rightHip);
        NormalizedLandmark midKnee = midpoint(leftKnee, rightKnee);

        // Tính góc thân-đùi
        double torsoHipAngle = calculateAngle(midShoulder, midHip, midKnee);

        State previousState = currentState;

        if (torsoHipAngle > SITUP_UP_ANGLE) {
            currentState = State.UP;  // Ngồi dậy
        } else if (torsoHipAngle < SITUP_DOWN_ANGLE) {
            currentState = State.DOWN;  // Nằm xuống
        }

        // Hoàn thành 1 rep: DOWN → UP
        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
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
            currentState = State.UP;  // Đẩy lên
        } else if (avgElbowAngle < PUSHUP_DOWN_ANGLE) {
            currentState = State.DOWN;  // Hạ xuống
        }

        // Hoàn thành 1 rep: DOWN → UP
        if (previousState == State.DOWN && currentState == State.UP) {
            repCount++;
        }
    }

    /**
     * Tính góc giữa 3 điểm (A-B-C), với B là đỉnh
     */
    private double calculateAngle(NormalizedLandmark pointA,
                                  NormalizedLandmark pointB,
                                  NormalizedLandmark pointC) {
        // Vector BA
        double ba_x = pointA.x() - pointB.x();
        double ba_y = pointA.y() - pointB.y();

        // Vector BC
        double bc_x = pointC.x() - pointB.x();
        double bc_y = pointC.y() - pointB.y();

        // Tích vô hướng
        double dotProduct = ba_x * bc_x + ba_y * bc_y;

        // Độ dài vector
        double magnitudeBA = Math.sqrt(ba_x * ba_x + ba_y * ba_y);
        double magnitudeBC = Math.sqrt(bc_x * bc_x + bc_y * bc_y);

        // Tránh chia cho 0
        if (magnitudeBA == 0 || magnitudeBC == 0) {
            return 0;
        }

        // Tính góc (radian)
        double cosAngle = dotProduct / (magnitudeBA * magnitudeBC);
        // Clamp giá trị trong khoảng [-1, 1]
        cosAngle = Math.max(-1.0, Math.min(1.0, cosAngle));
        double angleRad = Math.acos(cosAngle);

        // Chuyển sang độ
        double angleDeg = Math.toDegrees(angleRad);

        return angleDeg;
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
    }

    public ExerciseType getCurrentExercise() {
        return currentExercise;
    }

    public State getCurrentState() {
        return currentState;
    }
}