package com.group14.foodordering;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.group14.foodordering.model.User;
import com.group14.foodordering.service.FirebaseDatabaseService;
import com.group14.foodordering.util.AdminSessionHelper;
import com.group14.foodordering.util.CustomerSessionHelper;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private FirebaseDatabaseService dbService;
    private EditText emailOrPhoneInput;
    private EditText passwordInput;
    private TextView loginButton;
    private TextView cancelButton;
    private TextView registerLink;
    private TextView forgotLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbService = FirebaseDatabaseService.getInstance();

        emailOrPhoneInput = findViewById(R.id.inputEmailOrPhone);
        passwordInput = findViewById(R.id.inputPassword);
        loginButton = findViewById(R.id.buttonLogin);
        cancelButton = findViewById(R.id.buttonCancel);
        registerLink = findViewById(R.id.linkRegister);
        forgotLink = findViewById(R.id.linkForgot);

        // Ensure correct input types
        emailOrPhoneInput.setInputType(InputType.TYPE_CLASS_TEXT);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        loginButton.setOnClickListener(v -> {
            String emailOrPhone = emailOrPhoneInput.getText().toString().trim();
            if (emailOrPhone.contains("@")) {
                emailOrPhone = emailOrPhone.toLowerCase(Locale.ROOT);
            }
            String password = passwordInput.getText().toString().trim();
            if (emailOrPhone.isEmpty()) {
                Toast.makeText(this, "Email or Phone is required", Toast.LENGTH_SHORT).show();
                return;
            }
            loginCustomer(emailOrPhone, password);
        });

        cancelButton.setOnClickListener(v -> {
            // Go back to customer main
            Intent intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        forgotLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginCustomer(String emailOrPhone, String password) {
        dbService.getUserByEmailOrPhone(emailOrPhone, new FirebaseDatabaseService.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    tryAdminLogin(emailOrPhone, password);
                    return;
                }

                String role = user.getRole();
                if ("manager".equals(role) || "server".equals(role) || "kitchen".equals(role)) {
                    Toast.makeText(LoginActivity.this,
                            "Please use Admin Login for staff accounts", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                CustomerSessionHelper.saveCustomerSession(LoginActivity.this, user);

                // Go to member page
                Intent intent = new Intent(LoginActivity.this, MemberActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

                Toast.makeText(LoginActivity.this,
                        "Login successful: " + user.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                tryAdminLogin(emailOrPhone, password);
            }
        });
    }

    private void tryAdminLogin(String emailOrPhone, String password) {
        dbService.getAdminByStaffIdOrPhone(emailOrPhone, new FirebaseDatabaseService.AdminCallback() {
            @Override
            public void onSuccess(com.group14.foodordering.model.Admin admin) {
                if (admin == null) {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                AdminSessionHelper.saveAdminSession(LoginActivity.this, admin);

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(LoginActivity.this,
                        "Admin login successful: " + admin.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Admin login failed", e);
                String errorMessage = e.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "User not found";
                }
                Toast.makeText(LoginActivity.this,
                        "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}


