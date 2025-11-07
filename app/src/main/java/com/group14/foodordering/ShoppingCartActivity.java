package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.model.MenuItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shopping Cart Activity
 * Allows users to view and edit cart items
 */
public class ShoppingCartActivity extends AppCompatActivity {

    private static final String TAG = "ShoppingCartActivity";
    private RecyclerView cartRecyclerView;
    private TextView totalTextView;
    private TextView emptyCartTextView;
    private Button checkoutButton;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private Map<String, MenuItem> menuItemMap;
    private double totalPrice;

    // Cart data
    private Map<String, Integer> cart;
    private Map<String, Double> cartDrinkAdditions;
    private List<MenuItem> allMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // Initialize collections
        cart = new HashMap<>();
        cartDrinkAdditions = new HashMap<>();
        allMenuItems = new ArrayList<>();
        menuItemMap = new HashMap<>();
        cartItems = new ArrayList<>();

        setupViews();
        loadCartData();
    }

    private void setupViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalTextView = findViewById(R.id.totalTextView);
        emptyCartTextView = findViewById(R.id.emptyCartTextView);
        checkoutButton = findViewById(R.id.checkoutButton);

        // Setup RecyclerView - but don't set adapter yet, wait for data
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cartRecyclerView.setLayoutManager(layoutManager);
        cartRecyclerView.setHasFixedSize(false); // Allow dynamic sizing
        cartRecyclerView.setNestedScrollingEnabled(true);
        
        // Ensure RecyclerView is visible initially
        cartRecyclerView.setVisibility(View.VISIBLE);
        
        // Ensure RecyclerView can measure itself
        cartRecyclerView.setClipToPadding(false);
        cartRecyclerView.setClipChildren(false);
        
        // Debug initial state
        Log.d(TAG, "RecyclerView initialized - visibility: " + cartRecyclerView.getVisibility());
        Log.d(TAG, "RecyclerView ID: " + cartRecyclerView.getId());
        
        // Wait for layout to complete before setting up RecyclerView
        cartRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cartRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.d(TAG, "Layout complete - RecyclerView size: " + cartRecyclerView.getWidth() + "x" + cartRecyclerView.getHeight());
                
                // Force layout if needed
                if (cartRecyclerView.getWidth() == 0 || cartRecyclerView.getHeight() == 0) {
                    Log.w(TAG, "RecyclerView still has 0 dimensions, forcing layout");
                    cartRecyclerView.requestLayout();
                } else if (cartAdapter != null && cartItems.size() > 0) {
                    // Layout is complete and we have data, refresh the adapter
                    Log.d(TAG, "Layout complete with proper dimensions, refreshing adapter");
                    cartAdapter.notifyDataSetChanged();
                }
            }
        });

        // Back button
        Button backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Checkout button - just returns to MenuActivity
        checkoutButton.setOnClickListener(v -> {
            // Update cart in MenuActivity before returning
            MenuActivity menuActivity = MenuActivity.getInstance();
            if (menuActivity != null) {
                menuActivity.updateCart(cart, cartDrinkAdditions);
            }
            
            // Return to MenuActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("cart_updated", true);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Load cart data from Intent or MenuActivity
     */
    @SuppressWarnings("unchecked")
    private void loadCartData() {
        Intent intent = getIntent();
        boolean loadedFromIntent = false;
        
        // Try to load from Intent first
        if (intent != null) {
            try {
                Serializable cartObj = intent.getSerializableExtra("cart");
                Serializable cartDrinkObj = intent.getSerializableExtra("cartDrinkAdditions");
                Serializable menuItemsObj = intent.getSerializableExtra("allMenuItems");
                
                if (cartObj instanceof Map) {
                    Map<String, Integer> intentCart = (Map<String, Integer>) cartObj;
                    if (!intentCart.isEmpty()) {
                        cart = new HashMap<>(intentCart);
                        loadedFromIntent = true;
                        Log.d(TAG, "Loaded cart from Intent: " + cart.size() + " items");
                    }
                }
                if (cartDrinkObj instanceof Map) {
                    Map<String, Double> intentDrinkAdditions = (Map<String, Double>) cartDrinkObj;
                    if (!intentDrinkAdditions.isEmpty()) {
                        cartDrinkAdditions = new HashMap<>(intentDrinkAdditions);
                    }
                }
                if (menuItemsObj instanceof List) {
                    List<MenuItem> intentMenuItems = (List<MenuItem>) menuItemsObj;
                    if (!intentMenuItems.isEmpty()) {
                        allMenuItems = new ArrayList<>(intentMenuItems);
                        Log.d(TAG, "Loaded menu items from Intent: " + allMenuItems.size() + " items");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading from Intent", e);
            }
        }
        
        // Fallback to MenuActivity instance if Intent didn't have data
        if (!loadedFromIntent || cart.isEmpty() || allMenuItems == null || allMenuItems.isEmpty()) {
            MenuActivity menuActivity = MenuActivity.getInstance();
            if (menuActivity != null) {
                Map<String, Integer> menuCart = menuActivity.getCart();
                Map<String, Double> menuDrinkAdditions = menuActivity.getCartDrinkAdditions();
                List<MenuItem> menuItems = menuActivity.getAllMenuItems();
                
                if (menuCart != null && !menuCart.isEmpty()) {
                    cart = menuCart;
                }
                if (menuDrinkAdditions != null && !menuDrinkAdditions.isEmpty()) {
                    cartDrinkAdditions = menuDrinkAdditions;
                }
                if (menuItems != null && !menuItems.isEmpty()) {
                    allMenuItems = menuItems;
                }
                Log.d(TAG, "Loaded from MenuActivity: cart=" + (cart != null ? cart.size() : 0) + ", menuItems=" + (allMenuItems != null ? allMenuItems.size() : 0));
            }
        }
        
        // Ensure not null
        if (cart == null) cart = new HashMap<>();
        if (cartDrinkAdditions == null) cartDrinkAdditions = new HashMap<>();
        if (allMenuItems == null) allMenuItems = new ArrayList<>();
        
        // Build menu item map for quick lookup
        menuItemMap.clear();
        for (MenuItem item : allMenuItems) {
            if (item != null && item.getItemId() != null) {
                menuItemMap.put(item.getItemId(), item);
            }
        }
        
        Log.d(TAG, "Menu item map size: " + menuItemMap.size());
        Log.d(TAG, "Cart size: " + cart.size());
        
        // Build and display cart items
        buildCartItems();
        
        Log.d(TAG, "After buildCartItems: cartItems.size() = " + cartItems.size());
        
        // Create and set adapter after data is loaded
        cartAdapter = new CartAdapter();
        cartRecyclerView.setAdapter(cartAdapter);
        
        Log.d(TAG, "Adapter set. Item count: " + cartAdapter.getItemCount());
        Log.d(TAG, "RecyclerView adapter after setting: " + cartRecyclerView.getAdapter());
        
        updateDisplay();
        
        // Force RecyclerView to measure and layout after adapter is set
        cartRecyclerView.postDelayed(() -> {
            if (cartAdapter != null && cartAdapter.getItemCount() > 0) {
                Log.d(TAG, "Delayed refresh - forcing RecyclerView to update");
                cartAdapter.notifyDataSetChanged();
                cartRecyclerView.requestLayout();
                cartRecyclerView.invalidate();
            }
        }, 100);
        
        // Post a runnable to ensure RecyclerView is refreshed after layout
        cartRecyclerView.post(() -> {
            // Debug RecyclerView state
            Log.d(TAG, "RecyclerView visibility: " + cartRecyclerView.getVisibility());
            Log.d(TAG, "RecyclerView width: " + cartRecyclerView.getWidth() + ", height: " + cartRecyclerView.getHeight());
            Log.d(TAG, "RecyclerView measured: " + cartRecyclerView.getMeasuredWidth() + "x" + cartRecyclerView.getMeasuredHeight());
            Log.d(TAG, "RecyclerView layout manager: " + cartRecyclerView.getLayoutManager());
            Log.d(TAG, "RecyclerView adapter: " + cartRecyclerView.getAdapter());
            Log.d(TAG, "Adapter item count: " + (cartAdapter != null ? cartAdapter.getItemCount() : "null"));
            Log.d(TAG, "Cart items size: " + cartItems.size());
            
            if (cartAdapter != null && cartItems.size() > 0) {
                // Force a full refresh
                cartAdapter.notifyDataSetChanged();
                Log.d(TAG, "Post-layout refresh: " + cartItems.size() + " items");
                
                // Force layout pass
                cartRecyclerView.requestLayout();
                cartRecyclerView.invalidate();
                
                // Ensure RecyclerView is visible
                cartRecyclerView.setVisibility(View.VISIBLE);
                
                // Try scrolling to position 0 to trigger rendering
                if (cartAdapter.getItemCount() > 0) {
                    cartRecyclerView.scrollToPosition(0);
                    Log.d(TAG, "Scrolled to position 0");
                }
            } else {
                Log.w(TAG, "Cannot refresh: adapter=" + (cartAdapter != null ? "not null" : "null") + 
                      ", cartItems.size()=" + cartItems.size());
            }
        });
    }

    /**
     * Build cart items list from cart map
     */
    private void buildCartItems() {
        cartItems.clear();
        
        if (cart == null || cart.isEmpty()) {
            Log.d(TAG, "Cart is empty");
            return;
        }
        
        Log.d(TAG, "Building cart items from " + cart.size() + " entries");
        
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();
            
            if (quantity <= 0) {
                continue;
            }
            
            // Find menu item
            MenuItem menuItem = menuItemMap.get(itemId);
            if (menuItem == null) {
                // Try searching in allMenuItems
                for (MenuItem item : allMenuItems) {
                    if (item != null && item.getItemId() != null && item.getItemId().equals(itemId)) {
                        menuItem = item;
                        menuItemMap.put(itemId, item);
                        break;
                    }
                }
            }
            
            if (menuItem != null) {
                double drinkAddition = cartDrinkAdditions != null ? cartDrinkAdditions.getOrDefault(itemId, 0.0) : 0.0;
                CartItem cartItem = new CartItem(menuItem, quantity, drinkAddition);
                cartItems.add(cartItem);
                Log.d(TAG, "Added: " + menuItem.getName() + " x" + quantity);
            } else {
                Log.w(TAG, "Menu item not found for ID: " + itemId);
            }
        }
        
        Log.d(TAG, "Built " + cartItems.size() + " cart items");
    }

    /**
     * Update quantity for a cart item
     */
    private void updateQuantity(String itemId, int newQuantity) {
        if (newQuantity <= 0) {
            cart.remove(itemId);
            cartDrinkAdditions.remove(itemId);
        } else {
            cart.put(itemId, newQuantity);
        }
        
        // Update MenuActivity
        MenuActivity menuActivity = MenuActivity.getInstance();
        if (menuActivity != null) {
            menuActivity.updateCart(cart, cartDrinkAdditions);
        }
        
        buildCartItems();
        updateDisplay();
    }

    /**
     * Remove item from cart
     */
    private void removeItem(String itemId) {
        cart.remove(itemId);
        cartDrinkAdditions.remove(itemId);
        
        // Update MenuActivity
        MenuActivity menuActivity = MenuActivity.getInstance();
        if (menuActivity != null) {
            menuActivity.updateCart(cart, cartDrinkAdditions);
        }
        
        buildCartItems();
        updateDisplay();
    }

    /**
     * Update display
     */
    private void updateDisplay() {
        // Calculate total
        totalPrice = 0.0;
        for (CartItem item : cartItems) {
            totalPrice += item.getTotalPrice();
        }
        
        // Update total text
        if (totalTextView != null) {
            totalTextView.setText(String.format("Total: $%.2f", totalPrice));
        }
        
        // Update visibility
        if (cartItems.isEmpty()) {
            if (cartRecyclerView != null) {
                cartRecyclerView.setVisibility(View.GONE);
            }
            if (emptyCartTextView != null) {
                emptyCartTextView.setVisibility(View.VISIBLE);
            }
            if (checkoutButton != null) {
                checkoutButton.setEnabled(false);
            }
        } else {
            if (cartRecyclerView != null) {
                cartRecyclerView.setVisibility(View.VISIBLE);
            }
            if (emptyCartTextView != null) {
                emptyCartTextView.setVisibility(View.GONE);
            }
            if (checkoutButton != null) {
                checkoutButton.setEnabled(true);
            }
        }
        
        // Notify adapter
        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
            Log.d(TAG, "Adapter notified. Item count: " + cartItems.size());
            
            // Force RecyclerView to refresh
            if (cartRecyclerView != null && cartItems.size() > 0) {
                cartRecyclerView.post(() -> {
                    cartRecyclerView.invalidate();
                    cartRecyclerView.requestLayout();
                    if (cartRecyclerView.getAdapter() != null && cartRecyclerView.getAdapter().getItemCount() > 0) {
                        cartRecyclerView.scrollToPosition(0);
                    }
                });
            }
        } else {
            Log.e(TAG, "Adapter is null!");
        }
    }

    /**
     * Cart item data class
     */
    private static class CartItem {
        private MenuItem menuItem;
        private int quantity;
        private double drinkAddition;

        CartItem(MenuItem menuItem, int quantity, double drinkAddition) {
            this.menuItem = menuItem;
            this.quantity = quantity;
            this.drinkAddition = drinkAddition;
        }

        double getTotalPrice() {
            return (menuItem.getPrice() + drinkAddition) * quantity;
        }

        double getUnitPrice() {
            return menuItem.getPrice() + drinkAddition;
        }
    }

    /**
     * RecyclerView Adapter for cart items
     */
    private class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

        @NonNull
        @Override
        public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder called");
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cart, parent, false);
            if (view == null) {
                Log.e(TAG, "Failed to inflate item_cart layout!");
            } else {
                Log.d(TAG, "Successfully inflated item_cart layout, view: " + view.getClass().getSimpleName());
            }
            return new CartViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
            if (position >= 0 && position < cartItems.size()) {
                CartItem cartItem = cartItems.get(position);
                Log.d(TAG, "Binding item at position " + position + ": " + 
                    (cartItem != null && cartItem.menuItem != null ? cartItem.menuItem.getName() : "null"));
                holder.bind(cartItem);
            } else {
                Log.e(TAG, "Invalid position in onBindViewHolder: " + position + ", size: " + cartItems.size());
            }
        }

        @Override
        public int getItemCount() {
            int count = cartItems != null ? cartItems.size() : 0;
            Log.d(TAG, "getItemCount() called, returning: " + count);
            return count;
        }

        /**
         * ViewHolder for cart items
         */
        class CartViewHolder extends RecyclerView.ViewHolder {
            private TextView itemNameTextView;
            private TextView itemDescriptionTextView;
            private TextView itemPriceTextView;
            private TextView itemSubtotalTextView;
            private TextView quantityTextView;
            private TextView customizationTextView;
            private Button decreaseButton;
            private Button increaseButton;
            private Button removeButton;

            CartViewHolder(@NonNull View itemView) {
                super(itemView);
                itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
                itemDescriptionTextView = itemView.findViewById(R.id.itemDescriptionTextView);
                itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
                itemSubtotalTextView = itemView.findViewById(R.id.itemSubtotalTextView);
                quantityTextView = itemView.findViewById(R.id.quantityTextView);
                customizationTextView = itemView.findViewById(R.id.customizationTextView);
                decreaseButton = itemView.findViewById(R.id.decreaseButton);
                increaseButton = itemView.findViewById(R.id.increaseButton);
                removeButton = itemView.findViewById(R.id.removeButton);
                
                // Debug: Log if any views are null
                if (itemNameTextView == null) Log.e(TAG, "itemNameTextView is NULL!");
                if (itemDescriptionTextView == null) Log.e(TAG, "itemDescriptionTextView is NULL!");
                if (itemPriceTextView == null) Log.e(TAG, "itemPriceTextView is NULL!");
                if (itemSubtotalTextView == null) Log.e(TAG, "itemSubtotalTextView is NULL!");
                if (quantityTextView == null) Log.e(TAG, "quantityTextView is NULL!");
                if (customizationTextView == null) Log.e(TAG, "customizationTextView is NULL!");
                if (decreaseButton == null) Log.e(TAG, "decreaseButton is NULL!");
                if (increaseButton == null) Log.e(TAG, "increaseButton is NULL!");
                if (removeButton == null) Log.e(TAG, "removeButton is NULL!");
                
                Log.d(TAG, "ViewHolder created - all views found: " + 
                    (itemNameTextView != null && itemPriceTextView != null && 
                     itemSubtotalTextView != null && quantityTextView != null));
            }

            void bind(CartItem cartItem) {
                Log.d(TAG, "bind() called for position " + getAdapterPosition());
                if (cartItem == null || cartItem.menuItem == null) {
                    Log.e(TAG, "CartItem or MenuItem is null in bind()");
                    return;
                }
                
                MenuItem menuItem = cartItem.menuItem;
                String itemName = menuItem.getName() != null ? menuItem.getName() : "Unknown Item";
                Log.d(TAG, "Binding item: " + itemName + ", quantity: " + cartItem.quantity);
                
                // Set item name
                if (itemNameTextView != null) {
                    itemNameTextView.setText(itemName);
                }
                
                // Set item description
                if (itemDescriptionTextView != null) {
                    String description = menuItem.getDescription();
                    if (description != null && !description.trim().isEmpty()) {
                        itemDescriptionTextView.setText(description);
                        itemDescriptionTextView.setVisibility(View.VISIBLE);
                    } else {
                        itemDescriptionTextView.setVisibility(View.GONE);
                    }
                }
                
                // Set unit price
                if (itemPriceTextView != null) {
                    double unitPrice = cartItem.getUnitPrice();
                    String priceText = String.format("$%.2f", unitPrice);
                    itemPriceTextView.setText(priceText);
                }
                
                // Set subtotal
                if (itemSubtotalTextView != null) {
                    double subtotal = cartItem.getTotalPrice();
                    String subtotalText = String.format("$%.2f", subtotal);
                    itemSubtotalTextView.setText(subtotalText);
                }
                
                // Set quantity
                if (quantityTextView != null) {
                    String qtyText = String.valueOf(cartItem.quantity);
                    quantityTextView.setText(qtyText);
                }
                
                // Set customization/drink info
                if (customizationTextView != null) {
                    if (cartItem.drinkAddition > 0) {
                        customizationTextView.setText("Cold Drink (+$" + String.format("%.2f", cartItem.drinkAddition) + ")");
                        customizationTextView.setVisibility(View.VISIBLE);
                    } else if (menuItem.isHasDrink()) {
                        customizationTextView.setText("Hot Drink");
                        customizationTextView.setVisibility(View.VISIBLE);
                    } else {
                        customizationTextView.setVisibility(View.GONE);
                    }
                }

                // Set up quantity controls
                if (decreaseButton != null) {
                    decreaseButton.setOnClickListener(v -> {
                        int newQuantity = cartItem.quantity - 1;
                        updateQuantity(menuItem.getItemId(), newQuantity);
                    });
                }

                if (increaseButton != null) {
                    increaseButton.setOnClickListener(v -> {
                        int newQuantity = cartItem.quantity + 1;
                        updateQuantity(menuItem.getItemId(), newQuantity);
                    });
                }

                // Set up remove button
                if (removeButton != null) {
                    removeButton.setOnClickListener(v -> {
                        removeItem(menuItem.getItemId());
                    });
                }
            }
        }
    }
}
