package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.PlanExerciseDetailResponse; // Đảm bảo đường dẫn này đúng

import java.util.List;
import java.util.Locale;

public class PlanExerciseAdapter extends RecyclerView.Adapter<PlanExerciseAdapter.ExerciseDetailViewHolder> {

    private List<PlanExerciseDetailResponse> exercises;

    public PlanExerciseAdapter(List<PlanExerciseDetailResponse> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ExerciseDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_exercise, parent, false);
        return new ExerciseDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseDetailViewHolder holder, int position) {
        PlanExerciseDetailResponse exercise = exercises.get(position);
        holder.bind(exercise);
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    static class ExerciseDetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseSummary;

        public ExerciseDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvExerciseSummary = itemView.findViewById(R.id.tv_exercise_detail_summary);
        }

        public void bind(PlanExerciseDetailResponse exercise) {
            StringBuilder summary = new StringBuilder();
            summary.append(exercise.getExerciseName());

            // --- START OF MODIFIED LOGIC ---
            boolean hasSets = exercise.getSets() != null && exercise.getSets() > 0;
            boolean hasReps = exercise.getReps() != null && exercise.getReps() > 0;
            boolean hasDuration = exercise.getDuration() != null && exercise.getDuration() > 0;

            // Xử lý Sets và Reps
            if (hasSets) {
                summary.append(" - ").append(exercise.getSets()).append(" sets");
                if (hasReps) {
                    summary.append(" x ").append(exercise.getReps()).append(" reps");
                }
            }

            // Xử lý Duration
            if (hasDuration) {
                if (hasSets) { // Nếu đã có sets/reps, duration sẽ được thêm vào với ' x '
                    summary.append(" x ").append(exercise.getDuration()).append(" seconds");
                } else { // Nếu không có sets/reps, duration là thông số chính
                    summary.append(" - ").append(exercise.getDuration()).append(" seconds");
                }
            }

            // Xử lý Weight
            if (exercise.getWeight() != null && exercise.getWeight() > 0) {
                // Thêm dấu cách nếu trước đó đã có thông tin về sets/reps/duration
                if (hasSets || hasDuration) {
                    summary.append(" ");
                } else {
                    summary.append(" - "); // Nếu chưa có sets/reps/duration, thêm dấu "-"
                }
                summary.append("(").append(String.format(Locale.getDefault(), "%.1f", exercise.getWeight())).append(" kg)");
            }
            // --- END OF MODIFIED LOGIC ---

            tvExerciseSummary.setText(summary.toString());
        }
    }
}