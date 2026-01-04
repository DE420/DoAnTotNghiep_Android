package com.example.fitnessapp.model.response.nutrition;

import java.io.Serializable;

public class IngredientResponse implements Serializable {
    private Long id;
    private String name;
    private String image;
    private String standardUnit;
    private Float caloriesPerUnit;

    public IngredientResponse() {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStandardUnit() {
        return standardUnit;
    }

    public void setStandardUnit(String standardUnit) {
        this.standardUnit = standardUnit;
    }

    public Float getCaloriesPerUnit() {
        return caloriesPerUnit;
    }

    public void setCaloriesPerUnit(Float caloriesPerUnit) {
        this.caloriesPerUnit = caloriesPerUnit;
    }
}
