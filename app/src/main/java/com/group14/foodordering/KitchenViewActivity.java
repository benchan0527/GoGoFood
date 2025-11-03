package com.group14.foodordering;

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

import com.google.firebase.firestore.ListenerRegistration;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * Kitchen View Activity
 * UC-7: Kitchen staff can view new order list in real-time
 */
public class KitchenViewActivity extends AppCompatActivity {

    private static final String TAG = "KitchenViewActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<Order> pendingOrders;
    private ListenerRegistration ordersListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchen_view);

        dbService = FirebaseDatabaseService.getInstance();
        pendingOrders = new ArrayList<>();

        setupViews();
        loadPendingOrders();
    }

    private void setupViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrdersAdapter();
        ordersRecyclerView.setAdapter(ordersAdapter);
    }

    /**
     * Load pending orders
     */
    private void loadPendingOrders() {
        dbService.getPendingOrders(new FirebaseDatabaseService.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                pendingOrders.clear();
                pendingOrders.addAll(orders);
                ordersAdapter.notifyDataSetChanged();
                Log.d(TAG, "Pending orders loaded successfully, total: " + orders.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load pending orders", e);
                Toast.makeText(KitchenViewActivity.this, "Failed to load orders: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Update order status
     */
    private void updateOrderStatus(String orderId, String newStatus) {
        dbService.updateOrderStatus(orderId, newStatus, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                Log.d(TAG, "Order status updated successfully: " + documentId + " -> " + newStatus);
                Toast.makeText(KitchenViewActivity.this, "Order status updated", Toast.LENGTH_SHORT).show();
                loadPendingOrders(); // Refresh list
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update order status", e);
                Toast.makeText(KitchenViewActivity.this, "Update failed: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            TextView statusTextView;
            TextView itemsTextView;
            TextView totalTextView;
            Button preparingButton;
            Button readyButton;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
                orderTypeTextView = itemView.findViewById(R.id.orderTypeTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                itemsTextView = itemView.findViewById(R.id.itemsTextView);
                totalTextView = itemView.findViewById(R.id.totalTextView);
                preparingButton = itemView.findViewById(R.id.preparingButton);
                readyButton = itemView.findViewById(R.id.readyButton);
            }

            void bind(Order order) {
                orderIdTextView.setText("Order ID: " + order.getOrderId());
                orderTypeTextView.setText("Type: " + order.getOrderType());
                statusTextView.setText("Status: " + order.getStatus());
                
                // Display order items
                StringBuilder itemsText = new StringBuilder("Order Items:\n");
                for (OrderItem item : order.getItems()) {
                    itemsText.append(String.format("- %s x%d\n", item.getMenuItemName(), item.getQuantity()));
                }
                itemsTextView.setText(itemsText.toString());
                
                totalTextView.setText(String.format("Total: $%.2f", order.getTotal()));

                // Set buttons based on current status
                if ("pending".equals(order.getStatus())) {
                    preparingButton.setEnabled(true);
                    readyButton.setEnabled(false);
                    preparingButton.setOnClickListener(v -> updateOrderStatus(order.getOrderId(), "preparing"));
                } else if ("preparing".equals(order.getStatus())) {
                    preparingButton.setEnabled(false);
                    readyButton.setEnabled(true);
                    readyButton.setOnClickListener(v -> updateOrderStatus(order.getOrderId(), "ready"));
                } else {
                    preparingButton.setEnabled(false);
                    readyButton.setEnabled(false);
                }
            }
        }
    }
}

