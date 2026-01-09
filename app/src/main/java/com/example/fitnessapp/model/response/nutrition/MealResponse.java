package com.example.fitnessapp.model.response.nutrition;

import com.example.fitnessapp.enums.MealType;

import java.io.Serializable;
import java.util.List;

public class MealResponse implements Serializable {
    private Long id;
    private String name;
    private MealType mealType;
    private Float calories;
    private Float protein;
    private Float carbs;
    private Float fat;
    private List<MealDishResponse> dishes;

    public MealResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Float getCalories() {
        return calories;
    }

    public void setCalories(Float calories) {
        this.calories = calories;
    }

    public Float getProtein() {
        return protein;
    }

    public void setProtein(Float protein) {
        this.protein = protein;
    }

    public Float getCarbs() {
        return carbs;
    }

    public void setCarbs(Float carbs) {
        this.carbs = carbs;
    }

    public Float getFat() {
        return fat;
    }

    public void setFat(Float fat) {
        this.fat = fat;
    }

    public List<MealDishResponse> getDishes() {
        return dishes;
    }

    public void setDishes(List<MealDishResponse> dishes) {
        this.dishes = dishes;
    }
}
