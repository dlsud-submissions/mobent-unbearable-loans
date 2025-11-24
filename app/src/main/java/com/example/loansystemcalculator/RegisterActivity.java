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

    private EditText etFirstName, etMiddleInitial, etLastName;
    private EditText etBasicSalary, etPassword, etConfirmPassword;
    private MaterialButton btnRegister, btnSignIn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        updateFieldHints();
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etMiddleInitial = findViewById(R.id.etMiddleInitial);
        etBasicSalary = findViewById(R.id.etBasicSalary);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnSignIn = findViewById(R.id.btnSignIn);
    }

    private void updateFieldHints() {
        etFirstName.setHint("First Name");
        etFirstName.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        etLastName.setHint("Last Name");
        etLastName.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        etMiddleInitial.setHint("Middle Initial");
        etMiddleInitial.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

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
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String middleInitial = etMiddleInitial.getText().toString().trim();
        String basicSalaryStr = etBasicSalary.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("Please enter first name");
            etFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Please enter last name");
            etLastName.requestFocus();
            return;
        }

        if (middleInitial.length() > 1) {
            etMiddleInitial.setError("Middle initial should be one character only");
            etMiddleInitial.requestFocus();
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

        // Use current date as date hired
        String dateHired = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // If middle initial is empty, set to null
        if (middleInitial.isEmpty()) {
            middleInitial = null;
        }

        // Register employee using DatabaseHelper (auto-generates employee ID)
        boolean success = dbHelper.registerEmployee(
                firstName,
                middleInitial,
                lastName,
                dateHired,
                password,
                basicSalary
        );

        if (success) {
            // Generate the same employee ID to display to user
            String generatedEmployeeId = dbHelper.generateEmployeeId(firstName, lastName, middleInitial);
            showToast("Registration successful! Your Employee ID: " + generatedEmployeeId);
            navigateToHome(generatedEmployeeId);
        } else {
            showToast("Registration failed. Please try again.");
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}