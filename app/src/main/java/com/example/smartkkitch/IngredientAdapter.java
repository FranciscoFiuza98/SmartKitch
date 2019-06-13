package com.example.smartkkitch;

public class IngredientAdapter {

    private String id, name, imageUrl;
    private boolean imageChanged;

    public IngredientAdapter(String id, String name, String imageUrl, boolean imageChanged) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.imageChanged = imageChanged;
    }

    public boolean isImageChanged() {
        return imageChanged;
    }

    public void setImageChanged(boolean imageChanged) {
        this.imageChanged = imageChanged;
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
}
