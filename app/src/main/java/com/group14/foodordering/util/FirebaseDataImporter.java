package com.group14.foodordering.util;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.group14.foodordering.model.Admin;
import com.group14.foodordering.model.AppConfig;
import com.group14.foodordering.model.Branch;
import com.group14.foodordering.model.ItemModifier;
import com.group14.foodordering.model.MenuCategory;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.ModifierOption;
import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.model.Table;
import com.group14.foodordering.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Firebase data import utility
 * Used to import sample data from JSON files to Firebase
 */
public class FirebaseDataImporter {
    private static final String TAG = "FirebaseDataImporter";
    private FirebaseFirestore db;
    private Context context;
    private ImportCallback callback;

    public interface ImportCallback {
        void onProgress(String message);
        void onCollectionComplete(String collectionName, int successCount, int failCount);
        void onComplete(int totalSuccess, int totalFail);
        void onError(Exception e);
    }

    public FirebaseDataImporter(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Import data from assets folder
     */
    public void importFromAssets(String filename, ImportCallback callback) {
        this.callback = callback;
        
        try {
            InputStream inputStream = context.getAssets().open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            String jsonContent = jsonString.toString().trim();
            if (jsonContent.isEmpty()) {
                callback.onError(new Exception("JSON file is empty"));
                return;
            }

            JSONObject jsonData = new JSONObject(jsonContent);
            importFromJson(jsonData);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to read from assets", e);
            callback.onError(e);
        }
    }

    /**
     * Import data from file
     */
    public void importFromFile(String filePath, ImportCallback callback) {
        this.callback = callback;
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                callback.onError(new Exception("File not found: " + filePath));
                return;
            }

            FileInputStream inputStream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            String jsonContent = jsonString.toString().trim();
            if (jsonContent.isEmpty()) {
                callback.onError(new Exception("JSON file is empty"));
                return;
            }

            JSONObject jsonData = new JSONObject(jsonContent);
            importFromJson(jsonData);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to read file", e);
            callback.onError(e);
        }
    }

    /**
     * Import data from JSON object
     */
    public void importFromJson(JSONObject jsonData) {
        try {
            int totalSuccess = 0;
            int totalFail = 0;

            // Import users
            if (jsonData.has("users")) {
                ImportResult result = importUsers(jsonData.getJSONArray("users"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import admins
            if (jsonData.has("admins")) {
                ImportResult result = importAdmins(jsonData.getJSONArray("admins"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import restaurants
            if (jsonData.has("restaurants")) {
                ImportResult result = importRestaurants(jsonData.getJSONArray("restaurants"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import branches
            if (jsonData.has("branches")) {
                ImportResult result = importBranches(jsonData.getJSONArray("branches"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import tables
            if (jsonData.has("tables")) {
                ImportResult result = importTables(jsonData.getJSONArray("tables"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import menu categories
            if (jsonData.has("menuCategories")) {
                ImportResult result = importMenuCategories(jsonData.getJSONArray("menuCategories"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import menu items
            if (jsonData.has("menuItems")) {
                ImportResult result = importMenuItems(jsonData.getJSONArray("menuItems"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import item modifiers
            if (jsonData.has("itemModifiers")) {
                ImportResult result = importItemModifiers(jsonData.getJSONArray("itemModifiers"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            // Import app config
            if (jsonData.has("appConfig")) {
                ImportResult result = importAppConfig(jsonData.getJSONObject("appConfig"));
                totalSuccess += result.success;
                totalFail += result.fail;
            }

            callback.onComplete(totalSuccess, totalFail);
            
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
            callback.onError(e);
        }
    }

    /**
     * Import users
     */
    private ImportResult importUsers(JSONArray usersArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing users...");

        for (int i = 0; i < usersArray.length(); i++) {
            try {
                JSONObject userJson = usersArray.getJSONObject(i);
                User user = new User();
                user.setUserId(userJson.getString("userId"));
                user.setEmail(userJson.getString("email"));
                user.setName(userJson.getString("name"));
                user.setPhone(userJson.getString("phone"));
                user.setRole(userJson.getString("role"));
                user.setCreatedAt(userJson.getLong("createdAt"));
                user.setUpdatedAt(userJson.getLong("updatedAt"));

                db.collection("users")
                    .document(user.getUserId())
                    .set(user.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "User imported: " + user.getUserId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import user: " + user.getUserId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse user", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("users", result.success, result.fail);
        return result;
    }

    /**
     * Import admins
     */
    private ImportResult importAdmins(JSONArray adminsArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing admins...");

        for (int i = 0; i < adminsArray.length(); i++) {
            try {
                JSONObject adminJson = adminsArray.getJSONObject(i);
                Admin admin = new Admin();
                admin.setAdminId(adminJson.getString("adminId"));
                admin.setUserId(adminJson.getString("userId"));
                admin.setEmail(adminJson.getString("email"));
                admin.setName(adminJson.getString("name"));
                admin.setPhone(adminJson.getString("phone"));
                
                JSONArray permissionsArray = adminJson.getJSONArray("permissions");
                List<String> permissions = new ArrayList<>();
                for (int j = 0; j < permissionsArray.length(); j++) {
                    permissions.add(permissionsArray.getString(j));
                }
                admin.setPermissions(permissions);
                
                // Import restaurantIds if available
                if (adminJson.has("restaurantIds")) {
                    JSONArray restaurantIdsArray = adminJson.getJSONArray("restaurantIds");
                    List<String> restaurantIds = new ArrayList<>();
                    for (int j = 0; j < restaurantIdsArray.length(); j++) {
                        restaurantIds.add(restaurantIdsArray.getString(j));
                    }
                    admin.setRestaurantIds(restaurantIds);
                }
                
                admin.setActive(adminJson.getBoolean("isActive"));
                admin.setCreatedAt(adminJson.getLong("createdAt"));
                admin.setUpdatedAt(adminJson.getLong("updatedAt"));

                db.collection("admins")
                    .document(admin.getAdminId())
                    .set(admin.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Admin imported: " + admin.getAdminId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import admin: " + admin.getAdminId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse admin", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("admins", result.success, result.fail);
        return result;
    }

    /**
     * Import restaurants
     */
    private ImportResult importRestaurants(JSONArray restaurantsArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing restaurants...");

        for (int i = 0; i < restaurantsArray.length(); i++) {
            try {
                JSONObject restaurantJson = restaurantsArray.getJSONObject(i);
                Restaurant restaurant = new Restaurant();
                restaurant.setRestaurantId(restaurantJson.getString("restaurantId"));
                restaurant.setRestaurantName(restaurantJson.getString("restaurantName"));
                restaurant.setAddress(restaurantJson.getString("address"));
                restaurant.setPhoneNumber(restaurantJson.getString("phoneNumber"));
                restaurant.setActive(restaurantJson.getBoolean("isActive"));
                restaurant.setCreatedAt(restaurantJson.getLong("createdAt"));
                restaurant.setUpdatedAt(restaurantJson.getLong("updatedAt"));

                db.collection("restaurants")
                    .document(restaurant.getRestaurantId())
                    .set(restaurant.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Restaurant imported: " + restaurant.getRestaurantId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import restaurant: " + restaurant.getRestaurantId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse restaurant", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("restaurants", result.success, result.fail);
        return result;
    }

    /**
     * Import branches
     */
    private ImportResult importBranches(JSONArray branchesArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing branches...");

        for (int i = 0; i < branchesArray.length(); i++) {
            try {
                JSONObject branchJson = branchesArray.getJSONObject(i);
                Branch branch = new Branch();
                branch.setBranchId(branchJson.getString("branchId"));
                branch.setBranchName(branchJson.getString("branchName"));
                
                // Import restaurantId if available
                if (branchJson.has("restaurantId")) {
                    branch.setRestaurantId(branchJson.getString("restaurantId"));
                }
                
                branch.setAddress(branchJson.getString("address"));
                branch.setPhoneNumber(branchJson.getString("phoneNumber"));
                
                // Geolocation
                JSONObject geoJson = branchJson.getJSONObject("geolocation");
                Map<String, Double> geo = new HashMap<>();
                geo.put("latitude", geoJson.getDouble("latitude"));
                geo.put("longitude", geoJson.getDouble("longitude"));
                branch.setGeolocation(geo);
                
                // Opening hours
                JSONObject hoursJson = branchJson.getJSONObject("openingHours");
                Map<String, String> hours = new HashMap<>();
                Iterator<String> keys = hoursJson.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    hours.put(key, hoursJson.getString(key));
                }
                branch.setOpeningHours(hours);
                
                branch.setActive(branchJson.getBoolean("isActive"));
                branch.setCreatedAt(branchJson.getLong("createdAt"));
                branch.setUpdatedAt(branchJson.getLong("updatedAt"));

                db.collection("branches")
                    .document(branch.getBranchId())
                    .set(branch.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Branch imported: " + branch.getBranchId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import branch: " + branch.getBranchId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse branch", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("branches", result.success, result.fail);
        return result;
    }

    /**
     * Import tables
     */
    private ImportResult importTables(JSONArray tablesArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing tables...");

        for (int i = 0; i < tablesArray.length(); i++) {
            try {
                JSONObject tableJson = tablesArray.getJSONObject(i);
                Table table = new Table();
                table.setTableId(tableJson.getString("tableId"));
                table.setTableNumber(tableJson.getString("tableNumber"));
                table.setBranchId(tableJson.getString("branchId"));
                table.setStatus(tableJson.getString("status"));
                table.setCapacity(tableJson.getInt("capacity"));
                table.setCurrentOrderId(tableJson.optString("currentOrderId", ""));
                table.setCreatedAt(tableJson.getLong("createdAt"));
                table.setUpdatedAt(tableJson.getLong("updatedAt"));

                db.collection("tables")
                    .document(table.getTableId())
                    .set(table.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Table imported: " + table.getTableId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import table: " + table.getTableId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse table", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("tables", result.success, result.fail);
        return result;
    }

    /**
     * Import menu categories
     */
    private ImportResult importMenuCategories(JSONArray categoriesArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing menu categories...");

        for (int i = 0; i < categoriesArray.length(); i++) {
            try {
                JSONObject categoryJson = categoriesArray.getJSONObject(i);
                MenuCategory category = new MenuCategory();
                category.setCategoryId(categoryJson.getString("categoryId"));
                category.setCategoryName(categoryJson.getString("categoryName"));
                category.setDisplayName(categoryJson.getString("displayName"));
                category.setDisplayOrder(categoryJson.getInt("displayOrder"));
                category.setActive(categoryJson.getBoolean("isActive"));
                category.setCreatedAt(categoryJson.getLong("createdAt"));
                category.setUpdatedAt(categoryJson.getLong("updatedAt"));

                db.collection("menuCategories")
                    .document(category.getCategoryId())
                    .set(category.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Menu category imported: " + category.getCategoryId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import menu category: " + category.getCategoryId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse menu category", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("menuCategories", result.success, result.fail);
        return result;
    }

    /**
     * Import menu items
     */
    private ImportResult importMenuItems(JSONArray itemsArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing menu items...");

        for (int i = 0; i < itemsArray.length(); i++) {
            try {
                JSONObject itemJson = itemsArray.getJSONObject(i);
                MenuItem item = new MenuItem();
                item.setItemId(itemJson.getString("itemId"));
                item.setName(itemJson.getString("name"));
                item.setDescription(itemJson.optString("description", ""));
                item.setPrice(itemJson.getDouble("price"));
                item.setCategory(itemJson.getString("category"));
                item.setImageUrl(itemJson.optString("imageUrl", ""));
                item.setAvailable(itemJson.getBoolean("isAvailable"));
                item.setStock(itemJson.optInt("stock", -1));
                // Handle hasDrink field if present
                if (itemJson.has("hasDrink")) {
                    item.setHasDrink(itemJson.getBoolean("hasDrink"));
                }
                // Handle modifierIds if present
                if (itemJson.has("modifierIds")) {
                    JSONArray modifierIdsArray = itemJson.getJSONArray("modifierIds");
                    List<String> modifierIds = new ArrayList<>();
                    for (int j = 0; j < modifierIdsArray.length(); j++) {
                        modifierIds.add(modifierIdsArray.getString(j));
                    }
                    item.setModifierIds(modifierIds);
                }
                item.setCreatedAt(itemJson.getLong("createdAt"));
                item.setUpdatedAt(itemJson.getLong("updatedAt"));

                db.collection("menuItems")
                    .document(item.getItemId())
                    .set(item.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Menu item imported: " + item.getItemId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import menu item: " + item.getItemId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse menu item", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("menuItems", result.success, result.fail);
        return result;
    }

    /**
     * Import item modifiers
     */
    private ImportResult importItemModifiers(JSONArray modifiersArray) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing item modifiers...");

        for (int i = 0; i < modifiersArray.length(); i++) {
            try {
                JSONObject modifierJson = modifiersArray.getJSONObject(i);
                ItemModifier modifier = new ItemModifier();
                modifier.setModifierId(modifierJson.getString("modifierId"));
                modifier.setModifierGroup(modifierJson.getString("modifierGroup"));
                
                // Parse menuItemIds
                JSONArray menuItemIdsArray = modifierJson.getJSONArray("menuItemIds");
                List<String> menuItemIds = new ArrayList<>();
                for (int j = 0; j < menuItemIdsArray.length(); j++) {
                    menuItemIds.add(menuItemIdsArray.getString(j));
                }
                modifier.setMenuItemIds(menuItemIds);
                
                // Parse options
                JSONArray optionsArray = modifierJson.getJSONArray("options");
                List<ModifierOption> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    JSONObject optionJson = optionsArray.getJSONObject(j);
                    ModifierOption option = new ModifierOption();
                    option.setOptionName(optionJson.getString("optionName"));
                    option.setAdditionalPrice(optionJson.getDouble("additionalPrice"));
                    if (optionJson.has("isAvailable")) {
                        option.setAvailable(optionJson.getBoolean("isAvailable"));
                    }
                    options.add(option);
                }
                modifier.setOptions(options);
                
                // Optional fields
                if (modifierJson.has("isRequired")) {
                    modifier.setRequired(modifierJson.getBoolean("isRequired"));
                }
                if (modifierJson.has("minSelections")) {
                    modifier.setMinSelections(modifierJson.getInt("minSelections"));
                }
                if (modifierJson.has("maxSelections")) {
                    modifier.setMaxSelections(modifierJson.getInt("maxSelections"));
                }
                modifier.setCreatedAt(modifierJson.getLong("createdAt"));
                modifier.setUpdatedAt(modifierJson.getLong("updatedAt"));

                db.collection("itemModifiers")
                    .document(modifier.getModifierId())
                    .set(modifier.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Item modifier imported: " + modifier.getModifierId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to import item modifier: " + modifier.getModifierId(), e);
                    });

                result.success++;
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse item modifier", e);
                result.fail++;
            }
        }

        callback.onCollectionComplete("itemModifiers", result.success, result.fail);
        return result;
    }

    /**
     * Import app config
     */
    private ImportResult importAppConfig(JSONObject configJson) {
        ImportResult result = new ImportResult();
        callback.onProgress("Importing app config...");

        try {
            AppConfig config = new AppConfig();
            config.setConfigId(configJson.getString("configId"));
            config.setTaxRate(configJson.getDouble("taxRate"));
            config.setServiceChargeRate(configJson.getDouble("serviceChargeRate"));
            config.setCurrency(configJson.getString("currency"));
            config.setCurrencySymbol(configJson.getString("currencySymbol"));
            config.setPromotionBanner(configJson.optString("promotionBanner", ""));
            
            JSONArray paymentMethods = configJson.getJSONArray("supportedPaymentMethods");
            List<String> methods = new ArrayList<>();
            for (int i = 0; i < paymentMethods.length(); i++) {
                methods.add(paymentMethods.getString(i));
            }
            config.setSupportedPaymentMethods(methods);
            
            JSONArray deliveryOptions = configJson.getJSONArray("deliveryTimeOptions");
            List<String> options = new ArrayList<>();
            for (int i = 0; i < deliveryOptions.length(); i++) {
                options.add(deliveryOptions.getString(i));
            }
            config.setDeliveryTimeOptions(options);
            
            config.setCreatedAt(configJson.getLong("createdAt"));
            config.setUpdatedAt(configJson.getLong("updatedAt"));

            db.collection("appConfig")
                .document(config.getConfigId())
                .set(config.toMap())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "App config imported: " + config.getConfigId());
                    result.success++;
                    callback.onCollectionComplete("appConfig", result.success, result.fail);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to import app config", e);
                    result.fail++;
                    callback.onCollectionComplete("appConfig", result.success, result.fail);
                });

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse app config", e);
            result.fail++;
            callback.onCollectionComplete("appConfig", result.success, result.fail);
        }

        return result;
    }

    /**
     * Clear all data from Firebase collections
     * Deletes all documents from: users, admins, branches, tables, menuCategories, menuItems, appConfig
     * Uses batch writes for efficiency (max 500 operations per batch)
     */
    public void clearAllData(ImportCallback callback) {
        this.callback = callback;
        callback.onProgress("Starting to clear all database collections...");
        
        // List of collections to clear
        String[] collections = {
            "users",
            "admins",
            "branches",
            "tables",
            "menuCategories",
            "menuItems",
            "appConfig"
        };
        
        final int[] completedCollections = {0};
        final int[] totalDeleted = {0};
        final int[] totalFailed = {0};
        final int totalCollections = collections.length;
        final Object lock = new Object();
        
        // Process collections sequentially to avoid race conditions
        processCollectionClear(collections, 0, completedCollections, totalDeleted, totalFailed, 
                totalCollections, lock, callback);
    }
    
    private void processCollectionClear(String[] collections, int index, 
            int[] completedCollections, int[] totalDeleted, int[] totalFailed,
            int totalCollections, Object lock, ImportCallback callback) {
        
        if (index >= collections.length) {
            // All collections processed
            return;
        }
        
        final String collectionName = collections[index];
        callback.onProgress("Clearing collection: " + collectionName + "...");
        
        db.collection(collectionName)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onProgress("Collection '" + collectionName + "' is already empty");
                    synchronized (lock) {
                        completedCollections[0]++;
                        if (completedCollections[0] == totalCollections) {
                            callback.onProgress("All collections cleared!");
                            callback.onComplete(totalDeleted[0], totalFailed[0]);
                        }
                    }
                    // Process next collection
                    processCollectionClear(collections, index + 1, completedCollections, 
                            totalDeleted, totalFailed, totalCollections, lock, callback);
                    return;
                }
                
                int docCount = querySnapshot.size();
                callback.onProgress("Found " + docCount + " documents in '" + collectionName + "', deleting...");
                
                // Use batch writes (max 500 operations per batch)
                final int BATCH_SIZE = 500;
                List<com.google.firebase.firestore.QueryDocumentSnapshot> documents = new ArrayList<>();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                    documents.add(doc);
                }
                
                deleteDocumentsInBatches(documents, collectionName, 0, BATCH_SIZE, 
                        completedCollections, totalDeleted, totalFailed, totalCollections, 
                        collections, index, lock, callback);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get documents from collection: " + collectionName, e);
                callback.onProgress("Error reading collection '" + collectionName + "': " + e.getMessage());
                synchronized (lock) {
                    completedCollections[0]++;
                    totalFailed[0]++;
                    callback.onCollectionComplete(collectionName, 0, 1);
                    if (completedCollections[0] == totalCollections) {
                        callback.onProgress("All collections processed!");
                        callback.onComplete(totalDeleted[0], totalFailed[0]);
                    }
                }
                // Process next collection even if this one failed
                processCollectionClear(collections, index + 1, completedCollections, 
                        totalDeleted, totalFailed, totalCollections, lock, callback);
            });
    }
    
    private void deleteDocumentsInBatches(List<com.google.firebase.firestore.QueryDocumentSnapshot> documents,
            String collectionName, int startIndex, int batchSize,
            int[] completedCollections, int[] totalDeleted, int[] totalFailed,
            int totalCollections, String[] collections, int collectionIndex,
            Object lock, ImportCallback callback) {
        
        if (startIndex >= documents.size()) {
            // All documents in this collection deleted
            synchronized (lock) {
                completedCollections[0]++;
                if (completedCollections[0] == totalCollections) {
                    callback.onProgress("All collections cleared!");
                    callback.onComplete(totalDeleted[0], totalFailed[0]);
                }
            }
            // Process next collection
            processCollectionClear(collections, collectionIndex + 1, completedCollections, 
                    totalDeleted, totalFailed, totalCollections, lock, callback);
            return;
        }
        
        int endIndex = Math.min(startIndex + batchSize, documents.size());
        WriteBatch batch = db.batch();
        
        for (int i = startIndex; i < endIndex; i++) {
            batch.delete(documents.get(i).getReference());
        }
        
        final int batchStart = startIndex;
        final int batchEnd = endIndex;
        final int batchCount = endIndex - startIndex;
        
        batch.commit()
            .addOnSuccessListener(aVoid -> {
                callback.onProgress("Deleted " + batchEnd + "/" + documents.size() + 
                        " documents from '" + collectionName + "'");
                synchronized (lock) {
                    totalDeleted[0] += batchCount;
                }
                // Process next batch
                deleteDocumentsInBatches(documents, collectionName, endIndex, batchSize,
                        completedCollections, totalDeleted, totalFailed, totalCollections,
                        collections, collectionIndex, lock, callback);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to delete batch from collection: " + collectionName, e);
                callback.onProgress("Error deleting batch from '" + collectionName + "': " + e.getMessage());
                synchronized (lock) {
                    totalFailed[0] += batchCount;
                }
                // Continue with next batch even if this one failed
                deleteDocumentsInBatches(documents, collectionName, endIndex, batchSize,
                        completedCollections, totalDeleted, totalFailed, totalCollections,
                        collections, collectionIndex, lock, callback);
            });
    }

    private static class ImportResult {
        int success = 0;
        int fail = 0;
    }
}

