package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 菜单项数据模型
 * 用于存储餐厅菜单中的菜品信息
 */
public class MenuItem {
    private String itemId;
    private String name;
    private String description;
    private double price;
    private String category; // "appetizer", "main", "dessert", "beverage"
    private String imageUrl;
    private boolean isAvailable;
    private int stock; // 库存数量（可选）
    private long createdAt;
    private long updatedAt;

    // 默认构造函数
    public MenuItem() {
    }

    // 完整构造函数
    public MenuItem(String itemId, String name, String description, double price, String category) {
        this.itemId = itemId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isAvailable = true;
        this.stock = -1; // -1表示无限制
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

    public boolean isAvailable() {
        return isAvailable;
    }

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

    // 转换为Map（用于Firestore）
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", itemId);
        map.put("name", name);
        map.put("description", description);
        map.put("price", price);
        map.put("category", category);
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("isAvailable", isAvailable);
        map.put("stock", stock);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

