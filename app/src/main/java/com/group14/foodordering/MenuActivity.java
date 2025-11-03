package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.ArrayList;
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
    private List<MenuItem> menuItems;
    private Map<String, Integer> cart; // menuItemId -> quantity
    private TextView cartTotalTextView;
    private Button checkoutButton;
    private Button viewCartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dbService = FirebaseDatabaseService.getInstance();
        menuItems = new ArrayList<>();
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
        checkoutButton = findViewById(R.id.checkoutButton);
        viewCartButton = findViewById(R.id.viewCartButton);

        checkoutButton.setOnClickListener(v -> checkout());
        viewCartButton.setOnClickListener(v -> viewCart());

        updateCartDisplay();
    }

    /**
     * Load menu items
     */
    private void loadMenuItems() {
        dbService.getAllMenuItems(new FirebaseDatabaseService.MenuItemsCallback() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                menuItems.clear();
                menuItems.addAll(items);
                menuAdapter.notifyDataSetChanged();
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
     * Remove from cart
     */
    private void removeFromCart(String menuItemId) {
        int currentQuantity = cart.getOrDefault(menuItemId, 0);
        if (currentQuantity > 0) {
            if (currentQuantity == 1) {
                cart.remove(menuItemId);
            } else {
                cart.put(menuItemId, currentQuantity - 1);
            }
            updateCartDisplay();
            menuAdapter.notifyDataSetChanged();
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
            for (MenuItem item : menuItems) {
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
        Order order = new Order(orderId, "online");
        order.setUserId("guest_user"); // In a real app, get from logged-in user

        // Add order items
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            for (MenuItem item : menuItems) {
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

            for (MenuItem item : menuItems) {
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
            MenuItem item = menuItems.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return menuItems.size();
        }

        class MenuViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView descriptionTextView;
            TextView priceTextView;
            TextView categoryTextView;
            Button addButton;
            Button removeButton;
            TextView quantityTextView;

            MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.itemNameTextView);
                descriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                priceTextView = itemView.findViewById(R.id.itemPriceTextView);
                categoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
                addButton = itemView.findViewById(R.id.addButton);
                removeButton = itemView.findViewById(R.id.removeButton);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
            }

            void bind(MenuItem item) {
                nameTextView.setText(item.getName());
                descriptionTextView.setText(item.getDescription());
                priceTextView.setText(String.format("$%.2f", item.getPrice()));
                categoryTextView.setText(item.getCategory());

                int quantity = cart.getOrDefault(item.getItemId(), 0);
                quantityTextView.setText(String.valueOf(quantity));
                removeButton.setEnabled(quantity > 0);

                addButton.setOnClickListener(v -> addToCart(item.getItemId()));
                removeButton.setOnClickListener(v -> removeFromCart(item.getItemId()));
            }
        }
    }
}

