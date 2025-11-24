package com.example.loansystemcalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;

public class HomeEmployeeActivity extends AppCompatActivity {

    private TextView tvWelcomeEmployee, tvEmployeeId;
    private CardView cardApplyLoan, cardTransactionHistory;
    private View btnLogout;
    private String employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_employee);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get employee ID from intent
        employeeId = getIntent().getStringExtra("EMPLOYEE_ID");
        if (employeeId == null) {
            navigateToEmployeeLogin();
            return;
        }

        initializeViews();
        setupClickListeners();
        updateUI();
    }

    private void initializeViews() {
        tvWelcomeEmployee = findViewById(R.id.tvWelcomeEmployee);
        tvEmployeeId = findViewById(R.id.tvEmployeeId);
        cardApplyLoan = findViewById(R.id.cardApplyLoan);
        cardTransactionHistory = findViewById(R.id.cardTransactionHistory);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupClickListeners() {
        cardApplyLoan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLoanApplicationForm();
            }
        });

        cardTransactionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLoanApplicationsHistory();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void updateUI() {
        tvWelcomeEmployee.setText("Welcome, Employee!");
        tvEmployeeId.setText("Employee ID: " + employeeId);
    }

//    private void navigateToLoanApplicationForm() {
//        Intent intent = new Intent(HomeEmployeeActivity.this, EmployeeLoanApplicationFormActivity.class);
//        intent.putExtra("EMPLOYEE_ID", employeeId);
//        startActivity(intent);
//    }
//
//    private void navigateToLoanApplicationsHistory() {
//        Intent intent = new Intent(HomeEmployeeActivity.this, EmployeeLoanApplicationsHistoryActivity.class);
//        intent.putExtra("EMPLOYEE_ID", employeeId);
//        startActivity(intent);
//    }

    private void navigateToEmployeeLogin() {
        Intent intent = new Intent(HomeEmployeeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        Intent intent = new Intent(HomeEmployeeActivity.this, LandingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        logout();
    }
}