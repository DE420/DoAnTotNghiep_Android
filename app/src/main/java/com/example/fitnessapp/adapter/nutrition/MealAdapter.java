package com.example.fitnessapp.adapter.nutrition;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitnessapp.R;
import com.example.fitnessapp.model.response.nutrition.MealDishResponse;
import com.example.fitnessapp.model.response.nutrition.MealResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private final Context context;
    private List<MealResponse> mealList;
    private OnDishClickListener dishClickListener;

    public MealAdapter(Context context) {
        this.context = context;
        this.mealList = new ArrayList<>();
    }

    public void setMealList(List<MealResponse> mealList) {
        this.mealList = mealList != null ? mealList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnDishClickListener(OnDishClickListener listener) {
        this.dishClickListener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_section, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealResponse meal = mealList.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    class MealViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivMealIcon;
        private final TextView tvMealName;
        private final TextView tvMealCalories;
        private final RecyclerView rvMealDishes;
        private final MealDishAdapter dishAdapter;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMealIcon = itemView.findViewById(R.id.iv_meal_icon);
            tvMealName = itemView.findViewById(R.id.tv_meal_name);
            tvMealCalories = itemView.findViewById(R.id.tv_meal_calories);
            rvMealDishes = itemView.findViewById(R.id.rv_meal_dishes);

            // Setup dishes RecyclerView
            dishAdapter = new MealDishAdapter(context);
            rvMealDishes.setLayoutManager(new LinearLayoutManager(context));
            rvMealDishes.setAdapter(dishAdapter);
            rvMealDishes.setNestedScrollingEnabled(false);
        }

        public void bind(MealResponse meal) {
            // Set meal name
            if (meal.getMealType() != null) {
                tvMealName.setText(context.getString(meal.getMealType().getResId()));
            } else if (meal.getName() != null) {
                tvMealName.setText(meal.getName());
            }

            // Set meal calories
            if (meal.getCalories() != null) {
                tvMealCalories.setText(String.format(Locale.getDefault(),
                    "%.0f kcal", meal.getCalories()));
            } else {
                tvMealCalories.setText("-- kcal");
            }

            // Set meal icon based on meal type
            if (meal.getMealType() != null) {
                switch (meal.getMealType()) {
                    case BREAKFAST:
                        ivMealIcon.setImageResource(R.drawable.ic_breakfast_24);
                        break;
                    case LUNCH:
                        ivMealIcon.setImageResource(R.drawable.ic_lunch_24);
                        break;
                    case DINNER:
                        ivMealIcon.setImageResource(R.drawable.ic_dinner_24);
                        break;
                    case EXTRA_MEAL:
                        ivMealIcon.setImageResource(R.drawable.ic_snack_24);
                        break;
                    default:
                        ivMealIcon.setImageResource(R.drawable.ic_empty_nutrition_96);
                        break;
                }
            } else {
                ivMealIcon.setImageResource(R.drawable.ic_empty_nutrition_96);
            }

            // Set dishes
            if (meal.getDishes() != null) {
                dishAdapter.setDishList(meal.getDishes());

                // Pass click listener to dish adapter
                dishAdapter.setOnDishClickListener(new MealDishAdapter.OnDishClickListener() {
                    @Override
                    public void onDishClick(MealDishResponse dish) {
                        if (dishClickListener != null) {
                            dishClickListener.onDishClick(dish);
                        }
                    }

                    @Override
                    public void onViewRecipeClick(MealDishResponse dish) {
                        if (dishClickListener != null) {
                            dishClickListener.onViewRecipeClick(dish);
                        }
                    }
                });
            }
        }
    }

    public interface OnDishClickListener {
        void onDishClick(MealDishResponse dish);
        void onViewRecipeClick(MealDishResponse dish);
    }
}
