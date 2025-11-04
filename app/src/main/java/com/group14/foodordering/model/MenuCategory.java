package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Menu category data model
 * Used to store menu category information
 */
public class MenuCategory {
    private String categoryId;
    private String categoryName;
    private String displayName;
    private int displayOrder;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public MenuCategory() {
        this.isActive = true;
    }

    // Full constructor
    public MenuCategory(String categoryId, String categoryName, String displayName, int displayOrder) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.displayName = displayName;
        this.displayOrder = displayOrder;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    // Convert to Map (for Firestore)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", categoryId);
        map.put("categoryName", categoryName);
        map.put("displayName", displayName);
        map.put("displayOrder", displayOrder);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

