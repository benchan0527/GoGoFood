package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.model.Order;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Order Search Activity
 * GUI #16: Order List & Search Screen (POS)
 * Allows servers to search and list all active orders
 */
public class OrderSearchActivity extends AppCompatActivity {

    private static final String TAG = "OrderSearchActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView ordersRecyclerView;
    private OrdersAdapter ordersAdapter;
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    private EditText searchEditText;
    private Spinner statusFilterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_search);

        dbService = FirebaseDatabaseService.getInstance();
        allOrders = new ArrayList<>();
        filteredOrders = new ArrayList<>();

        setupViews();
        loadOrders();
    }

    private void setupViews() {
        searchEditText = findViewById(R.id.searchEditText);
        statusFilterSpinner = findViewById(R.id.statusFilterSpinner);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);

        // Setup RecyclerView
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersAdapter = new OrdersAdapter();
        ordersRecyclerView.setAdapter(ordersAdapter);

        // Setup status filter spinner
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.order_status_filter,
                android.R.layout.simple_spinner_item
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusFilterSpinner.setAdapter(statusAdapter);

        // Setup search text watcher
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup status filter listener
        statusFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Load all active orders
     */
    private void loadOrders() {
        dbService.getAllActiveOrders(new FirebaseDatabaseService.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                allOrders.clear();
                allOrders.addAll(orders);
                filterOrders();
                Log.d(TAG, "Loaded " + orders.size() + " active orders");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load orders", e);
                Toast.makeText(OrderSearchActivity.this, "Failed to load orders: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Filter orders based on search query and status filter
     */
    private void filterOrders() {
        String searchQuery = searchEditText.getText().toString().trim().toLowerCase();
        String statusFilter = statusFilterSpinner.getSelectedItem().toString();

        filteredOrders.clear();

        for (Order order : allOrders) {
            // Apply status filter
            boolean statusMatches = statusFilter.equals("All") || 
                                   statusFilter.equalsIgnoreCase(order.getStatus());

            if (!statusMatches) {
                continue;
            }

            // Apply search query
            if (searchQuery.isEmpty()) {
                filteredOrders.add(order);
            } else {
                // Search in order ID, table number, user ID
                String queryLower = searchQuery.toLowerCase();
                boolean matches = (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(queryLower))
                        || (order.getTableNumber() != null && order.getTableNumber().toLowerCase().contains(queryLower))
                        || (order.getUserId() != null && order.getUserId().toLowerCase().contains(queryLower));
                if (matches) {
                    filteredOrders.add(order);
                }
            }
        }

        ordersAdapter.notifyDataSetChanged();
    }

    /**
     * Handle order click - navigate to TableOrderActivity
     */
    private void onOrderClick(Order order) {
        if (order.getTableNumber() != null && !order.getTableNumber().isEmpty()) {
            Intent intent = new Intent(OrderSearchActivity.this, TableOrderActivity.class);
            intent.putExtra("tableNumber", order.getTableNumber());
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "This order is not a table order", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Orders RecyclerView Adapter
     */
    private class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_search, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = filteredOrders.get(position);
            holder.bind(order);
        }

        @Override
        public int getItemCount() {
            return filteredOrders.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView orderIdTextView;
            TextView tableNumberTextView;
            TextView statusTextView;
            TextView timeTextView;
            TextView totalTextView;
            TextView itemsCountTextView;

            OrderViewHolder(@NonNull View itemView) {
                super(itemView);
                orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
                tableNumberTextView = itemView.findViewById(R.id.tableNumberTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                totalTextView = itemView.findViewById(R.id.totalTextView);
                itemsCountTextView = itemView.findViewById(R.id.itemsCountTextView);
            }

            void bind(Order order) {
                if (order == null) {
                    return;
                }

                // Set order ID
                String orderId = order.getOrderId() != null ? order.getOrderId() : "N/A";
                if (orderId.length() > 8) {
                    orderIdTextView.setText("#" + orderId.substring(orderId.length() - 8));
                } else {
                    orderIdTextView.setText("#" + orderId);
                }

                // Set table number
                String tableNumber = order.getTableNumber() != null ? order.getTableNumber() : "N/A";
                tableNumberTextView.setText("Table: " + tableNumber);

                // Set status
                String status = order.getStatus() != null ? order.getStatus() : "pending";
                statusTextView.setText(status.toUpperCase());
                
                // Set status color
                int statusColor;
                switch (status) {
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
                        statusColor = 0xFF757575; // Grey
                        break;
                }
                statusTextView.setTextColor(statusColor);

                // Set time
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                String timeStr = sdf.format(new Date(order.getCreatedAt()));
                timeTextView.setText(timeStr);

                // Set total
                totalTextView.setText("$" + String.format("%.2f", order.getTotal()));

                // Set items count
                int itemsCount = order.getItems() != null ? order.getItems().size() : 0;
                itemsCountTextView.setText(itemsCount + " items");

                // Set click listener
                itemView.setOnClickListener(v -> onOrderClick(order));
            }
        }
    }
}

