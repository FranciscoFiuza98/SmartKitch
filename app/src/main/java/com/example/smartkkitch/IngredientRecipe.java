package com.example.smartkkitch;

public class IngredientRecipe {

    private String id, name, imageUrl, amount, unit;

    IngredientRecipe(String id, String name, String imageUrl, String amount, String unit) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.amount = amount;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
