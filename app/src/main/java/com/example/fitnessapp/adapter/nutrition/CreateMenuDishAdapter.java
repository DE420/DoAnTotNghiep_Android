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

/**
 * Adapter for displaying editable dishes in Create/Edit Menu screen
 * Allows users to change quantities and remove dishes
 */
public class CreateMenuDishAdapter extends RecyclerView.Adapter<CreateMenuDishAdapter.DishViewHolder> {

    private final Context context;
    private List<MealDishItem> dishList;
    private OnDishChangeListener listener;
    private OnDishClickListener dishClickListener;

    public CreateMenuDishAdapter(Context context) {
        this.context = context;
        this.dishList = new ArrayList<>();
    }

    public void setDishList(List<MealDishItem> dishList) {
        this.dishList = dishList != null ? dishList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<MealDishItem> getDishList() {
        return dishList;
    }

    public void addDish(MealDishResponse dish) {
        dishList.add(new MealDishItem(dish, 1)); // Default quantity of 1
        notifyItemInserted(dishList.size() - 1);
        if (listener != null) {
            listener.onDishesChanged();
        }
    }

    public void removeDish(int position) {
        if (position >= 0 && position < dishList.size()) {
            dishList.remove(position);
            notifyItemRemoved(position);
            if (listener != null) {
                listener.onDishesChanged();
            }
        }
    }

    public void setOnDishChangeListener(OnDishChangeListener listener) {
        this.listener = listener;
    }

    public void setOnDishClickListener(OnDishClickListener listener) {
        this.dishClickListener = listener;
    }

    @NonNull
    @Override
    public DishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_create_menu_dish, parent, false);
        return new DishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DishViewHolder holder, int position) {
        MealDishItem item = dishList.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return dishList.size();
    }

    class DishViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivDishImage;
        private final TextView tvDishName;
        private final TextView tvQuantity;
        private final TextView tvCalories;
        private final TextView tvNutrition;
        private final ImageButton btnDecrease;
        private final ImageButton btnIncrease;
        private final ImageButton btnRemove;

        public DishViewHolder(@NonNull View itemView) {
            super(itemView);
            ivDishImage = itemView.findViewById(R.id.iv_dish_image);
            tvDishName = itemView.findViewById(R.id.tv_dish_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvCalories = itemView.findViewById(R.id.tv_calories);
            tvNutrition = itemView.findViewById(R.id.tv_nutrition);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }

        public void bind(MealDishItem item, int position) {
            MealDishResponse dish = item.getDish();

            // Set dish name
            tvDishName.setText(dish.getName());

            // Set quantity
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Calculate and display total calories for this quantity
            if (dish.getCalories() != null) {
                double totalCalories = dish.getCalories() * item.getQuantity();
                tvCalories.setText(String.format(Locale.getDefault(), "%.0f kcal", totalCalories));
            } else {
                tvCalories.setText("- kcal");
            }

            // Calculate and display total nutrition (protein, carbs, fat) for this quantity
            double totalProtein = dish.getProtein() != null ? dish.getProtein() * item.getQuantity() : 0;
            double totalCarbs = dish.getCarbs() != null ? dish.getCarbs() * item.getQuantity() : 0;
            double totalFat = dish.getFat() != null ? dish.getFat() * item.getQuantity() : 0;

            String nutritionText = String.format(Locale.getDefault(),
                    "P: %.0fg | C: %.0fg | F: %.0fg",
                    totalProtein, totalCarbs, totalFat);
            tvNutrition.setText(nutritionText);

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

            // Dish item click listener to view details
            itemView.setOnClickListener(v -> {
                if (dishClickListener != null && dish.getDishId() != null) {
                    dishClickListener.onDishClick(dish.getDishId());
                }
            });

            // Decrease quantity button
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    notifyItemChanged(position);
                    if (listener != null) {
                        listener.onDishesChanged();
                    }
                }
            });

            // Increase quantity button
            btnIncrease.setOnClickListener(v -> {
                item.setQuantity(item.getQuantity() + 1);
                notifyItemChanged(position);
                if (listener != null) {
                    listener.onDishesChanged();
                }
            });

            // Remove dish button
            btnRemove.setOnClickListener(v -> {
                // Show confirmation dialog before removing
                new android.app.AlertDialog.Builder(context)
                        .setTitle("Remove Dish")
                        .setMessage("Are you sure you want to remove \"" + item.getDish().getName() + "\" from this meal?")
                        .setPositiveButton("Remove", (dialog, which) -> {
                            removeDish(position);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }
    }

    /**
     * Interface for listening to dish changes
     */
    public interface OnDishChangeListener {
        void onDishesChanged();
    }

    /**
     * Interface for listening to dish clicks
     */
    public interface OnDishClickListener {
        void onDishClick(Long dishId);
    }

    /**
     * Model class to hold dish and its quantity
     */
    public static class MealDishItem {
        private MealDishResponse dish;
        private int quantity;

        public MealDishItem(MealDishResponse dish, int quantity) {
            this.dish = dish;
            this.quantity = quantity;
        }

        public MealDishResponse getDish() {
            return dish;
        }

        public void setDish(MealDishResponse dish) {
            this.dish = dish;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}
