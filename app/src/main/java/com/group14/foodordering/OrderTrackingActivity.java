package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.List;

/**
 * Order Tracking Activity
 * UC-3: Customers can track order status
 */
public class OrderTrackingActivity extends AppCompatActivity {

    private static final String TAG = "OrderTrackingActivity";
    private FirebaseDatabaseService dbService;
    private TextView orderIdTextView;
    private TextView orderNumberTextView;
    private TextView restaurantNameTextView;
    private TextView statusTextView;
    private TextView itemsTextView;
    private TextView totalTextView;
    private Button returnToMenuButton;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Invalid order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbService = FirebaseDatabaseService.getInstance();
        setupViews();
        loadOrder();
    }

    private void setupViews() {
        orderIdTextView = findViewById(R.id.orderIdTextView);
        orderNumberTextView = findViewById(R.id.orderNumberTextView);
        restaurantNameTextView = findViewById(R.id.restaurantNameTextView);
        statusTextView = findViewById(R.id.statusTextView);
        itemsTextView = findViewById(R.id.itemsTextView);
        totalTextView = findViewById(R.id.totalTextView);
        returnToMenuButton = findViewById(R.id.returnToMenuButton);
        
        if (returnToMenuButton != null) {
            returnToMenuButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrderTrackingActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    /**
     * Load order information
     */
    private void loadOrder() {
        dbService.getOrderById(orderId, new FirebaseDatabaseService.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                displayOrder(order);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load order", e);
                Toast.makeText(OrderTrackingActivity.this, "Failed to load order: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Display order information
     */
    private void displayOrder(Order order) {
        // Display order number prominently
        String orderNumber = order.getOrderId() != null ? order.getOrderId() : "N/A";
        if (orderNumberTextView != null) {
            orderNumberTextView.setText("#" + orderNumber);
        }
        if (orderIdTextView != null) {
            orderIdTextView.setText("Order ID: " + orderNumber);
        }
        
        // Fetch and display restaurant name
        String restaurantId = order.getRestaurantId();
        if (restaurantNameTextView != null && restaurantId != null && !restaurantId.isEmpty()) {
            dbService.getRestaurantById(restaurantId, new FirebaseDatabaseService.RestaurantCallback() {
                @Override
                public void onSuccess(Restaurant restaurant) {
                    if (restaurant != null && restaurant.getRestaurantName() != null) {
                        restaurantNameTextView.setText("Restaurant: " + restaurant.getRestaurantName());
                        restaurantNameTextView.setVisibility(android.view.View.VISIBLE);
                    } else {
                        restaurantNameTextView.setVisibility(android.view.View.GONE);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to fetch restaurant name", e);
                    if (restaurantNameTextView != null) {
                        restaurantNameTextView.setVisibility(android.view.View.GONE);
                    }
                }
            });
        } else if (restaurantNameTextView != null) {
            restaurantNameTextView.setVisibility(android.view.View.GONE);
        }
        
        // Display status
        String statusText = "Status: " + getStatusText(order.getStatus());
        if (statusTextView != null) {
            statusTextView.setText(statusText);
        }

        // Display order items
        StringBuilder itemsText = new StringBuilder("Order Details:\n\n");
        for (OrderItem item : order.getItems()) {
            itemsText.append(String.format("%s x%d - $%.2f\n", 
                    item.getMenuItemName(), item.getQuantity(), item.getTotalPrice()));
        }
        itemsTextView.setText(itemsText.toString());

        // Display price breakdown
        StringBuilder totalText = new StringBuilder();
        totalText.append(String.format("Subtotal: $%.2f\n", order.getSubtotal()));
        if (order.getTax() > 0) {
            totalText.append(String.format("Tax (10%%): $%.2f\n", order.getTax()));
        }
        if (order.getServiceCharge() > 0) {
            totalText.append(String.format("Service Charge: $%.2f\n", order.getServiceCharge()));
        }
        if (order.getDiscount() > 0) {
            totalText.append(String.format("Discount: -$%.2f\n", order.getDiscount()));
        }
        totalText.append(String.format("\nTotal: $%.2f", order.getTotal()));
        totalTextView.setText(totalText.toString());
    }

    /**
     * Get status text
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending":
                return "Pending";
            case "preparing":
                return "Preparing";
            case "ready":
                return "Ready";
            case "completed":
                return "Completed";
            case "cancelled":
                return "Cancelled";
            default:
                return status;
        }
    }
}

