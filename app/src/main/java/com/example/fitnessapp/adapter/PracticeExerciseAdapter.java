package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.PracticeExerciseItem;
import com.example.fitnessapp.model.response.WorkoutDayExerciseResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PracticeExerciseAdapter extends RecyclerView.Adapter<PracticeExerciseAdapter.ExerciseViewHolder> {

    private List<PracticeExerciseItem> exerciseItems = new ArrayList<>();
    private OnExerciseActionListener listener;

    public interface OnExerciseActionListener {
        void onPlayClick(PracticeExerciseItem item, int position);
    }

    public PracticeExerciseAdapter(OnExerciseActionListener listener) {
        this.listener = listener;
    }

    public void setExerciseItems(List<PracticeExerciseItem> items) {
        this.exerciseItems = items;
        notifyDataSetChanged();
    }

    public List<PracticeExerciseItem> getExerciseItems() {
        return exerciseItems;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_practice_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        PracticeExerciseItem item = exerciseItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return exerciseItems.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvExerciseName;
        private TextView tvExerciseInfo;
        private TextView tvCompletionStatus;
        private ImageButton btnAction;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tvExerciseName);
            tvExerciseInfo = itemView.findViewById(R.id.tvExerciseInfo);
            tvCompletionStatus = itemView.findViewById(R.id.tvCompletionStatus);
            btnAction = itemView.findViewById(R.id.btnAction);
        }

        public void bind(PracticeExerciseItem item, int position) {
            WorkoutDayExerciseResponse exercise = item.getExercise();

            // Set exercise name - sử dụng helper method
            tvExerciseName.setText(exercise.getExerciseName());

            // Build exercise info
            String exerciseInfo = buildExerciseInfo(exercise);
            tvExerciseInfo.setText(exerciseInfo);

            // Set completion status
            int totalSets = exercise.getSets() != null ? exercise.getSets() : 0;
            String completionText = String.format(Locale.getDefault(),
                    "Số set đã hoàn thành: %d / %d",
                    item.getCompletedSets(),
                    totalSets);
            tvCompletionStatus.setText(completionText);

            // Update button state
            if (item.isCompleted()) {
                btnAction.setImageResource(R.drawable.ic_check);
                btnAction.setEnabled(false);
            } else {
                btnAction.setImageResource(R.drawable.ic_play);
                btnAction.setEnabled(true);
                btnAction.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPlayClick(item, position);
                    }
                });
            }
        }

        private String buildExerciseInfo(WorkoutDayExerciseResponse exercise) {
            StringBuilder info = new StringBuilder();

            boolean hasReps = exercise.getReps() != null && exercise.getReps() > 0;
            boolean hasDuration = exercise.getDuration() != null && exercise.getDuration() > 0;
            boolean hasWeight = exercise.getWeight() != null && exercise.getWeight() > 0;

            // Xử lý Reps hoặc Duration
            if (hasReps) {
                info.append(exercise.getReps()).append(" reps / set");
            } else if (hasDuration) {
                info.append(exercise.getDuration()).append(" s / set");
            }

            // Thêm Weight nếu có
            if (hasWeight) {
                if (info.length() > 0) {
                    info.append(" ");
                }
                info.append("(Tạ ").append(String.format(Locale.getDefault(), "%.0f", exercise.getWeight())).append("kg)");
            }

            // Nếu không có thông tin gì, hiển thị số sets
            if (info.length() == 0 && exercise.getSets() != null && exercise.getSets() > 0) {
                info.append(exercise.getSets()).append(" sets");
            }

            return info.length() > 0 ? info.toString() : "No info";
        }
    }
}