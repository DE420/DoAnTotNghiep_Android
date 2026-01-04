package com.example.fitnessapp.model.response.nutrition;

import java.io.Serializable;

public class DishIngredientResponse implements Serializable {
    private Long id;
    private Float quantity;           // Số lượng (VD: 200)
    private String unit;              // Đơn vị trong món (VD: gram)
    private String preparationNote;   // Ghi chú (VD: Thái hạt lựu)
    private IngredientResponse ingredient;  // Nested object

    public DishIngredientResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getQuantity() {
        return quantity;
    }

    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getPreparationNote() {
        return preparationNote;
    }

    public void setPreparationNote(String preparationNote) {
        this.preparationNote = preparationNote;
    }

    public IngredientResponse getIngredient() {
        return ingredient;
    }

    public void setIngredient(IngredientResponse ingredient) {
        this.ingredient = ingredient;
    }

    /**
     * Helper method to get formatted ingredient display
     * Example: "200g Chicken breast (Diced)"
     */
    public String getFormattedIngredient() {
        if (ingredient == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        // Add quantity and unit
        if (quantity != null && unit != null) {
            sb.append(String.format("%.0f%s ", quantity, unit));
        }

        // Add ingredient name
        sb.append(ingredient.getName());

        // Add preparation note if exists
        if (preparationNote != null && !preparationNote.isEmpty()) {
            sb.append(" (").append(preparationNote).append(")");
        }

        return sb.toString();
    }
}
