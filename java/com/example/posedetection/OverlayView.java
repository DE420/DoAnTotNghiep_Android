package com.example.posedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;

import java.util.List;

public class OverlayView extends View {

    private Paint landmarkPaint;
    private Paint connectionPaint;
    private List<NormalizedLandmark> poseLandmarks;
    private int imageWidth;
    private int imageHeight;
    private boolean isFrontCamera = true; // Mặc định là camera trước

    // Pose connections (33 landmarks)
    private static final int[][] POSE_CONNECTIONS = {
            // Face
            {0, 1}, {1, 2}, {2, 3}, {3, 7}, {0, 4}, {4, 5}, {5, 6}, {6, 8},
            // Body
            {9, 10},
            // Arms
            {11, 12}, {11, 13}, {13, 15}, {15, 17}, {15, 19}, {15, 21},
            {12, 14}, {14, 16}, {16, 18}, {16, 20}, {16, 22},
            // Torso
            {11, 23}, {12, 24}, {23, 24},
            // Legs
            {23, 25}, {25, 27}, {27, 29}, {29, 31}, {27, 31},
            {24, 26}, {26, 28}, {28, 30}, {30, 32}, {28, 32}
    };

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Paint cho landmarks (điểm)
        landmarkPaint = new Paint();
        landmarkPaint.setColor(Color.RED);
        landmarkPaint.setStrokeWidth(8f);
        landmarkPaint.setStyle(Paint.Style.FILL);
        landmarkPaint.setAntiAlias(true);

        // Paint cho connections (đường nối)
        connectionPaint = new Paint();
        connectionPaint.setColor(Color.GREEN);
        connectionPaint.setStrokeWidth(6f);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setAntiAlias(true);
    }

    public void setPoseLandmarks(List<NormalizedLandmark> landmarks,
                                 int imageWidth,
                                 int imageHeight) {
        this.poseLandmarks = landmarks;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        invalidate(); // Redraw
    }

    public void setFrontCamera(boolean isFrontCamera) {
        this.isFrontCamera = isFrontCamera;
    }

    public void clear() {
        this.poseLandmarks = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (poseLandmarks == null || poseLandmarks.isEmpty()) {
            return;
        }

        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;

        // Vẽ connections trước (đường nối giữa các điểm)
        for (int[] connection : POSE_CONNECTIONS) {
            if (connection[0] < poseLandmarks.size() &&
                    connection[1] < poseLandmarks.size()) {

                NormalizedLandmark start = poseLandmarks.get(connection[0]);
                NormalizedLandmark end = poseLandmarks.get(connection[1]);

                // Chỉ vẽ nếu visibility đủ cao
                if (start.visibility().isPresent() && end.visibility().isPresent() &&
                        start.visibility().get() > 0.5f && end.visibility().get() > 0.5f) {

                    // Mirror nếu là camera trước
                    float startX = isFrontCamera ?
                            (1 - start.x()) * imageWidth * scaleX :
                            start.x() * imageWidth * scaleX;
                    float startY = start.y() * imageHeight * scaleY;

                    float endX = isFrontCamera ?
                            (1 - end.x()) * imageWidth * scaleX :
                            end.x() * imageWidth * scaleX;
                    float endY = end.y() * imageHeight * scaleY;

                    canvas.drawLine(startX, startY, endX, endY, connectionPaint);
                }
            }
        }

        // Vẽ landmarks (các điểm) sau
        for (NormalizedLandmark landmark : poseLandmarks) {
            if (landmark.visibility().isPresent() &&
                    landmark.visibility().get() > 0.5f) {

                // Mirror nếu là camera trước
                float x = isFrontCamera ?
                        (1 - landmark.x()) * imageWidth * scaleX :
                        landmark.x() * imageWidth * scaleX;
                float y = landmark.y() * imageHeight * scaleY;

                canvas.drawCircle(x, y, 12f, landmarkPaint);
            }
        }
    }
}