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
import androidx.core.content.res.ResourcesCompat; // Added import

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminEmployeeViewerActivity extends AppCompatActivity {

    private TextView textViewAdminId, textViewEmployeeCount, textViewNoData;
    private LinearLayout employeeRecordsContainer;
    private Button btnBack, btnExport;
    private ProgressBar progressBar;
    private CardView cardTableContent;

    private DatabaseHelper dbHelper;
    private String adminId;

    private List<EmployeeRecord> employeeRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_employee_viewer);
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
        loadEmployeeRecords();
    }

    private void initializeViews() {
        textViewAdminId = findViewById(R.id.textViewAdminId);
        textViewEmployeeCount = findViewById(R.id.textViewEmployeeCount);
        textViewNoData = findViewById(R.id.textViewNoData);
        employeeRecordsContainer = findViewById(R.id.employeeRecordsContainer);
        btnBack = findViewById(R.id.btnBack);
        btnExport = findViewById(R.id.btnExport);
        progressBar = findViewById(R.id.progressBar);
        cardTableContent = findViewById(R.id.cardTableContent);
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnExport.setOnClickListener(v -> exportEmployeeRecords());
    }

    private void loadEmployeeRecords() {
        showLoading(true);

        new Thread(() -> {
            employeeRecords.clear();
            Cursor cursor = dbHelper.getAllEmployees();

            runOnUiThread(() -> {
                showLoading(false);

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        EmployeeRecord record = new EmployeeRecord();
                        record.setEmployeeId(cursor.getString(cursor.getColumnIndexOrThrow("employeeId")));
                        record.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow("firstName")));
                        record.setMiddleInitial(cursor.getString(cursor.getColumnIndexOrThrow("middleInitial")));
                        record.setLastName(cursor.getString(cursor.getColumnIndexOrThrow("lastName")));
                        record.setDateHired(cursor.getString(cursor.getColumnIndexOrThrow("dateHired")));
                        record.setBasicSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("basicSalary")));

                        employeeRecords.add(record);
                    } while (cursor.moveToNext());
                    cursor.close();

                    displayEmployeeRecords();
                } else {
                    showNoDataMessage(true);
                    if (cursor != null) {
                        cursor.close();
                    }
                }

                // Update admin info and count
                updateInfoDisplay();
            });
        }).start();
    }

    private void displayEmployeeRecords() {
        employeeRecordsContainer.removeAllViews();

        if (employeeRecords.isEmpty()) {
            showNoDataMessage(true);
            return;
        }

        showNoDataMessage(false);

        for (int i = 0; i < employeeRecords.size(); i++) {
            EmployeeRecord record = employeeRecords.get(i);
            View rowView = createTableRow(record, i);
            employeeRecordsContainer.addView(rowView);

            // Add click listener to show employee details
            rowView.setOnClickListener(v -> showEmployeeDetails(record));
        }
    }

    private View createTableRow(EmployeeRecord record, int position) {
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

        // Employee ID
        row.addView(createTextView(record.getEmployeeId(), 1.2f, false));

        // Full Name (First + Middle Initial + Last)
        String fullName = record.getFirstName() + " " +
                (record.getMiddleInitial() != null && !record.getMiddleInitial().isEmpty() ?
                        record.getMiddleInitial() + ". " : "") +
                record.getLastName();
        row.addView(createTextView(fullName, 2f, false));

        // Date Hired (formatted)
        String formattedDate = formatDate(record.getDateHired());
        row.addView(createTextView(formattedDate, 1.5f, false));

        // Basic Salary
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        format.setMaximumFractionDigits(2);
        row.addView(createTextView(format.format(record.getBasicSalary()), 1.3f, true));

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

        // FIXED: Use ResourcesCompat.getFont() for backward compatibility
        try {
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins));
        } catch (Exception e) {
            // Fallback to default typeface if font loading fails
            e.printStackTrace();
        }

        textView.setTextColor(Color.BLACK);

        if (alignEnd) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }

        return textView;
    }

    private void showEmployeeDetails(EmployeeRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Format the details
        String details = String.format(
                "Employee ID: %s\n\n" +
                        "Full Name: %s %s %s\n\n" +
                        "Date Hired: %s\n\n" +
                        "Basic Salary: %s\n\n" +
                        "Years of Service: %s",
                record.getEmployeeId(),
                record.getFirstName(),
                record.getMiddleInitial() != null && !record.getMiddleInitial().isEmpty() ? record.getMiddleInitial() + "." : "",
                record.getLastName(),
                formatDate(record.getDateHired()),
                NumberFormat.getCurrencyInstance(new Locale("en", "PH")).format(record.getBasicSalary()),
                calculateYearsOfService(record.getDateHired())
        );

        builder.setTitle("Employee Details")
                .setMessage(details)
                .setPositiveButton("Close", null)
                .setNeutralButton("View Loans", (dialog, which) -> viewEmployeeLoans(record.getEmployeeId()))
                .show();
    }

    private void viewEmployeeLoans(String employeeId) {
        // Navigate to loan history for this specific employee
        // You can create a new activity or dialog for this
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        StringBuilder loanDetails = new StringBuilder();
        Cursor cursor = dbHelper.getEmployeeLoanApplications(employeeId);

        if (cursor != null && cursor.moveToFirst()) {
            loanDetails.append("Loan History for ").append(employeeId).append("\n\n");
            do {
                String loanType = cursor.getString(cursor.getColumnIndexOrThrow("loanType"));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("requestedAmount"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("applicationDate"));

                loanDetails.append(String.format(
                        "Type: %s\nAmount: ₱%,.2f\nStatus: %s\nDate: %s\n\n",
                        loanType, amount, status, formatDate(date)
                ));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            loanDetails.append("No loan applications found for this employee.");
        }

        builder.setTitle("Employee Loan History")
                .setMessage(loanDetails.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void exportEmployeeRecords() {
        if (employeeRecords.isEmpty()) {
            Toast.makeText(this, "No records to export", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simple export implementation - could be enhanced to export to CSV or PDF
        StringBuilder exportData = new StringBuilder();
        exportData.append("Employee ID,Full Name,Date Hired,Basic Salary\n");

        for (EmployeeRecord record : employeeRecords) {
            String fullName = record.getFirstName() + " " +
                    (record.getMiddleInitial() != null && !record.getMiddleInitial().isEmpty() ?
                            record.getMiddleInitial() + ". " : "") +
                    record.getLastName();

            exportData.append(String.format("%s,%s,%s,%.2f\n",
                    record.getEmployeeId(),
                    fullName,
                    record.getDateHired(),
                    record.getBasicSalary()
            ));
        }

        // Create a share intent to export the data
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Employee Records")
                .setMessage("Export " + employeeRecords.size() + " records?")
                .setPositiveButton("Share", (dialog, which) -> {
                    // Implement sharing logic here
                    Toast.makeText(this, "Export feature would share data here", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateInfoDisplay() {
        textViewAdminId.setText(adminId);
        textViewEmployeeCount.setText(String.valueOf(employeeRecords.size()));
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
            // Handle both date-only and datetime formats
            if (dateString.contains(" ")) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(inputFormat.parse(dateString));
            } else {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(inputFormat.parse(dateString));
            }
        } catch (Exception e) {
            return dateString;
        }
    }

    private String calculateYearsOfService(String dateHired) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            java.util.Date hiredDate = format.parse(dateHired);
            java.util.Date currentDate = new java.util.Date();

            long diffInMillies = Math.abs(currentDate.getTime() - hiredDate.getTime());
            long diff = diffInMillies / (1000L * 60 * 60 * 24 * 365);

            return diff + " years";
        } catch (Exception e) {
            return "N/A";
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // Helper class to hold employee record data
    private static class EmployeeRecord {
        private String employeeId;
        private String firstName;
        private String middleInitial;
        private String lastName;
        private String dateHired;
        private double basicSalary;

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getMiddleInitial() { return middleInitial; }
        public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getDateHired() { return dateHired; }
        public void setDateHired(String dateHired) { this.dateHired = dateHired; }

        public double getBasicSalary() { return basicSalary; }
        public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadEmployeeRecords();
    }
}