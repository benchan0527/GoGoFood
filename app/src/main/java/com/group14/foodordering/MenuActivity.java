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
    private TextView cartTotalTextView;
    private TextView timeTextView;
    private Button checkoutButton;
    private Button viewCartButton;
    private Button dineInButton;
    private Button takeawayButton;
    private String selectedOrderType = "dine_in"; // "dine_in" or "takeaway"
    private String currentTimePeriod; // "breakfast", "lunch", "afternoon_tea", "dinner"
    private String currentTimeDisplay; // "Morning", "Lunch time", "Tea Time", "Dinner"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dbService = FirebaseDatabaseService.getInstance();
        allMenuItems = new ArrayList<>();
        displayedMenuItems = new ArrayList<>();
        cart = new HashMap<>();

        setupViews();
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
     * Load menu items
     */
    private void loadMenuItems() {
        dbService.getAllMenuItems(new FirebaseDatabaseService.MenuItemsCallback() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                allMenuItems.clear();
                allMenuItems.addAll(items);
                filterByTimePeriod();
                Log.d(TAG, "Menu items loaded successfully, total: " + items.size() + " items");
                Log.d(TAG, "Current time period: " + currentTimePeriod + ", filtered items: " + displayedMenuItems.size());
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
        int currentQuantity = cart.getOrDefault(menuItemId, 0);
        cart.put(menuItemId, currentQuantity + 1);
        updateCartDisplay();
        menuAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
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
                    totalPrice += item.getPrice() * quantity;
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
                    OrderItem orderItem = new OrderItem(itemId, item.getName(), quantity, item.getPrice());
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
                    double itemTotal = item.getPrice() * quantity;
                    totalPrice += itemTotal;
                    cartDetails.append(String.format("%s x%d - $%.2f\n", 
                            item.getName(), quantity, itemTotal));
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
