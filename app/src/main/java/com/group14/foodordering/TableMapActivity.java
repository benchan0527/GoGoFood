package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.ListenerRegistration;
import com.group14.foodordering.model.Table;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.ArrayList;
import java.util.List;

/**
 * Table Map Activity
 * GUI #14: POS Main Dashboard / Table Map Screen
 * Displays all restaurant tables with their status in a visual grid
 */
public class TableMapActivity extends AppCompatActivity {

    private static final String TAG = "TableMapActivity";
    private FirebaseDatabaseService dbService;
    private RecyclerView tablesRecyclerView;
    private TablesAdapter tablesAdapter;
    private List<Table> tables;
    private ListenerRegistration tablesListener;
    private String branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_map);

        dbService = FirebaseDatabaseService.getInstance();
        tables = new ArrayList<>();

        setupViews();
        // Use default branch ID from test data
        // In production, this should be retrieved from selected restaurant
        branchId = "branch_001";
        loadTables();
    }

    private void setupViews() {
        tablesRecyclerView = findViewById(R.id.tablesRecyclerView);
        
        // Use GridLayoutManager for table grid view
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3); // 3 columns
        tablesRecyclerView.setLayoutManager(layoutManager);
        
        tablesAdapter = new TablesAdapter();
        tablesRecyclerView.setAdapter(tablesAdapter);

        // Setup search orders button
        Button searchOrdersButton = findViewById(R.id.searchOrdersButton);
        searchOrdersButton.setOnClickListener(v -> {
            Intent intent = new Intent(TableMapActivity.this, OrderSearchActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Load tables from Firebase
     */
    private void loadTables() {
        // Use real-time listener for live updates
        tablesListener = dbService.listenToTablesByBranchId(branchId, new FirebaseDatabaseService.TablesCallback() {
            @Override
            public void onSuccess(List<Table> tablesList) {
                tables.clear();
                tables.addAll(tablesList);
                tablesAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + tables.size() + " tables");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to load tables", e);
                Toast.makeText(TableMapActivity.this, "Failed to load tables: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handle table click - navigate to TableOrderActivity
     */
    private void onTableClick(Table table) {
        Intent intent = new Intent(TableMapActivity.this, TableOrderActivity.class);
        intent.putExtra("tableNumber", table.getTableNumber());
        intent.putExtra("tableId", table.getTableId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tablesListener != null) {
            tablesListener.remove();
        }
    }

    /**
     * Tables RecyclerView Adapter
     */
    private class TablesAdapter extends RecyclerView.Adapter<TablesAdapter.TableViewHolder> {

        @NonNull
        @Override
        public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_table, parent, false);
            return new TableViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
            Table table = tables.get(position);
            holder.bind(table);
        }

        @Override
        public int getItemCount() {
            return tables.size();
        }

        class TableViewHolder extends RecyclerView.ViewHolder {
            View tableCard;
            TextView tableNumberTextView;
            TextView statusTextView;
            TextView capacityTextView;
            TextView orderIdTextView;

            TableViewHolder(@NonNull View itemView) {
                super(itemView);
                tableCard = itemView.findViewById(R.id.tableCard);
                tableNumberTextView = itemView.findViewById(R.id.tableNumberTextView);
                statusTextView = itemView.findViewById(R.id.statusTextView);
                capacityTextView = itemView.findViewById(R.id.capacityTextView);
                orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            }

            void bind(Table table) {
                if (table == null) {
                    return;
                }

                // Set table number
                String tableNumber = table.getTableNumber() != null ? table.getTableNumber() : "N/A";
                tableNumberTextView.setText(tableNumber);

                // Set capacity
                capacityTextView.setText("Capacity: " + table.getCapacity());

                // Set status with color coding
                String status = table.getStatus() != null ? table.getStatus() : "available";
                statusTextView.setText(status.toUpperCase());
                
                // Set background color based on status
                int backgroundColor;
                int textColor;
                switch (status) {
                    case "occupied":
                        backgroundColor = 0xFFF44336; // Red
                        textColor = 0xFFFFFFFF; // White
                        break;
                    case "needs_cleaning":
                        backgroundColor = 0xFFFFEB3B; // Yellow
                        textColor = 0xFF000000; // Black
                        break;
                    case "available":
                    default:
                        backgroundColor = 0xFF4CAF50; // Green
                        textColor = 0xFFFFFFFF; // White
                        break;
                }
                tableCard.setBackgroundColor(backgroundColor);
                statusTextView.setTextColor(textColor);

                // Show order ID if table is occupied
                if ("occupied".equals(status) && table.getCurrentOrderId() != null && 
                    !table.getCurrentOrderId().isEmpty()) {
                    String orderId = table.getCurrentOrderId();
                    if (orderId.length() > 8) {
                        orderIdTextView.setText("Order: " + orderId.substring(orderId.length() - 8));
                    } else {
                        orderIdTextView.setText("Order: " + orderId);
                    }
                    orderIdTextView.setVisibility(View.VISIBLE);
                } else {
                    orderIdTextView.setVisibility(View.GONE);
                }

                // Set click listener
                itemView.setOnClickListener(v -> onTableClick(table));
            }
        }
    }
}

