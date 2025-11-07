package com.group14.foodordering.util;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Permission Manager to check admin permissions
 * Provides methods to verify if admin has specific permissions
 */
public class PermissionManager {
    private static final String TAG = "PermissionManager";
    
    // Permission constants
    public static final String PERMISSION_MENU_EDIT = "menu_edit";
    public static final String PERMISSION_MENU_VIEW = "menu_view";
    public static final String PERMISSION_ORDER_VIEW = "order_view";
    public static final String PERMISSION_ORDER_UPDATE = "order_update";
    public static final String PERMISSION_ORDER_MANAGE = "order_manage";
    public static final String PERMISSION_REPORT_VIEW = "report_view";
    public static final String PERMISSION_INVENTORY_MANAGE = "inventory_manage";
    public static final String PERMISSION_TABLE_MANAGE = "table_manage";
    public static final String PERMISSION_USER_MANAGE = "user_manage";
    public static final String PERMISSION_ADMIN_MANAGE = "admin_manage";

    /**
     * Check if admin has a specific permission
     */
    public static boolean hasPermission(Context context, String permission) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            Log.d(TAG, "Admin not logged in");
            return false;
        }
        
        List<String> permissions = AdminSessionHelper.getAdminPermissions(context);
        if (permissions == null || permissions.isEmpty()) {
            Log.d(TAG, "No permissions found for admin");
            return false;
        }
        
        boolean hasPermission = permissions.contains(permission);
        Log.d(TAG, "Permission check - " + permission + ": " + hasPermission);
        return hasPermission;
    }

    /**
     * Check if admin has any of the specified permissions
     */
    public static boolean hasAnyPermission(Context context, String... permissions) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return false;
        }
        
        List<String> adminPermissions = AdminSessionHelper.getAdminPermissions(context);
        if (adminPermissions == null || adminPermissions.isEmpty()) {
            return false;
        }
        
        for (String permission : permissions) {
            if (adminPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if admin has all of the specified permissions
     */
    public static boolean hasAllPermissions(Context context, String... permissions) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return false;
        }
        
        List<String> adminPermissions = AdminSessionHelper.getAdminPermissions(context);
        if (adminPermissions == null || adminPermissions.isEmpty()) {
            return false;
        }
        
        for (String permission : permissions) {
            if (!adminPermissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if admin can view orders
     */
    public static boolean canViewOrders(Context context) {
        return hasAnyPermission(context, 
            PERMISSION_ORDER_VIEW, 
            PERMISSION_ORDER_UPDATE, 
            PERMISSION_ORDER_MANAGE);
    }

    /**
     * Check if admin can update orders
     */
    public static boolean canUpdateOrders(Context context) {
        return hasAnyPermission(context, 
            PERMISSION_ORDER_UPDATE, 
            PERMISSION_ORDER_MANAGE);
    }

    /**
     * Check if admin can manage orders (full control)
     */
    public static boolean canManageOrders(Context context) {
        return hasPermission(context, PERMISSION_ORDER_MANAGE);
    }

    /**
     * Check if admin can edit menu
     */
    public static boolean canEditMenu(Context context) {
        return hasPermission(context, PERMISSION_MENU_EDIT);
    }

    /**
     * Check if admin can view menu
     */
    public static boolean canViewMenu(Context context) {
        return hasAnyPermission(context, 
            PERMISSION_MENU_VIEW, 
            PERMISSION_MENU_EDIT);
    }

    /**
     * Check if admin can view reports
     */
    public static boolean canViewReports(Context context) {
        return hasPermission(context, PERMISSION_REPORT_VIEW);
    }

    /**
     * Check if admin can manage inventory
     */
    public static boolean canManageInventory(Context context) {
        return hasPermission(context, PERMISSION_INVENTORY_MANAGE);
    }

    /**
     * Check if admin can manage tables
     */
    public static boolean canManageTables(Context context) {
        return hasPermission(context, PERMISSION_TABLE_MANAGE);
    }

    /**
     * Check if admin can manage users
     */
    public static boolean canManageUsers(Context context) {
        return hasPermission(context, PERMISSION_USER_MANAGE);
    }

    /**
     * Check if admin can manage other admins
     */
    public static boolean canManageAdmins(Context context) {
        return hasPermission(context, PERMISSION_ADMIN_MANAGE);
    }

    /**
     * Get all permissions as a formatted string (for display)
     */
    public static String getPermissionsString(Context context) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return "No permissions";
        }
        
        List<String> permissions = AdminSessionHelper.getAdminPermissions(context);
        if (permissions == null || permissions.isEmpty()) {
            return "No permissions";
        }
        
        return String.join(", ", permissions);
    }
}

