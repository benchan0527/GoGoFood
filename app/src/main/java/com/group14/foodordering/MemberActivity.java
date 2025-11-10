package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.User;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.CustomerSessionHelper;
import com.group14.foodordering.util.DeviceIdHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Member Activity
 * Shows customer member information including points and order history
 * If user is not logged in, shows login screen
 * If user is admin/staff/manager, redirects to MainActivity
 */
public class MemberActivity extends AppCompatActivity {

    private static final String TAG = "MemberActivity";
    private FirebaseDatabaseService dbService;
    private TextView pointsTextView;
    private TextView memberIdTextView;
    private RecyclerView orderHistoryRecyclerView;
    private BottomNavigationView bottomNavigationView;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<Order> orderHistory;
    private String deviceId;
    private AlertDialog loginDialog;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dbService = FirebaseDatabaseService.getInstance();
        orderHistory = new ArrayList<>();
        deviceId = DeviceIdHelper.getDeviceId(this);

        // Check if admin/staff/manager is logged in
        if (AdminSessionHelper.isAdminLoggedIn(this)) {
            // Redirect to admin main page
            Intent intent = new Intent(MemberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Check if customer is logged in
        if (!CustomerSessionHelper.isCustomerLoggedIn(this)) {
            // Navigate to full-screen login
            Intent intent = new Intent(MemberActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Customer is logged in, show member page
        setContentView(R.layout.activity_member);
        setupViews();
        setupBottomNavigation();
        loadMemberInfo();
        loadOrderHistory();
    }

    private void setupViews() {
        pointsTextView = findViewById(R.id.pointsTextView);
        memberIdTextView = findViewById(R.id.memberIdTextView);
        orderHistoryRecyclerView = findViewById(R.id.orderHistoryRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        logoutButton = findViewById(R.id.logoutButton);

        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryAdapter = new OrderHistoryAdapter(orderHistory);
        orderHistoryRecyclerView.setAdapter(orderHistoryAdapter);

        // Display device ID as member ID
        memberIdTextView.setText("Member ID: " + deviceId);

        // Setup logout button
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Show confirmation dialog
                new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Clear customer session
                        CustomerSessionHelper.clearCustomerSession(this);
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        
                        // Redirect to customer main activity
                        Intent intent = new Intent(MemberActivity.this, CustomerMainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            return;
        }
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_main) {
                // Navigate to customer main screen
                Intent intent = new Intent(MemberActivity.this, CustomerMainActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_coupon) {
                // Coupon feature - placeholder for now
                Toast.makeText(this, "Coupon feature coming soon", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.nav_order) {
                // Navigate to menu/order screen
                Intent intent = new Intent(MemberActivity.this, MenuActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_member) {
                // Already on member screen
                return true;
            } else if (itemId == R.id.nav_other) {
                // Other features - placeholder for now
                Toast.makeText(this, "Other features coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            
            return false;
        });
        
        // Set member as selected by default
        bottomNavigationView.setSelectedItemId(R.id.nav_member);
    }

    // Login UI moved to LoginActivity

    /**
     * Login customer using email or phone
     * Also supports admin login - if user not found, tries admin lookup
     */
    private void loginCustomer(String emailOrPhone, String password) {
        dbService.getUserByEmailOrPhone(emailOrPhone, new FirebaseDatabaseService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    // User not found in users collection, try admin collection
                    tryAdminLogin(emailOrPhone, password);
                    return;
                }
                
                // Check if user is admin/staff/manager
                String role = user.getRole();
                if ("manager".equals(role) || "server".equals(role) || "kitchen".equals(role)) {
                    // For admin/staff, they should use admin login
                    Toast.makeText(MemberActivity.this, 
                        "Please use Admin Login for staff accounts", Toast.LENGTH_SHORT).show();
                    // Redirect to MainActivity for admin login
                    Intent intent = new Intent(MemberActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                
                // Save customer session (use commit to ensure synchronous save)
                CustomerSessionHelper.saveCustomerSession(MemberActivity.this, user);
                
                // Show member page
                findViewById(R.id.memberInfoCard).setVisibility(View.VISIBLE);
                findViewById(R.id.orderHistoryLabel).setVisibility(View.VISIBLE);
                orderHistoryRecyclerView.setVisibility(View.VISIBLE);
                
                // Load member info
                loadMemberInfo();
                loadOrderHistory();
                
                Toast.makeText(MemberActivity.this, 
                    "Login successful: " + user.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                // User not found, try admin login
                tryAdminLogin(emailOrPhone, password);
            }
        });
    }

    /**
     * Try to login as admin if user login failed
     */
    private void tryAdminLogin(String emailOrPhone, String password) {
        dbService.getAdminByStaffIdOrPhone(emailOrPhone, new FirebaseDatabaseService.AdminCallback() {
            @Override
            public void onSuccess(com.group14.foodordering.model.Admin admin) {
                if (admin == null) {
                    Toast.makeText(MemberActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Admin found, save session and redirect to MainActivity
                AdminSessionHelper.saveAdminSession(MemberActivity.this, admin);
                
                // Redirect to MainActivity for admin
                Intent intent = new Intent(MemberActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                
                Toast.makeText(MemberActivity.this, 
                    "Admin login successful: " + admin.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Admin login failed", e);
                String errorMessage = e.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "User not found";
                }
                Toast.makeText(MemberActivity.this, 
                    "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load member information (points/score)
     */
    private void loadMemberInfo() {
        // Get logged-in user ID
        String userId = CustomerSessionHelper.getUserId(this);
        if (userId == null) {
            pointsTextView.setText("0 Points");
            memberIdTextView.setText("Not logged in");
            return;
        }
        
        // Try to get user from Firebase to get latest points
        dbService.getUserById(userId, new FirebaseDatabaseService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    int points = user.getPoints();
                    pointsTextView.setText(points + " Points");
                    
                    // Update session with latest points
                    CustomerSessionHelper.updateUserPoints(MemberActivity.this, points);
                    
                    // Display user info
                    String userName = user.getName();
                    if (userName != null && !userName.isEmpty()) {
                        memberIdTextView.setText("Member: " + userName);
                    } else {
                        memberIdTextView.setText("Member ID: " + user.getUserId());
                    }
                    
                    Log.d(TAG, "Member points loaded: " + points);
                } else {
                    // User doesn't exist, use session data
                    int points = CustomerSessionHelper.getUserPoints(MemberActivity.this);
                    pointsTextView.setText(points + " Points");
                    String userName = CustomerSessionHelper.getUserName(MemberActivity.this);
                    if (userName != null && !userName.isEmpty()) {
                        memberIdTextView.setText("Member: " + userName);
                    } else {
                        memberIdTextView.setText("Member ID: " + userId);
                    }
                    Log.d(TAG, "User not found in Firebase, using session data");
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load member info", e);
                // Use session data as fallback
                int points = CustomerSessionHelper.getUserPoints(MemberActivity.this);
                pointsTextView.setText(points + " Points");
                String userName = CustomerSessionHelper.getUserName(MemberActivity.this);
                String userId = CustomerSessionHelper.getUserId(MemberActivity.this);
                if (userName != null && !userName.isEmpty()) {
                    memberIdTextView.setText("Member: " + userName);
                } else if (userId != null) {
                    memberIdTextView.setText("Member ID: " + userId);
                } else {
                    memberIdTextView.setText("Not logged in");
                }
            }
        });
    }

    /**
     * Load order history for this customer
     */
    private void loadOrderHistory() {
        // Get logged-in user ID
        String userId = CustomerSessionHelper.getUserId(this);
        if (userId == null) {
            // Not logged in, no order history
            orderHistory.clear();
            orderHistoryAdapter.notifyDataSetChanged();
            return;
        }
        
        dbService.getFirestore()
                .collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderHistory.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Order order = document.toObject(Order.class);
                            orderHistory.add(order);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing order: " + document.getId(), e);
                        }
                    }
                    orderHistoryAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + orderHistory.size() + " orders");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load order history", e);
                    Toast.makeText(this, "Failed to load order history", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Check if admin/staff/manager is logged in
        if (AdminSessionHelper.isAdminLoggedIn(this)) {
            // Redirect to admin main page
            Intent intent = new Intent(MemberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Check if customer is logged in
        if (!CustomerSessionHelper.isCustomerLoggedIn(this)) {
            Intent intent = new Intent(MemberActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // Refresh member info and order history when returning to this activity
        loadMemberInfo();
        loadOrderHistory();
        // Update bottom navigation selection
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_member);
        }
    }

    /**
     * Simple adapter for order history
     */
    private class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {
        private List<Order> orders;

        OrderHistoryAdapter(List<Order> orders) {
            this.orders = orders;
        }

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            Order order = orders.get(position);
            holder.orderIdText.setText("Order #" + order.getOrderId());
            holder.orderInfoText.setText(String.format("$%.2f - %s", order.getTotal(), order.getStatus()));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        class OrderViewHolder extends RecyclerView.ViewHolder {
            TextView orderIdText;
            TextView orderInfoText;

            OrderViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                orderIdText = itemView.findViewById(android.R.id.text1);
                orderInfoText = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}

