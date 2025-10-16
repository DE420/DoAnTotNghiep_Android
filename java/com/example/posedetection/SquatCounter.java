package com.example.posedetection;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

public class SquatCounter {

    // Pose landmark indices
    private static final int LEFT_HIP = 23;
    private static final int LEFT_KNEE = 25;
    private static final int LEFT_ANKLE = 27;
    private static final int RIGHT_HIP = 24;
    private static final int RIGHT_KNEE = 26;
    private static final int RIGHT_ANKLE = 28;

    // Squat states
    private enum SquatState {
        STANDING,
        SQUATTING,
        UNKNOWN
    }

    private SquatState currentState = SquatState.UNKNOWN;
    private int repCount = 0;

    // Threshold angles
    private static final double STANDING_ANGLE = 160.0; // Góc đầu gối khi đứng
    private static final double SQUATTING_ANGLE = 90.0; // Góc đầu gối khi ngồi

    public int processLandmarks(List<NormalizedLandmark> landmarks) {
        if (landmarks == null || landmarks.size() < 33) {
            return repCount;
        }

        // Tính góc đầu gối trái và phải
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

        // Lấy góc trung bình
        double avgKneeAngle = (leftKneeAngle + rightKneeAngle) / 2.0;

        // State machine để đếm rep
        SquatState previousState = currentState;

        if (avgKneeAngle > STANDING_ANGLE) {
            currentState = SquatState.STANDING;
        } else if (avgKneeAngle < SQUATTING_ANGLE) {
            currentState = SquatState.SQUATTING;
        }

        // Hoàn thành 1 rep: từ SQUATTING → STANDING
        if (previousState == SquatState.SQUATTING &&
                currentState == SquatState.STANDING) {
            repCount++;
        }

        return repCount;
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

        // Tính góc (radian)
        double angleRad = Math.acos(dotProduct / (magnitudeBA * magnitudeBC));

        // Chuyển sang độ
        double angleDeg = Math.toDegrees(angleRad);

        return angleDeg;
    }

    public int getRepCount() {
        return repCount;
    }

    public void reset() {
        repCount = 0;
        currentState = SquatState.UNKNOWN;
    }

    public SquatState getCurrentState() {
        return currentState;
    }
}