package com.example.loansystemcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.TintableCheckedTextView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoanEmergencyActivity extends AppCompatActivity {

    private Button btnCalculate, btnApply, btnBack;
    private EditText editTextLoanAmount;
    private TextView textViewServiceCharge, textViewTotalInterest, textViewTotalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loan_emergency);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        btnCalculate = findViewById(R.id.btnCalculate);
        btnApply = findViewById(R.id.btnApply);
        btnBack = findViewById(R.id.btnBack);

        editTextLoanAmount = findViewById(R.id.editTextLoanAmount);
        textViewServiceCharge = findViewById(R.id.textViewServiceCharge);
        textViewTotalInterest = findViewById(R.id.textViewTotalInterest);
        textViewTotalAmount = findViewById(R.id.textViewTotalAmount);


        btnCalculate.setOnClickListener(v -> {
            String amountInput = editTextLoanAmount.getText().toString();

            if (!amountInput.isEmpty()) {
                double loanAmount = Double.parseDouble(amountInput);

                // Validate loan amount range
                if (loanAmount < 5000 || loanAmount > 25000) {
                    Toast.makeText(LoanEmergencyActivity.this, "Loan amount must be between ₱5,000 and ₱25,000", Toast.LENGTH_LONG).show();
                    textViewServiceCharge.setText("");
                    textViewTotalInterest.setText("");
                    return;
                }

                int monthsToPay = 6;
                double interestRate = 0.006; // 0.60% per month

                // Service Charge 1%
                double serviceCharge = loanAmount * 0.01;

                // Total Interest (LoanCalculator method)
                double interest = LoanCalculator.computeInterest(loanAmount, interestRate, monthsToPay);

                // Total Amount
                double totalAmount = loanAmount + serviceCharge + interest;

                // Display results
                textViewServiceCharge.setText("₱" + String.format("%.2f", serviceCharge));
                textViewTotalInterest.setText("₱" + String.format("%.2f", interest));
                textViewTotalAmount.setText("₱" + String.format("%.2f", totalAmount));
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoanEmergencyActivity.this, LoanChooseTypeActivity.class);
            startActivity(intent);
        });
    }

}


