package com.example.fitnessapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.PlanResponse;

import java.util.ArrayList;
import java.util.List;

public class PracticePlanAdapter extends RecyclerView.Adapter<PracticePlanAdapter.PlanViewHolder> {

    private List<PlanResponse> planList = new ArrayList<>();
    private OnPlanClickListener listener;

    public interface OnPlanClickListener {
        void onPlayClick(PlanResponse plan);
    }

    public PracticePlanAdapter(OnPlanClickListener listener) {
        this.listener = listener;
    }

    public void setPlanList(List<PlanResponse> planList) {
        this.planList = planList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_practice_plan, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanResponse plan = planList.get(position);

        holder.tvPlanName.setText(plan.getName());
        holder.tvExerciseCount.setText(plan.getExerciseCount() + " bài tập");

        // NEW: Hiển thị icon dựa trên trạng thái hoàn thành
        if (plan.isCompleted()) {
            holder.btnPlay.setImageResource(R.drawable.ic_check); // Icon dấu tick
            holder.btnPlay.setColorFilter(Color.WHITE);
        } else {
            holder.btnPlay.setImageResource(R.drawable.ic_play); // Icon play
            holder.btnPlay.setColorFilter(android.graphics.Color.WHITE);
        }

        holder.btnPlay.setOnClickListener(v -> {
            if (listener != null && !plan.isCompleted()) {
                listener.onPlayClick(plan);
            }
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    class PlanViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPlanName;
        private TextView tvExerciseCount;
        private ImageButton btnPlay;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanName = itemView.findViewById(R.id.tvPlanName);
            tvExerciseCount = itemView.findViewById(R.id.tvExerciseCount);
            btnPlay = itemView.findViewById(R.id.btnPlay);
        }

        public void bind(PlanResponse plan) {
            tvPlanName.setText(plan.getName());

            // Hiển thị số lượng bài tập
            int exerciseCount = plan.getExerciseCount() != null ? plan.getExerciseCount() : 0;
            tvExerciseCount.setText(exerciseCount + " bài tập");

            btnPlay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPlayClick(plan);
                }
            });
        }
    }
}