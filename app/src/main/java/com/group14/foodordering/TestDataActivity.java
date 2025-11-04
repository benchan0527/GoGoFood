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
import com.group14.foodordering.util.FirebaseDataImporter;
import com.group14.foodordering.util.MenuJsonParser;

import java.io.File;
import java.io.InputStream;
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
        // Import complete sample data - single button for all imports
        Button btnImportSampleData = findViewById(R.id.btnImportSampleData);
        btnImportSampleData.setOnClickListener(v -> importSampleData());

        // Clear database button
        Button btnClearDatabase = findViewById(R.id.btnClearDatabase);
        if (btnClearDatabase != null) {
            btnClearDatabase.setOnClickListener(v -> clearDatabase());
        }

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

    /**
     * Clear all data from Firebase database
     */
    private void clearDatabase() {
        resultTextView.setText("Starting to clear all database data...\n");
        resultTextView.append("===========================================\n");
        resultTextView.append("WARNING: This will delete ALL data from Firebase!\n\n");
        
        FirebaseDataImporter importer = new FirebaseDataImporter(this);
        FirebaseDataImporter.ImportCallback callback = new FirebaseDataImporter.ImportCallback() {
            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                });
                Log.d(TAG, message);
            }

            @Override
            public void onCollectionComplete(String collectionName, int successCount, int failCount) {
                String message = String.format("Collection '%s': %d deleted, %d failed\n", 
                        collectionName, successCount, failCount);
                runOnUiThread(() -> {
                    resultTextView.append(message);
                });
                Log.d(TAG, message);
            }

            @Override
            public void onComplete(int totalSuccess, int totalFail) {
                String message = String.format("\n===========================================\n" +
                        "Database Cleared!\n" +
                        "Total: %d documents deleted, %d failed\n" +
                        "===========================================\n\n" +
                        "All data has been removed from Firebase.\n" +
                        "You can now import fresh data using 'Import Sample Data'.\n", 
                        totalSuccess, totalFail);
                runOnUiThread(() -> {
                    resultTextView.append(message);
                    Toast.makeText(TestDataActivity.this, 
                            "Database cleared: " + totalSuccess + " deleted, " + totalFail + " failed", 
                            Toast.LENGTH_LONG).show();
                });
                Log.d(TAG, message);
            }

            @Override
            public void onError(Exception e) {
                String message = "Clear error: " + e.getMessage();
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, message, e);
            }
        };
        
        importer.clearAllData(callback);
    }

    /**
     * Import complete sample data from firebase_sample_data.json (restructured version)
     * Tries to read from assets first, then falls back to file system
     */
    private void importSampleData() {
        resultTextView.setText("Starting complete sample data import...\n");
        resultTextView.append("===========================================\n");
        resultTextView.append("Using restructured menu data (with base items)\n");
        resultTextView.append("Searching for firebase_sample_data.json...\n\n");
        
        FirebaseDataImporter importer = new FirebaseDataImporter(this);
        FirebaseDataImporter.ImportCallback callback = new FirebaseDataImporter.ImportCallback() {
            @Override
            public void onProgress(String message) {
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                });
                Log.d(TAG, message);
            }

            @Override
            public void onCollectionComplete(String collectionName, int successCount, int failCount) {
                String message = String.format("Collection '%s': %d succeeded, %d failed\n", 
                        collectionName, successCount, failCount);
                runOnUiThread(() -> {
                    resultTextView.append(message);
                });
                Log.d(TAG, message);
            }

            @Override
            public void onComplete(int totalSuccess, int totalFail) {
                String message = String.format("\n===========================================\n" +
                        "Import Complete!\n" +
                        "Total: %d succeeded, %d failed\n" +
                        "===========================================\n\n" +
                        "All data has been imported to Firebase!\n" +
                        "The menu now includes base items (e.g., Pineapple Bun with Butter)\n" +
                        "that can be combined with other items.\n\n" +
                        "You can now:\n" +
                        "1. Use the admin accounts to login\n" +
                        "2. View the menu items (183 total items)\n" +
                        "3. Check tables and orders\n", 
                        totalSuccess, totalFail);
                runOnUiThread(() -> {
                    resultTextView.append(message);
                    Toast.makeText(TestDataActivity.this, 
                            "Import complete: " + totalSuccess + " succeeded, " + totalFail + " failed", 
                            Toast.LENGTH_LONG).show();
                });
                Log.d(TAG, message);
            }

            @Override
            public void onError(Exception e) {
                String message = "Import error: " + e.getMessage();
                runOnUiThread(() -> {
                    resultTextView.append(message + "\n");
                    Toast.makeText(TestDataActivity.this, message, Toast.LENGTH_LONG).show();
                });
                Log.e(TAG, message, e);
            }
        };
        
        // Try to read from assets first (recommended approach)
        boolean foundInAssets = false;
        try {
            // Check if file exists in assets
            InputStream testStream = getAssets().open("firebase_sample_data.json");
            testStream.close();
            foundInAssets = true;
        } catch (Exception e) {
            Log.d(TAG, "File not found in assets, will try file system", e);
        }
        
        if (foundInAssets) {
            resultTextView.append("✓ Found file in assets folder\n");
            resultTextView.append("Starting import from assets...\n\n");
            importer.importFromAssets("firebase_sample_data.json", callback);
        } else {
            resultTextView.append("✗ Not found in assets, trying file system...\n");
            
            // Fallback: Try to read from information folder (for development)
            String filePath = null;
            
            // Try restructured file first, then original
            File[] possiblePaths = {
                new File(getFilesDir().getParentFile().getParentFile().getParentFile(), "information/firebase_sample_data_restructured.json"),
                new File(getFilesDir().getParentFile().getParentFile().getParentFile(), "information/firebase_sample_data.json"),
                new File(getExternalFilesDir(null), "firebase_sample_data.json"),
                new File(getFilesDir(), "firebase_sample_data.json"),
                new File("/sdcard/Download/firebase_sample_data.json"),
                new File("/storage/emulated/0/Download/firebase_sample_data.json")
            };
            
            for (File path : possiblePaths) {
                if (path != null && path.exists()) {
                    filePath = path.getAbsolutePath();
                    break;
                }
            }
            
            if (filePath == null || !new File(filePath).exists()) {
                resultTextView.append("Error: Could not find firebase_sample_data.json file.\n");
                resultTextView.append("Tried locations:\n");
                resultTextView.append("  - assets/firebase_sample_data.json\n");
                for (File path : possiblePaths) {
                    if (path != null) {
                        resultTextView.append("  - " + path.getAbsolutePath() + "\n");
                    }
                }
                resultTextView.append("\nPlease ensure the file exists in one of these locations.\n");
                Toast.makeText(this, "Sample data file not found", Toast.LENGTH_LONG).show();
                return;
            }
            
            resultTextView.append("✓ Found file at: " + filePath + "\n");
            resultTextView.append("Starting import from file system...\n\n");
            importer.importFromFile(filePath, callback);
        }
    }

    /**
     * Verify admin data exists and can be retrieved
     */
    private void verifyAdminData() {
        resultTextView.setText("Verifying admin data...\n");
        resultTextView.append("===========================================\n\n");
        
        // Test admin IDs from sample data
        String[] adminIds = {"ADMIN001", "ADMIN002", "ADMIN003"};
        String[] phoneNumbers = {"+1234567894", "+1234567892", "+1234567893"};
        String[] names = {"Charlie Wang (Manager)", "Alice Johnson (Server)", "Bob Chen (Kitchen)"};
        
        final int[] completedCount = {0};
        final int[] successCount = {0};
        final int[] failCount = {0};
        final int total = adminIds.length;
        
        for (int i = 0; i < adminIds.length; i++) {
            final String adminId = adminIds[i];
            final String phone = phoneNumbers[i];
            final String name = names[i];
            final int index = i;
            
            // Test by adminId
            dbService.getAdminByStaffIdOrPhone(adminId, new FirebaseDatabaseService.AdminCallback() {
                @Override
                public void onSuccess(Admin admin) {
                    successCount[0]++;
                    String message = String.format("✓ Admin %d (%s):\n", index + 1, name) +
                            "  - Found by adminId: " + adminId + " ✓\n" +
                            "  - Name: " + admin.getName() + "\n" +
                            "  - Phone: " + admin.getPhone() + "\n" +
                            "  - Email: " + admin.getEmail() + "\n" +
                            "  - Active: " + admin.isActive() + "\n" +
                            "  - Permissions: " + java.util.Arrays.toString(admin.getPermissions()) + "\n";
                    runOnUiThread(() -> {
                        resultTextView.append(message);
                    });
                    
                    // Test by phone number
                    dbService.getAdminByStaffIdOrPhone(phone, new FirebaseDatabaseService.AdminCallback() {
                        @Override
                        public void onSuccess(Admin adminByPhone) {
                            String phoneMessage = "  - Found by phone: " + phone + " ✓\n\n";
                            runOnUiThread(() -> {
                                resultTextView.append(phoneMessage);
                            });
                            completedCount[0]++;
                            checkComplete();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            String phoneMessage = "  - Found by phone: " + phone + " ✗ (" + e.getMessage() + ")\n\n";
                            runOnUiThread(() -> {
                                resultTextView.append(phoneMessage);
                            });
                            completedCount[0]++;
                            checkComplete();
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    failCount[0]++;
                    completedCount[0]++;
                    String message = String.format("✗ Admin %d (%s):\n", index + 1, name) +
                            "  - adminId: " + adminId + " ✗\n" +
                            "  - Error: " + e.getMessage() + "\n\n";
                    runOnUiThread(() -> {
                        resultTextView.append(message);
                    });
                    checkComplete();
                }
                
                private void checkComplete() {
                    if (completedCount[0] == total) {
                        String summary = String.format(
                                "===========================================\n" +
                                "Verification Complete!\n" +
                                "Success: %d, Failed: %d\n" +
                                "===========================================\n\n" +
                                "Login Instructions:\n" +
                                "1. Go to MainActivity\n" +
                                "2. Click 'Admin Login' button\n" +
                                "3. Enter one of the following:\n" +
                                "   - ADMIN001 (or +1234567894) for Manager\n" +
                                "   - ADMIN002 (or +1234567892) for Server\n" +
                                "   - ADMIN003 (or +1234567893) for Kitchen\n" +
                                "4. Password field is optional (not verified yet)\n",
                                successCount[0], failCount[0]);
                        runOnUiThread(() -> {
                            resultTextView.append(summary);
                            Toast.makeText(TestDataActivity.this,
                                    "Verification complete: " + successCount[0] + " succeeded, " + failCount[0] + " failed",
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        }
    }
}

