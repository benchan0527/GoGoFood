package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Restaurant data model
 * Used to store restaurant information
 */
public class Restaurant {
    private String restaurantId;
    private String restaurantName;
    private String address;
    private String phoneNumber;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Restaurant() {
        this.isActive = true;
    }

    // Full constructor
    public Restaurant(String restaurantId, String restaurantName, String address, String phoneNumber) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    @Override
    public String toString() {
        return restaurantName != null ? restaurantName : "Unknown Restaurant";
    }

    // Convert to Map (for Firestore)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("restaurantId", restaurantId);
        map.put("restaurantName", restaurantName);
        map.put("address", address);
        map.put("phoneNumber", phoneNumber);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

