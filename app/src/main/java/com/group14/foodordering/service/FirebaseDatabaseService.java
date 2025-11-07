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
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.Order;
import com.group14.foodordering.model.Restaurant;
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
    private static final String COLLECTION_RESTAURANTS = "restaurants";
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
        admin.setActive(document.getBoolean("isActive") != null ? document.getBoolean("isActive") : false);
        
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
            for (String permission : permissionsArray) {
                permissionsList.add(permission);
            }
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
            for (String restaurantId : restaurantIdsArray) {
                restaurantIdsList.add(restaurantId);
            }
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
                                if (isAvailable != null && isAvailable != item.isAvailable()) {
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
                currentNumber = snapshot.getLong("currentNumber");
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
            String formattedNumber = String.format("%04d", orderNumber);
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
                        orders.sort((o1, o2) -> Long.compare(o1.getCreatedAt(), o2.getCreatedAt()));
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
                                        orders.sort((o1, o2) -> Long.compare(o1.getCreatedAt(), o2.getCreatedAt()));
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
                        orders.sort((o1, o2) -> Long.compare(o1.getCreatedAt(), o2.getCreatedAt()));
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
            restaurant.setActive(document.getBoolean("isActive") != null && document.getBoolean("isActive"));
            restaurant.setCreatedAt(document.getLong("createdAt") != null ? document.getLong("createdAt") : 0);
            restaurant.setUpdatedAt(document.getLong("updatedAt") != null ? document.getLong("updatedAt") : 0);
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
}

