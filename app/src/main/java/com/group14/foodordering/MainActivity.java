package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.AdminRoleHelper;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.PermissionManager;
import com.group14.foodordering.util.RestaurantPreferenceHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseDatabaseService dbService;
    private Button btnKitchenView;
    private Button btnTableOrder;
    private Button btnTestData;
    private Button btnPermissionManagement;
    private Button btnAdminLogin;
    private Button btnLogout;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout adminRestaurantSelectorLayout;
    private Spinner spinnerRestaurantSelector;
    private List<Restaurant> accessibleRestaurants;
    private ArrayAdapter<Restaurant> restaurantAdapter;
    private boolean isInitializingSpinner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        initializeFirebase();
        
        dbService = FirebaseDatabaseService.getInstance();
        
        // Check if admin is logged in, if not redirect to customer main screen
        if (!AdminSessionHelper.isAdminLoggedIn(this)) {
            // Redirect to customer main screen
            Intent intent = new Intent(MainActivity.this, CustomerMainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Test Firebase connection
        testFirebaseConnection();
        
        // Restore admin session if exists
        restoreAdminSession();
        
        // Setup navigation buttons
        setupButtons();
        // Setup bottom navigation
        setupBottomNavigation();
        // Setup restaurant selector
        setupRestaurantSelector();
        updateAdminButtonsVisibility();
    }

    /**
     * Restore admin session from SharedPreferences
     */
    private void restoreAdminSession() {
        if (AdminSessionHelper.isAdminLoggedIn(this)) {
            String adminName = AdminSessionHelper.getAdminName(this);
            Log.d(TAG, "Admin session restored: " + adminName);
        }
    }

    private void setupButtons() {
        // Restaurant selection button - always visible
        Button btnSelectRestaurant = findViewById(R.id.btnSelectRestaurant);
        btnSelectRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RestaurantSelectionActivity.class);
            startActivity(intent);
        });
        
        // Customer button - always visible
        Button btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            // Check if restaurant is selected
            if (!RestaurantPreferenceHelper.hasSelectedRestaurant(this)) {
                Toast.makeText(this, "Please select a restaurant first", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, RestaurantSelectionActivity.class);
                startActivity(intent);
                return;
            }
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        // Order History button - always visible for customers
        Button btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        // Admin buttons - hidden until login and permission check
        btnKitchenView = findViewById(R.id.btnKitchenView);
        btnKitchenView.setOnClickListener(v -> {
            // Check if admin is logged in and has permission to view orders
            if (AdminSessionHelper.isAdminLoggedIn(this) && 
                PermissionManager.canViewOrders(this)) {
                Intent intent = new Intent(MainActivity.this, KitchenViewActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Permission denied: You need order view permission", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        btnTableOrder = findViewById(R.id.btnTableOrder);
        btnTableOrder.setOnClickListener(v -> {
            // Check if admin is logged in and has permission to manage tables or orders
            if (AdminSessionHelper.isAdminLoggedIn(this) && 
                (PermissionManager.canManageTables(this) || 
                 PermissionManager.canManageOrders(this))) {
                Intent intent = new Intent(MainActivity.this, TableOrderActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Permission denied: You need table or order management permission", 
                    Toast.LENGTH_SHORT).show();
            }
        });

        btnTestData = findViewById(R.id.btnTestData);
        btnTestData.setOnClickListener(v -> {
            // TestDataActivity is always accessible for importing test data
            Intent intent = new Intent(MainActivity.this, TestDataActivity.class);
            startActivity(intent);
        });

        // Permission Management button
        btnPermissionManagement = findViewById(R.id.btnPermissionManagement);
        btnPermissionManagement.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PermissionManagementActivity.class);
            startActivity(intent);
        });

        // Admin login button
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        btnAdminLogin.setOnClickListener(v -> showAdminLoginDialog());
        
        // Logout button
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> handleLogout());
        
        // Restaurant selector UI
        adminRestaurantSelectorLayout = findViewById(R.id.adminRestaurantSelectorLayout);
        spinnerRestaurantSelector = findViewById(R.id.spinnerRestaurantSelector);
    }
    
    /**
     * Setup restaurant selector for admin
     */
    private void setupRestaurantSelector() {
        accessibleRestaurants = new ArrayList<>();
        
        // Create adapter for spinner
        restaurantAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, 
            accessibleRestaurants);
        restaurantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRestaurantSelector.setAdapter(restaurantAdapter);
        
        // Handle restaurant selection
        spinnerRestaurantSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Skip if we're initializing (programmatic selection)
                if (isInitializingSpinner) {
                    return;
                }
                
                if (position >= 0 && position < accessibleRestaurants.size()) {
                    Restaurant selectedRestaurant = accessibleRestaurants.get(position);
                    AdminSessionHelper.setAdminSelectedRestaurantId(MainActivity.this, 
                        selectedRestaurant.getRestaurantId());
                    AdminSessionHelper.setAdminSelectedRestaurantName(MainActivity.this, 
                        selectedRestaurant.getRestaurantName());
                    Log.d(TAG, "Admin selected restaurant: " + selectedRestaurant.getRestaurantName());
                    Toast.makeText(MainActivity.this, 
                        "Selected: " + selectedRestaurant.getRestaurantName(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    /**
     * Load restaurants for admin based on their access level
     */
    private void loadAdminRestaurants() {
        if (!AdminSessionHelper.isAdminLoggedIn(this)) {
            return;
        }
        
        List<String> accessibleRestaurantIds = AdminRoleHelper.getAccessibleRestaurantIds(this);
        
        dbService.getAllRestaurants(new FirebaseDatabaseService.RestaurantsCallback() {
            @Override
            public void onSuccess(List<Restaurant> allRestaurants) {
                accessibleRestaurants.clear();
                
                // Filter restaurants based on admin access
                if (accessibleRestaurantIds == null) {
                    // ADMIN: can access all restaurants
                    accessibleRestaurants.addAll(allRestaurants);
                } else {
                    // MANAGER/STAFF: filter by accessible restaurant IDs
                    for (Restaurant restaurant : allRestaurants) {
                        if (accessibleRestaurantIds.contains(restaurant.getRestaurantId())) {
                            accessibleRestaurants.add(restaurant);
                        }
                    }
                }
                
                restaurantAdapter.notifyDataSetChanged();
                
                // For ADMIN/MANAGER, try to restore previously selected restaurant
                if (!accessibleRestaurants.isEmpty()) {
                    isInitializingSpinner = true;
                    String selectedRestaurantId = AdminSessionHelper.getAdminSelectedRestaurantId(MainActivity.this);
                    boolean found = false;
                    
                    if (selectedRestaurantId != null) {
                        for (int i = 0; i < accessibleRestaurants.size(); i++) {
                            if (accessibleRestaurants.get(i).getRestaurantId().equals(selectedRestaurantId)) {
                                spinnerRestaurantSelector.setSelection(i, false);
                                found = true;
                                break;
                            }
                        }
                    }
                    
                    // If no previous selection or not found, select first restaurant by default
                    if (!found) {
                        Restaurant firstRestaurant = accessibleRestaurants.get(0);
                        AdminSessionHelper.setAdminSelectedRestaurantId(MainActivity.this, 
                            firstRestaurant.getRestaurantId());
                        AdminSessionHelper.setAdminSelectedRestaurantName(MainActivity.this, 
                            firstRestaurant.getRestaurantName());
                        spinnerRestaurantSelector.setSelection(0, false);
                    }
                    
                    isInitializingSpinner = false;
                }
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load restaurants for admin", e);
                Toast.makeText(MainActivity.this, 
                    "Failed to load restaurants: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Setup bottom navigation bar
     */
    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_admin_main) {
                // Already on main page, do nothing
                return true;
            } else if (itemId == R.id.nav_kitchen_view) {
                if (AdminSessionHelper.isAdminLoggedIn(this) && 
                    PermissionManager.canViewOrders(this)) {
                    Intent intent = new Intent(MainActivity.this, KitchenViewActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Permission denied: You need order view permission", 
                        Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_table_order) {
                if (AdminSessionHelper.isAdminLoggedIn(this) && 
                    (PermissionManager.canManageTables(this) || 
                     PermissionManager.canManageOrders(this))) {
                    Intent intent = new Intent(MainActivity.this, TableOrderActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Permission denied: You need table or order management permission", 
                        Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_test_data) {
                Intent intent = new Intent(MainActivity.this, TestDataActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_logout) {
                handleLogout();
                return true;
            }
            
            return false;
        });
        
        // Set the main menu item as selected by default
        bottomNavigationView.setSelectedItemId(R.id.nav_admin_main);
    }
    
    /**
     * Handle logout action
     */
    private void handleLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                AdminSessionHelper.clearAdminSession(this);
                Toast.makeText(this, "Admin logged out successfully", Toast.LENGTH_SHORT).show();
                
                // Redirect to customer main screen
                Intent intent = new Intent(MainActivity.this, CustomerMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Show admin login dialog
     */
    private void showAdminLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Admin Login");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText staffIdInput = new EditText(this);
        staffIdInput.setHint("Staff ID or Phone");
        staffIdInput.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(staffIdInput);

        final EditText passwordInput = new EditText(this);
        passwordInput.setHint("Password (optional)");
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(passwordInput);

        builder.setView(layout);

        builder.setPositiveButton("Login", (dialog, which) -> {
            String staffIdOrPhone = staffIdInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            
            if (staffIdOrPhone.isEmpty()) {
                Toast.makeText(this, "Staff ID or Phone is required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            loginAdmin(staffIdOrPhone, password);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Login admin using staff ID or phone
     */
    private void loginAdmin(String staffIdOrPhone, String password) {
        dbService.getAdminByStaffIdOrPhone(staffIdOrPhone, new FirebaseDatabaseService.AdminCallback() {
            @Override
            public void onSuccess(Admin admin) {
                // Check if admin is null
                if (admin == null) {
                    Toast.makeText(MainActivity.this, "Admin data is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // For now, we just check if admin exists and is active
                // In a real app, you would verify the password here
                if (admin.isActive()) {
                    // Save admin session to SharedPreferences
                    AdminSessionHelper.saveAdminSession(MainActivity.this, admin);
                    updateAdminButtonsVisibility();
                    
                    String permissionsInfo = PermissionManager.getPermissionsString(MainActivity.this);
                    Toast.makeText(MainActivity.this, 
                        "Admin login successful: " + admin.getName() + "\nPermissions: " + permissionsInfo, 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Admin account is inactive", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Admin login failed", e);
                Toast.makeText(MainActivity.this, "Admin login failed: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Update visibility of admin buttons based on login status and permissions
     */
    private void updateAdminButtonsVisibility() {
        // TestData button is always visible for importing test data
        btnTestData.setVisibility(View.VISIBLE);
        
        boolean isAdminLoggedIn = AdminSessionHelper.isAdminLoggedIn(this);
        
        if (isAdminLoggedIn) {
            String adminName = AdminSessionHelper.getAdminName(this);
            String adminInfo = adminName != null ? adminName : "Admin";
            
            // Show buttons based on permissions
            boolean canViewOrders = PermissionManager.canViewOrders(this);
            boolean canManageTables = PermissionManager.canManageTables(this);
            boolean canManageOrders = PermissionManager.canManageOrders(this);
            
            btnKitchenView.setVisibility(canViewOrders ? View.VISIBLE : View.GONE);
            btnTableOrder.setVisibility((canManageTables || canManageOrders) ? View.VISIBLE : View.GONE);
            
            // Show permission management button for ADMIN and MANAGER only
            String role = AdminRoleHelper.getAdminRole(this);
            boolean canManagePermissions = AdminRoleHelper.ROLE_ADMIN.equals(role) || 
                                          AdminRoleHelper.ROLE_MANAGER.equals(role);
            btnPermissionManagement.setVisibility(canManagePermissions ? View.VISIBLE : View.GONE);
            
            // Show logout button
            btnLogout.setVisibility(View.VISIBLE);
            
            // Update admin login button text with role info
            String roleDisplay = AdminRoleHelper.getRoleDisplayName(this);
            btnAdminLogin.setText("Admin: " + adminInfo + " (" + roleDisplay + ")");
            btnAdminLogin.setOnClickListener(v -> {
                // Show admin info or allow re-login
                showAdminLoginDialog();
            });
            
            // Show/hide restaurant selector based on role
            boolean needsSelection = AdminRoleHelper.needsRestaurantSelection(this);
            if (needsSelection && adminRestaurantSelectorLayout != null) {
                adminRestaurantSelectorLayout.setVisibility(View.VISIBLE);
                // Load restaurants for selection
                loadAdminRestaurants();
            } else {
                if (adminRestaurantSelectorLayout != null) {
                    adminRestaurantSelectorLayout.setVisibility(View.GONE);
                }
                // For STAFF, auto-select their single restaurant
                if (AdminRoleHelper.ROLE_STAFF.equals(role)) {
                    String staffRestaurantId = AdminRoleHelper.getStaffRestaurantId(this);
                    if (staffRestaurantId != null) {
                        // Load restaurant name and save it
                        dbService.getRestaurantById(staffRestaurantId, new FirebaseDatabaseService.RestaurantCallback() {
                            @Override
                            public void onSuccess(Restaurant restaurant) {
                                if (restaurant != null) {
                                    AdminSessionHelper.setAdminSelectedRestaurantId(MainActivity.this, 
                                        restaurant.getRestaurantId());
                                    AdminSessionHelper.setAdminSelectedRestaurantName(MainActivity.this, 
                                        restaurant.getRestaurantName());
                                }
                            }
                            
                            @Override
                            public void onFailure(Exception e) {
                                Log.e(TAG, "Failed to load staff restaurant", e);
                            }
                        });
                    }
                }
            }
            
            // Show bottom navigation
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        } else {
            btnKitchenView.setVisibility(View.GONE);
            btnTableOrder.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
            btnAdminLogin.setText("Admin Login");
            btnAdminLogin.setOnClickListener(v -> showAdminLoginDialog());
            
            // Hide restaurant selector
            if (adminRestaurantSelectorLayout != null) {
                adminRestaurantSelectorLayout.setVisibility(View.GONE);
            }
            
            // Hide bottom navigation when not logged in
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update button visibility when returning to this activity
        updateAdminButtonsVisibility();
    }

    /**
     * Initialize Firebase
     */
    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase already initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed: " + e.getMessage(), e);
            Toast.makeText(this, "Firebase initialization failed, please check google-services.json file", 
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Test Firebase connection
     */
    private void testFirebaseConnection() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Log.d(TAG, "Firebase Firestore instance created successfully");
            Toast.makeText(this, "Firebase connected successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Firebase connection test failed: " + e.getMessage(), e);
            Toast.makeText(this, "Firebase connection failed: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
    }
}