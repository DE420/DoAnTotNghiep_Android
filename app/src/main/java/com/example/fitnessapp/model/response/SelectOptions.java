package com.example.fitnessapp.model.response;

import com.google.gson.annotations.SerializedName;

public class SelectOptions {
    @SerializedName("value")
    private Long id;
    @SerializedName("label")
    private String name;

    // Constructor
    public SelectOptions(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}