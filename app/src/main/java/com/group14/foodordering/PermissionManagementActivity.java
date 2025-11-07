package com.group14.foodordering;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.AdminRoleHelper;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.PermissionManager;

import java.util.ArrayList;
import java.util.List;

public class PermissionManagementActivity extends AppCompatActivity {
    private static final String TAG = "PermissionManagement";
    
    private FirebaseDatabaseService dbService;
    private RecyclerView recyclerViewAdmins;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private AdminAdapter adminAdapter;
    private List<Admin> allAdmins = new ArrayList<>();
    private List<Admin> filteredAdmins = new ArrayList<>();
    private List<Restaurant> allRestaurants = new ArrayList<>();
    private String currentUserRole;
    private List<String> currentUserRestaurantIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_management);
        
        dbService = FirebaseDatabaseService.getInstance();
        
        // Get current user role and restaurant IDs
        currentUserRole = AdminRoleHelper.getAdminRole(this);
        currentUserRestaurantIds = AdminSessionHelper.getAdminRestaurantIds(this);
        
        if (currentUserRole == null) {
            Toast.makeText(this, "Admin not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        recyclerViewAdmins = findViewById(R.id.recyclerViewAdmins);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        
        // Setup RecyclerView
        adminAdapter = new AdminAdapter();
        recyclerViewAdmins.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAdmins.setAdapter(adminAdapter);
        
        // Load data
        loadRestaurants();
        loadAdmins();
    }
    
    private void loadRestaurants() {
        dbService.getAllRestaurants(new FirebaseDatabaseService.RestaurantsCallback() {
            @Override
            public void onSuccess(List<Restaurant> restaurants) {
                allRestaurants = restaurants;
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load restaurants", e);
            }
        });
    }
    
    private void loadAdmins() {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        
        dbService.getAllAdmins(new FirebaseDatabaseService.AdminsCallback() {
            @Override
            public void onSuccess(List<Admin> admins) {
                allAdmins = admins;
                filterAdmins();
                progressBar.setVisibility(View.GONE);
                
                if (filteredAdmins.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                }
                
                adminAdapter.notifyDataSetChanged();
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load admins", e);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PermissionManagementActivity.this, 
                    "Failed to load admins: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Filter admins based on current user's role
     * ADMIN can control MANAGER and STAFF
     * MANAGER can control STAFF only
     */
    private void filterAdmins() {
        filteredAdmins.clear();
        String currentAdminId = AdminSessionHelper.getAdminId(this);
        
        for (Admin admin : allAdmins) {
            // Skip current user
            if (admin.getAdminId().equals(currentAdminId)) {
                continue;
            }
            
            // Determine target admin's role
            String targetRole = getAdminRole(admin);
            
            if (AdminRoleHelper.ROLE_ADMIN.equals(currentUserRole)) {
                // ADMIN can control MANAGER and STAFF
                if (AdminRoleHelper.ROLE_MANAGER.equals(targetRole) || 
                    AdminRoleHelper.ROLE_STAFF.equals(targetRole)) {
                    filteredAdmins.add(admin);
                }
            } else if (AdminRoleHelper.ROLE_MANAGER.equals(currentUserRole)) {
                // MANAGER can control STAFF only
                if (AdminRoleHelper.ROLE_STAFF.equals(targetRole)) {
                    // Also check if the STAFF belongs to one of the MANAGER's restaurants
                    List<String> targetRestaurantIds = admin.getRestaurantIds();
                    if (targetRestaurantIds != null && !targetRestaurantIds.isEmpty()) {
                        // Check if any of the target's restaurants are in the manager's restaurants
                        boolean hasAccess = false;
                        for (String targetRestaurantId : targetRestaurantIds) {
                            if (currentUserRestaurantIds != null && 
                                currentUserRestaurantIds.contains(targetRestaurantId)) {
                                hasAccess = true;
                                break;
                            }
                        }
                        if (hasAccess) {
                            filteredAdmins.add(admin);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Determine admin role based on restaurantIds
     */
    private String getAdminRole(Admin admin) {
        List<String> restaurantIds = admin.getRestaurantIds();
        
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            return AdminRoleHelper.ROLE_ADMIN;
        }
        
        if (restaurantIds.size() == 1) {
            return AdminRoleHelper.ROLE_STAFF;
        }
        
        return AdminRoleHelper.ROLE_MANAGER;
    }
    
    /**
     * Get restaurant name by ID
     */
    private String getRestaurantName(String restaurantId) {
        for (Restaurant restaurant : allRestaurants) {
            if (restaurant.getRestaurantId().equals(restaurantId)) {
                return restaurant.getRestaurantName();
            }
        }
        return restaurantId;
    }
    
    private void showEditPermissionsDialog(Admin admin) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Permissions: " + admin.getName());
        
        // Create layout for dialog
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(32, 16, 32, 16);
        
        // Get all available permissions
        String[] allPermissions = {
            PermissionManager.PERMISSION_MENU_EDIT,
            PermissionManager.PERMISSION_MENU_VIEW,
            PermissionManager.PERMISSION_ORDER_VIEW,
            PermissionManager.PERMISSION_ORDER_UPDATE,
            PermissionManager.PERMISSION_ORDER_MANAGE,
            PermissionManager.PERMISSION_REPORT_VIEW,
            PermissionManager.PERMISSION_INVENTORY_MANAGE,
            PermissionManager.PERMISSION_TABLE_MANAGE,
            PermissionManager.PERMISSION_USER_MANAGE,
            PermissionManager.PERMISSION_ADMIN_MANAGE
        };
        
        List<String> currentPermissions = admin.getPermissions();
        List<CheckBox> checkBoxes = new ArrayList<>();
        
        // Create checkboxes for each permission
        for (String permission : allPermissions) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(permission);
            checkBox.setChecked(currentPermissions != null && currentPermissions.contains(permission));
            checkBoxes.add(checkBox);
            dialogLayout.addView(checkBox);
        }
        
        builder.setView(dialogLayout);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Collect selected permissions
            List<String> selectedPermissions = new ArrayList<>();
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isChecked()) {
                    selectedPermissions.add(checkBox.getText().toString());
                }
            }
            
            // Update admin permissions
            admin.setPermissions(selectedPermissions);
            saveAdmin(admin);
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.show();
    }
    
    private void saveAdmin(Admin admin) {
        dbService.createOrUpdateAdmin(admin, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                Toast.makeText(PermissionManagementActivity.this, 
                    "Permissions updated successfully", 
                    Toast.LENGTH_SHORT).show();
                loadAdmins(); // Reload to refresh the list
            }
            
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update admin", e);
                Toast.makeText(PermissionManagementActivity.this, 
                    "Failed to update permissions: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // RecyclerView Adapter
    private class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {
        
        @NonNull
        @Override
        public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_permission, parent, false);
            return new AdminViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
            Admin admin = filteredAdmins.get(position);
            holder.bind(admin);
        }
        
        @Override
        public int getItemCount() {
            return filteredAdmins.size();
        }
        
        class AdminViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewAdminName;
            private TextView textViewAdminId;
            private TextView textViewRole;
            private TextView textViewEmail;
            private TextView textViewRestaurants;
            private LinearLayout layoutPermissions;
            private Button btnEditPermissions;
            
            AdminViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewAdminName = itemView.findViewById(R.id.textViewAdminName);
                textViewAdminId = itemView.findViewById(R.id.textViewAdminId);
                textViewRole = itemView.findViewById(R.id.textViewRole);
                textViewEmail = itemView.findViewById(R.id.textViewEmail);
                textViewRestaurants = itemView.findViewById(R.id.textViewRestaurants);
                layoutPermissions = itemView.findViewById(R.id.layoutPermissions);
                btnEditPermissions = itemView.findViewById(R.id.btnEditPermissions);
            }
            
            void bind(Admin admin) {
                textViewAdminName.setText(admin.getName());
                textViewAdminId.setText(admin.getAdminId());
                textViewEmail.setText(admin.getEmail());
                
                // Set role
                String role = getAdminRole(admin);
                textViewRole.setText("Role: " + role);
                
                // Set restaurants
                List<String> restaurantIds = admin.getRestaurantIds();
                if (restaurantIds != null && !restaurantIds.isEmpty()) {
                    StringBuilder restaurantsText = new StringBuilder();
                    for (int i = 0; i < restaurantIds.size(); i++) {
                        if (i > 0) restaurantsText.append(", ");
                        restaurantsText.append(getRestaurantName(restaurantIds.get(i)));
                    }
                    textViewRestaurants.setText(restaurantsText.toString());
                } else {
                    textViewRestaurants.setText("All Restaurants");
                }
                
                // Set permissions
                layoutPermissions.removeAllViews();
                List<String> permissions = admin.getPermissions();
                if (permissions != null && !permissions.isEmpty()) {
                    for (String permission : permissions) {
                        TextView permissionView = new TextView(itemView.getContext());
                        permissionView.setText("• " + permission);
                        permissionView.setTextSize(12);
                        permissionView.setTextColor(0xFF666666);
                        permissionView.setPadding(0, 4, 0, 4);
                        layoutPermissions.addView(permissionView);
                    }
                } else {
                    TextView noPermissionsView = new TextView(itemView.getContext());
                    noPermissionsView.setText("No permissions assigned");
                    noPermissionsView.setTextSize(12);
                    noPermissionsView.setTextColor(0xFF999999);
                    noPermissionsView.setPadding(0, 4, 0, 4);
                    layoutPermissions.addView(noPermissionsView);
                }
                
                // Edit button
                btnEditPermissions.setOnClickListener(v -> showEditPermissionsDialog(admin));
            }
        }
    }
}

