package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.WorkoutDayLog;
import java.util.List;

public class WorkoutLogExerciseAdapter extends RecyclerView.Adapter<WorkoutLogExerciseAdapter.ViewHolder> {
    private List<WorkoutDayLog.WorkoutExerciseLog> exercises;

    public WorkoutLogExerciseAdapter(List<WorkoutDayLog.WorkoutExerciseLog> exercises) {
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_log_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutDayLog.WorkoutExerciseLog exercise = exercises.get(position);

        holder.tvExerciseName.setText(exercise.getExerciseName());
        holder.tvReps.setText(String.format("Reps: %d", exercise.getReps()));
        holder.tvSets.setText(String.format("Sets: %d", exercise.getSets()));
        holder.tvCalories.setText(String.format("Calo: %d", exercise.getCalories()));

        // Show duration if available
        if (exercise.getDuration() != null && exercise.getDuration() > 0) {
            holder.tvDuration.setVisibility(View.VISIBLE);
            holder.tvDuration.setText(String.format("Thời gian: %ds", exercise.getDuration()));
        } else {
            holder.tvDuration.setVisibility(View.GONE);
        }

        // Show weight if available
        if (exercise.getWeight() != null && exercise.getWeight() > 0) {
            holder.tvWeight.setVisibility(View.VISIBLE);
            holder.tvWeight.setText(String.format("Cân nặng (tạ): %.1fkg", exercise.getWeight()));
        } else {
            holder.tvWeight.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvExerciseName;
        TextView tvReps;
        TextView tvSets;
        TextView tvDuration;
        TextView tvWeight;
        TextView tvCalories;

        ViewHolder(View itemView) {
            super(itemView);
            tvExerciseName = itemView.findViewById(R.id.tv_exercise_name);
            tvReps = itemView.findViewById(R.id.tv_reps);
            tvSets = itemView.findViewById(R.id.tv_sets);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvWeight = itemView.findViewById(R.id.tv_weight);
            tvCalories = itemView.findViewById(R.id.tv_calories);
        }
    }
}