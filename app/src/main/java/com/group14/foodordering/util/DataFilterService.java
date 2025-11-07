package com.group14.foodordering.util;

import android.content.Context;
import android.util.Log;

import com.group14.foodordering.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Filter Service to filter data based on admin permissions and restaurant access
 * Ensures admins only see data they have permission to access
 */
public class DataFilterService {
    private static final String TAG = "DataFilterService";

    /**
     * Filter orders based on admin's restaurant access
     * If admin has no restaurant restrictions (empty restaurantIds), return all orders
     * Otherwise, only return orders for restaurants the admin has access to
     */
    public static List<Order> filterOrdersByRestaurantAccess(Context context, List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return orders;
        }

        // If not admin logged in, return all orders (customer view)
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return orders;
        }

        List<String> adminRestaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        
        // If admin has no restaurant restrictions (empty list means all restaurants)
        if (adminRestaurantIds == null || adminRestaurantIds.isEmpty()) {
            Log.d(TAG, "Admin has access to all restaurants, returning all orders");
            return orders;
        }

        // Filter orders by restaurant access
        List<Order> filteredOrders = new ArrayList<>();
        for (Order order : orders) {
            if (order != null && order.getRestaurantId() != null) {
                if (adminRestaurantIds.contains(order.getRestaurantId())) {
                    filteredOrders.add(order);
                } else {
                    Log.d(TAG, "Filtered out order " + order.getOrderId() + 
                        " - admin doesn't have access to restaurant " + order.getRestaurantId());
                }
            } else {
                // If order has no restaurantId, include it (might be legacy data)
                Log.w(TAG, "Order " + (order != null ? order.getOrderId() : "null") + 
                    " has no restaurantId, including it");
                if (order != null) {
                    filteredOrders.add(order);
                }
            }
        }

        Log.d(TAG, "Filtered orders: " + orders.size() + " -> " + filteredOrders.size() + 
            " (admin has access to " + adminRestaurantIds.size() + " restaurants)");
        return filteredOrders;
    }

    /**
     * Check if admin has access to a specific restaurant
     * Returns true if:
     * - Admin is not logged in (customer view)
     * - Admin has no restaurant restrictions (empty restaurantIds)
     * - Admin's restaurantIds contains the restaurantId
     */
    public static boolean hasRestaurantAccess(Context context, String restaurantId) {
        if (restaurantId == null || restaurantId.isEmpty()) {
            return true; // Allow access to orders/items without restaurantId
        }

        // If not admin logged in, allow access (customer view)
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return true;
        }

        return AdminSessionHelper.hasRestaurantAccess(context, restaurantId);
    }

    /**
     * Get list of restaurant IDs that admin has access to
     * Returns empty list if admin has access to all restaurants
     */
    public static List<String> getAccessibleRestaurantIds(Context context) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            // Customer view - return empty list (all restaurants)
            return new ArrayList<>();
        }

        List<String> restaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        if (restaurantIds == null || restaurantIds.isEmpty()) {
            // Empty list means access to all restaurants
            return new ArrayList<>();
        }

        return new ArrayList<>(restaurantIds);
    }

    /**
     * Check if admin should see all restaurants (no restrictions)
     */
    public static boolean hasAllRestaurantAccess(Context context) {
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return true; // Customer view sees all
        }

        List<String> restaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        return restaurantIds == null || restaurantIds.isEmpty();
    }

    /**
     * Filter a list of items by restaurant access
     * Generic method that filters items based on restaurantId field
     * Note: This assumes items have a getRestaurantId() method
     */
    public static <T> List<T> filterByRestaurantAccess(Context context, List<T> items, RestaurantIdExtractor<T> extractor) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        // If not admin logged in, return all items
        if (!AdminSessionHelper.isAdminLoggedIn(context)) {
            return items;
        }

        List<String> adminRestaurantIds = AdminSessionHelper.getAdminRestaurantIds(context);
        
        // If admin has no restaurant restrictions
        if (adminRestaurantIds == null || adminRestaurantIds.isEmpty()) {
            return items;
        }

        // Filter items by restaurant access
        List<T> filteredItems = new ArrayList<>();
        for (T item : items) {
            if (item != null) {
                String restaurantId = extractor.getRestaurantId(item);
                if (restaurantId == null || restaurantId.isEmpty() || 
                    adminRestaurantIds.contains(restaurantId)) {
                    filteredItems.add(item);
                }
            }
        }

        return filteredItems;
    }

    /**
     * Interface for extracting restaurantId from generic types
     */
    public interface RestaurantIdExtractor<T> {
        String getRestaurantId(T item);
    }
}

