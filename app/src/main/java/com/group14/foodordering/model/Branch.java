package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Branch data model
 * Used to store restaurant branch information
 */
public class Branch {
    private String branchId;
    private String branchName;
    private String restaurantId; // Linked to Restaurant
    private String address;
    private String phoneNumber;
    private Map<String, Double> geolocation; // {latitude, longitude}
    private Map<String, String> openingHours;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Branch() {
        this.geolocation = new HashMap<>();
        this.openingHours = new HashMap<>();
    }

    // Full constructor
    public Branch(String branchId, String branchName, String address, String phoneNumber) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.geolocation = new HashMap<>();
        this.openingHours = new HashMap<>();
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Full constructor with restaurantId
    public Branch(String branchId, String branchName, String restaurantId, String address, String phoneNumber) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.restaurantId = restaurantId;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.geolocation = new HashMap<>();
        this.openingHours = new HashMap<>();
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
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

    public Map<String, Double> getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(Map<String, Double> geolocation) {
        this.geolocation = geolocation;
    }

    public Map<String, String> getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(Map<String, String> openingHours) {
        this.openingHours = openingHours;
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
        map.put("branchId", branchId);
        map.put("branchName", branchName);
        map.put("restaurantId", restaurantId != null ? restaurantId : "");
        map.put("address", address);
        map.put("phoneNumber", phoneNumber);
        map.put("geolocation", geolocation);
        map.put("openingHours", openingHours);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

