package com.example.smartkkitch;

public class GeneratedRecipe {

    private String id, name, imageUrl, usedIngredients, missedIngredients, likes;

    public GeneratedRecipe(String id, String name, String imageUrl, String usedIngredients, String missedIngredients, String likes) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.usedIngredients = usedIngredients;
        this.missedIngredients = missedIngredients;
        this.likes = likes;
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

    public String getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(String usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public String getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(String missedIngredients) {
        this.missedIngredients = missedIngredients;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }
}
