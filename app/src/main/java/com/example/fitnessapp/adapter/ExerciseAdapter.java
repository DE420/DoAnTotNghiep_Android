package com.example.fitnessapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.ExerciseResponse;

import java.util.ArrayList;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private List<ExerciseResponse> exerciseList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ExerciseResponse exercise);
    }

    public ExerciseAdapter(OnItemClickListener listener) {
        this.exerciseList = new ArrayList<>();
        this.listener = listener;
    }

    // Cập nhật toàn bộ danh sách bài tập
    public void setExercises(List<ExerciseResponse> exercises) {
        this.exerciseList.clear();
        if (exercises != null) {
            this.exerciseList.addAll(exercises);
        }
        notifyDataSetChanged();
    }

    // Thêm bài tập vào cuối danh sách (dùng cho pagination)
    public void addExercises(List<ExerciseResponse> exercises) {
        if (exercises != null && !exercises.isEmpty()) {
            int startPosition = this.exerciseList.size();
            this.exerciseList.addAll(exercises);
            notifyItemRangeInserted(startPosition, exercises.size());
        }
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseResponse exercise = exerciseList.get(position);
        holder.bind(exercise, listener);
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView nameTextView;
        TextView levelTextView;
        TextView trainingTypeTextView;
        LinearLayout muscleGroupsContainer; // Container để chứa các Chip/TextView nhóm cơ

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.exercise_thumbnail);
            nameTextView = itemView.findViewById(R.id.exercise_name);
            levelTextView = itemView.findViewById(R.id.exercise_level);
            trainingTypeTextView = itemView.findViewById(R.id.exercise_training_type);
            muscleGroupsContainer = itemView.findViewById(R.id.muscle_groups_container);
        }

        public void bind(ExerciseResponse exercise, OnItemClickListener listener) {
            nameTextView.setText(exercise.getName());
            levelTextView.setText(exercise.getLevel());

            // Đặt màu nền cho Level dựa trên giá trị (ví dụ: Beginner, Intermediate, Advanced)
            if (exercise.getLevel() != null) {
                switch (exercise.getLevel().toUpperCase()) {
                    case "BEGINNER":
                        levelTextView.setBackgroundResource(R.drawable.bg_level_beginner);
                        break;
                    case "INTERMEDIATE":
                        levelTextView.setBackgroundResource(R.drawable.bg_level_intermediate);
                        break;
                    case "ADVANCED":
                        levelTextView.setBackgroundResource(R.drawable.bg_level_advanced);
                        break;
                    default:
                        levelTextView.setBackgroundResource(R.drawable.bg_level_default);
                        break;
                }
            } else {
                levelTextView.setBackgroundResource(R.drawable.bg_level_default);
            }

            trainingTypeTextView.setText(exercise.getTrainingType());

            // Load ảnh thumbnail bằng Glide
            if (exercise.getThumbnail() != null && !exercise.getThumbnail().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(exercise.getThumbnail())
                        .placeholder(R.drawable.ic_placeholder_exercise) // Icon placeholder
                        .error(R.drawable.ic_error_image) // Icon lỗi
                        .into(thumbnailImageView);
            } else {
                // Hiển thị icon placeholder nếu không có URL ảnh
                Glide.with(itemView.getContext()).load(R.drawable.ic_placeholder_exercise).into(thumbnailImageView);
            }

            // Thêm các TextView (như chip) cho nhóm cơ một cách động
            muscleGroupsContainer.removeAllViews(); // Xóa các view cũ trước khi thêm mới
            if (exercise.getMuscleGroups() != null) {
                for (String muscle : exercise.getMuscleGroups()) {
                    TextView chip = new TextView(itemView.getContext());
                    chip.setText(muscle);
                    chip.setBackgroundResource(R.drawable.bg_chip_muscle_group);
                    chip.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
                    // Đặt padding và margin động
                    int paddingHorizontal = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.chip_padding_horizontal);
                    int paddingVertical = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.chip_padding_vertical);
                    chip.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    int marginEnd = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.chip_margin_end);
                    params.setMarginEnd(marginEnd); // Khoảng cách giữa các chip
                    chip.setLayoutParams(params);
                    muscleGroupsContainer.addView(chip);
                }
            }

            // Đặt sự kiện click cho toàn bộ item
            itemView.setOnClickListener(v -> listener.onItemClick(exercise));
        }
    }
}