package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * User data model
 * Used to store basic customer information
 */
public class User {
    private String userId;
    private String email;
    private String name;
    private String phone;
    private String role; // "customer", "server", "kitchen", "admin"
    private int points; // Customer points/score (for customers only)
    private long createdAt;
    private long updatedAt;

    // Default constructor (required by Firebase)
    public User() {
    }

    // Full constructor
    public User(String userId, String email, String name, String phone, String role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.points = 0; // Initialize points to 0
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int pointsToAdd) {
        this.points += pointsToAdd;
        this.updatedAt = System.currentTimeMillis();
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
        map.put("userId", userId);
        map.put("email", email);
        map.put("name", name);
        map.put("phone", phone);
        map.put("role", role);
        map.put("points", points);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

