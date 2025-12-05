package com.example.loansystemcalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;
import android.text.Editable;
import android.text.TextWatcher;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmployeeId, etPassword;
    private MaterialButton btnSignIn, btnRegister;
    private DatabaseHelper dbHelper;

    // Mascot animation variables
    private ImageView mascotImage;
    private boolean isPasswordFocused = false;

    // Username mascot images (0-20+ characters, changes every 3 characters)
    // This gives you 8 images: 0, 3, 6, 9, 12, 15, 18, 21+
    private int[] usernameImages = {
            R.drawable.mascot_0,    // 0-2 characters
            R.drawable.mascot_1,    // 3-5 characters
            R.drawable.mascot_2,    // 6-8 characters
            R.drawable.mascot_3,    // 9-11 characters
            R.drawable.mascot_4,    // 12-14 characters
            R.drawable.mascot_5,    // 15-17 characters
            R.drawable.mascot_6,    // 18-20 characters
            R.drawable.mascot_7     // 21+ characters (stays at this)
    };

    // Password mascot images
    private int[] passwordImages = {
            R.drawable.mascot_password_0
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupMascotAnimation();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmployeeId = findViewById(R.id.etEmployeeId);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize mascot image only
        mascotImage = findViewById(R.id.mascotImage);

        // Change hints to match employee login
        etEmployeeId.setHint("Employee ID");
        etPassword.setHint("Password");

        // Change input type for employee ID
        etEmployeeId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
    }

    private void setupMascotAnimation() {
        // Employee ID text watcher
        etEmployeeId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();

                // Update mascot image if not password focused
                if (!isPasswordFocused) {
                    updateMascotImage(length, false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password text watcher
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();

                // Update mascot image if password focused
                if (isPasswordFocused) {
                    updateMascotImage(length, true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Employee ID focus listener
        etEmployeeId.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isPasswordFocused = false;
                updateMascotImage(etEmployeeId.getText().length(), false);
            } else if (!etPassword.hasFocus()) {
                if (mascotImage != null && usernameImages.length > 0) {
                    mascotImage.setImageResource(usernameImages[0]);
                }
            }
        });

        // Password focus listener
        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                isPasswordFocused = true;
                updateMascotImage(etPassword.getText().length(), true);
            } else if (!etEmployeeId.hasFocus()) {
                if (mascotImage != null && usernameImages.length > 0) {
                    mascotImage.setImageResource(usernameImages[0]);
                }
            } else {
                isPasswordFocused = false;
                updateMascotImage(etEmployeeId.getText().length(), false);
            }
        });
    }

    // Changes image every 3 characters, stays at last image when 21+ characters
    private void updateMascotImage(int characterCount, boolean isPassword) {
        if (mascotImage == null) return;

        // Select appropriate image array
        int[] imagesToUse = isPassword ? passwordImages : usernameImages;

        // Safety check: Make sure array is not empty
        if (imagesToUse.length == 0) return;

        // Divide character count by 3 to change image every 3 characters
        int imageIndex = characterCount / 3;

        // For username: Keep last image when at 21+ characters (index 7)
        // For password: Keep last image when exceeding array length
        if (!isPassword && characterCount >= 21) {
            imageIndex = usernameImages.length - 1; // Stay at last image (mascot_7)
        } else {
            // Make sure we don't exceed array bounds
            imageIndex = Math.min(imageIndex, imagesToUse.length - 1);
        }

        // Change the image safely
        try {
            mascotImage.setImageResource(imagesToUse[imageIndex]);
        } catch (Exception e) {
            // Fallback to first image if something goes wrong
            mascotImage.setImageResource(imagesToUse[0]);
        }
    }

    private void setupClickListeners() {
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginEmployee();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegistration();
            }
        });
    }

    private void loginEmployee() {
        String employeeId = etEmployeeId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (employeeId.isEmpty()) {
            etEmployeeId.setError("Please enter employee ID");
            etEmployeeId.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Validate credentials using DatabaseHelper
        boolean isValid = dbHelper.validateEmployeeLogin(employeeId, password);

        if (isValid) {
            showToast("Login successful!");
            navigateToHome(employeeId);
        } else {
            showToast("Invalid employee ID or password");
            etPassword.setText(""); // Clear password field
            etPassword.requestFocus();
        }
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome(String employeeId) {
        Intent intent = new Intent(LoginActivity.this, HomeEmployeeActivity.class);
        intent.putExtra("EMPLOYEE_ID", employeeId);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
