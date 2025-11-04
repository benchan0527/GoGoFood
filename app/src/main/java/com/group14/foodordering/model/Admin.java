package com.group14.foodordering.model;

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
    private String[] permissions; // Permission list, e.g., ["menu_edit", "report_view", "inventory_manage"]
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Admin() {
    }

    // Full constructor
    public Admin(String adminId, String userId, String email, String name, String phone, String[] permissions) {
        this.adminId = adminId;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.permissions = permissions;
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
        this.email = email;
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

    public String[] getPermissions() {
        return permissions;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
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
        // Convert permissions array to List for Firestore compatibility
        if (permissions != null) {
            List<String> permissionsList = new ArrayList<>();
            for (String permission : permissions) {
                permissionsList.add(permission);
            }
            map.put("permissions", permissionsList);
        } else {
            map.put("permissions", new ArrayList<>());
        }
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

