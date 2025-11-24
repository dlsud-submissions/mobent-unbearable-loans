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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmployeeId, etFirstName, etMiddleInitial, etLastName;
    private EditText etDateHired, etBasicSalary, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnSignIn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        updateFieldHints();
    }

    private void initializeViews() {
        // Map the existing fields
        etEmployeeId = findViewById(R.id.etEmployeeId);
        etBasicSalary = findViewById(R.id.etBasicSalary);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    private void updateFieldHints() {
        // Update hints to match employee registration fields
        etEmployeeId.setHint("Employee ID");
        etEmployeeId.setInputType(android.text.InputType.TYPE_CLASS_TEXT);

        etBasicSalary.setHint("Basic Salary");
        etBasicSalary.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        etPassword.setHint("Password");
        etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        etConfirmPassword.setHint("Confirm Password");
        etConfirmPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerEmployee();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLogin();
            }
        });
    }

    private void registerEmployee() {
        String employeeId = etEmployeeId.getText().toString().trim();
        String basicSalaryStr = etBasicSalary.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (employeeId.isEmpty()) {
            etEmployeeId.setError("Please enter employee ID");
            etEmployeeId.requestFocus();
            return;
        }

        if (basicSalaryStr.isEmpty()) {
            etBasicSalary.setError("Please enter basic salary");
            etBasicSalary.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            etPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        double basicSalary;
        try {
            basicSalary = Double.parseDouble(basicSalaryStr);
            if (basicSalary <= 0) {
                etBasicSalary.setError("Basic salary must be positive");
                etBasicSalary.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etBasicSalary.setError("Invalid salary format");
            etBasicSalary.requestFocus();
            return;
        }

        String firstName = "First";
        String middleInitial = null;
        String lastName = "Last";

        // Use current date as date hired
        String dateHired = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Register employee using DatabaseHelper
        boolean success = dbHelper.registerEmployee(
                employeeId,
                firstName,
                middleInitial,
                lastName,
                dateHired,
                password,
                basicSalary
        );

        if (success) {
            showToast("Registration successful!");
            navigateToHome(employeeId);
        } else {
            showToast("Registration failed. Employee ID might already exist.");
            etEmployeeId.requestFocus();
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome(String employeeId) {
        Intent intent = new Intent(RegisterActivity.this, HomeEmployeeActivity.class);
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