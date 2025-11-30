package com.example.loansystemcalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminProcessLoanActivity extends AppCompatActivity {

    private TextView textViewAdminId, textViewPendingCount, textViewNoData;
    private LinearLayout loanApplicationsContainer;
    private Button btnApprove, btnDeny, btnBack;
    private ProgressBar progressBar;
    private CardView cardTableContent;

    private DatabaseHelper dbHelper;
    private String adminId;
    private int selectedLoanId = -1;
    private View selectedRow = null;

    private List<LoanApplication> pendingApplications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_process_loan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        dbHelper = new DatabaseHelper(this);

        // Get admin ID from intent
        adminId = getIntent().getStringExtra("ADMIN_ID");
        if (adminId == null) {
            adminId = "admin"; // fallback
        }

        setupButtonListeners();
        loadPendingApplications();
    }

    private void initializeViews() {
        textViewAdminId = findViewById(R.id.textViewAdminId);
        textViewPendingCount = findViewById(R.id.textViewPendingCount);
        textViewNoData = findViewById(R.id.textViewNoData);
        loanApplicationsContainer = findViewById(R.id.loanApplicationsContainer);
        btnApprove = findViewById(R.id.btnApprove);
        btnDeny = findViewById(R.id.btnDeny);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        cardTableContent = findViewById(R.id.cardTableContent);
    }

    private void setupButtonListeners() {
        btnApprove.setOnClickListener(v -> approveSelectedLoan());
        btnDeny.setOnClickListener(v -> denySelectedLoan());

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadPendingApplications() {
        showLoading(true);

        new Thread(() -> {
            pendingApplications.clear();
            Cursor cursor = dbHelper.getPendingLoanApplications();

            runOnUiThread(() -> {
                showLoading(false);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        LoanApplication app = new LoanApplication();
                        app.setLoanId(cursor.getInt(cursor.getColumnIndexOrThrow("loanId")));
                        app.setEmployeeName(cursor.getString(cursor.getColumnIndexOrThrow("employeeName")));
                        app.setLoanType(cursor.getString(cursor.getColumnIndexOrThrow("loanTypeName")));
                        app.setRequestedAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("requestedAmount")));
                        app.setMonthsToPay(cursor.getInt(cursor.getColumnIndexOrThrow("monthsToPay")));

                        // Get application date
                        String dateString = cursor.getString(cursor.getColumnIndexOrThrow("applicationDate"));
                        app.setApplicationDate(dateString);

                        pendingApplications.add(app);
                    } while (cursor.moveToNext());
                    cursor.close();

                    displayPendingApplications();
                } else {
                    showNoDataMessage(true);
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // Update admin info
                updateAdminInfo();
            });
        }).start();
    }

    private void displayPendingApplications() {
        loanApplicationsContainer.removeAllViews();
        selectedLoanId = -1;
        selectedRow = null;
        updateActionButtons(false);

        if (pendingApplications.isEmpty()) {
            showNoDataMessage(true);
            return;
        }

        showNoDataMessage(false);

        for (int i = 0; i < pendingApplications.size(); i++) {
            LoanApplication app = pendingApplications.get(i);
            View rowView = createTableRow(app, i);
            loanApplicationsContainer.addView(rowView);
        }
    }

    private View createTableRow(LoanApplication app, int position) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Set padding and background
        int padding = dpToPx(12);
        row.setPadding(padding, padding, padding, padding);
        row.setBackgroundColor(position % 2 == 0 ? Color.parseColor("#F8F9FA") : Color.WHITE);

        // Make row clickable
        row.setClickable(true);
        row.setOnClickListener(v -> selectRow(row, app.getLoanId()));

        // Loan ID
        row.addView(createTextView(String.valueOf(app.getLoanId()), 1f, false));

        // Employee Name
        row.addView(createTextView(app.getEmployeeName(), 1.5f, false));

        // Loan Type
        row.addView(createTextView(app.getLoanType(), 1f, false));

        // Amount
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        format.setMaximumFractionDigits(2);
        row.addView(createTextView(format.format(app.getRequestedAmount()), 1.2f, true));

        // Months
        row.addView(createTextView(String.valueOf(app.getMonthsToPay()), 0.8f, true));

        // Date
        String formattedDate = formatDate(app.getApplicationDate());
        row.addView(createTextView(formattedDate, 1.2f, false));

        return row;
    }

    private TextView createTextView(String text, float weight, boolean alignEnd) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, weight
        );
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextSize(12);
        textView.setTypeface(getResources().getFont(R.font.poppins));
        textView.setTextColor(Color.BLACK);

        if (alignEnd) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }

        return textView;
    }

    private void selectRow(View row, int loanId) {
        // Deselect previous row
        if (selectedRow != null) {
            selectedRow.setBackgroundColor(
                    loanApplicationsContainer.indexOfChild(selectedRow) % 2 == 0
                            ? Color.parseColor("#F8F9FA") : Color.WHITE
            );
        }

        // Select new row
        selectedRow = row;
        selectedLoanId = loanId;
        row.setBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue selection color

        updateActionButtons(true);
    }

    private void updateActionButtons(boolean enabled) {
        btnApprove.setEnabled(enabled);
        btnDeny.setEnabled(enabled);

        // Visual feedback
        btnApprove.setAlpha(enabled ? 1.0f : 0.5f);
        btnDeny.setAlpha(enabled ? 1.0f : 0.5f);
    }

    private void approveSelectedLoan() {
        if (selectedLoanId == -1) return;

        showConfirmationDialog("Approve Loan",
                "Are you sure you want to approve this loan application?",
                "Approved");
    }

    private void denySelectedLoan() {
        if (selectedLoanId == -1) return;

        showConfirmationDialog("Deny Loan",
                "Are you sure you want to deny this loan application?",
                "Denied");
    }

    private void showConfirmationDialog(String title, String message, final String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> processLoanApplication(action))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processLoanApplication(String status) {
        showLoading(true);

        new Thread(() -> {
            boolean success = dbHelper.updateLoanStatus(selectedLoanId, status, adminId);

            runOnUiThread(() -> {
                showLoading(false);

                if (success) {
                    Toast.makeText(AdminProcessLoanActivity.this,
                            "Loan application " + status.toLowerCase() + " successfully",
                            Toast.LENGTH_SHORT).show();
                    loadPendingApplications(); // Refresh the list
                } else {
                    Toast.makeText(AdminProcessLoanActivity.this,
                            "Failed to update loan application",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void updateAdminInfo() {
        textViewAdminId.setText(adminId);
        textViewPendingCount.setText(String.valueOf(pendingApplications.size()));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        cardTableContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showNoDataMessage(boolean show) {
        textViewNoData.setVisibility(show ? View.VISIBLE : View.GONE);
        cardTableContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateString;
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Helper class to hold loan application data
    private static class LoanApplication {
        private int loanId;
        private String employeeName;
        private String loanType;
        private double requestedAmount;
        private int monthsToPay;
        private String applicationDate;

        public int getLoanId() {
            return loanId;
        }

        public void setLoanId(int loanId) {
            this.loanId = loanId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getLoanType() {
            return loanType;
        }

        public void setLoanType(String loanType) {
            this.loanType = loanType;
        }

        public double getRequestedAmount() {
            return requestedAmount;
        }

        public void setRequestedAmount(double requestedAmount) {
            this.requestedAmount = requestedAmount;
        }

        public int getMonthsToPay() {
            return monthsToPay;
        }

        public void setMonthsToPay(int monthsToPay) {
            this.monthsToPay = monthsToPay;
        }

        public String getApplicationDate() {
            return applicationDate;
        }

        public void setApplicationDate(String applicationDate) {
            this.applicationDate = applicationDate;
        }
    }
}