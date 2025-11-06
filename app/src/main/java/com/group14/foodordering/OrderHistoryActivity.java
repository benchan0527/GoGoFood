package com.group14.foodordering;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.DeviceIdHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Order History Activity
 * Shows customer's order history with real-time status updates
 */
public class OrderHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OrderHistoryActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView ordersRecyclerView;
    private OrderHistoryAdapter adapter;
    private List<Order> orders;
    private String deviceId;
    private ListenerRegistration listenerRegistration;
    private TextView emptyTextView;
    private Handler updateHandler;
    private Runnable pendingUpdate;
    private Executor backgroundExecutor;
    private static final long UPDATE_DEBOUNCE_MS = 300; // Debounce updates by 300ms
    // Shared date formatter for all ViewHolders (cached for performance)
    private static final SimpleDateFormat dateFormat = 
        new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        dbService = FirebaseDatabaseService.getInstance();
        orders = new ArrayList<>();
        deviceId = DeviceIdHelper.getDeviceId(this);
        updateHandler = new Handler(Looper.getMainLooper());
        backgroundExecutor = Executors.newSingleThreadExecutor();

        setupViews();
        setupRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening to orders with real-time updates
        startListeningToOrders();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening when activity is not visible
        stopListeningToOrders();
    }

    private void setupViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
    }

    private void setupRecyclerView() {
        adapter = new OrderHistoryAdapter(orders);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);
    }

    /**
     * Start listening to orders with real-time updates
     */
    private void startListeningToOrders() {
        listenerRegistration = dbService.listenToOrdersByUserId(deviceId, new FirebaseDatabaseService.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orderList) {
                // Use debounced update to avoid frequent UI refreshes
                updateOrdersList(orderList);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load orders", e);
                Toast.makeText(OrderHistoryActivity.this, "Failed to load orders: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Update orders list with debouncing and efficient diff updates
     */
    private void updateOrdersList(List<Order> newOrders) {
        // Cancel any pending update
        if (pendingUpdate != null) {
            updateHandler.removeCallbacks(pendingUpdate);
        }

        // Create debounced update
        pendingUpdate = () -> {
            List<Order> oldOrders = new ArrayList<>(orders);
            
            // Calculate diff in background thread for better performance
            backgroundExecutor.execute(() -> {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                    new OrderDiffCallback(oldOrders, newOrders));
                
                // Update UI on main thread
                updateHandler.post(() -> {
                    orders.clear();
                    orders.addAll(newOrders);
                    diffResult.dispatchUpdatesTo(adapter);
                    
                    // Show/hide empty message
                    if (orders.isEmpty()) {
                        emptyTextView.setVisibility(View.VISIBLE);
                        ordersRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyTextView.setVisibility(View.GONE);
                        ordersRecyclerView.setVisibility(View.VISIBLE);
                    }
                    
                    Log.d(TAG, "Orders updated: " + orders.size() + " orders");
                });
            });
        };

        // Post debounced update
        updateHandler.postDelayed(pendingUpdate, UPDATE_DEBOUNCE_MS);
    }

    /**
     * Stop listening to orders
     */
    private void stopListeningToOrders() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        // Cancel any pending updates
        if (pendingUpdate != null) {
            updateHandler.removeCallbacks(pendingUpdate);
            pendingUpdate = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListeningToOrders();
        if (updateHandler != null) {
            updateHandler.removeCallbacksAndMessages(null);
        }
        // Shutdown executor to free resources
        if (backgroundExecutor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) backgroundExecutor).shutdown();
        }
    }

    /**
     * Order History Adapter
     */
    private class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
        private List<Order> orderList;

        public OrderHistoryAdapter(List<Order> orderList) {
            this.orderList = orderList;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_history, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orderList.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return orderList.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            private TextView orderNumberTextView;
            private TextView orderDateTextView;
            private TextView statusTextView;
            private TextView itemsTextView;
            private TextView totalTextView;
            private View statusIndicator;

            public OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                orderNumberTextView = itemView.findViewById(R.id.orderNumberTextView);
                orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                itemsTextView = itemView.findViewById(R.id.itemsTextView);
                totalTextView = itemView.findViewById(R.id.totalTextView);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
            }

            public void bind(Order order) {
                // Order number
                orderNumberTextView.setText("#" + order.getOrderId());

                // Order date (using cached formatter)
                String dateStr = dateFormat.format(new Date(order.getCreatedAt()));
                orderDateTextView.setText(dateStr);

                // Status
                String status = order.getStatus() != null ? order.getStatus() : "pending";
                String statusText = getStatusText(status);
                statusTextView.setText(statusText);

                // Status indicator color
                int statusColor = getStatusColor(status);
                statusIndicator.setBackgroundColor(statusColor);

                // Order items summary
                StringBuilder itemsText = new StringBuilder();
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    int itemCount = 0;
                    int totalItems = order.getItems().size();
                    for (OrderItem item : order.getItems()) {
                        if (itemCount > 0) itemsText.append(", ");
                        itemsText.append(item.getMenuItemName());
                        if (item.getQuantity() > 1) {
                            itemsText.append(" x").append(item.getQuantity());
                        }
                        itemCount++;
                        if (itemCount >= 3) {
                            itemsText.append("...");
                            break;
                        }
                    }
                    if (totalItems > 3) {
                        itemsText.append(" (+").append(totalItems - 3).append(" more)");
                    }
                } else {
                    itemsText.append("No items");
                }
                itemsTextView.setText(itemsText.toString());

                // Total
                totalTextView.setText(String.format("$%.2f", order.getTotal()));

                // Click listener to view order details
                itemView.setOnClickListener(v -> {
                    // Navigate to order tracking page
                    android.content.Intent intent = new android.content.Intent(OrderHistoryActivity.this, OrderTrackingActivity.class);
                    intent.putExtra("orderId", order.getOrderId());
                    startActivity(intent);
                });
            }

            private String getStatusText(String status) {
                switch (status.toLowerCase()) {
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
                        return status.substring(0, 1).toUpperCase() + status.substring(1);
                }
            }

            private int getStatusColor(String status) {
                switch (status.toLowerCase()) {
                    case "pending":
                        return 0xFFF44336; // Red
                    case "preparing":
                        return 0xFFFF9800; // Orange
                    case "ready":
                        return 0xFF4CAF50; // Green
                    case "completed":
                        return 0xFF2196F3; // Blue
                    case "cancelled":
                        return 0xFF9E9E9E; // Grey
                    default:
                        return 0xFF9E9E9E;
                }
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates
     */
    private static class OrderDiffCallback extends DiffUtil.Callback {
        private final List<Order> oldOrders;
        private final List<Order> newOrders;

        OrderDiffCallback(List<Order> oldOrders, List<Order> newOrders) {
            this.oldOrders = oldOrders;
            this.newOrders = newOrders;
        }

        @Override
        public int getOldListSize() {
            return oldOrders.size();
        }

        @Override
        public int getNewListSize() {
            return newOrders.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldOrders.get(oldItemPosition).getOrderId()
                    .equals(newOrders.get(newItemPosition).getOrderId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Order oldOrder = oldOrders.get(oldItemPosition);
            Order newOrder = newOrders.get(newItemPosition);
            return oldOrder.getStatus().equals(newOrder.getStatus()) &&
                   oldOrder.getCreatedAt() == newOrder.getCreatedAt() &&
                   oldOrder.getTotal() == newOrder.getTotal();
        }
    }
}

