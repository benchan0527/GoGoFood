package com.group14.foodordering.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.group14.foodordering.model.User;

/**
 * Helper class to manage customer session using SharedPreferences
 * Stores customer login state and user information
 */
public class CustomerSessionHelper {
    private static final String TAG = "CustomerSessionHelper";
    private static final String PREF_NAME = "customer_session_prefs";
    
    // Keys for SharedPreferences
    private static final String KEY_IS_LOGGED_IN = "is_customer_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_POINTS = "user_points";
    private static final String KEY_LOGIN_TIME = "login_time";

    /**
     * Get SharedPreferences instance
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save customer session after successful login
     */
    public static void saveCustomerSession(Context context, User user) {
        try {
            SharedPreferences prefs = getPrefs(context);
            SharedPreferences.Editor editor = prefs.edit();
            
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putString(KEY_USER_ID, user.getUserId());
            editor.putString(KEY_USER_EMAIL, user.getEmail());
            editor.putString(KEY_USER_NAME, user.getName());
            editor.putString(KEY_USER_PHONE, user.getPhone());
            editor.putString(KEY_USER_ROLE, user.getRole());
            editor.putInt(KEY_USER_POINTS, user.getPoints());
            editor.putLong(KEY_LOGIN_TIME, System.currentTimeMillis());
            
            // Use commit() instead of apply() to ensure synchronous save
            // This prevents onResume() from checking login status before session is saved
            editor.commit();
            Log.d(TAG, "Customer session saved: " + user.getUserId());
        } catch (Exception e) {
            Log.e(TAG, "Failed to save customer session", e);
        }
    }

    /**
     * Clear customer session (logout)
     */
    public static void clearCustomerSession(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().clear().apply();
            Log.d(TAG, "Customer session cleared");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear customer session", e);
        }
    }

    /**
     * Check if customer is logged in
     */
    public static boolean isCustomerLoggedIn(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        } catch (Exception e) {
            Log.e(TAG, "Failed to check customer login status", e);
            return false;
        }
    }

    /**
     * Get logged-in user ID
     */
    public static String getUserId(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_USER_ID, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get user ID", e);
            return null;
        }
    }

    /**
     * Get logged-in user name
     */
    public static String getUserName(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_USER_NAME, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get user name", e);
            return null;
        }
    }

    /**
     * Get logged-in user email
     */
    public static String getUserEmail(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_USER_EMAIL, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get user email", e);
            return null;
        }
    }

    /**
     * Get logged-in user phone
     */
    public static String getUserPhone(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_USER_PHONE, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get user phone", e);
            return null;
        }
    }

    /**
     * Get logged-in user role
     */
    public static String getUserRole(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getString(KEY_USER_ROLE, null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get user role", e);
            return null;
        }
    }

    /**
     * Get logged-in user points
     */
    public static int getUserPoints(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            return prefs.getInt(KEY_USER_POINTS, 0);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get user points", e);
            return 0;
        }
    }

    /**
     * Update user points in session
     */
    public static void updateUserPoints(Context context, int points) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putInt(KEY_USER_POINTS, points).apply();
            Log.d(TAG, "User points updated: " + points);
        } catch (Exception e) {
            Log.e(TAG, "Failed to update user points", e);
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
     * Check if logged-in user is a customer
     */
    public static boolean isCustomer(Context context) {
        String role = getUserRole(context);
        return "customer".equals(role);
    }

    /**
     * Check if logged-in user is admin/staff/manager
     */
    public static boolean isAdminOrStaff(Context context) {
        String role = getUserRole(context);
        return "manager".equals(role) || "server".equals(role) || "kitchen".equals(role);
    }

    /**
     * Reconstruct User object from SharedPreferences (for convenience)
     */
    public static User getUserFromSession(Context context) {
        if (!isCustomerLoggedIn(context)) {
            return null;
        }
        
        User user = new User();
        user.setUserId(getUserId(context));
        user.setName(getUserName(context));
        user.setEmail(getUserEmail(context));
        user.setPhone(getUserPhone(context));
        user.setRole(getUserRole(context));
        user.setPoints(getUserPoints(context));
        
        return user;
    }
}

