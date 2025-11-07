package com.group14.foodordering.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.group14.foodordering.model.Admin;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to manage admin session using SharedPreferences
 * Stores admin login state, permissions, and restaurant access
 */
public class AdminSessionHelper {
    private static final String TAG = "AdminSessionHelper";
    private static final String PREF_NAME = "admin_session_prefs";
    
    // Keys for SharedPreferences
    private static final String KEY_IS_LOGGED_IN = "is_admin_logged_in";
    private static final String KEY_ADMIN_ID = "admin_id";
    private static final String KEY_ADMIN_NAME = "admin_name";
    private static final String KEY_ADMIN_EMAIL = "admin_email";
    private static final String KEY_ADMIN_PHONE = "admin_phone";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PERMISSIONS = "permissions";
    private static final String KEY_RESTAURANT_IDS = "restaurant_ids";
    private static final String KEY_SELECTED_RESTAURANT_ID = "admin_selected_restaurant_id";
    private static final String KEY_SELECTED_RESTAURANT_NAME = "admin_selected_restaurant_name";
    private static final String KEY_LOGIN_TIME = "login_time";

    /**
     * Get SharedPreferences instance
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save admin session after successful login
     */
    public static void saveAdminSession(Context context, Admin admin) {
        try {
            SharedPreferences prefs = getPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_ADMIN_ID, admin.getAdminId());
            editor.putString(KEY_ADMIN_NAME, admin.getName());
            editor.putString(KEY_ADMIN_EMAIL, admin.getEmail());
            editor.putString(KEY_ADMIN_PHONE, admin.getPhone());
            editor.putString(KEY_USER_ID, admin.getUserId());
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            
            // Save permissions list as JSON string
            List<String> permissions = admin.getPermissions();
            if (permissions != null && !permissions.isEmpty()) {
                JSONArray permissionsArray = new JSONArray();
                for (String permission : permissions) {
                    permissionsArray.put(permission);
                }
                editor.putString(KEY_PERMISSIONS, permissionsArray.toString());
            } else {
                editor.putString(KEY_PERMISSIONS, "[]");
            }
            
            // Save restaurantIds list as JSON string
            if (admin.getRestaurantIds() != null && !admin.getRestaurantIds().isEmpty()) {
                JSONArray restaurantIdsArray = new JSONArray();
                for (String restaurantId : admin.getRestaurantIds()) {
                    restaurantIdsArray.put(restaurantId);
                }
                editor.putString(KEY_RESTAURANT_IDS, restaurantIdsArray.toString());
            } else {
                editor.putString(KEY_RESTAURANT_IDS, "[]");
            }
            
            // Use commit() instead of apply() to ensure synchronous save
            // This prevents onResume() from checking login status before session is saved
            editor.commit();
            Log.d(TAG, "Admin session saved: " + admin.getAdminId());
        } catch (Exception e) {
            Log.e(TAG, "Failed to save admin session", e);
        }
    }

    /**
     * Clear admin session (logout)
     */
    public static void clearAdminSession(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().clear().apply();
            Log.d(TAG, "Admin session cleared");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear admin session", e);
        }
    }

    /**
     * Check if admin is logged in
     */
    public static boolean isAdminLoggedIn(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to check admin login status", e);
            return false;
        }
    }

    /**
     * Get logged-in admin ID
     */
    public static String getAdminId(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_ADMIN_ID, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin ID", e);
            return null;
        }
    }

    /**
     * Get logged-in admin name
     */
    public static String getAdminName(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_ADMIN_NAME, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin name", e);
            return null;
        }
    }

    /**
     * Get logged-in admin permissions
     */
    public static List<String> getAdminPermissions(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            String permissionsJson = prefs.getString(KEY_PERMISSIONS, "[]");
            if (permissionsJson != null && !permissionsJson.isEmpty() && !permissionsJson.equals("[]")) {
                JSONArray permissionsArray = new JSONArray(permissionsJson);
                List<String> permissions = new ArrayList<>();
                for (int i = 0; i < permissionsArray.length(); i++) {
                    permissions.add(permissionsArray.getString(i));
                }
                return permissions;
            }
            return new ArrayList<>();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse admin permissions JSON", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin permissions", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get logged-in admin restaurant IDs
     */
    public static List<String> getAdminRestaurantIds(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            String restaurantIdsJson = prefs.getString(KEY_RESTAURANT_IDS, "[]");
            if (restaurantIdsJson != null && !restaurantIdsJson.isEmpty() && !restaurantIdsJson.equals("[]")) {
                JSONArray restaurantIdsArray = new JSONArray(restaurantIdsJson);
                List<String> restaurantIds = new ArrayList<>();
                for (int i = 0; i < restaurantIdsArray.length(); i++) {
                    restaurantIds.add(restaurantIdsArray.getString(i));
                }
                return restaurantIds;
            }
            return new ArrayList<>();
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse restaurant IDs JSON", e);
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin restaurant IDs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get logged-in admin email
     */
    public static String getAdminEmail(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_ADMIN_EMAIL, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin email", e);
            return null;
        }
    }

    /**
     * Get logged-in admin phone
     */
    public static String getAdminPhone(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_ADMIN_PHONE, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin phone", e);
            return null;
        }
    }

    /**
     * Get login time
     */
    public static long getLoginTime(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getLong(KEY_LOGIN_TIME, 0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get login time", e);
            return 0;
        }
    }

    /**
     * Check if admin has access to a specific restaurant
     * Returns true if restaurantIds is empty (all restaurants) or contains the restaurantId
     */
    public static boolean hasRestaurantAccess(Context context, String restaurantId) {
        List<String> restaurantIds = getAdminRestaurantIds(context);
        // If restaurantIds is empty or null, admin has access to all restaurants
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            return true;
        }
        // Otherwise, check if restaurantId is in the list
        return restaurantIds.contains(restaurantId);
    }

    /**
     * Save admin's selected restaurant ID (for operations)
     */
    public static void setAdminSelectedRestaurantId(Context context, String restaurantId) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putString(KEY_SELECTED_RESTAURANT_ID, restaurantId).apply();
            Log.d(TAG, "Saved admin selected restaurant ID: " + restaurantId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save admin selected restaurant ID", e);
        }
    }
    
    /**
     * Get admin's selected restaurant ID
     */
    public static String getAdminSelectedRestaurantId(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_SELECTED_RESTAURANT_ID, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin selected restaurant ID", e);
            return null;
        }
    }
    
    /**
     * Save admin's selected restaurant name
     */
    public static void setAdminSelectedRestaurantName(Context context, String restaurantName) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putString(KEY_SELECTED_RESTAURANT_NAME, restaurantName).apply();
            Log.d(TAG, "Saved admin selected restaurant name: " + restaurantName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save admin selected restaurant name", e);
        }
    }
    
    /**
     * Get admin's selected restaurant name
     */
    public static String getAdminSelectedRestaurantName(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_SELECTED_RESTAURANT_NAME, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get admin selected restaurant name", e);
            return null;
        }
    }
    
    /**
     * Clear admin's selected restaurant
     */
    public static void clearAdminSelectedRestaurant(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit()
                    .remove(KEY_SELECTED_RESTAURANT_ID)
                    .remove(KEY_SELECTED_RESTAURANT_NAME)
                    .apply();
            Log.d(TAG, "Cleared admin selected restaurant");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear admin selected restaurant", e);
        }
    }
    
    /**
     * Reconstruct Admin object from SharedPreferences (for convenience)
     * Note: This doesn't include all fields, only those stored in preferences
     */
    public static Admin getAdminFromSession(Context context) {
        if (!isAdminLoggedIn(context)) {
            return null;
        }
        
        Admin admin = new Admin();
        admin.setAdminId(getAdminId(context));
        admin.setName(getAdminName(context));
        admin.setEmail(getAdminEmail(context));
        admin.setPhone(getAdminPhone(context));
        admin.setUserId(getPrefs(context).getString(KEY_USER_ID, null));
        admin.setPermissions(getAdminPermissions(context));
        
        List<String> restaurantIds = getAdminRestaurantIds(context);
        admin.setRestaurantIds(restaurantIds);
        admin.setActive(true);
        
        return admin;
    }
}

