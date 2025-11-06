package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * Drink Selection Activity
 * Allows users to choose from available drinks and select hot or iced version
 */
public class DrinkSelectionActivity extends AppCompatActivity {

    private static final String TAG = "DrinkSelectionActivity";
    public static final String EXTRA_MENU_ITEM = "menu_item";
    public static final String EXTRA_DRINK_NAME = "drink_name";
    public static final String EXTRA_DRINK_TYPE = "drink_type"; // "hot" or "iced"
    public static final String EXTRA_DRINK_PRICE_ADDITION = "drink_price_addition";

    private MenuItem menuItem;
    private TextView itemNameTextView;
    private TextView basePriceTextView;
    private TextView totalPriceTextView;
    private Button confirmButton;
    private RecyclerView drinksRecyclerView;
    private DrinkAdapter drinkAdapter;
    private List<MenuItem> availableDrinks;
    private MenuItem selectedDrink;
    private String selectedDrinkType = "hot"; // "hot" or "iced"
    private FirebaseDatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_selection);

        // Get menu item from intent
        menuItem = (MenuItem) getIntent().getSerializableExtra(EXTRA_MENU_ITEM);
        if (menuItem == null) {
            Log.e(TAG, "Menu item is null");
            Toast.makeText(this, "Error: Menu item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbService = FirebaseDatabaseService.getInstance();
        availableDrinks = new ArrayList<>();

        setupViews();
        loadAvailableDrinks();
    }

    private void setupViews() {
        itemNameTextView = findViewById(R.id.itemNameTextView);
        basePriceTextView = findViewById(R.id.basePriceTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        confirmButton = findViewById(R.id.confirmButton);
        drinksRecyclerView = findViewById(R.id.drinksRecyclerView);

        // Set item name
        itemNameTextView.setText(menuItem.getName());
        
        // Set base price
        basePriceTextView.setText(String.format("Base Price: $%.2f", menuItem.getPrice()));

        // Setup RecyclerView
        drinksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        drinkAdapter = new DrinkAdapter();
        drinksRecyclerView.setAdapter(drinkAdapter);

        // Confirm button click
        confirmButton.setOnClickListener(v -> confirmSelection());
        
        // Update price display initially
        updatePriceDisplay();
    }

    /**
     * Load available drinks from database
     */
    private void loadAvailableDrinks() {
        // Try multiple category variations
        dbService.getFirestore().collection("menuItems")
                .whereEqualTo("category", "drink")
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        availableDrinks.clear();
                        if (!task.getResult().isEmpty()) {
                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                                MenuItem drink = document.toObject(MenuItem.class);
                                if (drink != null) {
                                    drink.setItemId(document.getId());
                                    availableDrinks.add(drink);
                                }
                            }
                            Log.d(TAG, "Loaded " + availableDrinks.size() + " drinks from database");
                        }
                        
                        // If no drinks found with "drink", try "drinks" (plural)
                        if (availableDrinks.isEmpty()) {
                            Log.d(TAG, "No drinks found with category 'drink', trying 'drinks'");
                            dbService.getFirestore().collection("menuItems")
                                    .whereEqualTo("category", "drinks")
                                    .whereEqualTo("isAvailable", true)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                            for (com.google.firebase.firestore.QueryDocumentSnapshot document : task2.getResult()) {
                                                MenuItem drink = document.toObject(MenuItem.class);
                                                if (drink != null) {
                                                    drink.setItemId(document.getId());
                                                    availableDrinks.add(drink);
                                                }
                                            }
                                            Log.d(TAG, "Loaded " + availableDrinks.size() + " drinks from database (plural category)");
                                        }
                                        
                                        if (availableDrinks.isEmpty()) {
                                            Log.w(TAG, "No drinks found in Firestore, trying local JSON fallback");
                                            loadDrinksFromLocal();
                                        } else {
                                            updateDrinkList();
                                        }
                                    });
                        } else {
                            updateDrinkList();
                        }
                    } else {
                        Log.w(TAG, "Failed to load drinks from Firestore, trying local JSON fallback", 
                                task.getException());
                        loadDrinksFromLocal();
                    }
                });
    }
    
    /**
     * Update drink list UI after loading
     */
    private void updateDrinkList() {
        drinkAdapter.notifyDataSetChanged();
        
        // Select first drink by default
        if (!availableDrinks.isEmpty()) {
            selectedDrink = availableDrinks.get(0);
            selectedDrinkType = "hot";
            drinkAdapter.notifyDataSetChanged();
            updatePriceDisplay();
        } else {
            Log.w(TAG, "No drinks available, trying local JSON fallback");
            loadDrinksFromLocal();
        }
    }

    /**
     * Load drinks from local JSON file (fallback)
     */
    private void loadDrinksFromLocal() {
        try {
            java.io.InputStream inputStream = getAssets().open("firebase_sample_data.json");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line).append("\n");
            }
            reader.close();
            inputStream.close();

            org.json.JSONObject jsonData = new org.json.JSONObject(jsonString.toString());
            org.json.JSONArray itemsArray = jsonData.getJSONArray("menuItems");
            
            availableDrinks.clear();
            for (int i = 0; i < itemsArray.length(); i++) {
                org.json.JSONObject itemJson = itemsArray.getJSONObject(i);
                String category = itemJson.optString("category", "");
                boolean isAvailable = itemJson.optBoolean("isAvailable", true);
                
                if ("drink".equals(category) && isAvailable) {
                    MenuItem drink = new MenuItem();
                    drink.setItemId(itemJson.getString("itemId"));
                    drink.setName(itemJson.getString("name"));
                    drink.setDescription(itemJson.optString("description", ""));
                    drink.setPrice(itemJson.getDouble("price"));
                    drink.setCategory(category);
                    drink.setAvailable(isAvailable);
                    availableDrinks.add(drink);
                }
            }
            
            Log.d(TAG, "Loaded " + availableDrinks.size() + " drinks from local JSON");
            drinkAdapter.notifyDataSetChanged();
            
            // Select first drink by default
            if (!availableDrinks.isEmpty()) {
                selectedDrink = availableDrinks.get(0);
                selectedDrinkType = "hot";
                drinkAdapter.notifyDataSetChanged();
                updatePriceDisplay();
            } else {
                Log.w(TAG, "No drinks found in local JSON either");
                Toast.makeText(this, "No drinks available. Please check the menu.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load drinks from local JSON", e);
            Toast.makeText(this, "Failed to load drinks: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updatePriceDisplay() {
        double basePrice = menuItem.getPrice();
        double drinkAddition = 0.0;
        String drinkText = "Hot Drink";

        if (selectedDrink != null) {
            // Iced drinks typically cost $3 more
            if ("iced".equals(selectedDrinkType)) {
                drinkAddition = 3.0;
                drinkText = selectedDrink.getName() + " (Iced)";
            } else {
                drinkAddition = 0.0;
                drinkText = selectedDrink.getName() + " (Hot)";
            }
        }

        double totalPrice = basePrice + drinkAddition;
        
        if (drinkAddition > 0) {
            totalPriceTextView.setText(String.format("Total: $%.2f (+$%.2f for iced drink)", 
                    totalPrice, drinkAddition));
        } else {
            totalPriceTextView.setText(String.format("Total: $%.2f", totalPrice));
        }
    }

    private void confirmSelection() {
        if (selectedDrink == null) {
            Toast.makeText(this, "Please select a drink", Toast.LENGTH_SHORT).show();
            return;
        }

        double drinkPriceAddition = "iced".equals(selectedDrinkType) ? 3.0 : 0.0;

        // Return result to MenuActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_MENU_ITEM, menuItem);
        resultIntent.putExtra(EXTRA_DRINK_NAME, selectedDrink.getName());
        resultIntent.putExtra(EXTRA_DRINK_TYPE, selectedDrinkType);
        resultIntent.putExtra(EXTRA_DRINK_PRICE_ADDITION, drinkPriceAddition);
        setResult(RESULT_OK, resultIntent);
        
        finish();
    }

    /**
     * Drink Adapter for RecyclerView
     */
    private class DrinkAdapter extends RecyclerView.Adapter<DrinkAdapter.DrinkViewHolder> {

        @NonNull
        @Override
        public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_drink_selection, parent, false);
            return new DrinkViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
            MenuItem drink = availableDrinks.get(position);
            holder.bind(drink);
        }

        @Override
        public int getItemCount() {
            return availableDrinks.size();
        }

        class DrinkViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView drinkNameTextView;
            TextView drinkPriceTextView;
            RadioGroup drinkTypeRadioGroup;
            RadioButton hotRadioButton;
            RadioButton icedRadioButton;

            DrinkViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (CardView) itemView;
                drinkNameTextView = itemView.findViewById(R.id.drinkNameTextView);
                drinkPriceTextView = itemView.findViewById(R.id.drinkPriceTextView);
                drinkTypeRadioGroup = itemView.findViewById(R.id.drinkTypeRadioGroup);
                hotRadioButton = itemView.findViewById(R.id.hotRadioButton);
                icedRadioButton = itemView.findViewById(R.id.icedRadioButton);
            }

            void bind(MenuItem drink) {
                // Extract drink base name (remove "(Hot / Iced)" suffix)
                String drinkName = drink.getName();
                if (drinkName.contains("(Hot / Iced)")) {
                    drinkName = drinkName.replace("(Hot / Iced)", "").trim();
                } else if (drinkName.contains("(Hot)")) {
                    drinkName = drinkName.replace("(Hot)", "").trim();
                }
                drinkNameTextView.setText(drinkName);
                
                // Format price
                double price = drink.getPrice();
                if (price == (int) price) {
                    drinkPriceTextView.setText(String.format("$%d", (int) price));
                } else {
                    drinkPriceTextView.setText(String.format("$%.2f", price));
                }

                // Check if this drink is selected
                boolean isSelected = selectedDrink != null && selectedDrink.getItemId().equals(drink.getItemId());
                
                // Clear any existing listeners to avoid conflicts
                drinkTypeRadioGroup.setOnCheckedChangeListener(null);
                
                // Set background color and enable/disable radio buttons based on selection
                if (isSelected) {
                    cardView.setCardBackgroundColor(0x30FF6B35); // Light orange background
                    cardView.setCardElevation(6f); // Higher elevation for selected
                    
                    // Enable radio buttons for selected drink
                    hotRadioButton.setEnabled(true);
                    icedRadioButton.setEnabled(true);
                    hotRadioButton.setAlpha(1.0f);
                    icedRadioButton.setAlpha(1.0f);
                    
                    // Set radio button selection based on selected type
                    if ("iced".equals(selectedDrinkType)) {
                        icedRadioButton.setChecked(true);
                    } else {
                        hotRadioButton.setChecked(true);
                    }
                } else {
                    cardView.setCardBackgroundColor(0xFFFFFFFF); // White background
                    cardView.setCardElevation(3f); // Normal elevation
                    
                    // Disable radio buttons for non-selected drinks
                    hotRadioButton.setEnabled(false);
                    icedRadioButton.setEnabled(false);
                    hotRadioButton.setAlpha(0.5f); // Dimmed appearance
                    icedRadioButton.setAlpha(0.5f); // Dimmed appearance
                    
                    // Clear radio button selection for unselected items
                    drinkTypeRadioGroup.clearCheck();
                }

                // Set click listener for the entire item (selects the drink)
                itemView.setOnClickListener(v -> {
                    // Only allow selection if not already selected
                    if (!isSelected) {
                        selectedDrink = drink;
                        selectedDrinkType = "hot"; // Default to hot when selecting
                        drinkAdapter.notifyDataSetChanged();
                        updatePriceDisplay();
                    }
                });

                // Set radio button listeners - only work when this drink is selected
                if (isSelected) {
                    drinkTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        // Double-check this drink is still selected (may have changed)
                        boolean currentlySelected = selectedDrink != null && selectedDrink.getItemId().equals(drink.getItemId());
                        if (currentlySelected) {
                            if (checkedId == hotRadioButton.getId()) {
                                selectedDrinkType = "hot";
                                updatePriceDisplay();
                            } else if (checkedId == icedRadioButton.getId()) {
                                selectedDrinkType = "iced";
                                updatePriceDisplay();
                            }
                        }
                    });
                }
            }
        }
    }
}

