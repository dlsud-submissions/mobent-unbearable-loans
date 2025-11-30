package com.example.loansystemcalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.text.DecimalFormat;
import java.time.LocalDate;

public class LoanSpecialActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String employeeId;
    private String employeeDateHired;

    private EditText editTextLoanAmount, editTextMonthsToPay;
    private TextView textViewEligibilityStatus, textViewInterestRate;
    private TextView textViewServiceCharge, textViewTotalInterest, textViewTotalAmount, textViewMonthlyAmortization;
    private Button btnCalculate, btnApply, btnBack;
    private CardView cardEligibility, cardCalculation;

    private DecimalFormat currencyFormat = new DecimalFormat("₱#,##0.00");
    private DecimalFormat percentFormat = new DecimalFormat("0.00%");

    private double currentInterestRate = 0.0;
    private double currentTotalAmountDue = 0.0;
    private double currentMonthlyAmortization = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loan_special);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupClickListeners();

        // Get employee ID from intent
        employeeId = getIntent().getStringExtra("EMPLOYEE_ID");
        if (employeeId == null) {
            Toast.makeText(this, "Error: Employee ID not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        checkEligibility();
    }

    private void initializeViews() {
        editTextLoanAmount = findViewById(R.id.editTextLoanAmount);
        editTextMonthsToPay = findViewById(R.id.editTextMonthsToPay);

        textViewEligibilityStatus = findViewById(R.id.textViewEligibilityStatus);
        textViewInterestRate = findViewById(R.id.textViewInterestRate);

        textViewServiceCharge = findViewById(R.id.textViewServiceCharge);
        textViewTotalInterest = findViewById(R.id.textViewTotalInterest);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);
        textViewMonthlyAmortization = findViewById(R.id.textViewMonthlyAmortization);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnApply = findViewById(R.id.btnApply);
        btnBack = findViewById(R.id.btnBack);

        cardEligibility = findViewById(R.id.cardEligibility);
        cardCalculation = findViewById(R.id.cardCalculation);
    }

    private void setupClickListeners() {
        btnCalculate.setOnClickListener(v -> calculateLoan());
        btnApply.setOnClickListener(v -> applyForLoan());
        btnBack.setOnClickListener(v -> finish());

        // Real-time calculation when months change
        editTextMonthsToPay.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !TextUtils.isEmpty(editTextMonthsToPay.getText())) {
                updateInterestRate();
            }
        });
    }

    private void checkEligibility() {
        // Get employee's date hired from database
        LocalDate dateHired = dbHelper.getEmployeeDateHired(employeeId);
        if (dateHired != null) {
            employeeDateHired = dateHired.toString();
            boolean isEligible = LoanCalculator.isEligibleForSpecial(employeeDateHired);

            if (isEligible) {
                textViewEligibilityStatus.setText("✓ Eligible for Special Loan (5+ years in service)");
                cardEligibility.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                enableForm(true);
            } else {
                textViewEligibilityStatus.setText("✗ Not eligible. Requires 5+ years in service.");
                cardEligibility.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                enableForm(false);
            }
        } else {
            textViewEligibilityStatus.setText("Error: Could not verify employment history");
            cardEligibility.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            enableForm(false);
        }
    }

    private void enableForm(boolean enabled) {
        editTextLoanAmount.setEnabled(enabled);
        editTextMonthsToPay.setEnabled(enabled);
        btnCalculate.setEnabled(enabled);
        btnApply.setEnabled(enabled);

        if (!enabled) {
            editTextLoanAmount.setText("");
            editTextMonthsToPay.setText("");
            clearCalculationResults();
        }
    }

    private void updateInterestRate() {
        if (TextUtils.isEmpty(editTextMonthsToPay.getText())) {
            return;
        }

        try {
            int months = Integer.parseInt(editTextMonthsToPay.getText().toString());

            // Get interest rate from database (Special Loan has loanTypeId = 2)
            currentInterestRate = dbHelper.getInterestRate(2, months);

            if (currentInterestRate > 0) {
                textViewInterestRate.setText("Interest Rate: " + percentFormat.format(currentInterestRate));
            } else {
                textViewInterestRate.setText("Invalid months range (1-18 months)");
            }
        } catch (NumberFormatException e) {
            textViewInterestRate.setText("Enter valid months");
        }
    }

    private void calculateLoan() {
        if (!validateInputs()) {
            return;
        }

        try {
            double loanAmount = Double.parseDouble(editTextLoanAmount.getText().toString());
            int monthsToPay = Integer.parseInt(editTextMonthsToPay.getText().toString());

            // Validate loan amount range for Special Loan
            if (!LoanCalculator.isValidLoanAmount("special", loanAmount)) {
                Toast.makeText(this, "Loan amount must be between ₱50,000 and ₱100,000", Toast.LENGTH_LONG).show();
                return;
            }

            // Validate months to pay
            if (!LoanCalculator.isValidMonthsToPay("special", monthsToPay)) {
                Toast.makeText(this, "Months to pay must be between 1-18", Toast.LENGTH_LONG).show();
                return;
            }

            // Calculate service charge (0% for Special Loan)
            double serviceCharge = LoanCalculator.computeServiceCharge("special", loanAmount);

            // Calculate total interest
            double totalInterest = LoanCalculator.computeInterest(loanAmount, currentInterestRate, monthsToPay);

            // Calculate total amount due
            currentTotalAmountDue = LoanCalculator.computeSpecialLoan(loanAmount, monthsToPay, currentInterestRate);

            // Calculate monthly amortization
            currentMonthlyAmortization = LoanCalculator.computeSpecialMonthlyAmortization(loanAmount, monthsToPay, currentInterestRate);

            // Display results
            displayCalculationResults(serviceCharge, totalInterest, currentTotalAmountDue, currentMonthlyAmortization);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayCalculationResults(double serviceCharge, double totalInterest, double totalAmountDue, double monthlyAmortization) {
        textViewServiceCharge.setText(currencyFormat.format(serviceCharge));
        textViewTotalInterest.setText(currencyFormat.format(totalInterest));
        textViewTotalAmount.setText(currencyFormat.format(totalAmountDue));
        textViewMonthlyAmortization.setText(currencyFormat.format(monthlyAmortization));

        // Show monthly amortization section
        findViewById(R.id.layoutMonthlyAmortization).setVisibility(View.VISIBLE);

        // Enable apply button
        btnApply.setEnabled(true);
    }

    private void clearCalculationResults() {
        textViewServiceCharge.setText("₱0.00");
        textViewTotalInterest.setText("₱0.00");
        textViewTotalAmount.setText("₱0.00");
        textViewMonthlyAmortization.setText("₱0.00");

        findViewById(R.id.layoutMonthlyAmortization).setVisibility(View.GONE);
        btnApply.setEnabled(false);
    }

    private boolean validateInputs() {
        if (TextUtils.isEmpty(editTextLoanAmount.getText())) {
            Toast.makeText(this, "Please enter loan amount", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(editTextMonthsToPay.getText())) {
            Toast.makeText(this, "Please enter months to pay", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void applyForLoan() {
        if (!validateInputs()) {
            return;
        }

        try {
            double loanAmount = Double.parseDouble(editTextLoanAmount.getText().toString());
            int monthsToPay = Integer.parseInt(editTextMonthsToPay.getText().toString());

            // Calculate values for database storage
            double serviceCharge = LoanCalculator.computeServiceCharge("special", loanAmount);
            double interestAmount = LoanCalculator.computeInterest(loanAmount, currentInterestRate, monthsToPay);
            double takeHomeLoan = loanAmount; // For special loan, take home is the full loan amount

            // Apply for loan (Special Loan has loanTypeId = 2)
            boolean success = dbHelper.applyForLoan(
                    employeeId,
                    2, // Special Loan Type ID
                    loanAmount,
                    monthsToPay,
                    currentTotalAmountDue,
                    currentInterestRate,
                    interestAmount,
                    serviceCharge,
                    takeHomeLoan
            );

            if (success) {
                Toast.makeText(this, "Loan application submitted successfully!", Toast.LENGTH_LONG).show();

                // Navigate back to loan type selection or main menu
                Intent intent = new Intent(this, LoanChooseTypeActivity.class);
                intent.putExtra("EMPLOYEE_ID", employeeId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to submit loan application", Toast.LENGTH_LONG).show();
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error in loan application", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}