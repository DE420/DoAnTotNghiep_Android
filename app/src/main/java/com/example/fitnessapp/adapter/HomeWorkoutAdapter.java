package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.databinding.ItemHomeWorkoutCardBinding;
import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;
import com.example.fitnessapp.model.response.PlanResponse;

import java.util.ArrayList;
import java.util.List;

public class HomeWorkoutAdapter extends RecyclerView.Adapter<HomeWorkoutAdapter.ViewHolder> {

    private List<PlanResponse> workoutPlans = new ArrayList<>();
    private OnWorkoutClickListener listener;

    public interface OnWorkoutClickListener {
        void onWorkoutClick(PlanResponse plan);
    }

    public HomeWorkoutAdapter(OnWorkoutClickListener listener) {
        this.listener = listener;
    }

    public void setWorkoutPlans(List<PlanResponse> plans) {
        this.workoutPlans = plans != null ? plans : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHomeWorkoutCardBinding binding = ItemHomeWorkoutCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(workoutPlans.get(position));
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemHomeWorkoutCardBinding binding;

        ViewHolder(ItemHomeWorkoutCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PlanResponse plan) {
            // Set plan name and description
            binding.tvPlanName.setText(plan.getName());
            binding.tvPlanDescription.setText(plan.getDescription());

            // Set duration
            String duration = itemView.getContext().getString(
                    R.string.home_workout_weeks_format,
                    plan.getDurationWeek() != null ? plan.getDurationWeek() : 0
            );
            binding.tvDuration.setText(duration);

            // Set days per week
            String days = itemView.getContext().getString(
                    R.string.home_workout_days_format,
                    plan.getDaysPerWeek() != null ? plan.getDaysPerWeek() : 0
            );
            binding.tvDaysPerWeek.setText(days);

            // Set difficulty
            String difficulty = getDifficultyText(plan.getDifficultyLevel());
            binding.tvDifficulty.setText(difficulty);

            // Set goal
            String goal = getGoalText(plan.getTargetGoal());
            binding.tvGoal.setText(goal);

            // Click listener
            binding.btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onWorkoutClick(plan);
                }
            });
        }

        private String getDifficultyText(DifficultyLevel level) {
            if (level == null) return "";
            switch (level) {
                case BEGINNER: return "Dễ";
                case INTERMEDIATE: return "Trung bình";
                case ADVANCED: return "Nâng cao";
                default: return "";
            }
        }

        private String getGoalText(FitnessGoal goal) {
            if (goal == null) return "";
            switch (goal) {
                case LOSE_WEIGHT: return "Giảm cân";
                case MUSCLE_GAIN: return "Tăng cơ";
                case GAIN_WEIGHT: return "Tăng cân";
                case SHAPE_BODY: return "Săn chắc";
                case OTHERS: return "Khác";
                default: return "";
            }
        }
    }
}
