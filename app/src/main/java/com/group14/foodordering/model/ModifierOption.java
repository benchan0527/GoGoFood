package com.group14.foodordering.model;

import java.io.Serializable;

/**
 * Modifier option data model
 * Represents a single option within a modifier group (e.g., "Large" size, "Extra Cheese" add-on)
 */
public class ModifierOption implements Serializable {
    private String optionName;
    private double additionalPrice;
    private boolean isAvailable;

    // Default constructor
    public ModifierOption() {
        this.isAvailable = true;
    }

    // Full constructor
    public ModifierOption(String optionName, double additionalPrice) {
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public double getAdditionalPrice() {
        return additionalPrice;
    }

    public void setAdditionalPrice(double additionalPrice) {
        this.additionalPrice = additionalPrice;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
}

