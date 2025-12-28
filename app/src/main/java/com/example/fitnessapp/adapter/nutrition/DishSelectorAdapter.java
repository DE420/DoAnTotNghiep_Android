package com.example.fitnessapp.adapter.nutrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying dishes in dish selector dialog
 */
public class DishSelectorAdapter extends RecyclerView.Adapter<DishSelectorAdapter.DishViewHolder> {

    private final Context context;
    private List<MealDishResponse> dishList;
    private OnDishClickListener listener;

    public DishSelectorAdapter(Context context) {
        this.context = context;
        this.dishList = new ArrayList<>();
    }

    public void setDishList(List<MealDishResponse> dishList) {
        this.dishList = dishList != null ? dishList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnDishClickListener(OnDishClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dish_selector, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        MealDishResponse dish = dishList.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDishImage;
        private final TextView tvDishName;
        private final TextView tvCookingTime;
        private final TextView tvCalories;
        private final TextView tvProtein;
        private final TextView tvCarbs;
        private final TextView tvFat;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.iv_dish_image);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvCookingTime = itemView.findViewById(R.id.tv_cooking_time);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvProtein = itemView.findViewById(R.id.tv_protein);
            tvCarbs = itemView.findViewById(R.id.tv_carbs);
            tvFat = itemView.findViewById(R.id.tv_fat);
        }

        public void bind(MealDishResponse dish) {
            // Set dish name
            tvDishName.setText(dish.getName());

            // Set cooking time
            if (dish.getCookingTime() != null && !dish.getCookingTime().isEmpty()) {
                tvCookingTime.setText(dish.getCookingTime());
            } else {
                tvCookingTime.setText("-");
            }

            // Set nutrition info
            if (dish.getCalories() != null) {
                tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", dish.getCalories()));
            } else {
                tvCalories.setText("- kcal");
            }

            if (dish.getProtein() != null) {
                tvProtein.setText(String.format(Locale.getDefault(), "P: %.0fg", dish.getProtein()));
            } else {
                tvProtein.setText("P: -");
            }

            if (dish.getCarbs() != null) {
                tvCarbs.setText(String.format(Locale.getDefault(), "C: %.0fg", dish.getCarbs()));
            } else {
                tvCarbs.setText("C: -");
            }

            if (dish.getFat() != null) {
                tvFat.setText(String.format(Locale.getDefault(), "F: %.0fg", dish.getFat()));
            } else {
                tvFat.setText("F: -");
            }

            // Load dish image
            if (dish.getImage() != null && !dish.getImage().isEmpty()) {
                Glide.with(context)
                        .load(dish.getImage())
                        .placeholder(R.drawable.ic_empty_nutrition_96)
                        .error(R.drawable.ic_empty_nutrition_96)
                        .centerCrop()
                        .into(ivDishImage);
            } else {
                ivDishImage.setImageResource(R.drawable.ic_empty_nutrition_96);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDishClick(dish);
                }
            });
        }
    }

    public interface OnDishClickListener {
        void onDishClick(MealDishResponse dish);
    }
}
