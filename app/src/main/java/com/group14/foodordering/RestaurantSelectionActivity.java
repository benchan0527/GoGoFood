package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.model.Restaurant;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.RestaurantPreferenceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Restaurant Selection Activity
 * Allows customers to select a restaurant before ordering
 */
public class RestaurantSelectionActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantSelectionActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView restaurantsRecyclerView;
    private RestaurantAdapter adapter;
    private List<Restaurant> restaurants;
    private TextView emptyTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_selection);

        dbService = FirebaseDatabaseService.getInstance();
        restaurants = new ArrayList<>();

        setupViews();
        setupRecyclerView();
        loadRestaurants();
    }

    private void setupViews() {
        restaurantsRecyclerView = findViewById(R.id.restaurantsRecyclerView);
        emptyTextView = findViewById(R.id.emptyTextView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new RestaurantAdapter(restaurants, restaurant -> {
            // Save selected restaurant
            RestaurantPreferenceHelper.setSelectedRestaurantId(this, restaurant.getRestaurantId());
            RestaurantPreferenceHelper.setSelectedRestaurantName(this, restaurant.getRestaurantName());
            
            Log.d(TAG, "Restaurant selected: " + restaurant.getRestaurantName());
            Toast.makeText(this, "Selected: " + restaurant.getRestaurantName(), Toast.LENGTH_SHORT).show();
            
            // Check if this was called from MenuActivity (via startActivityForResult)
            // If so, return result to MenuActivity, otherwise go to MainActivity
            if (getCallingActivity() != null && 
                getCallingActivity().getClassName().equals(MenuActivity.class.getName())) {
                // Return result to calling activity
                setResult(RESULT_OK);
                finish();
            } else {
                // Return to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
        restaurantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        restaurantsRecyclerView.setAdapter(adapter);
    }

    /**
     * Load restaurants from Firebase
     */
    private void loadRestaurants() {
        progressBar.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        
        dbService.getAllRestaurants(new FirebaseDatabaseService.RestaurantsCallback() {
            @Override
            public void onSuccess(List<Restaurant> restaurantList) {
                progressBar.setVisibility(View.GONE);
                restaurants.clear();
                restaurants.addAll(restaurantList);
                adapter.notifyDataSetChanged();
                
                if (restaurants.isEmpty()) {
                    emptyTextView.setVisibility(View.VISIBLE);
                } else {
                    emptyTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Failed to load restaurants", e);
                Toast.makeText(RestaurantSelectionActivity.this, 
                        "Failed to load restaurants: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                emptyTextView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Restaurant Adapter
     */
    private static class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {
        private List<Restaurant> restaurants;
        private OnRestaurantClickListener listener;

        interface OnRestaurantClickListener {
            void onRestaurantClick(Restaurant restaurant);
        }

        RestaurantAdapter(List<Restaurant> restaurants, OnRestaurantClickListener listener) {
            this.restaurants = restaurants;
            this.listener = listener;
        }

        @NonNull
        @Override
        public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_restaurant, parent, false);
            return new RestaurantViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
            Restaurant restaurant = restaurants.get(position);
            holder.bind(restaurant, listener);
        }

        @Override
        public int getItemCount() {
            return restaurants.size();
        }

        static class RestaurantViewHolder extends RecyclerView.ViewHolder {
            TextView restaurantNameTextView;
            TextView restaurantAddressTextView;
            TextView restaurantPhoneTextView;

            RestaurantViewHolder(@NonNull View itemView) {
                super(itemView);
                restaurantNameTextView = itemView.findViewById(R.id.restaurantNameTextView);
                restaurantAddressTextView = itemView.findViewById(R.id.restaurantAddressTextView);
                restaurantPhoneTextView = itemView.findViewById(R.id.restaurantPhoneTextView);
            }

            void bind(Restaurant restaurant, OnRestaurantClickListener listener) {
                restaurantNameTextView.setText(restaurant.getRestaurantName());
                restaurantAddressTextView.setText(restaurant.getAddress());
                restaurantPhoneTextView.setText(restaurant.getPhoneNumber());
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRestaurantClick(restaurant);
                    }
                });
            }
        }
    }
}

