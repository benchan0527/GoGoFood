package com.group14.foodordering.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Helper class to manage restaurant selection in SharedPreferences
 */
public class RestaurantPreferenceHelper {
    private static final String TAG = "RestaurantPreferenceHelper";
    private static final String PREF_NAME = "restaurant_prefs";
    private static final String KEY_SELECTED_RESTAURANT_ID = "selected_restaurant_id";
    private static final String KEY_SELECTED_RESTAURANT_NAME = "selected_restaurant_name";

    /**
     * Get SharedPreferences instance
     */
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save selected restaurant ID
     */
    public static void setSelectedRestaurantId(Context context, String restaurantId) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putString(KEY_SELECTED_RESTAURANT_ID, restaurantId).apply();
            Log.d(TAG, "Saved selected restaurant ID: " + restaurantId);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save restaurant ID", e);
        }
    }

    /**
     * Get selected restaurant ID
     */
    public static String getSelectedRestaurantId(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            String restaurantId = prefs.getString(KEY_SELECTED_RESTAURANT_ID, null);
            Log.d(TAG, "Retrieved selected restaurant ID: " + restaurantId);
            return restaurantId;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get restaurant ID", e);
            return null;
        }
    }

    /**
     * Save selected restaurant name
     */
    public static void setSelectedRestaurantName(Context context, String restaurantName) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putString(KEY_SELECTED_RESTAURANT_NAME, restaurantName).apply();
            Log.d(TAG, "Saved selected restaurant name: " + restaurantName);
        } catch (Exception e) {
            Log.e(TAG, "Failed to save restaurant name", e);
        }
    }

    /**
     * Get selected restaurant name
     */
    public static String getSelectedRestaurantName(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            String restaurantName = prefs.getString(KEY_SELECTED_RESTAURANT_NAME, null);
            Log.d(TAG, "Retrieved selected restaurant name: " + restaurantName);
            return restaurantName;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get restaurant name", e);
            return null;
        }
    }

    /**
     * Clear selected restaurant
     */
    public static void clearSelectedRestaurant(Context context) {
        try {
            SharedPreferences prefs = getPrefs(context);
            prefs.edit()
                    .remove(KEY_SELECTED_RESTAURANT_ID)
                    .remove(KEY_SELECTED_RESTAURANT_NAME)
                    .apply();
            Log.d(TAG, "Cleared selected restaurant");
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear restaurant", e);
        }
    }

    /**
     * Check if a restaurant is selected
     */
    public static boolean hasSelectedRestaurant(Context context) {
        String restaurantId = getSelectedRestaurantId(context);
        return restaurantId != null && !restaurantId.isEmpty();
    }
}

