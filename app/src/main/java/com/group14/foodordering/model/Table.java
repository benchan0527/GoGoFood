package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Table data model
 * Used to manage restaurant table status
 */
public class Table {
    private String tableId;
    private String tableNumber;
    private String branchId;
    private String status; // "available", "occupied", "needs_cleaning"
    private int capacity;
    private String currentOrderId;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public Table() {
        this.status = "available";
        this.capacity = 4;
    }

    // Full constructor
    public Table(String tableId, String tableNumber, String branchId, int capacity) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.branchId = branchId;
        this.capacity = capacity;
        this.status = "available";
        this.currentOrderId = "";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(String currentOrderId) {
        this.currentOrderId = currentOrderId;
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
        map.put("tableId", tableId);
        map.put("tableNumber", tableNumber);
        map.put("branchId", branchId);
        map.put("status", status);
        map.put("capacity", capacity);
        map.put("currentOrderId", currentOrderId != null ? currentOrderId : "");
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

