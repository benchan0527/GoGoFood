package com.group14.foodordering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Order data model
 * Used to store complete order information
 */
public class Order {
    private String orderId;
    private String userId; // Customer ID (if online order)
    private String restaurantId; // Restaurant ID
    private String tableNumber; // Table number (if table order)
    private String orderType; // "online", "table"
    private List<OrderItem> items;
    private String status; // "pending", "preparing", "ready", "completed", "cancelled"
    private double subtotal;
    private double serviceCharge;
    private double discount;
    private double total;
    private String paymentMethod; // "cash", "card", "mobile_wallet"
    private String paymentStatus; // "pending", "paid", "refunded"
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Order() {
        this.items = new ArrayList<>();
        this.status = "pending";
        this.paymentStatus = "pending";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor
    public Order(String orderId, String orderType) {
        this.orderId = orderId;
        this.orderType = orderType;
        this.items = new ArrayList<>();
        this.status = "pending";
        this.paymentStatus = "pending";
        this.subtotal = 0.0;
        this.serviceCharge = 0.0;
        this.discount = 0.0;
        this.total = 0.0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
        calculateTotal();
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        calculateTotal();
    }

    public void removeItem(OrderItem item) {
        this.items.remove(item);
        calculateTotal();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(double serviceCharge) {
        this.serviceCharge = serviceCharge;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    // Calculate total price
    private void calculateTotal() {
        subtotal = 0.0;
        for (OrderItem item : items) {
            subtotal += item.getTotalPrice();
        }
        total = subtotal + serviceCharge - discount;
        this.updatedAt = System.currentTimeMillis();
    }

    // Convert to Map (for Firestore)
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderId);
        map.put("userId", userId != null ? userId : "");
        map.put("restaurantId", restaurantId != null ? restaurantId : "");
        map.put("tableNumber", tableNumber != null ? tableNumber : "");
        map.put("orderType", orderType);
        
        // Convert OrderItem list
        List<Map<String, Object>> itemsList = new ArrayList<>();
        for (OrderItem item : items) {
            itemsList.add(item.toMap());
        }
        map.put("items", itemsList);
        
        map.put("status", status);
        map.put("subtotal", subtotal);
        map.put("serviceCharge", serviceCharge);
        map.put("discount", discount);
        map.put("total", total);
        map.put("paymentMethod", paymentMethod != null ? paymentMethod : "");
        map.put("paymentStatus", paymentStatus);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

