package com.example.fitnessapp.model.response.nutrition;

import java.io.Serializable;
import java.util.List;

public class DishResponse implements Serializable {
    private Long id;
    private String name;
    private Integer cookingTime;  // Changed from String to Integer (backend sends Integer)
    private String image;
    private Float calories;
    private Float protein;
    private Float fat;
    private Float carbs;
    private List<DishIngredientResponse> ingredients;  // Changed from String to List
    private String preparation;

    public DishResponse() {
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

    public Integer getCookingTime() {
        return cookingTime;
    }

    public void setCookingTime(Integer cookingTime) {
        this.cookingTime = cookingTime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Float getFat() {
        return fat;
    }

    public void setFat(Float fat) {
        this.fat = fat;
    }

    public Float getCarbs() {
        return carbs;
    }

    public void setCarbs(Float carbs) {
        this.carbs = carbs;
    }

    public List<DishIngredientResponse> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<DishIngredientResponse> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Helper method to get ingredients as formatted string for display
     * Example: "200g Chicken breast (Diced)\n100g Rice\n50g Broccoli"
     */
    public String getIngredientsAsString() {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ingredients.size(); i++) {
            sb.append(ingredients.get(i).getFormattedIngredient());
            if (i < ingredients.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }
}
