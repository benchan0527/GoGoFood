package com.group14.foodordering;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.ListenerRegistration;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.DataFilterService;
import com.group14.foodordering.util.PermissionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Kitchen View Activity
 * UC-7: Kitchen staff can view new order list in real-time
 * UC-8: Kitchen staff can update order status
 */
public class KitchenViewActivity extends AppCompatActivity {

    private static final String TAG = "KitchenViewActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView ordersRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrdersAdapter ordersAdapter;
    private List<Order> pendingOrders;
    private ListenerRegistration ordersListener;
    private Handler timeUpdateHandler;
    private Runnable timeUpdateRunnable;
    private SimpleDateFormat timeFormat;
    private Map<String, String> restaurantNameCache; // restaurantId -> restaurantName

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_view);

        // Check admin login and permissions
        if (!AdminSessionHelper.isAdminLoggedIn(this)) {
            Toast.makeText(this, "Please login as admin first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (!PermissionManager.canViewOrders(this)) {
            Toast.makeText(this, "Permission denied: You need order view permission", 
                Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbService = FirebaseDatabaseService.getInstance();
        pendingOrders = new ArrayList<>();
        restaurantNameCache = new HashMap<>();
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeUpdateHandler = new Handler(Looper.getMainLooper());

        setupViews();
        setupRealTimeListener();
    }

    private void setupViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrdersAdapter();
        ordersRecyclerView.setAdapter(ordersAdapter);

        // Setup swipe to refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Real-time listener will automatically update, but we can refresh manually
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    /**
     * Setup real-time listener for automatic updates
     */
    private void setupRealTimeListener() {
        ordersListener = dbService.listenToPendingOrders(new FirebaseDatabaseService.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                // Filter orders by admin's restaurant access
                List<Order> filteredOrders = DataFilterService.filterOrdersByRestaurantAccess(
                    KitchenViewActivity.this, orders);
                
                pendingOrders.clear();
                pendingOrders.addAll(filteredOrders);
                ordersAdapter.notifyDataSetChanged();
                Log.d(TAG, "Orders updated via real-time listener, total: " + orders.size() + 
                    ", filtered: " + filteredOrders.size());
                
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Real-time listener error", e);
                Toast.makeText(KitchenViewActivity.this, "Connection error: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /**
     * Show order detail modal dialog
     * GUI #19: Ticket Detail & Update Modal
     */
    private void showOrderDetailModal(Order order) {
        if (order == null) {
            return;
        }

        // Create custom dialog view
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order_detail, null);
        
        // Initialize views
        TextView orderIdTextView = dialogView.findViewById(R.id.dialogOrderIdTextView);
        TextView orderTypeTextView = dialogView.findViewById(R.id.dialogOrderTypeTextView);
        TextView tableNumberTextView = dialogView.findViewById(R.id.dialogTableNumberTextView);
        TextView statusTextView = dialogView.findViewById(R.id.dialogStatusTextView);
        TextView timeTextView = dialogView.findViewById(R.id.dialogTimeTextView);
        TextView itemsTextView = dialogView.findViewById(R.id.dialogItemsTextView);
        TextView totalTextView = dialogView.findViewById(R.id.dialogTotalTextView);
        Button preparingButton = dialogView.findViewById(R.id.dialogPreparingButton);
        Button readyButton = dialogView.findViewById(R.id.dialogReadyButton);

        // Set order information
        String orderId = order.getOrderId() != null ? order.getOrderId() : "N/A";
        if (orderId.length() > 8) {
            orderIdTextView.setText("Order #" + orderId.substring(orderId.length() - 8));
        } else {
            orderIdTextView.setText("Order #" + orderId);
        }

        String orderType = order.getOrderType() != null ? order.getOrderType() : "N/A";
        String typeDisplay = "table".equalsIgnoreCase(orderType) ? "Table Order" : "Takeaway";
        orderTypeTextView.setText(typeDisplay);

        if ("table".equalsIgnoreCase(orderType) && order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
            tableNumberTextView.setText("Table: " + order.getTableNumber());
            tableNumberTextView.setVisibility(View.VISIBLE);
        } else {
            tableNumberTextView.setVisibility(View.GONE);
        }

        String status = order.getStatus() != null ? order.getStatus() : "pending";
        statusTextView.setText(status.toUpperCase());
        
        // Set status color
        int statusColor;
        switch (status.toLowerCase()) {
            case "pending":
                statusColor = 0xFFF44336; // Red
                break;
            case "preparing":
                statusColor = 0xFFFF9800; // Orange
                break;
            case "ready":
                statusColor = 0xFF4CAF50; // Green
                break;
            default:
                statusColor = 0xFF9E9E9E; // Grey
                break;
        }
        statusTextView.setTextColor(statusColor);

        timeTextView.setText(formatTime(order.getCreatedAt()) + " (" + formatElapsedTime(order.getCreatedAt()) + ")");

        // Display order items
        StringBuilder itemsText = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                if (item != null) {
                    String itemName = item.getMenuItemName() != null ? item.getMenuItemName() : "Unknown";
                    int quantity = item.getQuantity();
                    itemsText.append(String.format("• %s x%d", itemName, quantity));
                    
                    if (item.getCustomization() != null && !item.getCustomization().trim().isEmpty()) {
                        itemsText.append(" (").append(item.getCustomization()).append(")");
                    }
                    
                    if (item.getCookingDetails() != null && !item.getCookingDetails().trim().isEmpty()) {
                        itemsText.append(" [").append(item.getCookingDetails()).append("]");
                    }
                    
                    itemsText.append("\n");
                }
            }
        } else {
            itemsText.append("No items");
        }
        itemsTextView.setText(itemsText.toString());

        totalTextView.setText("$" + String.format("%.2f", order.getTotal()));

        // Set button states and listeners
        if ("pending".equals(status)) {
            preparingButton.setEnabled(true);
            preparingButton.setAlpha(1.0f);
            preparingButton.setOnClickListener(v -> {
                updateOrderStatus(order.getOrderId(), "preparing");
                // Dialog will be dismissed by the real-time update
            });
            readyButton.setEnabled(false);
            readyButton.setAlpha(0.5f);
        } else if ("preparing".equals(status)) {
            preparingButton.setEnabled(false);
            preparingButton.setAlpha(0.5f);
            readyButton.setEnabled(true);
            readyButton.setAlpha(1.0f);
            readyButton.setOnClickListener(v -> {
                updateOrderStatus(order.getOrderId(), "ready");
                // Dialog will be dismissed by the real-time update
            });
        } else {
            preparingButton.setEnabled(false);
            preparingButton.setAlpha(0.5f);
            readyButton.setEnabled(false);
            readyButton.setAlpha(0.5f);
        }

        // Create and show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Order Details");
        builder.setPositiveButton("Close", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Update order status
     */
    private void updateOrderStatus(String orderId, String newStatus) {
        // Check permission before updating
        if (!PermissionManager.canUpdateOrders(this)) {
            Toast.makeText(this, "Permission denied: You need order update permission", 
                Toast.LENGTH_SHORT).show();
            return;
        }

        dbService.updateOrderStatus(orderId, newStatus, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                Log.d(TAG, "Order status updated successfully: " + documentId + " -> " + newStatus);
                Toast.makeText(KitchenViewActivity.this, "Order status updated", Toast.LENGTH_SHORT).show();
                // Real-time listener will automatically refresh the list
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update order status", e);
                Toast.makeText(KitchenViewActivity.this, "Update failed: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Format elapsed time
     */
    private String formatElapsedTime(long createdAt) {
        long elapsed = System.currentTimeMillis() - createdAt;
        long minutes = elapsed / 60000;
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else {
            long hours = minutes / 60;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }
    }

    /**
     * Format time from timestamp
     */
    private String formatTime(long timestamp) {
        return timeFormat.format(new Date(timestamp));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start periodic time updates
        startTimeUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop periodic time updates
        stopTimeUpdates();
    }

    private void startTimeUpdates() {
        timeUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                // Update elapsed times every minute
                if (ordersAdapter != null) {
                    ordersAdapter.notifyDataSetChanged();
                }
                timeUpdateHandler.postDelayed(this, 60000); // Update every minute
            }
        };
        timeUpdateHandler.post(timeUpdateRunnable);
    }

    private void stopTimeUpdates() {
        if (timeUpdateRunnable != null) {
            timeUpdateHandler.removeCallbacks(timeUpdateRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimeUpdates();
        if (ordersListener != null) {
            ordersListener.remove();
        }
    }

    /**
     * Orders adapter
     */
    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_kitchen, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = pendingOrders.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return pendingOrders.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView orderIdTextView;
            TextView orderTypeTextView;
            TextView tableNumberTextView;
            TextView restaurantNameTextView;
            TextView statusBadge;
            TextView timeTextView;
            TextView elapsedTimeTextView;
            TextView itemsTextView;
            TextView totalTextView;
            Button preparingButton;
            Button readyButton;
            View statusIndicator;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
                orderTypeTextView = itemView.findViewById(R.id.orderTypeTextView);
                tableNumberTextView = itemView.findViewById(R.id.tableNumberTextView);
                restaurantNameTextView = itemView.findViewById(R.id.restaurantNameTextView);
                statusBadge = itemView.findViewById(R.id.statusBadge);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                elapsedTimeTextView = itemView.findViewById(R.id.elapsedTimeTextView);
                itemsTextView = itemView.findViewById(R.id.itemsTextView);
                totalTextView = itemView.findViewById(R.id.totalTextView);
                preparingButton = itemView.findViewById(R.id.preparingButton);
                readyButton = itemView.findViewById(R.id.readyButton);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
            }

            void bind(Order order) {
                if (order == null) {
                    return;
                }
                
                String orderId = order.getOrderId() != null ? order.getOrderId() : "N/A";
                String orderType = order.getOrderType() != null ? order.getOrderType() : "N/A";
                String status = order.getStatus() != null ? order.getStatus() : "pending";
                
                // Set order ID
                if (!"N/A".equals(orderId) && orderId.length() > 8) {
                    orderIdTextView.setText("#" + orderId.substring(orderId.length() - 8));
                } else {
                    orderIdTextView.setText("#" + orderId);
                }
                
                // Set order type
                String typeDisplay = "table".equalsIgnoreCase(orderType) ? "Table Order" : "Takeaway";
                orderTypeTextView.setText(typeDisplay);
                
                // Set table number (if available)
                if (tableNumberTextView != null) {
                    if ("table".equalsIgnoreCase(orderType) && order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
                        tableNumberTextView.setText("Table " + order.getTableNumber());
                        tableNumberTextView.setVisibility(View.VISIBLE);
                    } else {
                        tableNumberTextView.setVisibility(View.GONE);
                    }
                }
                
                // Set restaurant name
                if (restaurantNameTextView != null) {
                    String restaurantId = order.getRestaurantId();
                    if (restaurantId != null && !restaurantId.isEmpty()) {
                        String restaurantName = restaurantNameCache.get(restaurantId);
                        if (restaurantName != null) {
                            restaurantNameTextView.setText(restaurantName);
                            restaurantNameTextView.setVisibility(View.VISIBLE);
                        } else {
                            // Fetch restaurant name
                            dbService.getRestaurantById(restaurantId, new FirebaseDatabaseService.RestaurantCallback() {
                                @Override
                                public void onSuccess(Restaurant restaurant) {
                                    if (restaurant != null && restaurant.getRestaurantName() != null) {
                                        String name = restaurant.getRestaurantName();
                                        restaurantNameCache.put(restaurantId, name);
                                        restaurantNameTextView.setText(name);
                                        restaurantNameTextView.setVisibility(View.VISIBLE);
                                    } else {
                                        restaurantNameTextView.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e(TAG, "Failed to fetch restaurant name", e);
                                    restaurantNameTextView.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        restaurantNameTextView.setVisibility(View.GONE);
                    }
                }
                
                // Set status badge with color
                if (statusBadge != null) {
                    String statusDisplay = status.substring(0, 1).toUpperCase() + status.substring(1);
                    statusBadge.setText(statusDisplay);
                    
                    // Set status color
                    int statusColor;
                    int indicatorColor;
                    switch (status.toLowerCase()) {
                        case "pending":
                            statusColor = 0xFFF44336; // Red
                            indicatorColor = 0xFFF44336;
                            break;
                        case "preparing":
                            statusColor = 0xFFFF9800; // Orange
                            indicatorColor = 0xFFFF9800;
                            break;
                        case "ready":
                            statusColor = 0xFF4CAF50; // Green
                            indicatorColor = 0xFF4CAF50;
                            break;
                        default:
                            statusColor = 0xFF9E9E9E; // Grey
                            indicatorColor = 0xFF9E9E9E;
                            break;
                    }
                    statusBadge.setBackgroundColor(statusColor);
                    if (statusIndicator != null) {
                        statusIndicator.setBackgroundColor(indicatorColor);
                    }
                }
                
                // Set time information
                long createdAt = order.getCreatedAt();
                if (timeTextView != null) {
                    timeTextView.setText(formatTime(createdAt));
                }
                if (elapsedTimeTextView != null) {
                    elapsedTimeTextView.setText(formatElapsedTime(createdAt));
                }
                
                // Display order items with modifiers
                StringBuilder itemsText = new StringBuilder();
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    for (OrderItem item : order.getItems()) {
                        if (item != null) {
                            String itemName = item.getMenuItemName() != null ? item.getMenuItemName() : "Unknown";
                            int quantity = item.getQuantity();
                            itemsText.append(String.format("• %s x%d", itemName, quantity));
                            
                            // Add customization if available
                            if (item.getCustomization() != null && !item.getCustomization().trim().isEmpty()) {
                                itemsText.append(" (").append(item.getCustomization()).append(")");
                            }
                            
                            // Add cooking details if available
                            if (item.getCookingDetails() != null && !item.getCookingDetails().trim().isEmpty()) {
                                itemsText.append(" [").append(item.getCookingDetails()).append("]");
                            }
                            
                            itemsText.append("\n");
                        }
                    }
                } else {
                    itemsText.append("No items");
                }
                itemsTextView.setText(itemsText.toString());
                
                totalTextView.setText(String.format("$%.2f", order.getTotal()));

                // Set item click listener to show detail modal
                itemView.setOnClickListener(v -> showOrderDetailModal(order));

                // Set buttons based on current status
                if ("pending".equals(status)) {
                    if (preparingButton != null) {
                        preparingButton.setEnabled(true);
                        preparingButton.setAlpha(1.0f);
                        preparingButton.setOnClickListener(v -> {
                            v.setClickable(false); // Prevent double click
                            updateOrderStatus(order.getOrderId(), "preparing");
                        });
                    }
                    if (readyButton != null) {
                        readyButton.setEnabled(false);
                        readyButton.setAlpha(0.5f);
                    }
                } else if ("preparing".equals(status)) {
                    if (preparingButton != null) {
                        preparingButton.setEnabled(false);
                        preparingButton.setAlpha(0.5f);
                    }
                    if (readyButton != null) {
                        readyButton.setEnabled(true);
                        readyButton.setAlpha(1.0f);
                        readyButton.setOnClickListener(v -> updateOrderStatus(order.getOrderId(), "ready"));
                    }
                } else {
                    if (preparingButton != null) {
                        preparingButton.setEnabled(false);
                        preparingButton.setAlpha(0.5f);
                    }
                    if (readyButton != null) {
                        readyButton.setEnabled(false);
                        readyButton.setAlpha(0.5f);
                    }
                }
            }
        }
    }
}

