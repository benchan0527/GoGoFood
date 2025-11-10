package com.group14.foodordering;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.group14.foodordering.model.MenuItem;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.UUID;

/**
 * Editor screen to create or edit a menu item (UC-9)
 */
public class MenuItemEditorActivity extends AppCompatActivity {

	private FirebaseDatabaseService dbService;

	private EditText etName;
	private EditText etDescription;
	private EditText etPrice;
	private EditText etCategory;
	private EditText etImageUrl;
	private Switch switchAvailable;
	private Switch switchHasDrink;
	private EditText etStock;
	private Button btnSave;

	private String editingItemId;
	private MenuItem editingItem;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_item_editor);

		dbService = FirebaseDatabaseService.getInstance();

		etName = findViewById(R.id.etName);
		etDescription = findViewById(R.id.etDescription);
		etPrice = findViewById(R.id.etPrice);
		etCategory = findViewById(R.id.etCategory);
		etImageUrl = findViewById(R.id.etImageUrl);
		switchAvailable = findViewById(R.id.switchAvailable);
		switchHasDrink = findViewById(R.id.switchHasDrink);
		etStock = findViewById(R.id.etStock);
		btnSave = findViewById(R.id.btnSave);

		editingItemId = getIntent().getStringExtra("itemId");
		if (editingItemId != null && !editingItemId.isEmpty()) {
			loadItem(editingItemId);
		}

		btnSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveItem();
			}
		});
	}

	private void loadItem(String itemId) {
		dbService.getMenuItemById(itemId, new FirebaseDatabaseService.MenuItemCallback() {
			@Override
			public void onSuccess(MenuItem item) {
				if (item == null) return;
				editingItem = item;
				etName.setText(item.getName());
				etDescription.setText(item.getDescription());
				etPrice.setText(String.valueOf(item.getPrice()));
				etCategory.setText(item.getCategory());
				etImageUrl.setText(item.getImageUrl());
				switchAvailable.setChecked(item.isAvailable());
				switchHasDrink.setChecked(item.isHasDrink());
				etStock.setText(String.valueOf(item.getStock()));
			}

			@Override
			public void onFailure(Exception e) {
				Toast.makeText(MenuItemEditorActivity.this, "Load failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void saveItem() {
		String name = etName.getText().toString().trim();
		String priceStr = etPrice.getText().toString().trim();
		String category = etCategory.getText().toString().trim();

		if (TextUtils.isEmpty(name)) {
			etName.setError("Name required");
			return;
		}
		double price = 0;
		try {
			price = Double.parseDouble(priceStr);
		} catch (Exception ignored) {}
		if (price < 0) {
			etPrice.setError("Price must be >= 0");
			return;
		}
		if (TextUtils.isEmpty(category)) {
			etCategory.setError("Category required");
			return;
		}

		MenuItem item = editingItem != null ? editingItem : new MenuItem();
		if (editingItem == null) {
			String id = UUID.randomUUID().toString();
			item.setItemId(id);
			item.setCreatedAt(System.currentTimeMillis());
		}
		item.setName(name);
		item.setDescription(etDescription.getText().toString().trim());
		item.setPrice(price);
		item.setCategory(category);
		item.setImageUrl(etImageUrl.getText().toString().trim());
		item.setAvailable(switchAvailable.isChecked());
		item.setHasDrink(switchHasDrink.isChecked());
		try {
			item.setStock(Integer.parseInt(etStock.getText().toString().trim()));
		} catch (Exception e) {
			item.setStock(-1);
		}
		item.setUpdatedAt(System.currentTimeMillis());

		dbService.createOrUpdateMenuItem(item, new FirebaseDatabaseService.DatabaseCallback() {
		 @Override
		 public void onSuccess(String documentId) {
			 Toast.makeText(MenuItemEditorActivity.this, "Saved", Toast.LENGTH_SHORT).show();
			 finish();
		 }
		 @Override
		 public void onFailure(Exception e) {
			 Toast.makeText(MenuItemEditorActivity.this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
		 }
		});
	}
}


