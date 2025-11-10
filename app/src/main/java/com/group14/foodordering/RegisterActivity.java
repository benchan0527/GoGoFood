package com.group14.foodordering;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group14.foodordering.model.User;
import java.util.Locale;
import com.group14.foodordering.service.FirebaseDatabaseService;

import java.util.UUID;

/**
 * Registration screen for customers (test-data mode).
 * Creates a User document in Firestore via FirebaseDatabaseService without FirebaseAuth.
 */
public class RegisterActivity extends AppCompatActivity {

    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private Button registerButton;
    private ProgressBar progressBar;

    private FirebaseDatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbService = FirebaseDatabaseService.getInstance();
        setupViews();
    }

    private void setupViews() {
        nameInput = findViewById(R.id.inputName);
        emailInput = findViewById(R.id.inputEmail);
        phoneInput = findViewById(R.id.inputPhone);
        registerButton = findViewById(R.id.btnRegisterSubmit);
        progressBar = findViewById(R.id.progressBar);

        registerButton.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        if (!TextUtils.isEmpty(email)) {
            email = email.toLowerCase(Locale.ROOT);
        }
        String phone = phoneInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name required");
            return;
        }
        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Provide email or phone", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // Generate simple userId in test mode
        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId, email, name, phone, "customer");

        dbService.createOrUpdateUser(newUser, new FirebaseDatabaseService.DatabaseCallback() {
            @Override
            public void onSuccess(String documentId) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
    }
}


