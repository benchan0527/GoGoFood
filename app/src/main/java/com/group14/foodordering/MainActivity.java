package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.group14.foodordering.model.Admin;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.PermissionManager;
import com.group14.foodordering.util.RestaurantPreferenceHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseDatabaseService dbService;
    private Button btnKitchenView;
    private Button btnTableOrder;
    private Button btnTestData;
    private Button btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        initializeFirebase();
        
        dbService = FirebaseDatabaseService.getInstance();
        
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

        // Admin login button
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        btnAdminLogin.setOnClickListener(v -> showAdminLoginDialog());
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
            
            btnAdminLogin.setText("Admin: " + adminInfo);
            btnAdminLogin.setOnClickListener(v -> {
                // Logout
                AdminSessionHelper.clearAdminSession(this);
                updateAdminButtonsVisibility();
                Toast.makeText(this, "Admin logged out", Toast.LENGTH_SHORT).show();
            });
        } else {
            btnKitchenView.setVisibility(View.GONE);
            btnTableOrder.setVisibility(View.GONE);
            btnAdminLogin.setText("Admin Login");
            btnAdminLogin.setOnClickListener(v -> showAdminLoginDialog());
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