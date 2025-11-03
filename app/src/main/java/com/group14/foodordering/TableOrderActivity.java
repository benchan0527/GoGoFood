package com.group14.foodordering;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
 * Table Order Activity
 * UC-4: Servers can create table orders
 * UC-5: Servers can modify existing orders
 */
public class TableOrderActivity extends AppCompatActivity {

    private static final String TAG = "TableOrderActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView menuRecyclerView;
    private RecyclerView orderItemsRecyclerView;
    private MenuAdapter menuAdapter;
    private OrderItemsAdapter orderItemsAdapter;
    private List<MenuItem> menuItems;
    private List<OrderItem> currentOrderItems;
    private String currentTableNumber;
    private String currentOrderId;
    private TextView tableNumberTextView;
    private TextView orderTotalTextView;
    private Button createOrderButton;
    private Button updateOrderButton;
    private Button loadExistingOrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_order);

        dbService = FirebaseDatabaseService.getInstance();
        menuItems = new ArrayList<>();
        currentOrderItems = new ArrayList<>();

        setupViews();
        loadMenuItems();
        promptTableNumber();
    }

    private void setupViews() {
        tableNumberTextView = findViewById(R.id.tableNumberTextView);
        orderTotalTextView = findViewById(R.id.orderTotalTextView);
        createOrderButton = findViewById(R.id.createOrderButton);
        updateOrderButton = findViewById(R.id.updateOrderButton);
        loadExistingOrderButton = findViewById(R.id.loadExistingOrderButton);

        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuAdapter = new MenuAdapter();
        menuRecyclerView.setAdapter(menuAdapter);

        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemsAdapter = new OrderItemsAdapter();
        orderItemsRecyclerView.setAdapter(orderItemsAdapter);

        createOrderButton.setOnClickListener(v -> createOrder());
        updateOrderButton.setOnClickListener(v -> updateOrder());
        loadExistingOrderButton.setOnClickListener(v -> promptLoadOrder());

        updateOrderButton.setEnabled(false);
    }

    /**
     * Prompt for table number
     */
    private void promptTableNumber() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Table Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Please enter table number");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String tableNumber = input.getText().toString().trim();
            if (!tableNumber.isEmpty()) {
                currentTableNumber = tableNumber;
                tableNumberTextView.setText("Table: " + tableNumber);
                loadExistingOrdersForTable(tableNumber);
            } else {
                Toast.makeText(this, "Table number cannot be empty", Toast.LENGTH_SHORT).show();
                promptTableNumber();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> finish());
        builder.show();
    }

    /**
     * Load all orders for a table
     */
    private void loadExistingOrdersForTable(String tableNumber) {
        dbService.getOrdersByTableNumber(tableNumber, new FirebaseDatabaseService.OrdersCallback() {
            @Override
            public void onSuccess(List<Order> orders) {
                if (!orders.isEmpty()) {
                    // Show dialog for user to select order to load
                    showOrderSelectionDialog(orders);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "No existing orders for this table");
            }
        });
    }

    /**
     * Show order selection dialog
     */
    private void showOrderSelectionDialog(List<Order> orders) {
        String[] orderIds = new String[orders.size()];
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            orderIds[i] = "Order ID: " + order.getOrderId() + 
                    " (Status: " + order.getStatus() + ", Total: $" + order.getTotal() + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Order to Load");
        builder.setItems(orderIds, (dialog, which) -> {
            loadOrder(orders.get(which));
        });
        builder.setNegativeButton("Create New Order", (dialog, which) -> {
            // Create new order
        });
        builder.show();
    }

    /**
     * Prompt to load order
     */
    private void promptLoadOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Order ID");

        final EditText input = new EditText(this);
        input.setHint("Please enter order ID");
        builder.setView(input);

        builder.setPositiveButton("Load", (dialog, which) -> {
            String orderId = input.getText().toString().trim();
            if (!orderId.isEmpty()) {
                dbService.getOrderById(orderId, new FirebaseDatabaseService.OrderCallback() {
                    @Override
                    public void onSuccess(Order order) {
                        if (order.getTableNumber().equals(currentTableNumber)) {
                            loadOrder(order);
                        } else {
                            Toast.makeText(TableOrderActivity.this, 
                                    "This order does not belong to current table", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(TableOrderActivity.this, 
                                "Failed to load order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Load order
     */
    private void loadOrder(Order order) {
        currentOrderId = order.getOrderId();
        currentOrderItems.clear();
        currentOrderItems.addAll(order.getItems());
        orderItemsAdapter.notifyDataSetChanged();
        updateOrderButton.setEnabled(true);
        createOrderButton.setEnabled(false);
        updateTotal();
        Toast.makeText(this, "Order loaded successfully", Toast.LENGTH_SHORT).show();
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
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load menu items", e);
                Toast.makeText(TableOrderActivity.this, "Failed to load menu: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Add order item
     */
    private void addOrderItem(String menuItemId, String customization, String cookingDetails) {
        for (MenuItem item : menuItems) {
            if (item.getItemId().equals(menuItemId)) {
                OrderItem orderItem = new OrderItem(menuItemId, item.getName(), 1, item.getPrice());
                orderItem.setCustomization(customization);
                orderItem.setCookingDetails(cookingDetails);
                
                // Check if same item already exists
                boolean found = false;
                for (OrderItem existingItem : currentOrderItems) {
                    if (existingItem.getMenuItemId().equals(menuItemId) &&
                        existingItem.getCustomization().equals(customization) &&
                        existingItem.getCookingDetails().equals(cookingDetails)) {
                        existingItem.setQuantity(existingItem.getQuantity() + 1);
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                    currentOrderItems.add(orderItem);
                }
                
                orderItemsAdapter.notifyDataSetChanged();
                updateTotal();
                break;
            }
        }
    }

    /**
     * Remove order item
     */
    private void removeOrderItem(int position) {
        if (position >= 0 && position < currentOrderItems.size()) {
            currentOrderItems.remove(position);
            orderItemsAdapter.notifyDataSetChanged();
            updateTotal();
        }
    }

    /**
     * Update total
     */
    private void updateTotal() {
        double total = 0.0;
        for (OrderItem item : currentOrderItems) {
            total += item.getTotalPrice();
        }
        orderTotalTextView.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Create order
     */
    private void createOrder() {
        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "Order items cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String orderId = "order_" + UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, "table");
        order.setTableNumber(currentTableNumber);

        for (OrderItem item : currentOrderItems) {
            order.addItem(item);
        }

        order.setTax(order.getSubtotal() * 0.1);
        order.setServiceCharge(0.0);

        dbService.createOrder(order, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                Log.d(TAG, "Order created successfully: " + documentId);
                Toast.makeText(TableOrderActivity.this, "Order created successfully!", Toast.LENGTH_SHORT).show();
                currentOrderId = documentId;
                createOrderButton.setEnabled(false);
                updateOrderButton.setEnabled(true);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Order creation failed", e);
                Toast.makeText(TableOrderActivity.this, "Order creation failed: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Update order
     */
    private void updateOrder() {
        if (currentOrderId == null) {
            Toast.makeText(this, "No order to update", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "Order items cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        dbService.getOrderById(currentOrderId, new FirebaseDatabaseService.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                order.getItems().clear();
                for (OrderItem item : currentOrderItems) {
                    order.addItem(item);
                }
                order.setTax(order.getSubtotal() * 0.1);

                dbService.updateOrder(order, new FirebaseDatabaseService.DatabaseCallback() {
                    @Override
                    public void onSuccess(String documentId) {
                        Log.d(TAG, "Order updated successfully: " + documentId);
                        Toast.makeText(TableOrderActivity.this, "Order updated successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Order update failed", e);
                        Toast.makeText(TableOrderActivity.this, "Order update failed: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(TableOrderActivity.this, "Failed to load order: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Menu adapter
     */
    private class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

        @NonNull
        @Override
        public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_menu_table, parent, false);
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
            TextView priceTextView;
            Button addButton;

            MenuViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.itemNameTextView);
                priceTextView = itemView.findViewById(R.id.itemPriceTextView);
                addButton = itemView.findViewById(R.id.addButton);
            }

            void bind(MenuItem item) {
                nameTextView.setText(item.getName());
                priceTextView.setText(String.format("$%.2f", item.getPrice()));

                addButton.setOnClickListener(v -> {
                    // Show customization dialog
                    showCustomizationDialog(item);
                });
            }
        }
    }

    /**
     * Show customization dialog
     */
    private void showCustomizationDialog(MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Dish: " + item.getName());

        final EditText customizationInput = new EditText(this);
        customizationInput.setHint("Customization (e.g., no onions)");
        customizationInput.setInputType(InputType.TYPE_CLASS_TEXT);

        final EditText cookingInput = new EditText(this);
        cookingInput.setHint("Cooking instructions (e.g., less salt)");
        cookingInput.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(customizationInput);
        layout.addView(cookingInput);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String customization = customizationInput.getText().toString().trim();
            String cooking = cookingInput.getText().toString().trim();
            addOrderItem(item.getItemId(), customization, cooking);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Order items adapter
     */
    private class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {

        @NonNull
        @Override
        public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_order_item, parent, false);
            return new OrderItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
            OrderItem item = currentOrderItems.get(position);
            holder.bind(item, position);
        }

        @Override
        public int getItemCount() {
            return currentOrderItems.size();
        }

        class OrderItemViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            TextView quantityTextView;
            TextView priceTextView;
            TextView customizationTextView;
            Button removeButton;

            OrderItemViewHolder(@NonNull View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.itemNameTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                priceTextView = itemView.findViewById(R.id.priceTextView);
                customizationTextView = itemView.findViewById(R.id.customizationTextView);
                removeButton = itemView.findViewById(R.id.removeButton);
            }

            void bind(OrderItem item, int position) {
                nameTextView.setText(item.getMenuItemName());
                quantityTextView.setText("x" + item.getQuantity());
                priceTextView.setText(String.format("$%.2f", item.getTotalPrice()));
                
                StringBuilder customizationText = new StringBuilder();
                if (item.getCustomization() != null && !item.getCustomization().isEmpty()) {
                    customizationText.append("Custom: ").append(item.getCustomization());
                }
                if (item.getCookingDetails() != null && !item.getCookingDetails().isEmpty()) {
                    if (customizationText.length() > 0) customizationText.append("\n");
                    customizationText.append("Cooking: ").append(item.getCookingDetails());
                }
                customizationTextView.setText(customizationText.toString());

                removeButton.setOnClickListener(v -> removeOrderItem(position));
            }
        }
    }
}

