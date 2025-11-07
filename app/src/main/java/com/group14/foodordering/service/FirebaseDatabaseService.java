package com.group14.foodordering.service;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.model.Table;
import com.group14.foodordering.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Firebase database service class
 * Provides CRUD operations for users, admins, menu items and orders
 */
public class FirebaseDatabaseService {
    private static final String TAG = "FirebaseDatabaseService";
    private final FirebaseFirestore db;
    
    // Collection names
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ADMINS = "admins";
    private static final String COLLECTION_MENU_ITEMS = "menuItems";
    private static final String COLLECTION_ORDERS = "orders";
    private static final String COLLECTION_RESTAURANTS = "restaurants";
    private static final String COLLECTION_TABLES = "tables";
    private static final String COLLECTION_COUNTERS = "counters";
    private static final String COUNTER_DOC_ID = "orderCounter";

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

    /**
     * Get Firestore instance
     */
    public FirebaseFirestore getFirestore() {
        return db;
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

    /**
     * Get user by phone number
     */
    public void getUserByPhone(String phone, UserCallback callback) {
        db.collection(COLLECTION_USERS)
                .whereEqualTo("phone", phone)
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

    /**
     * Get user by email or phone for login
     */
    public void getUserByEmailOrPhone(String emailOrPhone, UserCallback callback) {
        // Try by email first
        getUserByEmail(emailOrPhone, new UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onFailure(Exception e) {
                // If not found by email, try by phone
                getUserByPhone(emailOrPhone, callback);
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
                            Admin admin = documentToAdmin(document);
                            if (admin != null) {
                                if (callback != null) callback.onSuccess(admin);
                            } else {
                                if (callback != null) callback.onFailure(new Exception("Admin data is invalid"));
                            }
                        } else {
                            if (callback != null) callback.onFailure(new Exception("Admin not found"));
                        }
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Convert Admin document to Admin object, handling permissions conversion
     */
    private Admin documentToAdmin(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }
        
        Admin admin = new Admin();
        admin.setAdminId(document.getString("adminId"));
        admin.setUserId(document.getString("userId"));
        admin.setEmail(document.getString("email"));
        admin.setName(document.getString("name"));
        admin.setPhone(document.getString("phone"));
        Boolean isActive = document.getBoolean("isActive");
        admin.setActive(isActive != null && isActive);
        
        // Handle permissions conversion - Firestore stores as List<String>
        Object permissionsObj = document.get("permissions");
        if (permissionsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> permissionsList = (List<String>) permissionsObj;
            admin.setPermissions(permissionsList);
        } else if (permissionsObj instanceof String[]) {
            // Legacy support: convert String[] to List
            String[] permissionsArray = (String[]) permissionsObj;
            List<String> permissionsList = new ArrayList<>();
            Collections.addAll(permissionsList, permissionsArray);
            admin.setPermissions(permissionsList);
        } else {
            admin.setPermissions(new ArrayList<>());
        }
        
        // Handle restaurantIds conversion
        Object restaurantIdsObj = document.get("restaurantIds");
        if (restaurantIdsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> restaurantIdsList = (List<String>) restaurantIdsObj;
            admin.setRestaurantIds(restaurantIdsList);
        } else if (restaurantIdsObj instanceof String[]) {
            // Legacy support: convert String[] to List
            String[] restaurantIdsArray = (String[]) restaurantIdsObj;
            List<String> restaurantIdsList = new ArrayList<>();
            Collections.addAll(restaurantIdsList, restaurantIdsArray);
            admin.setRestaurantIds(restaurantIdsList);
        } else {
            admin.setRestaurantIds(new ArrayList<>());
        }
        
        Long createdAt = document.getLong("createdAt");
        if (createdAt != null) {
            admin.setCreatedAt(createdAt);
        }
        
        Long updatedAt = document.getLong("updatedAt");
        if (updatedAt != null) {
            admin.setUpdatedAt(updatedAt);
        }
        
        return admin;
    }

    /**
     * Get admin by adminId (staff ID), email, or phone for login
     */
    public void getAdminByStaffIdOrPhone(String staffIdOrPhone, AdminCallback callback) {
        // Try by adminId first
        db.collection(COLLECTION_ADMINS)
                .document(staffIdOrPhone)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        Admin admin = documentToAdmin(task.getResult());
                        if (admin != null && admin.isActive()) {
                            if (callback != null) callback.onSuccess(admin);
                            return;
                        }
                    }
                    // If not found by adminId or admin is null/inactive, try by phone
                    db.collection(COLLECTION_ADMINS)
                            .whereEqualTo("phone", staffIdOrPhone)
                            .whereEqualTo("isActive", true)
                            .limit(1)
                            .get()
                            .addOnCompleteListener(phoneTask -> {
                                if (phoneTask.isSuccessful()) {
                                    QuerySnapshot querySnapshot = phoneTask.getResult();
                                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                        Admin admin = documentToAdmin(querySnapshot.getDocuments().get(0));
                                        if (admin != null) {
                                            if (callback != null) callback.onSuccess(admin);
                                            return;
                                        }
                                    }
                                }
                                // If not found by phone, try by email
                                db.collection(COLLECTION_ADMINS)
                                        .whereEqualTo("email", staffIdOrPhone)
                                        .whereEqualTo("isActive", true)
                                        .limit(1)
                                        .get()
                                        .addOnCompleteListener(emailTask -> {
                                            if (emailTask.isSuccessful()) {
                                                QuerySnapshot emailSnapshot = emailTask.getResult();
                                                if (emailSnapshot != null && !emailSnapshot.isEmpty()) {
                                                    Admin admin = documentToAdmin(emailSnapshot.getDocuments().get(0));
                                                    if (admin != null) {
                                                        if (callback != null) callback.onSuccess(admin);
                                                    } else {
                                                        if (callback != null) callback.onFailure(new Exception("Admin data is invalid"));
                                                    }
                                                } else {
                                                    if (callback != null) callback.onFailure(new Exception("Admin not found"));
                                                }
                                            } else {
                                                if (callback != null) callback.onFailure(emailTask.getException());
                                            }
                                        });
                            });
                });
    }

    /**
     * Get all admins
     */
    public void getAllAdmins(AdminsCallback callback) {
        db.collection(COLLECTION_ADMINS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Admin> admins = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Admin admin = documentToAdmin(document);
                                if (admin != null) {
                                    admins.add(admin);
                                }
                            }
                        }
                        if (callback != null) callback.onSuccess(admins);
                    } else {
                        Log.e(TAG, "Failed to get admins", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
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
                        int totalDocs = task.getResult().size();
                        int availableCount = 0;
                        int nullCount = 0;
                        int unavailableCount = 0;
                        
                        Log.d(TAG, "Total documents retrieved: " + totalDocs);
                        
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                MenuItem item = document.toObject(MenuItem.class);
                                if (item == null) {
                                    nullCount++;
                                    Log.w(TAG, "Failed to convert document to MenuItem: " + document.getId());
                                    continue;
                                }
                                
                                // Debug: Check if isAvailable field is being read correctly
                                Boolean isAvailable = document.getBoolean("isAvailable");
                                if (isAvailable == null) {
                                    // If isAvailable field is missing, try to set it manually
                                    isAvailable = item.isAvailable();
                                    Log.d(TAG, "isAvailable field missing in document, using default: " + isAvailable + " for item: " + item.getItemId());
                                }
                                
                                // Manually set isAvailable if it wasn't read correctly
                                if (isAvailable != null && !isAvailable.equals(item.isAvailable())) {
                                    item.setAvailable(isAvailable);
                                }
                                
                                // Check if hasDrink field is being read correctly
                                Boolean hasDrink = document.getBoolean("hasDrink");
                                if (hasDrink != null && hasDrink != item.isHasDrink()) {
                                    item.setHasDrink(hasDrink);
                                }
                                
                                Log.d(TAG, "Item: " + item.getItemId() + ", Name: " + item.getName() + ", isAvailable: " + item.isAvailable() + ", hasDrink: " + item.isHasDrink());
                                
                                if (item.isAvailable()) {
                                    menuItems.add(item);
                                    availableCount++;
                                } else {
                                    unavailableCount++;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error processing document: " + document.getId(), e);
                                nullCount++;
                            }
                        }
                        
                        Log.d(TAG, "Menu items summary - Total: " + totalDocs + ", Available: " + availableCount + ", Unavailable: " + unavailableCount + ", Null: " + nullCount);
                        
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
     * Get next order number (0001-1000, cycles back to 0001 after 1000)
     */
    public void getNextOrderNumber(OrderNumberCallback callback) {
        DocumentReference counterRef = db.collection(COLLECTION_COUNTERS).document(COUNTER_DOC_ID);
        
        db.runTransaction((Transaction transaction) -> {
            DocumentSnapshot snapshot = transaction.get(counterRef);
            long currentNumber;
            
            if (snapshot.exists() && snapshot.contains("currentNumber")) {
                Long currentNumberObj = snapshot.getLong("currentNumber");
                currentNumber = currentNumberObj != null ? currentNumberObj : 0;
            } else {
                currentNumber = 0;
            }
            
            // Increment and wrap around at 1000
            currentNumber++;
            if (currentNumber > 1000) {
                currentNumber = 1;
            }
            
            // Update the counter
            Map<String, Object> counterData = new HashMap<>();
            counterData.put("currentNumber", currentNumber);
            counterData.put("updatedAt", System.currentTimeMillis());
            transaction.set(counterRef, counterData);
            
            return currentNumber;
        }).addOnSuccessListener(orderNumber -> {
            // Format as 4-digit string (0001-1000)
            String formattedNumber = String.format(Locale.getDefault(), "%04d", orderNumber);
            if (callback != null) {
                callback.onSuccess(formattedNumber);
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to get next order number", e);
            if (callback != null) {
                callback.onFailure(e);
            }
        });
    }

    /**
     * Create order
     */
    public void createOrder(Order order, DatabaseCallback callback) {
        // Validate that order has items
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            Log.e(TAG, "Cannot create order: order is null or has no items");
            if (callback != null) {
                callback.onFailure(new Exception("Order must contain at least one item"));
            }
            return;
        }
        
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
        // Validate that order has items
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            Log.e(TAG, "Cannot update order: order is null or has no items");
            if (callback != null) {
                callback.onFailure(new Exception("Order must contain at least one item"));
            }
            return;
        }
        
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
                            Order order = documentToOrder(document);
                            if (order != null) {
                                if (callback != null) callback.onSuccess(order);
                            } else {
                                if (callback != null) callback.onFailure(new Exception("Failed to deserialize order"));
                            }
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
        
        // Try query with orderBy first (requires composite index)
        db.collection(COLLECTION_ORDERS)
                .whereIn("status", statusList)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Order order = documentToOrder(document);
                                if (order != null) {
                                    orders.add(order);
                                }
                            }
                        }
                        // Sort by createdAt if not already sorted (fallback)
                        orders.sort(Comparator.comparingLong(Order::getCreatedAt));
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        Exception exception = task.getException();
                        Log.w(TAG, "Failed to get pending orders with orderBy, trying without orderBy", exception);
                        // If query fails (likely due to missing index), try without orderBy
                        db.collection(COLLECTION_ORDERS)
                                .whereIn("status", statusList)
                                .get()
                                .addOnCompleteListener(fallbackTask -> {
                                    if (fallbackTask.isSuccessful()) {
                                        List<Order> orders = new ArrayList<>();
                                        QuerySnapshot querySnapshot = fallbackTask.getResult();
                                        if (querySnapshot != null) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                Order order = documentToOrder(document);
                                                if (order != null) {
                                                    orders.add(order);
                                                }
                                            }
                                        }
                                        // Sort by createdAt manually
                                        orders.sort(Comparator.comparingLong(Order::getCreatedAt));
                                        if (callback != null) callback.onSuccess(orders);
                                    } else {
                                        Log.e(TAG, "Failed to get pending orders", fallbackTask.getException());
                                        if (callback != null) callback.onFailure(fallbackTask.getException());
                                    }
                                });
                    }
                });
    }

    /**
     * Convert Firestore document to Order object, handling null items
     */
    private Order documentToOrder(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }
        
        try {
            Order order = document.toObject(Order.class);
            if (order == null) {
                Log.w(TAG, "Failed to deserialize order from document: " + document.getId());
                return null;
            }
            
            // Ensure items list is not null
            if (order.getItems() == null) {
                order.setItems(new ArrayList<>());
                Log.w(TAG, "Order " + order.getOrderId() + " has null items, initializing empty list");
            }
            
            // Ensure orderId is set from document ID if missing
            if (order.getOrderId() == null || order.getOrderId().isEmpty()) {
                order.setOrderId(document.getId());
            }
            
            return order;
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing order from document: " + document.getId(), e);
            return null;
        }
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
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Order order = documentToOrder(document);
                                if (order != null) {
                                    orders.add(order);
                                }
                            }
                        }
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Listen to orders by user ID with real-time updates
     * Returns a ListenerRegistration that should be removed when done
     * Note: Requires Firestore composite index on (userId, createdAt)
     * Firestore will automatically suggest creating the index if missing
     */
    public com.google.firebase.firestore.ListenerRegistration listenToOrdersByUserId(
            String userId, OrdersCallback callback) {
        return db.collection(COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to orders", e);
                        // If index is missing, Firestore error will include a link to create it
                        // Check the logcat for the index creation link
                        if (callback != null) callback.onFailure(e);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Order order = documentToOrder(document);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        if (callback != null) callback.onSuccess(new ArrayList<>());
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
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Order order = documentToOrder(document);
                                if (order != null) {
                                    orders.add(order);
                                }
                            }
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

    /**
     * Listen to pending orders in real-time (for kitchen view)
     * Returns a ListenerRegistration that should be removed when done
     */
    public ListenerRegistration listenToPendingOrders(OrdersCallback callback) {
        List<String> statusList = new ArrayList<>();
        statusList.add("pending");
        statusList.add("preparing");
        
        // Use query without orderBy for listener (simpler, works without index)
        // We'll sort manually in the callback
        return db.collection(COLLECTION_ORDERS)
                .whereIn("status", statusList)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Real-time listener error", error);
                        if (callback != null) callback.onFailure(error);
                        return;
                    }
                    
                    if (snapshot != null) {
                        List<Order> orders = new ArrayList<>();
                        for (QueryDocumentSnapshot document : snapshot) {
                            Order order = documentToOrder(document);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        // Sort by createdAt manually (oldest first)
                        orders.sort(Comparator.comparingLong(Order::getCreatedAt));
                        if (callback != null) callback.onSuccess(orders);
                    }
                });
    }

    // ==================== Restaurant Operations ====================

    /**
     * Get all active restaurants
     */
    public void getAllRestaurants(RestaurantsCallback callback) {
        // Try query with orderBy first (requires composite index)
        db.collection(COLLECTION_RESTAURANTS)
                .whereEqualTo("isActive", true)
                .orderBy("restaurantName")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Restaurant> restaurants = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Restaurant restaurant = documentToRestaurant(document);
                                if (restaurant != null) {
                                    restaurants.add(restaurant);
                                }
                            }
                        }
                        // Sort by name if not already sorted (fallback)
                        restaurants.sort((r1, r2) -> {
                            String name1 = r1.getRestaurantName() != null ? r1.getRestaurantName() : "";
                            String name2 = r2.getRestaurantName() != null ? r2.getRestaurantName() : "";
                            return name1.compareToIgnoreCase(name2);
                        });
                        if (callback != null) callback.onSuccess(restaurants);
                    } else {
                        Exception exception = task.getException();
                        Log.w(TAG, "Failed to get restaurants with orderBy, trying without orderBy", exception);
                        // If query fails (likely due to missing index), try without orderBy
                        db.collection(COLLECTION_RESTAURANTS)
                                .whereEqualTo("isActive", true)
                                .get()
                                .addOnCompleteListener(fallbackTask -> {
                                    if (fallbackTask.isSuccessful()) {
                                        List<Restaurant> restaurants = new ArrayList<>();
                                        QuerySnapshot querySnapshot = fallbackTask.getResult();
                                        if (querySnapshot != null) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                Restaurant restaurant = documentToRestaurant(document);
                                                if (restaurant != null) {
                                                    restaurants.add(restaurant);
                                                }
                                            }
                                        }
                                        // Sort by name manually
                                        restaurants.sort((r1, r2) -> {
                                            String name1 = r1.getRestaurantName() != null ? r1.getRestaurantName() : "";
                                            String name2 = r2.getRestaurantName() != null ? r2.getRestaurantName() : "";
                                            return name1.compareToIgnoreCase(name2);
                                        });
                                        if (callback != null) callback.onSuccess(restaurants);
                                    } else {
                                        Log.w(TAG, "Failed to get restaurants with filter, trying without filter", fallbackTask.getException());
                                        // Last resort: get all restaurants without filter
                                        db.collection(COLLECTION_RESTAURANTS)
                                                .get()
                                                .addOnCompleteListener(lastResortTask -> {
                                                    if (lastResortTask.isSuccessful()) {
                                                        List<Restaurant> restaurants = new ArrayList<>();
                                                        QuerySnapshot querySnapshot = lastResortTask.getResult();
                                                        if (querySnapshot != null) {
                                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                                Restaurant restaurant = documentToRestaurant(document);
                                                                // Filter by isActive in memory
                                                                if (restaurant != null && restaurant.isActive()) {
                                                                    restaurants.add(restaurant);
                                                                }
                                                            }
                                                        }
                                                        // Sort by name manually
                                                        restaurants.sort((r1, r2) -> {
                                                            String name1 = r1.getRestaurantName() != null ? r1.getRestaurantName() : "";
                                                            String name2 = r2.getRestaurantName() != null ? r2.getRestaurantName() : "";
                                                            return name1.compareToIgnoreCase(name2);
                                                        });
                                                        if (callback != null) callback.onSuccess(restaurants);
                                                    } else {
                                                        Log.e(TAG, "Failed to get restaurants", lastResortTask.getException());
                                                        if (callback != null) callback.onFailure(lastResortTask.getException());
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    /**
     * Get restaurant by ID
     */
    public void getRestaurantById(String restaurantId, RestaurantCallback callback) {
        db.collection(COLLECTION_RESTAURANTS)
                .document(restaurantId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Restaurant restaurant = documentToRestaurant(document);
                            if (callback != null) callback.onSuccess(restaurant);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("Restaurant not found"));
                        }
                    } else {
                        Log.e(TAG, "Failed to get restaurant", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Convert DocumentSnapshot to Restaurant
     */
    private Restaurant documentToRestaurant(DocumentSnapshot document) {
        try {
            Restaurant restaurant = new Restaurant();
            restaurant.setRestaurantId(document.getString("restaurantId"));
            restaurant.setRestaurantName(document.getString("restaurantName"));
            restaurant.setAddress(document.getString("address"));
            restaurant.setPhoneNumber(document.getString("phoneNumber"));
            Boolean isActive = document.getBoolean("isActive");
            restaurant.setActive(isActive != null && isActive);
            Long createdAt = document.getLong("createdAt");
            restaurant.setCreatedAt(createdAt != null ? createdAt : 0);
            Long updatedAt = document.getLong("updatedAt");
            restaurant.setUpdatedAt(updatedAt != null ? updatedAt : 0);
            return restaurant;
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert document to Restaurant", e);
            return null;
        }
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

    public interface AdminsCallback {
        void onSuccess(List<Admin> admins);
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

    public interface OrderNumberCallback {
        void onSuccess(String orderNumber);
        void onFailure(Exception e);
    }

    public interface RestaurantCallback {
        void onSuccess(Restaurant restaurant);
        void onFailure(Exception e);
    }

    public interface RestaurantsCallback {
        void onSuccess(List<Restaurant> restaurants);
        void onFailure(Exception e);
    }

    public interface TablesCallback {
        void onSuccess(List<Table> tables);
        void onFailure(Exception e);
    }

    // ==================== Table Operations ====================

    /**
     * Get all tables by branch ID
     */
    public void getTablesByBranchId(String branchId, TablesCallback callback) {
        db.collection(COLLECTION_TABLES)
                .whereEqualTo("branchId", branchId)
                .orderBy("tableNumber")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Table> tables = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Table table = documentToTable(document);
                                if (table != null) {
                                    tables.add(table);
                                }
                            }
                        }
                        // Sort by table number if not already sorted
                        tables.sort((t1, t2) -> {
                            String num1 = t1.getTableNumber() != null ? t1.getTableNumber() : "";
                            String num2 = t2.getTableNumber() != null ? t2.getTableNumber() : "";
                            return num1.compareToIgnoreCase(num2);
                        });
                        if (callback != null) callback.onSuccess(tables);
                    } else {
                        Exception exception = task.getException();
                        Log.w(TAG, "Failed to get tables with orderBy, trying without orderBy", exception);
                        // Fallback: try without orderBy
                        db.collection(COLLECTION_TABLES)
                                .whereEqualTo("branchId", branchId)
                                .get()
                                .addOnCompleteListener(fallbackTask -> {
                                    if (fallbackTask.isSuccessful()) {
                                        List<Table> tables = new ArrayList<>();
                                        QuerySnapshot querySnapshot = fallbackTask.getResult();
                                        if (querySnapshot != null) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                Table table = documentToTable(document);
                                                if (table != null) {
                                                    tables.add(table);
                                                }
                                            }
                                        }
                                        // Sort by table number manually
                                        tables.sort((t1, t2) -> {
                                            String num1 = t1.getTableNumber() != null ? t1.getTableNumber() : "";
                                            String num2 = t2.getTableNumber() != null ? t2.getTableNumber() : "";
                                            return num1.compareToIgnoreCase(num2);
                                        });
                                        if (callback != null) callback.onSuccess(tables);
                                    } else {
                                        Log.e(TAG, "Failed to get tables", fallbackTask.getException());
                                        if (callback != null) callback.onFailure(fallbackTask.getException());
                                    }
                                });
                    }
                });
    }

    /**
     * Listen to tables by branch ID with real-time updates
     * Returns a ListenerRegistration that should be removed when done
     */
    public ListenerRegistration listenToTablesByBranchId(String branchId, TablesCallback callback) {
        return db.collection(COLLECTION_TABLES)
                .whereEqualTo("branchId", branchId)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to tables", e);
                        if (callback != null) callback.onFailure(e);
                        return;
                    }

                    if (querySnapshot != null) {
                        List<Table> tables = new ArrayList<>();
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            Table table = documentToTable(document);
                            if (table != null) {
                                tables.add(table);
                            }
                        }
                        // Sort by table number
                        tables.sort((t1, t2) -> {
                            String num1 = t1.getTableNumber() != null ? t1.getTableNumber() : "";
                            String num2 = t2.getTableNumber() != null ? t2.getTableNumber() : "";
                            return num1.compareToIgnoreCase(num2);
                        });
                        if (callback != null) callback.onSuccess(tables);
                    } else {
                        if (callback != null) callback.onSuccess(new ArrayList<>());
                    }
                });
    }

    /**
     * Update table status
     */
    public void updateTableStatus(String tableId, String status, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(COLLECTION_TABLES)
                .document(tableId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Table status updated successfully: " + tableId + " -> " + status);
                    if (callback != null) callback.onSuccess(tableId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Table status update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Update table current order ID
     */
    public void updateTableCurrentOrderId(String tableId, String orderId, DatabaseCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentOrderId", orderId != null ? orderId : "");
        updates.put("updatedAt", System.currentTimeMillis());
        // Update status based on orderId
        if (orderId != null && !orderId.isEmpty()) {
            updates.put("status", "occupied");
        } else {
            updates.put("status", "available");
        }

        db.collection(COLLECTION_TABLES)
                .document(tableId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Table order ID updated successfully: " + tableId);
                    if (callback != null) callback.onSuccess(tableId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Table order ID update failed", e);
                    if (callback != null) callback.onFailure(e);
                });
    }

    /**
     * Get table by ID
     */
    public void getTableById(String tableId, TableCallback callback) {
        db.collection(COLLECTION_TABLES)
                .document(tableId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Table table = documentToTable(document);
                            if (callback != null) callback.onSuccess(table);
                        } else {
                            if (callback != null) callback.onFailure(new Exception("Table not found"));
                        }
                    } else {
                        Log.e(TAG, "Failed to get table", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Convert DocumentSnapshot to Table
     */
    private Table documentToTable(DocumentSnapshot document) {
        try {
            Table table = new Table();
            table.setTableId(document.getString("tableId"));
            table.setTableNumber(document.getString("tableNumber"));
            table.setBranchId(document.getString("branchId"));
            table.setStatus(document.getString("status"));
            Long capacity = document.getLong("capacity");
            table.setCapacity(capacity != null ? capacity.intValue() : 4);
            table.setCurrentOrderId(document.getString("currentOrderId") != null ? document.getString("currentOrderId") : "");
            Long createdAt = document.getLong("createdAt");
            table.setCreatedAt(createdAt != null ? createdAt : 0);
            Long updatedAt = document.getLong("updatedAt");
            table.setUpdatedAt(updatedAt != null ? updatedAt : 0);
            return table;
        } catch (Exception e) {
            Log.e(TAG, "Failed to convert document to Table", e);
            return null;
        }
    }

    public interface TableCallback {
        void onSuccess(Table table);
        void onFailure(Exception e);
    }

    // ==================== Order Search Operations ====================

    /**
     * Search orders by various criteria
     */
    public void searchOrders(String searchQuery, String statusFilter, OrdersCallback callback) {
        Query query = db.collection(COLLECTION_ORDERS);

        // Apply status filter if provided
        if (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equals("all")) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Order order = documentToOrder(document);
                                if (order != null) {
                                    // Apply search query filter if provided
                                    if (searchQuery == null || searchQuery.isEmpty()) {
                                        orders.add(order);
                                    } else {
                                        String queryLower = searchQuery.toLowerCase();
                                        // Search in order ID, table number, customer name
                                        boolean matches = (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(queryLower))
                                                || (order.getTableNumber() != null && order.getTableNumber().toLowerCase().contains(queryLower))
                                                || (order.getUserId() != null && order.getUserId().toLowerCase().contains(queryLower));
                                        if (matches) {
                                            orders.add(order);
                                        }
                                    }
                                }
                            }
                        }
                        // Sort by createdAt descending (newest first)
                        orders.sort((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        Log.e(TAG, "Failed to search orders", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }

    /**
     * Get all active orders (pending, preparing, ready)
     */
    public void getAllActiveOrders(OrdersCallback callback) {
        List<String> statusList = new ArrayList<>();
        statusList.add("pending");
        statusList.add("preparing");
        statusList.add("ready");

        db.collection(COLLECTION_ORDERS)
                .whereIn("status", statusList)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Order> orders = new ArrayList<>();
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                Order order = documentToOrder(document);
                                if (order != null) {
                                    orders.add(order);
                                }
                            }
                        }
                        // Sort by createdAt descending (newest first)
                        orders.sort((o1, o2) -> Long.compare(o2.getCreatedAt(), o1.getCreatedAt()));
                        if (callback != null) callback.onSuccess(orders);
                    } else {
                        Log.e(TAG, "Failed to get active orders", task.getException());
                        if (callback != null) callback.onFailure(task.getException());
                    }
                });
    }
}

