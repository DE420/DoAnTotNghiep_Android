package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.WorkoutDayLog;
import java.util.List;

public class WorkoutDayLogAdapter extends RecyclerView.Adapter<WorkoutDayLogAdapter.ViewHolder> {
    private List<WorkoutDayLog> dayLogs;

    public WorkoutDayLogAdapter(List<WorkoutDayLog> dayLogs) {
        this.dayLogs = dayLogs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_day, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutDayLog dayLog = dayLogs.get(position);
        holder.tvDayName.setText(dayLog.getDate());

        WorkoutLogExerciseAdapter exerciseAdapter = new WorkoutLogExerciseAdapter(dayLog.getExercises());
        holder.recyclerViewExercises.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerViewExercises.setAdapter(exerciseAdapter);
    }

    @Override
    public int getItemCount() {
        return dayLogs != null ? dayLogs.size() : 0;
    }

    public void updateData(List<WorkoutDayLog> newDayLogs) {
        this.dayLogs = newDayLogs;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        RecyclerView recyclerViewExercises;

        ViewHolder(View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tv_plan_day_name);
            recyclerViewExercises = itemView.findViewById(R.id.recycler_view_day_exercises);
        }
    }
}