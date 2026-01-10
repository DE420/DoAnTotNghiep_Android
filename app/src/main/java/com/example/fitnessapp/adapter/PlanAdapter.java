package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.PlanResponse;
import com.example.fitnessapp.enums.DifficultyLevel;
import com.example.fitnessapp.enums.FitnessGoal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    private List<PlanResponse> plans = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isMyPlanView = false;

    public interface OnItemClickListener {
        void onItemClick(PlanResponse plan);
    }

    public PlanAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setPlans(List<PlanResponse> plans) {
        this.plans.clear();
        if (plans != null) {
            this.plans.addAll(plans);
        }
        notifyDataSetChanged();
    }

    public void addPlans(List<PlanResponse> newPlans) {
        if (newPlans != null && !newPlans.isEmpty()) {
            int startPosition = plans.size();
            plans.addAll(newPlans);
            notifyItemRangeInserted(startPosition, newPlans.size());
        }
    }

    public void setIsMyPlanView(boolean isMyPlanView) {
        this.isMyPlanView = isMyPlanView;
        notifyDataSetChanged(); // Refresh all items to show/hide start date
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanResponse plan = plans.get(position);
        holder.bind(plan, listener, isMyPlanView);
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    static class PlanViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvStartDate;
        TextView tvLevel;
        TextView tvGoal;
        TextView tvDuration;
        TextView tvDaysPerWeek;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_plan_item_name);
            tvStartDate = itemView.findViewById(R.id.tv_plan_item_start_date);
            tvLevel = itemView.findViewById(R.id.tv_plan_item_level);
            tvGoal = itemView.findViewById(R.id.tv_plan_item_goal);
            tvDuration = itemView.findViewById(R.id.tv_plan_item_duration);
            tvDaysPerWeek = itemView.findViewById(R.id.tv_plan_item_days_per_week);
        }

        public void bind(PlanResponse plan, OnItemClickListener listener, boolean isMyPlanView) {
            tvName.setText(plan.getName());

            // Show/hide Start Date based on isMyPlanView flag
            if (isMyPlanView) {
                tvStartDate.setVisibility(View.VISIBLE);
                if (plan.getStartDate() != null && !plan.getStartDate().isEmpty()) {
                    tvStartDate.setText("Bắt đầu từ " + plan.getStartDate());
                } else {
                    tvStartDate.setText("Chưa có ngày bắt đầu");
                }
            } else {
                tvStartDate.setVisibility(View.GONE);
            }

            // Set Level
            if (plan.getDifficultyLevel() != null) {
                tvLevel.setText(itemView.getContext().getString(plan.getDifficultyLevel().getResId()));
                tvLevel.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
                String levelText = plan.getDifficultyLevel().name().toLowerCase(Locale.ROOT);
                switch (levelText) {
                    case "beginner":
                        tvLevel.setBackgroundResource(R.drawable.bg_level_beginner);
                        break;
                    case "intermediate":
                        tvLevel.setBackgroundResource(R.drawable.bg_level_intermediate);
                        break;
                    case "advanced":
                        tvLevel.setBackgroundResource(R.drawable.bg_level_advanced);
                        break;
                    default:
                        tvLevel.setBackgroundResource(R.drawable.bg_level_default);
                        break;
                }
            } else {
                tvLevel.setText("N/A");
                tvLevel.setBackgroundResource(R.drawable.bg_level_default);
                tvLevel.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }

            // Set Goal
            if (plan.getTargetGoal() != null) {
                tvGoal.setText(itemView.getContext().getString(plan.getTargetGoal().getResId())); // Use string resource from enum
                tvGoal.setBackgroundResource(R.drawable.bg_goal_tag); // Apply common goal tag background
                tvGoal.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            } else {
                tvGoal.setText("N/A");
                tvGoal.setBackgroundResource(R.drawable.bg_goal_tag);
                tvGoal.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            }

            // Set Duration
            if (plan.getDurationWeek() != null) {
                tvDuration.setText(plan.getDurationWeek() + " tuần");
            } else {
                tvDuration.setText("N/A");
            }

            // Set Days Per Week
            if (plan.getDaysPerWeek() != null) {
                tvDaysPerWeek.setText(plan.getDaysPerWeek() + " ngày / tuần");
            } else {
                tvDaysPerWeek.setText("N/A");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(plan);
                }
            });
        }
    }
}