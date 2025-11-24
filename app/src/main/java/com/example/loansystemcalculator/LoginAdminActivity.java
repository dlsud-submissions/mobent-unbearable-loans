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
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

public class LoginAdminActivity extends AppCompatActivity {

    private EditText etAdminId, etPassword;
    private MaterialButton btnLogin;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etAdminId = findViewById(R.id.etAdminId);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAdmin();
            }
        });
    }

    private void loginAdmin() {
        String adminId = etAdminId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (adminId.isEmpty()) {
            etAdminId.setError("Please enter admin ID");
            etAdminId.requestFocus();
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
        boolean isValid = dbHelper.validateAdminLogin(adminId, password);

        if (isValid) {
            showToast("Admin login successful!");
            navigateToAdminHome(adminId);
        } else {
            showToast("Invalid admin ID or password");
            etPassword.setText(""); // Clear password field
            etPassword.requestFocus();
        }
    }

    private void navigateToLanding() {
        Intent intent = new Intent(LoginAdminActivity.this, LandingActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminHome(String adminId) {
        Intent intent = new Intent(LoginAdminActivity.this, HomeAdminActivity.class);
        intent.putExtra("ADMIN_ID", adminId);
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