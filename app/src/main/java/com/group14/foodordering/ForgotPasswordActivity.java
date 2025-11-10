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
import com.group14.foodordering.service.FirebaseDatabaseService;
import java.util.Locale;

/**
 * Forgot Password (test-data mode).
 * Since FirebaseAuth is not integrated, we simulate a reset by verifying the user exists
 * and showing a confirmation message. No actual password handling is stored.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailOrPhoneInput;
    private Button resetButton;
    private ProgressBar progressBar;
    private FirebaseDatabaseService dbService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbService = FirebaseDatabaseService.getInstance();
        setupViews();
    }

    private void setupViews() {
        emailOrPhoneInput = findViewById(R.id.inputEmailOrPhone);
        resetButton = findViewById(R.id.btnResetSubmit);
        progressBar = findViewById(R.id.progressBar);

        resetButton.setOnClickListener(v -> attemptReset());
    }

    private void attemptReset() {
        String emailOrPhone = emailOrPhoneInput.getText().toString().trim();
        // Normalize email to lowercase; numbers remain unaffected
        if (emailOrPhone.contains("@")) {
            emailOrPhone = emailOrPhone.toLowerCase(Locale.ROOT);
        }
        if (TextUtils.isEmpty(emailOrPhone)) {
            emailOrPhoneInput.setError("Email or phone required");
            return;
        }

        setLoading(true);

        // In test mode, just check existence
        dbService.getUserByEmailOrPhone(emailOrPhone, new FirebaseDatabaseService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                setLoading(false);
                if (user == null) {
                    Toast.makeText(ForgotPasswordActivity.this, "Account not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForgotPasswordActivity.this, "Reset link sent (test mode)", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Exception e) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, "Lookup failed: " + (e != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        resetButton.setEnabled(!loading);
    }
}


