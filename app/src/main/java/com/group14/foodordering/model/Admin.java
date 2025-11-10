package com.group14.foodordering.model;

import java.util.Locale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin data model
 * Extends User class, contains admin-specific permissions
 */
public class Admin {
    private String adminId;
    private String userId; // Linked to User table
    private String email;
    private String name;
    private String phone;
    private List<String> permissions; // Permission list, e.g., ["menu_edit", "report_view", "inventory_manage"]
    private List<String> restaurantIds; // List of restaurant IDs this admin can manage
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Admin() {
    }

    // Full constructor
    public Admin(String adminId, String userId, String email, String name, String phone, List<String> permissions) {
        this.adminId = adminId;
        this.userId = userId;
        this.email = email != null ? email.toLowerCase(Locale.ROOT) : null;
        this.name = name;
        this.phone = phone;
        this.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.toLowerCase(Locale.ROOT) : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getPermissions() {
        return permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
    }

    public List<String> getRestaurantIds() {
        return restaurantIds;
    }

    public void setRestaurantIds(List<String> restaurantIds) {
        this.restaurantIds = restaurantIds;
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
        map.put("adminId", adminId);
        map.put("userId", userId);
        map.put("email", email);
        map.put("name", name);
        map.put("phone", phone);
        // Permissions is already a List, so use it directly
        if (permissions != null && !permissions.isEmpty()) {
            map.put("permissions", new ArrayList<>(permissions));
        } else {
            map.put("permissions", new ArrayList<>());
        }
        // Convert restaurantIds list
        if (restaurantIds != null) {
            map.put("restaurantIds", restaurantIds);
        } else {
            map.put("restaurantIds", new ArrayList<>());
        }
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

