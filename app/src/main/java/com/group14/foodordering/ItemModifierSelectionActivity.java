package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.group14.foodordering.model.ItemModifier;
import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.model.ModifierOption;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Item Modifier Selection Activity
 * Allows users to select modifiers (Size, Add-ons, Sides, Drinks, etc.) for menu items
 */
public class ItemModifierSelectionActivity extends AppCompatActivity {

    private static final String TAG = "ItemModifierSelection";
    public static final String EXTRA_MENU_ITEM = "menu_item";
    public static final String EXTRA_PRICE_ADDITION = "price_addition";
    public static final String EXTRA_MODIFIER_SELECTIONS = "modifier_selections";

    private MenuItem menuItem;
    private TextView itemNameTextView;
    private TextView basePriceTextView;
    private TextView totalPriceTextView;
    private TextView drinkSelectionLabel;
    private TextView modifiersLabel;
    private Button confirmButton;
    private RecyclerView modifiersRecyclerView;
    private RecyclerView drinksRecyclerView;
    private ModifierAdapter modifierAdapter;
    private DrinkAdapter drinkAdapter;
    private List<ItemModifier> itemModifiers;
    private List<MenuItem> availableDrinks;
    private MenuItem selectedDrink;
    private String selectedDrinkType; // "hot" or "iced"
    private Map<String, List<String>> selectedOptions; // modifierId -> list of selected option names
    private FirebaseDatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_modifier_selection);

        // Get menu item from intent
        menuItem = (MenuItem) getIntent().getSerializableExtra(EXTRA_MENU_ITEM);
        if (menuItem == null) {
            Log.e(TAG, "Menu item is null");
            Toast.makeText(this, "Error: Menu item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbService = FirebaseDatabaseService.getInstance();
        itemModifiers = new ArrayList<>();
        availableDrinks = new ArrayList<>();
        selectedOptions = new HashMap<>();
        selectedDrinkType = "hot"; // Default to hot

        setupViews();
        
        // Load drinks if item has drink option
        if (menuItem.isHasDrink()) {
            loadAvailableDrinks();
        }
        
        // Load modifiers if item has modifiers
        if (menuItem.getModifierIds() != null && !menuItem.getModifierIds().isEmpty()) {
            loadItemModifiers();
        }
        
        // If no drinks and no modifiers, show error
        if (!menuItem.isHasDrink() && (menuItem.getModifierIds() == null || menuItem.getModifierIds().isEmpty())) {
            Toast.makeText(this, "No options available for this item", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupViews() {
        itemNameTextView = findViewById(R.id.itemNameTextView);
        basePriceTextView = findViewById(R.id.basePriceTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        drinkSelectionLabel = findViewById(R.id.drinkSelectionLabel);
        modifiersLabel = findViewById(R.id.modifiersLabel);
        confirmButton = findViewById(R.id.confirmButton);
        modifiersRecyclerView = findViewById(R.id.modifiersRecyclerView);
        drinksRecyclerView = findViewById(R.id.drinksRecyclerView);

        // Set item name
        itemNameTextView.setText(menuItem.getName());
        
        // Set base price
        basePriceTextView.setText(String.format("Base Price: $%.2f", menuItem.getPrice()));

        // Setup Modifiers RecyclerView
        modifiersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        modifierAdapter = new ModifierAdapter();
        modifiersRecyclerView.setAdapter(modifierAdapter);
        
        // Setup Drinks RecyclerView (if needed)
        if (menuItem.isHasDrink()) {
            drinksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            drinkAdapter = new DrinkAdapter();
            drinksRecyclerView.setAdapter(drinkAdapter);
            drinkSelectionLabel.setVisibility(View.VISIBLE);
            drinksRecyclerView.setVisibility(View.VISIBLE);
        }

        // Confirm button click
        confirmButton.setOnClickListener(v -> confirmSelection());
        
        // Update price display initially
        updatePriceDisplay();
    }

    /**
     * Load item modifiers from database
     */
    private void loadItemModifiers() {
        if (menuItem.getModifierIds() == null || menuItem.getModifierIds().isEmpty()) {
            Log.w(TAG, "Menu item has no modifiers");
            // Don't show error if item has drinks - that's fine
            if (!menuItem.isHasDrink()) {
                Toast.makeText(this, "No options available for this item", Toast.LENGTH_SHORT).show();
                finish();
            }
            return;
        }

        // Load each modifier
        List<String> modifierIds = menuItem.getModifierIds();
        int totalModifiers = modifierIds.size();
        final int[] loadedCount = {0};

        for (String modifierId : modifierIds) {
            dbService.getFirestore().collection("itemModifiers")
                    .document(modifierId)
                    .get()
                    .addOnCompleteListener(task -> {
                        loadedCount[0]++;
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                            ItemModifier modifier = task.getResult().toObject(ItemModifier.class);
                            if (modifier != null) {
                                modifier.setModifierId(task.getResult().getId());
                                itemModifiers.add(modifier);
                                // Initialize selected options for this modifier
                                selectedOptions.put(modifier.getModifierId(), new ArrayList<>());
                                
                                // If required, select first option by default
                                if (modifier.isRequired() && !modifier.getOptions().isEmpty()) {
                                    ModifierOption firstOption = modifier.getOptions().get(0);
                                    selectedOptions.get(modifier.getModifierId()).add(firstOption.getOptionName());
                                }
                            }
                        } else {
                            Log.w(TAG, "Failed to load modifier: " + modifierId);
                        }

                        // When all modifiers are loaded, update UI
                        if (loadedCount[0] == totalModifiers) {
                            if (itemModifiers.isEmpty()) {
                                Log.w(TAG, "No modifiers found, trying local JSON fallback");
                                loadModifiersFromLocal();
                            } else {
                                modifiersLabel.setVisibility(View.VISIBLE);
                                modifierAdapter.notifyDataSetChanged();
                                updatePriceDisplay();
                            }
                        }
                    });
        }
    }

    /**
     * Load modifiers from local JSON file (fallback)
     */
    private void loadModifiersFromLocal() {
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
            org.json.JSONArray modifiersArray = jsonData.getJSONArray("itemModifiers");
            
            itemModifiers.clear();
            selectedOptions.clear();
            
            for (int i = 0; i < modifiersArray.length(); i++) {
                org.json.JSONObject modifierJson = modifiersArray.getJSONObject(i);
                String modifierId = modifierJson.getString("modifierId");
                
                // Check if this modifier applies to the current menu item
                if (menuItem.getModifierIds() != null && menuItem.getModifierIds().contains(modifierId)) {
                    ItemModifier modifier = new ItemModifier();
                    modifier.setModifierId(modifierId);
                    modifier.setModifierGroup(modifierJson.getString("modifierGroup"));
                    
                    // Parse menuItemIds
                    org.json.JSONArray menuItemIdsArray = modifierJson.getJSONArray("menuItemIds");
                    List<String> menuItemIds = new ArrayList<>();
                    for (int j = 0; j < menuItemIdsArray.length(); j++) {
                        menuItemIds.add(menuItemIdsArray.getString(j));
                    }
                    modifier.setMenuItemIds(menuItemIds);
                    
                    // Parse options
                    org.json.JSONArray optionsArray = modifierJson.getJSONArray("options");
                    List<ModifierOption> options = new ArrayList<>();
                    for (int j = 0; j < optionsArray.length(); j++) {
                        org.json.JSONObject optionJson = optionsArray.getJSONObject(j);
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
                    
                    itemModifiers.add(modifier);
                    selectedOptions.put(modifierId, new ArrayList<>());
                    
                    // If required, select first option by default
                    if (modifier.isRequired() && !modifier.getOptions().isEmpty()) {
                        ModifierOption firstOption = modifier.getOptions().get(0);
                        selectedOptions.get(modifierId).add(firstOption.getOptionName());
                    }
                }
            }
            
            Log.d(TAG, "Loaded " + itemModifiers.size() + " modifiers from local JSON");
            if (!itemModifiers.isEmpty()) {
                modifiersLabel.setVisibility(View.VISIBLE);
            }
            modifierAdapter.notifyDataSetChanged();
            updatePriceDisplay();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load modifiers from local JSON", e);
            Toast.makeText(this, "Failed to load modifiers", Toast.LENGTH_SHORT).show();
        }
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
                            for (QueryDocumentSnapshot document : task.getResult()) {
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
                                            for (QueryDocumentSnapshot document : task2.getResult()) {
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
        if (drinkAdapter != null) {
            drinkAdapter.notifyDataSetChanged();
        }
        
        // Select first drink by default
        if (!availableDrinks.isEmpty()) {
            selectedDrink = availableDrinks.get(0);
            selectedDrinkType = "hot";
            if (drinkAdapter != null) {
                drinkAdapter.notifyDataSetChanged();
            }
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
            org.json.JSONArray menuItemsArray = jsonData.getJSONArray("menuItems");
            
            availableDrinks.clear();
            for (int i = 0; i < menuItemsArray.length(); i++) {
                org.json.JSONObject itemJson = menuItemsArray.getJSONObject(i);
                String category = itemJson.getString("category");
                if (category.equals("drink") || category.equals("drinks")) {
                    MenuItem drink = new MenuItem();
                    drink.setItemId(itemJson.getString("itemId"));
                    drink.setName(itemJson.getString("name"));
                    drink.setPrice(itemJson.getDouble("price"));
                    drink.setAvailable(itemJson.getBoolean("isAvailable"));
                    availableDrinks.add(drink);
                }
            }
            
            if (availableDrinks.isEmpty()) {
                Log.w(TAG, "No drinks found in local JSON");
                return;
            }
            
            Log.d(TAG, "Loaded " + availableDrinks.size() + " drinks from local JSON");
            updateDrinkList();
        } catch (Exception e) {
            Log.e(TAG, "Failed to load drinks from local JSON", e);
            Toast.makeText(this, "Failed to load drinks", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePriceDisplay() {
        double basePrice = menuItem.getPrice();
        double totalAddition = 0.0;

        // Calculate price addition from drink
        if (menuItem.isHasDrink()) {
            // If drink is provided, add based on temperature
            if (selectedDrink != null) {
                if ("iced".equals(selectedDrinkType)) {
                    totalAddition += 3.0; // Iced drink adds $3
                } else {
                    totalAddition += 0.0; // Hot drink adds $0
                }
            }
        } else {
            // If drink is not provided, add fixed $15
            totalAddition += 15.0;
        }

        // Calculate total price addition from all selected modifiers
        for (ItemModifier modifier : itemModifiers) {
            List<String> selected = selectedOptions.get(modifier.getModifierId());
            if (selected != null) {
                for (String optionName : selected) {
                    // Find the option and add its price
                    for (ModifierOption option : modifier.getOptions()) {
                        if (option.getOptionName().equals(optionName) && option.isAvailable()) {
                            totalAddition += option.getAdditionalPrice();
                            break;
                        }
                    }
                }
            }
        }

        double totalPrice = basePrice + totalAddition;
        
        if (totalAddition > 0) {
            totalPriceTextView.setText(String.format("Total: $%.2f (+$%.2f)", totalPrice, totalAddition));
        } else {
            totalPriceTextView.setText(String.format("Total: $%.2f", totalPrice));
        }
    }

    private void confirmSelection() {
        // Validate required modifiers
        for (ItemModifier modifier : itemModifiers) {
            if (modifier.isRequired()) {
                List<String> selected = selectedOptions.get(modifier.getModifierId());
                if (selected == null || selected.isEmpty()) {
                    Toast.makeText(this, "Please select at least one option for " + modifier.getModifierGroup(), 
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            // Validate min/max selections
            List<String> selected = selectedOptions.get(modifier.getModifierId());
            if (selected != null) {
                int minSelections = modifier.getMinSelections();
                int maxSelections = modifier.getMaxSelections();
                
                if (selected.size() < minSelections) {
                    Toast.makeText(this, 
                            "Please select at least " + minSelections + " option(s) for " + modifier.getModifierGroup(), 
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (maxSelections > 0 && selected.size() > maxSelections) {
                    Toast.makeText(this, 
                            "Please select at most " + maxSelections + " option(s) for " + modifier.getModifierGroup(), 
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Calculate total price addition
        double totalAddition = 0.0;
        
        // Add drink price based on hasDrink flag
        if (menuItem.isHasDrink()) {
            // If drink is provided, add based on temperature
            if (selectedDrink != null) {
                if ("iced".equals(selectedDrinkType)) {
                    totalAddition += 3.0; // Iced drink adds $3
                } else {
                    totalAddition += 0.0; // Hot drink adds $0
                }
            }
        } else {
            // If drink is not provided, add fixed $15
            totalAddition += 15.0;
        }
        
        // Add modifier prices
        for (ItemModifier modifier : itemModifiers) {
            List<String> selected = selectedOptions.get(modifier.getModifierId());
            if (selected != null) {
                for (String optionName : selected) {
                    for (ModifierOption option : modifier.getOptions()) {
                        if (option.getOptionName().equals(optionName) && option.isAvailable()) {
                            totalAddition += option.getAdditionalPrice();
                            break;
                        }
                    }
                }
            }
        }

        // Create modifier selections map
        Map<String, Object> modifierSelections = new HashMap<>();
        
        // Add drink selection if selected
        if (selectedDrink != null) {
            List<String> drinkSelection = new ArrayList<>();
            String drinkName = selectedDrink.getName();
            // Remove "(Hot / Iced)" suffix if present
            if (drinkName.contains("(Hot / Iced)")) {
                drinkName = drinkName.replace("(Hot / Iced)", "").trim();
            } else if (drinkName.contains("(Hot)")) {
                drinkName = drinkName.replace("(Hot)", "").trim();
            }
            drinkSelection.add(drinkName + " (" + (selectedDrinkType.equals("hot") ? "Hot" : "Iced") + ")");
            modifierSelections.put("Drink", drinkSelection);
        }
        
        // Add modifier selections
        for (ItemModifier modifier : itemModifiers) {
            List<String> selected = selectedOptions.get(modifier.getModifierId());
            if (selected != null && !selected.isEmpty()) {
                modifierSelections.put(modifier.getModifierGroup(), selected);
            }
        }

        // Return result to MenuActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_MENU_ITEM, menuItem);
        resultIntent.putExtra(EXTRA_PRICE_ADDITION, totalAddition);
        resultIntent.putExtra(EXTRA_MODIFIER_SELECTIONS, (Serializable) modifierSelections);
        setResult(RESULT_OK, resultIntent);
        
        finish();
    }

    /**
     * Modifier Adapter for RecyclerView
     */
    private class ModifierAdapter extends RecyclerView.Adapter<ModifierAdapter.ModifierViewHolder> {

        @NonNull
        @Override
        public ModifierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_modifier_selection, parent, false);
            return new ModifierViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ModifierViewHolder holder, int position) {
            ItemModifier modifier = itemModifiers.get(position);
            holder.bind(modifier);
        }

        @Override
        public int getItemCount() {
            return itemModifiers.size();
        }

        class ModifierViewHolder extends RecyclerView.ViewHolder {
            TextView modifierGroupTextView;
            TextView modifierRequiredTextView;
            RecyclerView optionsRecyclerView;
            OptionAdapter optionAdapter;

            ModifierViewHolder(@NonNull View itemView) {
                super(itemView);
                modifierGroupTextView = itemView.findViewById(R.id.modifierGroupTextView);
                modifierRequiredTextView = itemView.findViewById(R.id.modifierRequiredTextView);
                optionsRecyclerView = itemView.findViewById(R.id.optionsRecyclerView);
            }

            void bind(ItemModifier modifier) {
                modifierGroupTextView.setText(modifier.getModifierGroup());
                
                // Show required indicator
                if (modifier.isRequired()) {
                    modifierRequiredTextView.setText("Required");
                    modifierRequiredTextView.setVisibility(TextView.VISIBLE);
                } else {
                    modifierRequiredTextView.setText("Optional");
                    modifierRequiredTextView.setVisibility(TextView.VISIBLE);
                }

                // Setup options RecyclerView
                optionsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                optionAdapter = new OptionAdapter(modifier);
                optionsRecyclerView.setAdapter(optionAdapter);
            }
        }
    }

    /**
     * Option Adapter for modifier options
     */
    private class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.OptionViewHolder> {
        private ItemModifier modifier;
        private List<ModifierOption> availableOptions;

        OptionAdapter(ItemModifier modifier) {
            this.modifier = modifier;
            this.availableOptions = new ArrayList<>();
            for (ModifierOption option : modifier.getOptions()) {
                if (option.isAvailable()) {
                    availableOptions.add(option);
                }
            }
        }

        @NonNull
        @Override
        public OptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_modifier_option, parent, false);
            return new OptionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OptionViewHolder holder, int position) {
            ModifierOption option = availableOptions.get(position);
            holder.bind(option);
        }

        @Override
        public int getItemCount() {
            return availableOptions.size();
        }

        private void notifyItemRangeChanged(ItemModifier modifier) {
            // Notify the parent adapter to refresh all modifiers
            ItemModifierSelectionActivity.this.modifierAdapter.notifyDataSetChanged();
        }

        class OptionViewHolder extends RecyclerView.ViewHolder {
            RadioButton radioButton;
            CheckBox checkBox;
            TextView optionNameTextView;
            TextView optionPriceTextView;

            OptionViewHolder(@NonNull View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.optionRadioButton);
                checkBox = itemView.findViewById(R.id.optionCheckBox);
                optionNameTextView = itemView.findViewById(R.id.optionNameTextView);
                optionPriceTextView = itemView.findViewById(R.id.optionPriceTextView);
            }

            void bind(ModifierOption option) {
                optionNameTextView.setText(option.getOptionName());
                
                // Format price
                double price = option.getAdditionalPrice();
                if (price == 0.0) {
                    optionPriceTextView.setText("");
                } else if (price == (int) price) {
                    optionPriceTextView.setText(String.format("+$%d", (int) price));
                } else {
                    optionPriceTextView.setText(String.format("+$%.2f", price));
                }

                // Determine if single or multiple selection
                // Single selection: maxSelections == 1, or if required and minSelections == 1 and maxSelections <= 1
                int maxSelections = modifier.getMaxSelections();
                boolean isSingleSelection = maxSelections == 1;

                // Show radio button for single selection, checkbox for multiple
                if (isSingleSelection) {
                    radioButton.setVisibility(android.view.View.VISIBLE);
                    checkBox.setVisibility(android.view.View.GONE);
                    
                    // Check if this option is selected
                    List<String> selected = selectedOptions.get(modifier.getModifierId());
                    boolean isSelected = selected != null && selected.contains(option.getOptionName());
                    radioButton.setChecked(isSelected);
                    
                    // Set click listener on the entire item view for better UX
                    itemView.setOnClickListener(v -> {
                        // Clear other selections for this modifier
                        selectedOptions.put(modifier.getModifierId(), new ArrayList<>());
                        selectedOptions.get(modifier.getModifierId()).add(option.getOptionName());
                        // Notify all items in this modifier group to update
                        notifyItemRangeChanged(modifier);
                        updatePriceDisplay();
                    });
                } else {
                    checkBox.setVisibility(android.view.View.VISIBLE);
                    radioButton.setVisibility(android.view.View.GONE);
                    
                    // Check if this option is selected
                    List<String> selected = selectedOptions.get(modifier.getModifierId());
                    boolean isSelected = selected != null && selected.contains(option.getOptionName());
                    checkBox.setChecked(isSelected);
                    
                    // Set click listener on the entire item view for better UX
                    itemView.setOnClickListener(v -> {
                        List<String> selectedList = selectedOptions.get(modifier.getModifierId());
                        if (selectedList == null) {
                            selectedList = new ArrayList<>();
                            selectedOptions.put(modifier.getModifierId(), selectedList);
                        }
                        
                        boolean wasSelected = selectedList.contains(option.getOptionName());
                        
                        if (!wasSelected) {
                            // Check max selections
                            int maxSelectionsCheck = modifier.getMaxSelections();
                            if (maxSelectionsCheck > 0 && selectedList.size() >= maxSelectionsCheck) {
                                Toast.makeText(itemView.getContext(), 
                                        "Maximum " + maxSelectionsCheck + " selection(s) allowed", 
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            selectedList.add(option.getOptionName());
                            checkBox.setChecked(true);
                        } else {
                            selectedList.remove(option.getOptionName());
                            checkBox.setChecked(false);
                        }
                        updatePriceDisplay();
                    });
                }
            }
        }
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
                // Extract drink base name and determine if iced option is available
                String drinkName = drink.getName();
                boolean hasIcedOption = drinkName.contains("(Hot / Iced)");
                boolean hasHotOnly = drinkName.contains("(Hot)") && !drinkName.contains("(Hot / Iced)");
                
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

                // Always show temperature options, but disable unavailable ones
                drinkTypeRadioGroup.setVisibility(View.VISIBLE);
                hotRadioButton.setVisibility(View.VISIBLE);
                icedRadioButton.setVisibility(View.VISIBLE);
                
                // Check if this drink is selected
                boolean isSelected = selectedDrink != null && selectedDrink.getItemId().equals(drink.getItemId());
                
                // Clear any existing listeners to avoid conflicts
                drinkTypeRadioGroup.setOnCheckedChangeListener(null);
                
                // Set background color and enable/disable radio buttons based on selection and availability
                if (isSelected) {
                    cardView.setCardBackgroundColor(0x30FF6B35); // Light orange background
                    cardView.setCardElevation(6f); // Higher elevation for selected
                    
                    // Enable radio buttons only for selected drink, based on availability
                    if (hasIcedOption) {
                        // Both options available
                        hotRadioButton.setEnabled(true);
                        icedRadioButton.setEnabled(true);
                        hotRadioButton.setAlpha(1.0f);
                        icedRadioButton.setAlpha(1.0f);
                    } else if (hasHotOnly) {
                        // Only Hot available, disable Iced
                        hotRadioButton.setEnabled(true);
                        icedRadioButton.setEnabled(false);
                        hotRadioButton.setAlpha(1.0f);
                        icedRadioButton.setAlpha(0.5f); // Dimmed appearance
                    } else {
                        // Default: both available
                        hotRadioButton.setEnabled(true);
                        icedRadioButton.setEnabled(true);
                        hotRadioButton.setAlpha(1.0f);
                        icedRadioButton.setAlpha(1.0f);
                    }
                    
                    // Set radio button selection based on selected type
                    drinkTypeRadioGroup.setOnCheckedChangeListener(null); // Clear listener to avoid recursion
                    if ("iced".equals(selectedDrinkType) && hasIcedOption) {
                        icedRadioButton.setChecked(true);
                    } else {
                        hotRadioButton.setChecked(true);
                    }
                    // If no iced option, ensure selectedDrinkType is "hot"
                    if (!hasIcedOption) {
                        selectedDrinkType = "hot";
                        hotRadioButton.setChecked(true);
                    }
                } else {
                    cardView.setCardBackgroundColor(0xFFFFFFFF); // White background
                    cardView.setCardElevation(3f); // Normal elevation
                    // Disable ALL radio buttons for unselected drinks (regardless of availability)
                    hotRadioButton.setEnabled(false);
                    icedRadioButton.setEnabled(false);
                    hotRadioButton.setAlpha(0.5f); // Dimmed appearance
                    icedRadioButton.setAlpha(0.5f); // Dimmed appearance
                    drinkTypeRadioGroup.setOnCheckedChangeListener(null); // Clear listener
                    drinkTypeRadioGroup.clearCheck(); // Clear selection for unselected items
                }

                // Set click listener for the entire item (selects the drink)
                itemView.setOnClickListener(v -> {
                    // Only allow selection if not already selected
                    if (!isSelected) {
                        selectedDrink = drink;
                        // If drink doesn't have iced option, force hot
                        if (!hasIcedOption) {
                            selectedDrinkType = "hot";
                        } else {
                            selectedDrinkType = "hot"; // Default to hot when selecting
                        }
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
                            } else if (checkedId == icedRadioButton.getId() && hasIcedOption) {
                                // Only allow iced if it's available
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

