package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group14.foodordering.model.MenuCategory;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Menu Activity
 * UC-1: Customers can view menu and place orders
 */
public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView menuRecyclerView;
    private MenuAdapter menuAdapter;
    private List<MenuItem> allMenuItems;
    private List<MenuItem> displayedMenuItems;
    private Map<String, Integer> cart; // menuItemId -> quantity
    private Map<String, Double> cartDrinkAdditions; // menuItemId -> drink price addition
    private TextView cartTotalTextView;
    private TextView timeTextView;
    private Button checkoutButton;
    private Button viewCartButton;
    private Button dineInButton;
    private Button takeawayButton;
    private LinearLayout typeButtonsContainer;
    private String selectedOrderType = "dine_in"; // "dine_in" or "takeaway"
    private String currentTimePeriod; // "breakfast", "lunch", "afternoon_tea", "dinner"
    private String currentTimeDisplay; // "Morning", "Lunch time", "Tea Time", "Dinner"
    private String selectedCategory; // Selected category for filtering
    private List<MenuCategory> menuCategories;
    private Map<String, Button> categoryButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dbService = FirebaseDatabaseService.getInstance();
        allMenuItems = new ArrayList<>();
        displayedMenuItems = new ArrayList<>();
        cart = new HashMap<>();
        cartDrinkAdditions = new HashMap<>();
        menuCategories = new ArrayList<>();
        categoryButtons = new HashMap<>();

        setupViews();
        loadMenuCategories();
        loadMenuItems();
    }

    private void setupViews() {
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter();
        menuRecyclerView.setAdapter(menuAdapter);

        cartTotalTextView = findViewById(R.id.cartTotalTextView);
        timeTextView = findViewById(R.id.timeTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        viewCartButton = findViewById(R.id.viewCartButton);
        dineInButton = findViewById(R.id.dineInButton);
        takeawayButton = findViewById(R.id.takeawayButton);
        typeButtonsContainer = findViewById(R.id.typeButtonsContainer);

        checkoutButton.setOnClickListener(v -> checkout());
        viewCartButton.setOnClickListener(v -> viewCart());

        // Setup order type buttons
        dineInButton.setOnClickListener(v -> selectOrderType("dine_in"));
        takeawayButton.setOnClickListener(v -> selectOrderType("takeaway"));

        // Determine current time period
        determineTimePeriod();
        updateOrderTypeButtons();

        updateCartDisplay();
    }

    /**
     * Determine current time period based on current time
     */
    private void determineTimePeriod() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int currentTimeMinutes = hour * 60 + minute;

        // Morning: 7AM-11AM (420-660 minutes)
        if (currentTimeMinutes >= 420 && currentTimeMinutes < 660) {
            currentTimePeriod = "breakfast";
            currentTimeDisplay = "7AM-11AM";
        }
        // Lunch: 11AM-2PM (660-840 minutes)
        else if (currentTimeMinutes >= 660 && currentTimeMinutes < 840) {
            currentTimePeriod = "lunch";
            currentTimeDisplay = "11AM-2PM";
        }
        // Tea Time: 2PM-5PM (840-1020 minutes)
        else if (currentTimeMinutes >= 840 && currentTimeMinutes < 1020) {
            currentTimePeriod = "afternoon_tea";
            currentTimeDisplay = "2PM-5PM";
        }
        // Dinner: 5PM-10PM (1020-1320 minutes)
        else if (currentTimeMinutes >= 1020 && currentTimeMinutes < 1320) {
            currentTimePeriod = "dinner";
            currentTimeDisplay = "5PM-10PM";
        }
        // Default to breakfast for other times
        else {
            currentTimePeriod = "breakfast";
            currentTimeDisplay = "7AM-11AM";
        }

        // Update time display
        if (timeTextView != null) {
            timeTextView.setText(currentTimeDisplay);
        }
    }

    /**
     * Select order type (Dine in or Takeaway)
     */
    private void selectOrderType(String orderType) {
        selectedOrderType = orderType;
        updateOrderTypeButtons();
    }

    /**
     * Update order type button styles
     */
    private void updateOrderTypeButtons() {
        if (selectedOrderType.equals("dine_in")) {
            dineInButton.setBackgroundColor(0xFFFF6B35); // Orange background
            dineInButton.setTextColor(0xFF000000); // Black text
            takeawayButton.setBackgroundColor(0xFFFFFFFF); // White background
            takeawayButton.setTextColor(0xFF000000); // Black text
        } else {
            takeawayButton.setBackgroundColor(0xFFFF6B35); // Orange background
            takeawayButton.setTextColor(0xFF000000); // Black text
            dineInButton.setBackgroundColor(0xFFFFFFFF); // White background
            dineInButton.setTextColor(0xFF000000); // Black text
        }
    }

    /**
     * Load menu categories
     */
    private void loadMenuCategories() {
        dbService.getFirestore().collection("menuCategories")
                .whereEqualTo("isActive", true)
                .orderBy("displayOrder")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        menuCategories.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuCategory category = document.toObject(MenuCategory.class);
                            menuCategories.add(category);
                        }
                        Log.d(TAG, "Loaded " + menuCategories.size() + " categories from Firestore");
                        createTypeSelectorButtons();
                        // Set default selected category based on time period
                        if (selectedCategory == null && !categoryButtons.isEmpty()) {
                            // First try to find category matching current time period
                            String categoryToSelect = null;
                            for (MenuCategory cat : menuCategories) {
                                if (shouldShowCategory(cat)) {
                                    String catName = cat.getCategoryName().toLowerCase().replace(" ", "_");
                                    if (catName.equals(currentTimePeriod)) {
                                        categoryToSelect = cat.getCategoryName();
                                        break;
                                    }
                                }
                            }
                            // If no time-based category found, select first available category
                            if (categoryToSelect == null && !categoryButtons.isEmpty()) {
                                categoryToSelect = categoryButtons.keySet().iterator().next();
                            }
                            if (categoryToSelect != null) {
                                selectCategory(categoryToSelect);
                            }
                        }
                    } else {
                        Log.w(TAG, "Failed to load menu categories from Firestore or empty result, trying local JSON fallback", 
                                task.getException());
                        // Fallback: Load from local JSON file
                        loadMenuCategoriesFromLocal();
                    }
                });
    }

    /**
     * Load menu categories from local JSON file (fallback)
     */
    private void loadMenuCategoriesFromLocal() {
        try {
            java.io.InputStream inputStream = getAssets().open("firebase_sample_data.json");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            org.json.JSONObject jsonData = new org.json.JSONObject(jsonString.toString());
            org.json.JSONArray categoriesArray = jsonData.getJSONArray("menuCategories");
            
            menuCategories.clear();
            for (int i = 0; i < categoriesArray.length(); i++) {
                org.json.JSONObject categoryJson = categoriesArray.getJSONObject(i);
                if (categoryJson.getBoolean("isActive")) {
                    MenuCategory category = new MenuCategory();
                    category.setCategoryId(categoryJson.getString("categoryId"));
                    category.setCategoryName(categoryJson.getString("categoryName"));
                    category.setDisplayName(categoryJson.getString("displayName"));
                    category.setDisplayOrder(categoryJson.getInt("displayOrder"));
                    category.setActive(categoryJson.getBoolean("isActive"));
                    category.setCreatedAt(categoryJson.getLong("createdAt"));
                    category.setUpdatedAt(categoryJson.getLong("updatedAt"));
                    menuCategories.add(category);
                }
            }
            
            // Sort by display order
            menuCategories.sort((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()));
            
            Log.d(TAG, "Loaded " + menuCategories.size() + " categories from local JSON");
            createTypeSelectorButtons();
            
            // Set default selected category based on time period
            if (selectedCategory == null && !categoryButtons.isEmpty()) {
                // First try to find category matching current time period
                String categoryToSelect = null;
                for (MenuCategory cat : menuCategories) {
                    if (shouldShowCategory(cat)) {
                        String catName = cat.getCategoryName().toLowerCase().replace(" ", "_");
                        if (catName.equals(currentTimePeriod)) {
                            categoryToSelect = cat.getCategoryName();
                            break;
                        }
                    }
                }
                // If no time-based category found, select first available category
                if (categoryToSelect == null && !categoryButtons.isEmpty()) {
                    categoryToSelect = categoryButtons.keySet().iterator().next();
                }
                if (categoryToSelect != null) {
                    selectCategory(categoryToSelect);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load menu categories from local JSON", e);
            Toast.makeText(this, "Failed to load menu categories", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create type selector buttons dynamically
     * Only show categories that are available at current time
     */
    private void createTypeSelectorButtons() {
        typeButtonsContainer.removeAllViews();
        categoryButtons.clear();

        for (MenuCategory category : menuCategories) {
            // Check if this category should be shown at current time
            if (!shouldShowCategory(category)) {
                continue; // Skip this category
            }

            Button button = new Button(this);
            button.setText(category.getDisplayName());
            button.setTextSize(14);
            button.setPadding(24, 12, 24, 12);
            button.setBackgroundColor(0xFFFFFFFF);
            button.setTextColor(0xFF000000);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            button.setLayoutParams(params);
            
            button.setOnClickListener(v -> selectCategory(category.getCategoryName()));
            
            typeButtonsContainer.addView(button);
            categoryButtons.put(category.getCategoryName(), button);
        }
        
        Log.d(TAG, "Created " + categoryButtons.size() + " category buttons for current time: " + currentTimePeriod);
    }

    /**
     * Check if a category should be shown at current time
     * Time-based categories (breakfast, lunch, afternoon_tea, dinner) only shown if matches current time
     * Non-time-based categories (all_day_breakfast, drink) always shown
     */
    private boolean shouldShowCategory(MenuCategory category) {
        String categoryName = category.getCategoryName().toLowerCase();
        
        // Always show these categories (non-time-based)
        if (categoryName.equals("all day breakfast") || categoryName.equals("drink")) {
            return true;
        }
        
        // For time-based categories, only show if matches current time period
        if (categoryName.equals("breakfast")) {
            return currentTimePeriod.equals("breakfast");
        } else if (categoryName.equals("lunch")) {
            return currentTimePeriod.equals("lunch");
        } else if (categoryName.equals("afternoon tea")) {
            return currentTimePeriod.equals("afternoon_tea");
        } else if (categoryName.equals("dinner")) {
            return currentTimePeriod.equals("dinner");
        }
        
        // Default: show if we're not sure
        return true;
    }

    /**
     * Select category and filter menu items
     */
    private void selectCategory(String categoryName) {
        selectedCategory = categoryName;
        updateCategoryButtons();
        filterByCategory();
    }

    /**
     * Update category button styles
     */
    private void updateCategoryButtons() {
        for (Map.Entry<String, Button> entry : categoryButtons.entrySet()) {
            Button button = entry.getValue();
            if (entry.getKey().equals(selectedCategory)) {
                // Selected button style - gray underline
                button.setBackgroundColor(0xFFFFFFFF);
                button.setTextColor(0xFF000000);
                // Add underline effect
                button.setPaintFlags(button.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            } else {
                // Unselected button style
                button.setBackgroundColor(0xFFFFFFFF);
                button.setTextColor(0xFF666666);
                button.setPaintFlags(button.getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            }
        }
    }

    /**
     * Load menu items
     */
    private void loadMenuItems() {
        dbService.getAllMenuItems(new FirebaseDatabaseService.MenuItemsCallback() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                allMenuItems.clear();
                allMenuItems.addAll(items);
                if (selectedCategory != null) {
                    filterByCategory();
                } else {
                    filterByTimePeriod();
                }
                Log.d(TAG, "Menu items loaded successfully, total: " + items.size() + " items");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load menu items", e);
                Toast.makeText(MenuActivity.this, "Failed to load menu: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filter menu items by selected category
     * Also applies time period filter for time-based categories
     */
    private void filterByCategory() {
        displayedMenuItems.clear();
        
        if (selectedCategory == null) {
            filterByTimePeriod();
            return;
        }
        
        // Find category matching selected category name
        // Map category names to Firebase category values
        // "Breakfast" -> "breakfast"
        // "All Day Breakfast" -> "all_day_breakfast"
        // "Lunch" -> "lunch"
        // "Afternoon Tea" -> "afternoon_tea"
        // "Dinner" -> "dinner"
        // "Drink" -> "drink"
        String categoryToFilter = null;
        boolean isTimeBasedCategory = false;
        for (MenuCategory cat : menuCategories) {
            if (cat.getCategoryName().equals(selectedCategory)) {
                // Map category name to Firebase category value
                String categoryName = cat.getCategoryName().toLowerCase();
                if (categoryName.equals("breakfast")) {
                    categoryToFilter = "breakfast";
                    isTimeBasedCategory = true;
                } else if (categoryName.equals("all day breakfast")) {
                    categoryToFilter = "all_day_breakfast";
                    isTimeBasedCategory = false; // All day breakfast always available
                } else if (categoryName.equals("lunch")) {
                    categoryToFilter = "lunch";
                    isTimeBasedCategory = true;
                } else if (categoryName.equals("afternoon tea")) {
                    categoryToFilter = "afternoon_tea";
                    isTimeBasedCategory = true;
                } else if (categoryName.equals("dinner")) {
                    categoryToFilter = "dinner";
                    isTimeBasedCategory = true;
                } else if (categoryName.equals("drink")) {
                    categoryToFilter = "drink";
                    isTimeBasedCategory = false; // Drinks available all day
                } else {
                    // Fallback: convert to lowercase and replace spaces with underscores
                    categoryToFilter = categoryName.replace(" ", "_");
                    isTimeBasedCategory = false;
                }
                break;
            }
        }
        
        if (categoryToFilter == null) {
            filterByTimePeriod();
            return;
        }
        
        Log.d(TAG, "Filtering by category: " + categoryToFilter + " (selected: " + selectedCategory + "), time-based: " + isTimeBasedCategory);
        
        // For time-based categories, only show items matching both category AND time period
        // For non-time-based categories (like drinks, all day breakfast), show all items in that category
        for (MenuItem item : allMenuItems) {
            if (item.getCategory() != null) {
                String itemCategory = item.getCategory();
                
                // Check if item matches the selected category
                boolean matchesCategory = itemCategory.equals(categoryToFilter);
                
                // Also check if item category contains the filter (for comma-separated categories like "breakfast,lunch")
                if (!matchesCategory && itemCategory.contains(",")) {
                    String[] categories = itemCategory.split(",");
                    for (String cat : categories) {
                        if (cat.trim().equals(categoryToFilter)) {
                            matchesCategory = true;
                            break;
                        }
                    }
                }
                
                if (matchesCategory) {
                    // If it's a time-based category, only show items if current time period matches
                    if (isTimeBasedCategory) {
                        // For time-based categories, only show items when:
                        // 1. The selected category matches current time period (e.g., selecting "breakfast" during breakfast time)
                        // 2. AND the item's category matches the current time period
                        
                        // First check: Does the selected category match current time?
                        boolean categoryMatchesTime = categoryToFilter.equals(currentTimePeriod);
                        
                        // Second check: Does the item category match current time?
                        boolean itemMatchesTime = itemCategory.equals(currentTimePeriod);
                        
                        // Also check for comma-separated categories
                        if (!itemMatchesTime && itemCategory.contains(",")) {
                            String[] categories = itemCategory.split(",");
                            for (String cat : categories) {
                                if (cat.trim().equals(currentTimePeriod)) {
                                    itemMatchesTime = true;
                                    break;
                                }
                            }
                        }
                        
                        // Only show if both category selection and item match current time period
                        if (categoryMatchesTime && itemMatchesTime) {
                            displayedMenuItems.add(item);
                        } else {
                            Log.d(TAG, "Item filtered out: " + item.getName() + 
                                    " (category=" + itemCategory + ", selected=" + categoryToFilter + 
                                    ", currentTime=" + currentTimePeriod + ")");
                        }
                    } else {
                        // Non-time-based categories: show all items in that category (e.g., drinks, all day breakfast)
                        displayedMenuItems.add(item);
                    }
                }
            }
        }
        
        menuAdapter.notifyDataSetChanged();
        
        if (displayedMenuItems.isEmpty()) {
            Log.w(TAG, "No menu items found for category: " + selectedCategory + " (filter: " + categoryToFilter + ") at current time: " + currentTimePeriod);
        } else {
            Log.d(TAG, "Found " + displayedMenuItems.size() + " items for category: " + selectedCategory);
        }
    }

    /**
     * Filter menu items by current time period
     */
    private void filterByTimePeriod() {
        displayedMenuItems.clear();
        
        // Map time periods to Firebase categories
        // Note: Firebase uses "breakfast", "lunch", "afternoon_tea", "dinner"
        // But menu items might use "breakfast", "lunch", "dinner" directly
        String categoryToFilter = currentTimePeriod;
        
        // Handle special case for afternoon_tea - it might be stored differently
        if (currentTimePeriod.equals("afternoon_tea")) {
            // Try to find items with "afternoon_tea" category, or use "lunch" as fallback
            categoryToFilter = "afternoon_tea";
        }
        
        for (MenuItem item : allMenuItems) {
            if (item.getCategory() != null && item.getCategory().equals(categoryToFilter)) {
                displayedMenuItems.add(item);
            }
        }
        
        // If no items found for afternoon_tea, try "lunch" as fallback
        if (currentTimePeriod.equals("afternoon_tea") && displayedMenuItems.isEmpty()) {
            Log.d(TAG, "No items found for afternoon_tea, trying lunch category");
            categoryToFilter = "lunch";
            for (MenuItem item : allMenuItems) {
                if (item.getCategory() != null && item.getCategory().equals(categoryToFilter)) {
                    displayedMenuItems.add(item);
                }
            }
        }
        
        menuAdapter.notifyDataSetChanged();
        
        if (displayedMenuItems.isEmpty()) {
            Log.w(TAG, "No menu items found for time period: " + currentTimePeriod);
            Toast.makeText(this, "No menu items available for " + currentTimeDisplay, 
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Add to cart
     */
    private void addToCart(String menuItemId) {
        // Find the menu item
        MenuItem item = null;
        for (MenuItem menuItem : allMenuItems) {
            if (menuItem.getItemId().equals(menuItemId)) {
                item = menuItem;
                break;
            }
        }
        
        if (item == null) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if item has modifiers (including drinks or other options)
        if (item.getModifierIds() != null && !item.getModifierIds().isEmpty()) {
            // Navigate to modifier selection (handles all types of modifiers)
            Intent intent = new Intent(this, ItemModifierSelectionActivity.class);
            intent.putExtra(ItemModifierSelectionActivity.EXTRA_MENU_ITEM, item);
            startActivityForResult(intent, 1001);
        } else if (item.isHasDrink()) {
            // Legacy support: if hasDrink but no modifiers, use old drink selection
            Intent intent = new Intent(this, DrinkSelectionActivity.class);
            intent.putExtra(DrinkSelectionActivity.EXTRA_MENU_ITEM, item);
            startActivityForResult(intent, 1001);
        } else {
            // Directly add to cart
            addToCartDirectly(menuItemId, 0.0, null);
        }
    }
    
    /**
     * Add to cart directly with optional modifier price addition
     */
    private void addToCartDirectly(String menuItemId, double modifierPriceAddition, Map<String, Object> modifierSelections) {
        int currentQuantity = cart.getOrDefault(menuItemId, 0);
        cart.put(menuItemId, currentQuantity + 1);
        cartDrinkAdditions.put(menuItemId, modifierPriceAddition);
        // Store modifier selections if provided
        if (modifierSelections != null) {
            // Store in a separate map for cart details
            // For now, just store the price addition
        }
        updateCartDisplay();
        menuAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null) {
                // Check if it's from ItemModifierSelectionActivity
                MenuItem item = (MenuItem) data.getSerializableExtra(ItemModifierSelectionActivity.EXTRA_MENU_ITEM);
                if (item != null) {
                    double modifierPriceAddition = data.getDoubleExtra(ItemModifierSelectionActivity.EXTRA_PRICE_ADDITION, 0.0);
                    Map<String, Object> modifierSelections = (Map<String, Object>) data.getSerializableExtra(ItemModifierSelectionActivity.EXTRA_MODIFIER_SELECTIONS);
                    addToCartDirectly(item.getItemId(), modifierPriceAddition, modifierSelections);
                    Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Legacy: Check if it's from DrinkSelectionActivity
                item = (MenuItem) data.getSerializableExtra(DrinkSelectionActivity.EXTRA_MENU_ITEM);
                if (item != null) {
                    String drinkName = data.getStringExtra(DrinkSelectionActivity.EXTRA_DRINK_NAME);
                    String drinkType = data.getStringExtra(DrinkSelectionActivity.EXTRA_DRINK_TYPE);
                    double drinkPriceAddition = data.getDoubleExtra(DrinkSelectionActivity.EXTRA_DRINK_PRICE_ADDITION, 0.0);
                    
                    addToCartDirectly(item.getItemId(), drinkPriceAddition, null);
                    
                    String drinkText = (drinkName != null && !drinkName.isEmpty()) ? 
                            (drinkName + " (" + (drinkType.equals("hot") ? "Hot" : "Iced") + ")") :
                            (drinkType.equals("hot") ? "Hot Drink" : "Iced Drink");
                    Toast.makeText(this, "Added to cart with " + drinkText, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Update cart display
     */
    private void updateCartDisplay() {
        int totalItems = 0;
        double totalPrice = 0.0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            totalItems += quantity;

            // Find corresponding menu item to calculate price
            for (MenuItem item : allMenuItems) {
                if (item.getItemId().equals(itemId)) {
                    double itemPrice = item.getPrice();
                    double drinkAddition = cartDrinkAdditions.getOrDefault(itemId, 0.0);
                    totalPrice += (itemPrice + drinkAddition) * quantity;
                    break;
                }
            }
        }

        if (totalItems > 0) {
            cartTotalTextView.setText(String.format("Cart: %d items | Total: $%.2f", totalItems, totalPrice));
            checkoutButton.setEnabled(true);
            viewCartButton.setEnabled(true);
        } else {
            cartTotalTextView.setText("Cart is empty");
            checkoutButton.setEnabled(false);
            viewCartButton.setEnabled(false);
        }
    }

    /**
     * Checkout
     */
    private void checkout() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order
        String orderId = "order_" + UUID.randomUUID().toString().substring(0, 8);
        String orderType = selectedOrderType.equals("dine_in") ? "dine_in" : "takeaway";
        Order order = new Order(orderId, orderType);
        order.setUserId("guest_user"); // In a real app, get from logged-in user

        // Add order items
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            for (MenuItem item : allMenuItems) {
                if (item.getItemId().equals(itemId)) {
                    double itemPrice = item.getPrice();
                    double drinkAddition = cartDrinkAdditions.getOrDefault(itemId, 0.0);
                    String itemName = item.getName();
                    if (drinkAddition > 0) {
                        itemName += " (Cold Drink)";
                    } else if (item.isHasDrink()) {
                        itemName += " (Hot Drink)";
                    }
                    OrderItem orderItem = new OrderItem(itemId, itemName, quantity, itemPrice + drinkAddition);
                    order.addItem(orderItem);
                    break;
                }
            }
        }

        order.setTax(order.getSubtotal() * 0.1); // Assume 10% tax
        order.setServiceCharge(0.0);

        // Save order to database
        dbService.createOrder(order, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                Log.d(TAG, "Order created successfully: " + documentId);
                Toast.makeText(MenuActivity.this, "Order created successfully! Order ID: " + documentId, 
                        Toast.LENGTH_LONG).show();
                // Clear cart
                cart.clear();
                cartDrinkAdditions.clear();
                updateCartDisplay();
                menuAdapter.notifyDataSetChanged();
                
                // Navigate to order tracking page
                Intent intent = new Intent(MenuActivity.this, OrderTrackingActivity.class);
                intent.putExtra("orderId", orderId);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Order creation failed", e);
                Toast.makeText(MenuActivity.this, "Order creation failed: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * View cart details
     */
    private void viewCart() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder cartDetails = new StringBuilder("Cart Details:\n\n");
        double totalPrice = 0.0;

        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            for (MenuItem item : allMenuItems) {
                if (item.getItemId().equals(itemId)) {
                    double itemPrice = item.getPrice();
                    double drinkAddition = cartDrinkAdditions.getOrDefault(itemId, 0.0);
                    double itemTotal = (itemPrice + drinkAddition) * quantity;
                    totalPrice += itemTotal;
                    String itemName = item.getName();
                    if (drinkAddition > 0) {
                        itemName += " (Cold Drink)";
                    } else if (item.isHasDrink()) {
                        itemName += " (Hot Drink)";
                    }
                    cartDetails.append(String.format("%s x%d - $%.2f\n", 
                            itemName, quantity, itemTotal));
                    break;
                }
            }
        }

        cartDetails.append(String.format("\nTotal: $%.2f", totalPrice));
        Toast.makeText(this, cartDetails.toString(), Toast.LENGTH_LONG).show();
    }

    /**
     * Menu adapter
     */
    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu, parent, false);
            return new MenuViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
            MenuItem item = displayedMenuItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return displayedMenuItems.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            ImageView itemImageView;
            TextView nameTextView;
            TextView descriptionTextView;
            TextView priceTextView;
            Button orderNowButton;

            MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                itemImageView = itemView.findViewById(R.id.itemImageView);
                nameTextView = itemView.findViewById(R.id.itemNameTextView);
                descriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                priceTextView = itemView.findViewById(R.id.itemPriceTextView);
                orderNowButton = itemView.findViewById(R.id.orderNowButton);
            }

            void bind(MenuItem item) {
                nameTextView.setText(item.getName());
                
                if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                    descriptionTextView.setText(item.getDescription());
                    descriptionTextView.setVisibility(View.VISIBLE);
                } else {
                    descriptionTextView.setVisibility(View.GONE);
                }
                
                // Format price as integer if it's a whole number
                double price = item.getPrice();
                if (price == (int) price) {
                    priceTextView.setText(String.format("$%d", (int) price));
                } else {
                    priceTextView.setText(String.format("$%.2f", price));
                }

                // Load image using Glide
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(item.getImageUrl())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .into(itemImageView);
                } else {
                    itemImageView.setImageResource(android.R.drawable.ic_menu_gallery);
                }

                // Set button click listener
                orderNowButton.setOnClickListener(v -> addToCart(item.getItemId()));
            }
        }
    }
}
