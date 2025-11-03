package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单项数据模型
 * 表示订单中的单个菜品项及其定制信息
 */
public class OrderItem {
    private String orderItemId;
    private String menuItemId;
    private String menuItemName;
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private String customization; // 定制要求，如"no onion", "extra cheese"
    private String cookingDetails; // 烹饪要求

    // 默认构造函数
    public OrderItem() {
    }

    // 完整构造函数
    public OrderItem(String menuItemId, String menuItemName, int quantity, double unitPrice) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }

    // Getters and Setters
    public String getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(String orderItemId) {
        this.orderItemId = orderItemId;
    }

    public String getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(String menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.totalPrice = this.quantity * this.unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCustomization() {
        return customization;
    }

    public void setCustomization(String customization) {
        this.customization = customization;
    }

    public String getCookingDetails() {
        return cookingDetails;
    }

    public void setCookingDetails(String cookingDetails) {
        this.cookingDetails = cookingDetails;
    }

    // 转换为Map（用于Firestore）
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderItemId", orderItemId != null ? orderItemId : "");
        map.put("menuItemId", menuItemId);
        map.put("menuItemName", menuItemName);
        map.put("quantity", quantity);
        map.put("unitPrice", unitPrice);
        map.put("totalPrice", totalPrice);
        map.put("customization", customization != null ? customization : "");
        map.put("cookingDetails", cookingDetails != null ? cookingDetails : "");
        return map;
    }
}

