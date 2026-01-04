package com.example.fitnessapp.model.response.nutrition;

import com.example.fitnessapp.enums.FitnessGoal;

import java.io.Serializable;
import java.util.List;

public class MenuResponse implements Serializable {
    private Long id;
    private Integer displayOrder;
    private String name;
    private String description;
    private String image;
    private FitnessGoal fitnessGoal;
    private Integer caloriesTarget;
    private Boolean isPrivate;
    private Boolean isOwner;
    private Long creatorId;
    private String creatorName;
    private String creatorAvatar;
    private String createdAt;
    private String updatedAt;
    private Float calories;
    private Float protein;
    private Float carbs;
    private Float fat;
    private List<MealResponse> meals;

    public MenuResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public FitnessGoal getFitnessGoal() {
        return fitnessGoal;
    }

    public void setFitnessGoal(FitnessGoal fitnessGoal) {
        this.fitnessGoal = fitnessGoal;
    }

    public Integer getCaloriesTarget() {
        return caloriesTarget;
    }

    public void setCaloriesTarget(Integer caloriesTarget) {
        this.caloriesTarget = caloriesTarget;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Boolean getIsOwner() {
        return isOwner;
    }

    public void setIsOwner(Boolean isOwner) {
        this.isOwner = isOwner;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorAvatar() {
        return creatorAvatar;
    }

    public void setCreatorAvatar(String creatorAvatar) {
        this.creatorAvatar = creatorAvatar;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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

    public List<MealResponse> getMeals() {
        return meals;
    }

    public void setMeals(List<MealResponse> meals) {
        this.meals = meals;
    }
}
