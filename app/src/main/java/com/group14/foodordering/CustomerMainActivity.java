package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.RestaurantPreferenceHelper;

/**
 * Customer Main Activity
 * Main screen for customers with bottom navigation bar
 */
public class CustomerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Button btnTestDataPublic;
    private Button btnSelectRestaurant;
    private Button btnViewMenu;
    private Button btnOrderHistory;
    private Button btnAdminPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        setupViews();
        setupBottomNavigation();
    }

    private void setupViews() {
        btnTestDataPublic = findViewById(R.id.btnTestDataPublic);
        btnSelectRestaurant = findViewById(R.id.btnSelectRestaurant);
        btnViewMenu = findViewById(R.id.btnViewMenu);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        btnTestDataPublic.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMainActivity.this, TestDataActivity.class);
            startActivity(intent);
        });

        btnSelectRestaurant.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMainActivity.this, RestaurantSelectionActivity.class);
            startActivity(intent);
        });

        btnViewMenu.setOnClickListener(v -> {
            if (!RestaurantPreferenceHelper.hasSelectedRestaurant(this)) {
                Toast.makeText(this, "Please select a restaurant first", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CustomerMainActivity.this, RestaurantSelectionActivity.class);
                startActivity(intent);
                return;
            }
            Intent intent = new Intent(CustomerMainActivity.this, MenuActivity.class);
            startActivity(intent);
        });

        btnOrderHistory.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMainActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        btnAdminPanel.setOnClickListener(v -> {
            if (AdminSessionHelper.isAdminLoggedIn(this)) {
                Intent intent = new Intent(CustomerMainActivity.this, MainActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please login as admin first", Toast.LENGTH_SHORT).show();
            }
        });

        // Show admin panel button if admin is logged in
        updateAdminButtonVisibility();
    }

    private void updateAdminButtonVisibility() {
        if (AdminSessionHelper.isAdminLoggedIn(this)) {
            btnAdminPanel.setVisibility(View.VISIBLE);
        } else {
            btnAdminPanel.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_main) {
                // Already on main screen
                return true;
            } else if (itemId == R.id.nav_coupon) {
                // Coupon feature - placeholder for now
                Toast.makeText(this, "Coupon feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_order) {
                // Navigate to menu/order screen
                if (!RestaurantPreferenceHelper.hasSelectedRestaurant(this)) {
                    Toast.makeText(this, "Please select a restaurant first", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CustomerMainActivity.this, RestaurantSelectionActivity.class);
                    startActivity(intent);
                    return true;
                }
                Intent intent = new Intent(CustomerMainActivity.this, MenuActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_member) {
                // Navigate to member screen
                Intent intent = new Intent(CustomerMainActivity.this, MemberActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_other) {
                // Other features - placeholder for now
                Toast.makeText(this, "Other features coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            
            return false;
        });
        
        // Set main as selected by default
        bottomNavigationView.setSelectedItemId(R.id.nav_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update bottom navigation selection when returning to this activity
        bottomNavigationView.setSelectedItemId(R.id.nav_main);
        // Update admin button visibility
        updateAdminButtonVisibility();
    }
}

