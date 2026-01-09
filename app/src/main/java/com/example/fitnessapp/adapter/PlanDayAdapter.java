package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.PlanDayResponse;

import java.util.List;

public class PlanDayAdapter extends RecyclerView.Adapter<PlanDayAdapter.PlanDayViewHolder> {

    private List<PlanDayResponse> days;

    public PlanDayAdapter(List<PlanDayResponse> days) {
        this.days = days;
    }

    @NonNull
    @Override
    public PlanDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plan_day, parent, false);
        return new PlanDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanDayViewHolder holder, int position) {
        PlanDayResponse day = days.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return days != null ? days.size() : 0;
    }

    static class PlanDayViewHolder extends RecyclerView.ViewHolder {
        TextView tvPlanDayName;
        RecyclerView recyclerViewDayExercises;
        PlanExerciseAdapter exerciseAdapter;

        public PlanDayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPlanDayName = itemView.findViewById(R.id.tv_plan_day_name);
            recyclerViewDayExercises = itemView.findViewById(R.id.recycler_view_day_exercises);
            recyclerViewDayExercises.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerViewDayExercises.setNestedScrollingEnabled(false); // Quan trọng cho RecyclerView lồng nhau
        }

        public void bind(PlanDayResponse day) {
            // Chuyển đổi dayOfWeek thành tên ngày (ví dụ: 1 -> Monday)
            String dayName = getDayName(day.getDayOfWeek());
            tvPlanDayName.setText(dayName);

            // Cập nhật RecyclerView con cho các bài tập
            if (exerciseAdapter == null) {
                exerciseAdapter = new PlanExerciseAdapter(day.getExercises());
                recyclerViewDayExercises.setAdapter(exerciseAdapter);
            } else {
                // Nếu danh sách bài tập thay đổi, bạn có thể cần một phương thức `setExercises` trong PlanExerciseAdapter
                // exerciseAdapter.setExercises(day.getExercises());
                exerciseAdapter = new PlanExerciseAdapter(day.getExercises()); // Tạo lại adapter nếu đơn giản
                recyclerViewDayExercises.setAdapter(exerciseAdapter);
                exerciseAdapter.notifyDataSetChanged();
            }
        }

        private String getDayName(int dayOfWeek) {
            // Android Calendar.MONDAY = 2, Calendar.SUNDAY = 1, etc.
            // Nếu dayOfWeek từ BE là 1=Monday, 7=Sunday:
            switch (dayOfWeek) {
                case 1: return "Monday";
                case 2: return "Tuesday";
                case 3: return "Wednesday";
                case 4: return "Thursday";
                case 5: return "Friday";
                case 6: return "Saturday";
                case 0: return "Sunday";
                default: return "Day " + dayOfWeek;
            }
        }
    }
}