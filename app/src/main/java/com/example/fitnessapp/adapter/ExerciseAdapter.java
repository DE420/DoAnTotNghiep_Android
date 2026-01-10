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

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.iv_exercise_item_image);
            nameTextView = itemView.findViewById(R.id.tv_exercise_item_name);
            levelTextView = itemView.findViewById(R.id.tv_exercise_item_level);
            trainingTypeTextView = itemView.findViewById(R.id.tv_exercise_item_training_type);
        }

        public void bind(ExerciseResponse exercise, OnItemClickListener listener) {
            nameTextView.setText(exercise.getName());

            // Chuyển đổi level sang tiếng Việt
            String levelText = exercise.getLevel();
            if ("BEGINNER".equalsIgnoreCase(levelText)) {
                levelText = "Cơ bản";
                levelTextView.setBackgroundResource(R.drawable.bg_level_beginner);
            } else if ("INTERMEDIATE".equalsIgnoreCase(levelText)) {
                levelText = "Trung cấp";
                levelTextView.setBackgroundResource(R.drawable.bg_level_intermediate);
            } else if ("ADVANCED".equalsIgnoreCase(levelText)) {
                levelText = "Nâng cao";
                levelTextView.setBackgroundResource(R.drawable.bg_level_advanced);
            } else {
                levelTextView.setBackgroundResource(R.drawable.bg_level_default);
            }
            levelTextView.setText(levelText);

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

            // Đặt sự kiện click cho toàn bộ item
            itemView.setOnClickListener(v -> listener.onItemClick(exercise));
        }
    }
}