package com.group14.foodordering.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Application configuration data model
 * Used to store global application settings
 */
public class AppConfig {
    private String configId;
    private double serviceChargeRate;
    private List<String> supportedPaymentMethods;
    private String currency;
    private String currencySymbol;
    private String promotionBanner;
    private List<String> deliveryTimeOptions;
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public AppConfig() {
        this.configId = "settings";
        this.serviceChargeRate = 0.10;
        this.currency = "USD";
        this.currencySymbol = "$";
    }

    // Getters and Setters
    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public double getServiceChargeRate() {
        return serviceChargeRate;
    }

    public void setServiceChargeRate(double serviceChargeRate) {
        this.serviceChargeRate = serviceChargeRate;
    }

    public List<String> getSupportedPaymentMethods() {
        return supportedPaymentMethods;
    }

    public void setSupportedPaymentMethods(List<String> supportedPaymentMethods) {
        this.supportedPaymentMethods = supportedPaymentMethods;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getPromotionBanner() {
        return promotionBanner;
    }

    public void setPromotionBanner(String promotionBanner) {
        this.promotionBanner = promotionBanner;
    }

    public List<String> getDeliveryTimeOptions() {
        return deliveryTimeOptions;
    }

    public void setDeliveryTimeOptions(List<String> deliveryTimeOptions) {
        this.deliveryTimeOptions = deliveryTimeOptions;
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
        map.put("configId", configId);
        map.put("serviceChargeRate", serviceChargeRate);
        map.put("supportedPaymentMethods", supportedPaymentMethods);
        map.put("currency", currency);
        map.put("currencySymbol", currencySymbol);
        map.put("promotionBanner", promotionBanner != null ? promotionBanner : "");
        map.put("deliveryTimeOptions", deliveryTimeOptions);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

