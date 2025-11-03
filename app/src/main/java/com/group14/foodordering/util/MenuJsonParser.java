package com.group14.foodordering.util;

import android.content.Context;
import android.util.Log;

import com.group14.foodordering.model.MenuItem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Utility class to parse menu.json file and convert it to MenuItem objects
 */
public class MenuJsonParser {
    private static final String TAG = "MenuJsonParser";

    /**
     * Parse menu.json file from assets and return list of MenuItem objects
     */
    public static List<MenuItem> parseMenuFromAssets(Context context, String filename) {
        List<MenuItem> menuItems = new ArrayList<>();
        
        try {
            InputStream inputStream = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            String currentCategory = "main"; // Default category
            String line;
            String currentItemName = null;
            String previousLine = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and UI elements
                if (line.isEmpty() || 
                    line.startsWith("//") || 
                    line.startsWith("Slide") || 
                    line.equals("Order Now")) {
                    continue;
                }
                
                // Detect category headers
                if (isCategoryHeader(line)) {
                    currentCategory = normalizeCategory(line);
                    Log.d(TAG, "Found category: " + currentCategory);
                    currentItemName = null; // Reset item name when category changes
                    previousLine = null;
                    continue;
                }
                
                // Detect price (lines starting with $)
                if (line.startsWith("$")) {
                    try {
                        double price = parsePrice(line);
                        // Use previous line as item name if currentItemName is not set
                        String itemName = currentItemName;
                        if ((itemName == null || itemName.isEmpty()) && previousLine != null && !previousLine.startsWith("$")) {
                            itemName = previousLine;
                        }
                        
                        if (itemName != null && !itemName.isEmpty() && !isNonItemLine(itemName)) {
                            // Create menu item
                            String itemId = "item_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                            MenuItem item = new MenuItem(itemId, itemName, "", price, currentCategory);
                            item.setDescription(itemName);
                            item.setAvailable(true);
                            menuItems.add(item);
                            
                            Log.d(TAG, "Added item: " + itemName + " - $" + price);
                        }
                        currentItemName = null; // Reset for next item
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Failed to parse price: " + line);
                    }
                    previousLine = line;
                    continue;
                }
                
                // This is likely an item name/description
                // Skip if it's a known non-item line
                if (!isNonItemLine(line)) {
                    currentItemName = line;
                }
                previousLine = line;
            }
            
            reader.close();
            inputStream.close();
            
            Log.d(TAG, "Parsed " + menuItems.size() + " menu items from " + filename);
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to read menu file: " + filename, e);
        }
        
        return menuItems;
    }

    /**
     * Parse menu.json file from file path (for development/testing)
     */
    public static List<MenuItem> parseMenuFromFile(String filePath) {
        List<MenuItem> menuItems = new ArrayList<>();
        
        try {
            java.io.File file = new java.io.File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "Menu file not found: " + filePath);
                return menuItems;
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new java.io.FileInputStream(file), "UTF-8"));
            
            String currentCategory = "main";
            String line;
            String currentItemName = null;
            String previousLine = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and UI elements
                if (line.isEmpty() || 
                    line.startsWith("//") || 
                    line.startsWith("Slide") || 
                    line.equals("Order Now")) {
                    continue;
                }
                
                // Detect category headers
                if (isCategoryHeader(line)) {
                    currentCategory = normalizeCategory(line);
                    Log.d(TAG, "Found category: " + currentCategory);
                    currentItemName = null; // Reset item name when category changes
                    previousLine = null;
                    continue;
                }
                
                // Detect price (lines starting with $)
                if (line.startsWith("$")) {
                    try {
                        double price = parsePrice(line);
                        // Use previous line as item name if currentItemName is not set
                        String itemName = currentItemName;
                        if ((itemName == null || itemName.isEmpty()) && previousLine != null && !previousLine.startsWith("$")) {
                            itemName = previousLine;
                        }
                        
                        if (itemName != null && !itemName.isEmpty() && !isNonItemLine(itemName)) {
                            String itemId = "item_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
                            MenuItem item = new MenuItem(itemId, itemName, "", price, currentCategory);
                            item.setDescription(itemName);
                            item.setAvailable(true);
                            menuItems.add(item);
                            
                            Log.d(TAG, "Added item: " + itemName + " - $" + price);
                        }
                        currentItemName = null; // Reset for next item
                    } catch (NumberFormatException e) {
                        Log.w(TAG, "Failed to parse price: " + line);
                    }
                    previousLine = line;
                    continue;
                }
                
                // This is likely an item name
                if (!isNonItemLine(line)) {
                    currentItemName = line;
                }
                previousLine = line;
            }
            
            reader.close();
            
            Log.d(TAG, "Parsed " + menuItems.size() + " menu items from file");
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to read menu file: " + filePath, e);
        }
        
        return menuItems;
    }

    /**
     * Check if a line is a category header
     */
    private static boolean isCategoryHeader(String line) {
        String[] categories = {
            "Breakfast", "Lunch", "Dinner", 
            "Hot Drinks", "Cold Drinks",
            "Afternoon Tea", "Tea Set", "Tea Time"
        };
        
        for (String category : categories) {
            if (line.equalsIgnoreCase(category)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Normalize category name to standard categories
     */
    private static String normalizeCategory(String category) {
        String lower = category.toLowerCase();
        
        if (lower.contains("breakfast")) {
            return "breakfast";
        } else if (lower.contains("lunch")) {
            return "main";
        } else if (lower.contains("dinner")) {
            return "main";
        } else if (lower.contains("drink") || lower.contains("tea")) {
            return "beverage";
        } else {
            return "main";
        }
    }

    /**
     * Parse price from string like "$33" or "$40.5"
     */
    private static double parsePrice(String priceStr) {
        // Remove $ and any whitespace
        String cleaned = priceStr.replace("$", "").trim();
        return Double.parseDouble(cleaned);
    }

    /**
     * Check if line is not an item (UI elements, headers, etc.)
     */
    private static boolean isNonItemLine(String line) {
        String lower = line.toLowerCase();
        return lower.contains("weekly specials") ||
               lower.contains("no msg added") ||
               lower.contains("combo") && !lower.contains("rice") ||
               lower.equals("fuel up with protein") ||
               lower.equals("wholesome delights");
    }
}

