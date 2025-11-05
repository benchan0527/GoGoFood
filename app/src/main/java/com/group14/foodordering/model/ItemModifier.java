package com.group14.foodordering.model;

import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Item modifier data model
 * Represents a modifier group that can be applied to menu items (e.g., "Size", "Add-ons", "Sides")
 */
public class ItemModifier implements Serializable {
    private String modifierId;
    private String modifierGroup; // e.g., "Size", "Add-ons", "Beverages", "Sides"
    private List<String> menuItemIds; // List of menu item IDs this modifier applies to
    private List<ModifierOption> options; // List of available options
    private boolean isRequired; // Whether at least one option must be selected
    private int minSelections; // Minimum number of selections (0 = optional)
    private int maxSelections; // Maximum number of selections (-1 = unlimited)
    private long createdAt;
    private long updatedAt;

    // Default constructor
    public ItemModifier() {
        this.menuItemIds = new ArrayList<>();
        this.options = new ArrayList<>();
        this.isRequired = false;
        this.minSelections = 0;
        this.maxSelections = -1; // -1 means unlimited
    }

    // Full constructor
    public ItemModifier(String modifierId, String modifierGroup) {
        this.modifierId = modifierId;
        this.modifierGroup = modifierGroup;
        this.menuItemIds = new ArrayList<>();
        this.options = new ArrayList<>();
        this.isRequired = false;
        this.minSelections = 0;
        this.maxSelections = -1;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getModifierId() {
        return modifierId;
    }

    public void setModifierId(String modifierId) {
        this.modifierId = modifierId;
    }

    public String getModifierGroup() {
        return modifierGroup;
    }

    public void setModifierGroup(String modifierGroup) {
        this.modifierGroup = modifierGroup;
    }

    public List<String> getMenuItemIds() {
        return menuItemIds;
    }

    public void setMenuItemIds(List<String> menuItemIds) {
        this.menuItemIds = menuItemIds;
    }

    public List<ModifierOption> getOptions() {
        return options;
    }

    public void setOptions(List<ModifierOption> options) {
        this.options = options;
    }

    @PropertyName("isRequired")
    public boolean isRequired() {
        return isRequired;
    }

    @PropertyName("isRequired")
    public void setRequired(boolean required) {
        isRequired = required;
    }

    public int getMinSelections() {
        return minSelections;
    }

    public void setMinSelections(int minSelections) {
        this.minSelections = minSelections;
    }

    public int getMaxSelections() {
        return maxSelections;
    }

    public void setMaxSelections(int maxSelections) {
        this.maxSelections = maxSelections;
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
        map.put("modifierId", modifierId);
        map.put("modifierGroup", modifierGroup);
        map.put("menuItemIds", menuItemIds);
        
        List<Map<String, Object>> optionsList = new ArrayList<>();
        for (ModifierOption option : options) {
            Map<String, Object> optionMap = new HashMap<>();
            optionMap.put("optionName", option.getOptionName());
            optionMap.put("additionalPrice", option.getAdditionalPrice());
            optionMap.put("isAvailable", option.isAvailable());
            optionsList.add(optionMap);
        }
        map.put("options", optionsList);
        
        map.put("isRequired", isRequired);
        map.put("minSelections", minSelections);
        map.put("maxSelections", maxSelections);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}

