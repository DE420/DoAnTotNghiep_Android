package com.example.fitnessapp.model.request.nutrition;

import com.example.fitnessapp.enums.MealType;

import java.util.List;

public class MealRequest {
    private String name;
    private MealType mealType;
    private List<MealDishRequest> dishes;

    public MealRequest() {
    }

    public MealRequest(String name, MealType mealType, List<MealDishRequest> dishes) {
        this.name = name;
        this.mealType = mealType;
        this.dishes = dishes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public List<MealDishRequest> getDishes() {
        return dishes;
    }

    public void setDishes(List<MealDishRequest> dishes) {
        this.dishes = dishes;
    }
}
