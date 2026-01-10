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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PlanDayAdapter extends RecyclerView.Adapter<PlanDayAdapter.PlanDayViewHolder> {

    private List<PlanDayResponse> days;

    public PlanDayAdapter(List<PlanDayResponse> days) {
        this.days = sortDays(days);
    }

    /**
     * Sắp xếp các ngày theo thứ tự: Thứ Hai (1) -> Chủ Nhật (0 hoặc 7)
     */
    private List<PlanDayResponse> sortDays(List<PlanDayResponse> days) {
        if (days == null) {
            return new ArrayList<>();
        }

        List<PlanDayResponse> sortedDays = new ArrayList<>(days);
        Collections.sort(sortedDays, new Comparator<PlanDayResponse>() {
            @Override
            public int compare(PlanDayResponse day1, PlanDayResponse day2) {
                int dayOfWeek1 = normalizeDayOfWeek(day1.getDayOfWeek());
                int dayOfWeek2 = normalizeDayOfWeek(day2.getDayOfWeek());
                return Integer.compare(dayOfWeek1, dayOfWeek2);
            }
        });

        return sortedDays;
    }

    /**
     * Chuẩn hóa dayOfWeek: Chủ Nhật (0 hoặc 7) -> 7, các ngày khác giữ nguyên
     * Để Thứ Hai (1) đến Thứ Bảy (6) xếp trước Chủ Nhật (7)
     */
    private int normalizeDayOfWeek(int dayOfWeek) {
        if (dayOfWeek == 0) {
            return 7; // Chủ Nhật cuối tuần
        }
        return dayOfWeek;
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
            // Chuyển đổi dayOfWeek thành tên ngày (ví dụ: 1 -> Thứ Hai)
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
            // dayOfWeek từ backend: 1=Monday, 2=Tuesday, ..., 7=Sunday, 0=Sunday
            switch (dayOfWeek) {
                case 1: return "Thứ Hai";
                case 2: return "Thứ Ba";
                case 3: return "Thứ Tư";
                case 4: return "Thứ Năm";
                case 5: return "Thứ Sáu";
                case 6: return "Thứ Bảy";
                case 0: return "Chủ Nhật";
                case 7: return "Chủ Nhật";
                default: return "Ngày " + dayOfWeek;
            }
        }
    }
}