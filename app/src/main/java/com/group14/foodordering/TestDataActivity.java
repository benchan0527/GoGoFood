package com.group14.foodordering;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.OrderItem;
import com.group14.foodordering.model.User;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.MenuJsonParser;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Test data storage Activity
 * Used for testing Firebase database CRUD operations
 */
public class TestDataActivity extends AppCompatActivity {

    private static final String TAG = "TestDataActivity";
    private FirebaseDatabaseService dbService;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_data);

        dbService = FirebaseDatabaseService.getInstance();
        resultTextView = findViewById(R.id.resultTextView);

        setupButtons();
    }

    private void setupButtons() {
        // Test user creation
        Button btnTestUser = findViewById(R.id.btnTestUser);
        btnTestUser.setOnClickListener(v -> testCreateUser());

        // Test admin creation
        Button btnTestAdmin = findViewById(R.id.btnTestAdmin);
        btnTestAdmin.setOnClickListener(v -> testCreateAdmin());

        // Test menu item creation
        Button btnTestMenuItem = findViewById(R.id.btnTestMenuItem);
        btnTestMenuItem.setOnClickListener(v -> testCreateMenuItem());

        // Test order creation
        Button btnTestOrder = findViewById(R.id.btnTestOrder);
        btnTestOrder.setOnClickListener(v -> testCreateOrder());

        // Test get menu
        Button btnGetMenu = findViewById(R.id.btnGetMenu);
        btnGetMenu.setOnClickListener(v -> testGetMenuItems());

        // Test get orders
        Button btnGetOrders = findViewById(R.id.btnGetOrders);
        btnGetOrders.setOnClickListener(v -> testGetPendingOrders());

        // Import menu from JSON
        Button btnImportMenu = findViewById(R.id.btnImportMenu);
        btnImportMenu.setOnClickListener(v -> importMenuFromJson());

        // Clear results
        Button btnClear = findViewById(R.id.btnClear);
        btnClear.setOnClickListener(v -> resultTextView.setText(""));
    }

    /**
     * Test user creation
     */
    private void testCreateUser() {
        String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User(userId, "test@example.com", "Test User", "1234567890", "customer");
        
        dbService.createOrUpdateUser(user, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                String message = "User created successfully: " + documentId;
                Log.d(TAG, message);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                String message = "User creation failed: " + e.getMessage();
                Log.e(TAG, message, e);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Test admin creation
     */
    private void testCreateAdmin() {
        String adminId = "admin_" + UUID.randomUUID().toString().substring(0, 8);
        String userId = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String[] permissions = {"menu_edit", "report_view", "inventory_manage"};
        Admin admin = new Admin(adminId, userId, "admin@example.com", "Administrator", "9876543210", permissions);
        
        dbService.createOrUpdateAdmin(admin, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                String message = "Admin created successfully: " + documentId;
                Log.d(TAG, message);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                String message = "Admin creation failed: " + e.getMessage();
                Log.e(TAG, message, e);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Test menu item creation
     */
    private void testCreateMenuItem() {
        String itemId = "item_" + UUID.randomUUID().toString().substring(0, 8);
        MenuItem item = new MenuItem(itemId, "Test Dish", "This is a test dish", 29.99, "main");
        item.setDescription("Delicious test dish with various ingredients");
        
        dbService.createOrUpdateMenuItem(item, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                String message = "Menu item created successfully: " + documentId;
                Log.d(TAG, message);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                String message = "Menu item creation failed: " + e.getMessage();
                Log.e(TAG, message, e);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Test order creation
     */
    private void testCreateOrder() {
        String orderId = "order_" + UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, "online");
        order.setUserId("test_user_123");
        
        // Add order items
        OrderItem item1 = new OrderItem("item_001", "Test Dish 1", 2, 29.99);
        item1.setCustomization("No onions");
        order.addItem(item1);
        
        OrderItem item2 = new OrderItem("item_002", "Test Dish 2", 1, 19.99);
        item2.setCookingDetails("Less salt");
        order.addItem(item2);
        
        order.setTax(5.0);
        order.setServiceCharge(3.0);
        
        dbService.createOrder(order, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                String message = "Order created successfully: " + documentId + "\nTotal: $" + order.getTotal();
                Log.d(TAG, message);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, "Order created successfully", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                String message = "Order creation failed: " + e.getMessage();
                Log.e(TAG, message, e);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Test getting menu items
     */
    private void testGetMenuItems() {
        dbService.getAllMenuItems(new FirebaseDatabaseService.MenuItemsCallback() {
            @Override
            public void onSuccess(java.util.List<MenuItem> items) {
                StringBuilder messageBuilder = new StringBuilder("Menu items retrieved successfully, total: " + items.size() + " items\n");
                for (MenuItem item : items) {
                    messageBuilder.append("- ").append(item.getName())
                            .append(" ($").append(item.getPrice()).append(")\n");
                }
                final String message = messageBuilder.toString();
                Log.d(TAG, message);
                runOnUiThread(() -> {
                    resultTextView.append(message);
                    Toast.makeText(TestDataActivity.this, "Retrieved " + items.size() + " menu items", 
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                String message = "Failed to retrieve menu items: " + e.getMessage();
                Log.e(TAG, message, e);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Test getting pending orders
     */
    private void testGetPendingOrders() {
        dbService.getPendingOrders(new FirebaseDatabaseService.OrdersCallback() {
            @Override
            public void onSuccess(java.util.List<Order> orders) {
                StringBuilder messageBuilder = new StringBuilder("Pending orders retrieved successfully, total: " + orders.size() + " orders\n");
                for (Order order : orders) {
                    messageBuilder.append("- Order ID: ").append(order.getOrderId())
                            .append(", Status: ").append(order.getStatus())
                            .append(", Total: $").append(order.getTotal()).append("\n");
                }
                final String message = messageBuilder.toString();
                Log.d(TAG, message);
                runOnUiThread(() -> {
                    resultTextView.append(message);
                    Toast.makeText(TestDataActivity.this, "Retrieved " + orders.size() + " pending orders", 
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                String message = "Failed to retrieve orders: " + e.getMessage();
                Log.e(TAG, message, e);
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Import menu items from menu.json file
     */
    private void importMenuFromJson() {
        resultTextView.append("Starting menu import...\n");
        
        // Try to read from assets folder first (standard Android approach)
        List<MenuItem> menuItems = null;
        try {
            resultTextView.append("Reading menu from assets...\n");
            menuItems = MenuJsonParser.parseMenuFromAssets(this, "menu.json");
        } catch (Exception e) {
            Log.e(TAG, "Failed to read from assets", e);
            // Fallback: Try to read from information folder (for development)
            String filePath = null;
            File projectRoot = new File(getFilesDir().getParentFile().getParentFile().getParentFile(), "information/menu.json");
            if (projectRoot.exists()) {
                filePath = projectRoot.getAbsolutePath();
            }
            
            if (filePath != null && new File(filePath).exists()) {
                resultTextView.append("Reading menu from: " + filePath + "\n");
                menuItems = MenuJsonParser.parseMenuFromFile(filePath);
            } else {
                resultTextView.append("Error: Could not find menu.json file.\n");
                resultTextView.append("Please ensure menu.json is in assets folder.\n");
                Toast.makeText(this, "Menu file not found", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        if (menuItems == null || menuItems.isEmpty()) {
            resultTextView.append("No menu items found in file.\n");
            Toast.makeText(this, "No menu items found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        resultTextView.append("Found " + menuItems.size() + " menu items to import.\n");
        resultTextView.append("Starting import to Firebase...\n");
        
        // Import items to Firebase
        final int[] successCount = {0};
        final int[] failCount = {0};
        final int totalItems = menuItems.size();
        
        for (MenuItem item : menuItems) {
            dbService.createOrUpdateMenuItem(item, new FirebaseDatabaseService.DatabaseCallback() {
                @Override
                public void onSuccess(String documentId) {
                    successCount[0]++;
                    if (successCount[0] + failCount[0] == totalItems) {
                        final String message = "Import completed: " + successCount[0] + " succeeded, " + failCount[0] + " failed";
                        runOnUiThread(() -> {
                            resultTextView.append(message + "\n");
                            Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    failCount[0]++;
                    Log.e(TAG, "Failed to import item: " + item.getName(), e);
                    if (successCount[0] + failCount[0] == totalItems) {
                        final String message = "Import completed: " + successCount[0] + " succeeded, " + failCount[0] + " failed";
                        runOnUiThread(() -> {
                            resultTextView.append(message + "\n");
                            Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        }
    }
}

