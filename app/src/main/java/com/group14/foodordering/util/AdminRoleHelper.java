package com.group14.foodordering.util;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Helper class to determine admin role and restaurant access
 * ADMIN - can control all restaurants (restaurantIds is empty/null)
 * MANAGER - can control one or more restaurants (restaurantIds has >1 items)
 * STAFF - one restaurant only (restaurantIds has exactly 1 item)
 */
public class AdminRoleHelper {
    private static final String TAG = "AdminRoleHelper";
    
    // Role constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_STAFF = "STAFF";
    
    /**
     * Get admin role based on restaurantIds
     * @param context Context
     * @return Role string (ADMIN, MANAGER, or STAFF)
     */
    public static String getAdminRole(Context context) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return null;
        }
        
        List<String> restaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        
        // If restaurantIds is null or empty, admin has access to all restaurants (ADMIN)
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            return ROLE_ADMIN;
        }
        
        // If exactly 1 restaurant, it's STAFF
        if (restaurantIds.size() == 1) {
            return ROLE_STAFF;
        }
        
        // If more than 1 restaurant, it's MANAGER
        return ROLE_MANAGER;
    }
    
    /**
     * Check if admin needs to select a restaurant
     * Returns true if:
     * - ADMIN role (can select any restaurant)
     * - MANAGER role (has multiple restaurants, needs to select)
     * Returns false if:
     * - STAFF role (only one restaurant, no selection needed)
     */
    public static boolean needsRestaurantSelection(Context context) {
        String role = getAdminRole(context);
        if (role == null) {
            return false;
        }
        
        // ADMIN and MANAGER need restaurant selection
        // STAFF doesn't need selection (only one restaurant)
        return ROLE_ADMIN.equals(role) || ROLE_MANAGER.equals(role);
    }
    
    /**
     * Get list of restaurant IDs the admin can access
     * For ADMIN: returns null (all restaurants)
     * For MANAGER/STAFF: returns their assigned restaurantIds
     */
    public static List<String> getAccessibleRestaurantIds(Context context) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return null;
        }
        
        List<String> restaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        
        // If empty or null, admin can access all restaurants (return null to indicate all)
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            return null; // null means all restaurants
        }
        
        return restaurantIds;
    }
    
    /**
     * Check if admin has access to a specific restaurant
     * @param context Context
     * @param restaurantId Restaurant ID to check
     * @return true if admin has access, false otherwise
     */
    public static boolean hasAccessToRestaurant(Context context, String restaurantId) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return false;
        }
        
        List<String> accessibleIds = getAccessibleRestaurantIds(context);
        
        // If null, admin can access all restaurants
        if (accessibleIds == null) {
            return true;
        }
        
        // Check if restaurantId is in the accessible list
        return accessibleIds.contains(restaurantId);
    }
    
    /**
     * Get the single restaurant ID for STAFF role
     * Returns null if not STAFF or if no restaurant assigned
     */
    public static String getStaffRestaurantId(Context context) {
        String role = getAdminRole(context);
        if (!ROLE_STAFF.equals(role)) {
            return null;
        }
        
        List<String> restaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        if (restaurantIds != null && !restaurantIds.isEmpty()) {
            return restaurantIds.get(0);
        }
        
        return null;
    }
    
    /**
     * Get role display name
     */
    public static String getRoleDisplayName(Context context) {
        String role = getAdminRole(context);
        if (role == null) {
            return "Unknown";
        }
        
        switch (role) {
            case ROLE_ADMIN:
                return "Administrator";
            case ROLE_MANAGER:
                return "Manager";
            case ROLE_STAFF:
                return "Staff";
            default:
                return "Unknown";
        }
    }
}

