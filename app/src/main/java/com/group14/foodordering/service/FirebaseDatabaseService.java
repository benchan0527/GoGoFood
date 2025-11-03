package com.group14.foodordering.service;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Firebase database service class
 * Provides CRUD operations for users, admins, menu items and orders
 */
public class FirebaseDatabaseService {
    private static final String TAG = "FirebaseDatabaseService";
    private FirebaseFirestore db;
    
    // Collection names
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ADMINS = "admins";
    private static final String COLLECTION_MENU_ITEMS = "menuItems";
    private static final String COLLECTION_ORDERS = "orders";

    private static FirebaseDatabaseService instance;

    private FirebaseDatabaseService() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseDatabaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseService();
        }
        return instance;
    }

    // ==================== User Operations ====================

    /**
     * Create or update user
     */
    public void createOrUpdateUser(User user, DatabaseCallback callback) {
        Map<String, Object> userMap = user.toMap();
        userMap.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_USERS)
                .document(user.getUserId())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created/updated successfully: " + user.getUserId());
                    if (callback != null) callback.onSuccess(user.getUserId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "User creation/update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Get user by ID
     */
    public void getUserById(String userId, UserCallback callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            User user = document.toObject(User.class);
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("User not found"));
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get user by email
     */
    public void getUserByEmail(String email, UserCallback callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            User user = document.toObject(User.class);
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("User not found"));
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    // ==================== Admin Operations ====================

    /**
     * Create or update admin
     */
    public void createOrUpdateAdmin(Admin admin, DatabaseCallback callback) {
        Map<String, Object> adminMap = admin.toMap();
        adminMap.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_ADMINS)
                .document(admin.getAdminId())
                .set(adminMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Admin created/updated successfully: " + admin.getAdminId());
                    if (callback != null) callback.onSuccess(admin.getAdminId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Admin creation/update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Get admin by ID
     */
    public void getAdminById(String adminId, AdminCallback callback) {
        db.collection(COLLECTION_ADMINS)
                .document(adminId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Admin admin = document.toObject(Admin.class);
                            if (callback != null) callback.onSuccess(admin);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("Admin not found"));
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get admin by adminId (staff ID) or phone for login
     */
    public void getAdminByStaffIdOrPhone(String staffIdOrPhone, AdminCallback callback) {
        // Try by adminId first
        db.collection(COLLECTION_ADMINS)
                .document(staffIdOrPhone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Admin admin = task.getResult().toObject(Admin.class);
                        if (admin != null && admin.isActive()) {
                            if (callback != null) callback.onSuccess(admin);
                            return;
                        }
                    }
                    // If not found by adminId, try by phone
                    db.collection(COLLECTION_ADMINS)
                            .whereEqualTo("phone", staffIdOrPhone)
                            .whereEqualTo("isActive", true)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(phoneTask -> {
                                if (phoneTask.isSuccessful()) {
                                    QuerySnapshot querySnapshot = phoneTask.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        Admin admin = querySnapshot.getDocuments().get(0).toObject(Admin.class);
                                        if (callback != null) callback.onSuccess(admin);
                                    } else {
                                        if (callback != null) callback.onFailure(new Exception("Admin not found"));
                                    }
                                } else {
                                    if (callback != null) callback.onFailure(phoneTask.getException());
                                }
                            });
                });
    }

    // ==================== MenuItem Operations ====================

    /**
     * Create or update menu item
     */
    public void createOrUpdateMenuItem(MenuItem menuItem, DatabaseCallback callback) {
        Map<String, Object> itemMap = menuItem.toMap();
        itemMap.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_MENU_ITEMS)
                .document(menuItem.getItemId())
                .set(itemMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Menu item created/updated successfully: " + menuItem.getItemId());
                    if (callback != null) callback.onSuccess(menuItem.getItemId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Menu item creation/update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Get all menu items
     */
    public void getAllMenuItems(MenuItemsCallback callback) {
        // Get all items without filters to avoid index requirements, then filter/sort in memory
        db.collection(COLLECTION_MENU_ITEMS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MenuItem> menuItems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuItem item = document.toObject(MenuItem.class);
                            if (item != null && item.isAvailable()) {
                                menuItems.add(item);
                            }
                        }
                        // Sort by name in memory
                        menuItems.sort((a, b) -> {
                            String nameA = a.getName() != null ? a.getName() : "";
                            String nameB = b.getName() != null ? b.getName() : "";
                            return nameA.compareToIgnoreCase(nameB);
                        });
                        if (callback != null) callback.onSuccess(menuItems);
                    } else {
                        Log.e(TAG, "Failed to get menu items", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get menu item by ID
     */
    public void getMenuItemById(String itemId, MenuItemCallback callback) {
        db.collection(COLLECTION_MENU_ITEMS)
                .document(itemId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            MenuItem item = document.toObject(MenuItem.class);
                            if (callback != null) callback.onSuccess(item);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("Menu item not found"));
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get menu items by category
     */
    public void getMenuItemsByCategory(String category, MenuItemsCallback callback) {
        db.collection(COLLECTION_MENU_ITEMS)
                .whereEqualTo("category", category)
                .whereEqualTo("isAvailable", true)
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<MenuItem> menuItems = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MenuItem item = document.toObject(MenuItem.class);
                            menuItems.add(item);
                        }
                        if (callback != null) callback.onSuccess(menuItems);
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Delete menu item (soft delete: set as unavailable)
     */
    public void deleteMenuItem(String itemId, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isAvailable", false);
        updates.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_MENU_ITEMS)
                .document(itemId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Menu item deleted successfully: " + itemId);
                    if (callback != null) callback.onSuccess(itemId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Menu item deletion failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    // ==================== Order Operations ====================

    /**
     * Create order
     */
    public void createOrder(Order order, DatabaseCallback callback) {
        Map<String, Object> orderMap = order.toMap();
        orderMap.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_ORDERS)
                .document(order.getOrderId())
                .set(orderMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order created successfully: " + order.getOrderId());
                    if (callback != null) callback.onSuccess(order.getOrderId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Order creation failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Update order
     */
    public void updateOrder(Order order, DatabaseCallback callback) {
        Map<String, Object> orderMap = order.toMap();
        orderMap.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_ORDERS)
                .document(order.getOrderId())
                .update(orderMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order updated successfully: " + order.getOrderId());
                    if (callback != null) callback.onSuccess(order.getOrderId());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Order update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Get order by ID
     */
    public void getOrderById(String orderId, OrderCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Order order = document.toObject(Order.class);
                            if (callback != null) callback.onSuccess(order);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("Order not found"));
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get all pending orders (for kitchen view)
     */
    public void getPendingOrders(OrdersCallback callback) {
        List<String> statusList = new ArrayList<>();
        statusList.add("pending");
        statusList.add("preparing");
        
        db.collection(COLLECTION_ORDERS)
                .whereIn("status", statusList)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        Log.e(TAG, "Failed to get pending orders", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get all orders by user ID
     */
    public void getOrdersByUserId(String userId, OrdersCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        }
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get all orders by table number
     */
    public void getOrdersByTableNumber(String tableNumber, OrdersCallback callback) {
        db.collection(COLLECTION_ORDERS)
                .whereEqualTo("tableNumber", tableNumber)
                .whereEqualTo("orderType", "table")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Order order = document.toObject(Order.class);
                            orders.add(order);
                        }
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Update order status
     */
    public void updateOrderStatus(String orderId, String status, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());
        
        db.collection(COLLECTION_ORDERS)
                .document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order status updated successfully: " + orderId + " -> " + status);
                    if (callback != null) callback.onSuccess(orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Order status update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    // ==================== Callback Interfaces ====================

    public interface DatabaseCallback {
        void onSuccess(String documentId);
        void onFailure(Exception e);
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public interface AdminCallback {
        void onSuccess(Admin admin);
        void onFailure(Exception e);
    }

    public interface MenuItemCallback {
        void onSuccess(MenuItem item);
        void onFailure(Exception e);
    }

    public interface MenuItemsCallback {
        void onSuccess(List<MenuItem> items);
        void onFailure(Exception e);
    }

    public interface OrderCallback {
        void onSuccess(Order order);
        void onFailure(Exception e);
    }

    public interface OrdersCallback {
        void onSuccess(List<Order> orders);
        void onFailure(Exception e);
    }
}

