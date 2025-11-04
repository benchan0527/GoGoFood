package com.group14.foodordering.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu item data model
 * Used to store menu item information in the restaurant menu
 */
public class MenuItem implements Serializable {
    private String itemId;
    private String name;
    private String description;
    private double price;
    private String category; // "appetizer", "main", "dessert", "beverage"
    private String imageUrl;
    private boolean isAvailable;
    private boolean hasDrink; // Whether the item includes a drink option
    private int stock; // Stock quantity (optional)
    private List<String> modifierIds; // List of modifier IDs that apply to this item
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public MenuItem() {
        this.modifierIds = new ArrayList<>();
    }

    // Full constructor
    public MenuItem(String itemId, String name, String description, double price, String category) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isAvailable = true;
        this.stock = -1; // -1 means unlimited
        this.modifierIds = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("isAvailable")
    public boolean isAvailable() {
        return isAvailable;
    }

    @PropertyName("isAvailable")
    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isHasDrink() {
        return hasDrink;
    }

    public void setHasDrink(boolean hasDrink) {
        this.hasDrink = hasDrink;
    }

    public List<String> getModifierIds() {
        return modifierIds;
    }

    public void setModifierIds(List<String> modifierIds) {
        this.modifierIds = modifierIds;
    }

    // Convert to Map (for Firestore)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("name", name);
        map.put("description", description);
        map.put("price", price);
        map.put("category", category);
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("isAvailable", isAvailable);
        map.put("hasDrink", hasDrink);
        map.put("stock", stock);
        map.put("modifierIds", modifierIds != null ? modifierIds : new ArrayList<>());
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

