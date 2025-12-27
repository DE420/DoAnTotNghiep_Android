package com.example.fitnessapp.adapter.nutrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

public class MealDishAdapter extends RecyclerView.Adapter<MealDishAdapter.MealDishViewHolder> {

    private final Context context;
    private List<MealDishResponse> dishList;
    private OnDishClickListener listener;

    public MealDishAdapter(Context context) {
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
    public MealDishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_dish, parent, false);
        return new MealDishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealDishViewHolder holder, int position) {
        MealDishResponse dish = dishList.get(position);
        holder.bind(dish);
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    class MealDishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDishImage;
        private final TextView tvDishName;
        private final TextView tvQuantity;
        private final TextView tvDishCalories;
        private final ImageButton ibViewRecipe;

        public MealDishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.iv_dish_image);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvDishCalories = itemView.findViewById(R.id.tv_dish_calories);
            ibViewRecipe = itemView.findViewById(R.id.ib_view_recipe);
        }

        public void bind(MealDishResponse dish) {
            // Set dish name
            tvDishName.setText(dish.getName());

            // Set quantity
            if (dish.getQuantity() != null) {
                String quantityText = String.format(Locale.getDefault(),
                    "x %d %s", dish.getQuantity(), context.getString(R.string.servings));
                tvQuantity.setText(quantityText);
            } else {
                tvQuantity.setText("");
            }

            // Set calories
            if (dish.getTotalCalories() != null) {
                tvDishCalories.setText(String.format(Locale.getDefault(),
                    "%.0f kcal", dish.getTotalCalories()));
            } else {
                tvDishCalories.setText("-- kcal");
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

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDishClick(dish);
                }
            });

            ibViewRecipe.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewRecipeClick(dish);
                }
            });
        }
    }

    public interface OnDishClickListener {
        void onDishClick(MealDishResponse dish);
        void onViewRecipeClick(MealDishResponse dish);
    }
}
