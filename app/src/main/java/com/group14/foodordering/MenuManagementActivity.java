package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.PermissionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager screen to view and manage menu items (UC-9)
 */
public class MenuManagementActivity extends AppCompatActivity {

	private FirebaseDatabaseService dbService;
	private RecyclerView recyclerView;
	private ItemsAdapter adapter;
	private View emptyView;
	private View progressView;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_management);

		if (!PermissionManager.canEditMenu(this)) {
			Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		dbService = FirebaseDatabaseService.getInstance();
		recyclerView = findViewById(R.id.recyclerViewMenuItems);
		emptyView = findViewById(R.id.emptyView);
		progressView = findViewById(R.id.progressBar);

		adapter = new ItemsAdapter();
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);

		loadItems();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_management_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (item.getItemId() == R.id.action_add_item) {
			Intent intent = new Intent(this, MenuItemEditorActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Refresh list after returning from editor
		loadItems();
	}

	private void loadItems() {
		progressView.setVisibility(View.VISIBLE);
		dbService.getAllMenuItems(new FirebaseDatabaseService.MenuItemsCallback() {
			@Override
			public void onSuccess(List<com.group14.foodordering.model.MenuItem> items) {
				progressView.setVisibility(View.GONE);
				adapter.setItems(items);
				emptyView.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
			}

			@Override
			public void onFailure(Exception e) {
				progressView.setVisibility(View.GONE);
				Toast.makeText(MenuManagementActivity.this, "Failed to load items: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private class ItemsAdapter extends RecyclerView.Adapter<ItemViewHolder> {
		private final List<com.group14.foodordering.model.MenuItem> items = new ArrayList<>();

		void setItems(List<com.group14.foodordering.model.MenuItem> newItems) {
			items.clear();
			if (newItems != null) items.addAll(newItems);
			notifyDataSetChanged();
		}

		@NonNull
		@Override
		public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_manage, parent, false);
			return new ItemViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
			holder.bind(items.get(position));
		}

		@Override
		public int getItemCount() {
			return items.size();
		}
	}

	private class ItemViewHolder extends RecyclerView.ViewHolder {
		private final TextView tvName;
		private final TextView tvPrice;
		private final TextView tvCategory;
		private final TextView tvAvailabilityStatus;
		private final TextView tvDrinkInfo;
		private final Switch switchAvailable;
		private final ImageButton btnEdit;
		private final ImageButton btnDelete;
		private CompoundButton.OnCheckedChangeListener checkedChangeListener;

		ItemViewHolder(@NonNull View itemView) {
			super(itemView);
			tvName = itemView.findViewById(R.id.tvItemName);
			tvPrice = itemView.findViewById(R.id.tvItemPrice);
			tvCategory = itemView.findViewById(R.id.tvItemCategory);
			tvAvailabilityStatus = itemView.findViewById(R.id.tvAvailabilityStatus);
			tvDrinkInfo = itemView.findViewById(R.id.tvDrinkInfo);
			switchAvailable = itemView.findViewById(R.id.switchAvailable);
			btnEdit = itemView.findViewById(R.id.btnEdit);
			btnDelete = itemView.findViewById(R.id.btnDelete);
		}

		void bind(com.group14.foodordering.model.MenuItem item) {
			tvName.setText(item.getName());
			tvPrice.setText(String.format("$%.2f", item.getPrice()));
			
			// Display category/type with proper formatting
			String category = item.getCategory() != null && !item.getCategory().isEmpty() ? item.getCategory() : "Unknown";
			// Capitalize first letter and replace underscores with spaces
			if (category.length() > 1) {
				category = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase().replace("_", " ");
			} else {
				category = category.toUpperCase();
			}
			tvCategory.setText("Type: " + category);
			// Set category badge color (blue-ish)
			tvCategory.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_blue_dark));
			
			// Display availability status
			boolean isAvailable = item.isAvailable();
			switchAvailable.setOnCheckedChangeListener(null);
			switchAvailable.setChecked(isAvailable);
			
			// Update availability badge
			updateAvailabilityBadge(isAvailable);
			
			// Display drink information
			boolean hasDrink = item.isHasDrink();
			if (hasDrink) {
				tvDrinkInfo.setText("✓ Includes Drink");
				tvDrinkInfo.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
			} else {
				tvDrinkInfo.setText("No Drink");
				tvDrinkInfo.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
			}

			checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					item.setAvailable(isChecked);
					updateAvailabilityBadge(isChecked);
					dbService.createOrUpdateMenuItem(item, new FirebaseDatabaseService.DatabaseCallback() {
						@Override
						public void onSuccess(String documentId) {
							Toast.makeText(MenuManagementActivity.this, "Availability updated", Toast.LENGTH_SHORT).show();
						}

						@Override
						public void onFailure(Exception e) {
							Toast.makeText(MenuManagementActivity.this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
							// Revert switch on failure
							switchAvailable.setOnCheckedChangeListener(null);
							switchAvailable.setChecked(!isChecked);
							updateAvailabilityBadge(!isChecked);
							switchAvailable.setOnCheckedChangeListener(checkedChangeListener);
						}
					});
				}
			};
			switchAvailable.setOnCheckedChangeListener(checkedChangeListener);

			btnEdit.setOnClickListener(v -> {
				Intent intent = new Intent(MenuManagementActivity.this, MenuItemEditorActivity.class);
				intent.putExtra("itemId", item.getItemId());
				startActivity(intent);
			});

			btnDelete.setOnClickListener(v -> {
				dbService.deleteMenuItem(item.getItemId(), new FirebaseDatabaseService.DatabaseCallback() {
					@Override
					public void onSuccess(String documentId) {
						Toast.makeText(MenuManagementActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
						loadItems();
					}

					@Override
					public void onFailure(Exception e) {
						Toast.makeText(MenuManagementActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			});
		}
		
		private void updateAvailabilityBadge(boolean isAvailable) {
			if (isAvailable) {
				tvAvailabilityStatus.setText("✓ Available");
				tvAvailabilityStatus.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
			} else {
				tvAvailabilityStatus.setText("✗ Unavailable");
				tvAvailabilityStatus.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
			}
		}
	}
}


