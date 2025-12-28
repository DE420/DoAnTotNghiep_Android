package com.example.fitnessapp.model.response.nutrition;

import java.io.Serializable;

public class MealDishResponse implements Serializable {
    private Long dishId;
    private Long id;  // Some APIs return 'id' instead of 'dishId'
    private String name;
    private String image;
    private Integer quantity;

    // Total nutrition values (from menu detail API - total for the quantity)
    private Float totalCalories;
    private Float totalProtein;
    private Float totalCarbs;
    private Float totalFat;

    // Per-serving nutrition details (for dish selector and calculations)
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;
    private String cookingTime; // Backend returns as string like "10 mins"

    public MealDishResponse() {
    }

    public Long getDishId() {
        // Return id if dishId is null (for backwards compatibility)
        return dishId != null ? dishId : id;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Long getId() {
        // Return dishId if id is null (for backwards compatibility)
        return id != null ? id : dishId;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(Float totalCalories) {
        this.totalCalories = totalCalories;
    }

    public Float getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(Float totalProtein) {
        this.totalProtein = totalProtein;
    }

    public Float getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(Float totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    public Float getTotalFat() {
        return totalFat;
    }

    public void setTotalFat(Float totalFat) {
        this.totalFat = totalFat;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }

    public String getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(String cookingTime) {
        this.cookingTime = cookingTime;
    }
}
