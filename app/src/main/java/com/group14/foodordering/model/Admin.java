package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员数据模型
 * 扩展了User类，包含管理员特定权限
 */
public class Admin {
    private String adminId;
    private String userId; // 关联到User表
    private String email;
    private String name;
    private String phone;
    private String[] permissions; // 权限列表，如 ["menu_edit", "report_view", "inventory_manage"]
    private boolean isActive;
    private long createdAt;
    private long updatedAt;

    // 默认构造函数
    public Admin() {
    }

    // 完整构造函数
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

    // 转换为Map（用于Firestore）
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("adminId", adminId);
        map.put("userId", userId);
        map.put("email", email);
        map.put("name", name);
        map.put("phone", phone);
        map.put("permissions", permissions);
        map.put("isActive", isActive);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

