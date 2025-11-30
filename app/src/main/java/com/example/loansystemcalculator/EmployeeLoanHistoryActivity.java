package com.example.loansystemcalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmployeeLoanHistoryActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String employeeId;

    private TextView textViewEmployeeId, textViewTotalApplications, textViewNoData;
    private LinearLayout loanHistoryContainer;
    private ProgressBar progressBar;
    private Button btnBack;

    private DecimalFormat currencyFormat = new DecimalFormat("₱#,##0.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_loan_history);
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
        loadEmployeeLoanHistory();
    }

    private void initializeViews() {
        textViewEmployeeId = findViewById(R.id.textViewEmployeeId);
        textViewTotalApplications = findViewById(R.id.textViewTotalApplications);
        textViewNoData = findViewById(R.id.textViewNoData);
        loanHistoryContainer = findViewById(R.id.loanHistoryContainer);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadEmployeeLoanHistory() {
        showLoading(true);

        // Set employee ID
        textViewEmployeeId.setText(employeeId);

        // Get all loan applications for this employee using the new method
        Cursor cursor = dbHelper.getEmployeeLoanApplications(employeeId);

        if (cursor != null && cursor.getCount() > 0) {
            displayLoanHistory(cursor);
            textViewNoData.setVisibility(View.GONE);
            textViewTotalApplications.setText(String.valueOf(cursor.getCount()));
        } else {
            textViewNoData.setVisibility(View.VISIBLE);
            loanHistoryContainer.removeAllViews();
            textViewTotalApplications.setText("0");
        }

        showLoading(false);
    }

    private void displayLoanHistory(Cursor cursor) {
        loanHistoryContainer.removeAllViews();

        if (cursor.moveToFirst()) {
            int rowNumber = 0;
            do {
                // Create table row
                LinearLayout tableRow = createTableRow(
                        cursor.getString(cursor.getColumnIndexOrThrow("loanId")),
                        cursor.getString(cursor.getColumnIndexOrThrow("loanType")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("requestedAmount")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("monthsToPay")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("applicationDate")),
                        rowNumber
                );

                loanHistoryContainer.addView(tableRow);
                rowNumber++;
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private LinearLayout createTableRow(String loanId, String loanType, double amount,
                                        int months, String status, String date, int rowNumber) {
        LinearLayout tableRow = new LinearLayout(this);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        tableRow.setOrientation(LinearLayout.HORIZONTAL);
        tableRow.setPadding(12, 12, 12, 12);

        // Alternate row colors for better readability
        if (rowNumber % 2 == 0) {
            tableRow.setBackgroundColor(Color.parseColor("#F8F9FA"));
        } else {
            tableRow.setBackgroundColor(Color.WHITE);
        }

        // Loan ID Column
        tableRow.addView(createTableCell(loanId, 1.2f, Gravity.START));

        // Loan Type Column
        tableRow.addView(createTableCell(loanType, 1.5f, Gravity.START));

        // Amount Column
        tableRow.addView(createTableCell(currencyFormat.format(amount), 1.3f, Gravity.END));

        // Months Column
        tableRow.addView(createTableCell(String.valueOf(months), 1.0f, Gravity.CENTER));

        // Status Column
        TextView statusCell = createTableCell(status, 1.5f, Gravity.CENTER);
        setStatusColor(statusCell, status);
        tableRow.addView(statusCell);

        // Date Column
        String formattedDate = formatDate(date);
        tableRow.addView(createTableCell(formattedDate, 1.5f, Gravity.CENTER));

        return tableRow;
    }

    private TextView createTableCell(String text, float weight, int gravity) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, weight
        );
        params.setMargins(2, 0, 2, 0);
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextSize(12);
        textView.setTypeface(getResources().getFont(R.font.poppins));
        textView.setTextColor(Color.BLACK);
        textView.setGravity(gravity);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        return textView;
    }

    private void setStatusColor(TextView statusView, String status) {
        int textColor;
        int bgColor;

        switch (status.toLowerCase()) {
            case "approved":
                textColor = Color.parseColor("#28A745");
                bgColor = Color.parseColor("#E8F5E8");
                break;
            case "pending":
                textColor = Color.parseColor("#FFC107");
                bgColor = Color.parseColor("#FFF8E1");
                break;
            case "denied":
                textColor = Color.parseColor("#DC3545");
                bgColor = Color.parseColor("#FDE8E8");
                break;
            default:
                textColor = Color.BLACK;
                bgColor = Color.TRANSPARENT;
        }

        statusView.setTextColor(textColor);
        statusView.setBackgroundColor(bgColor);
        statusView.setPadding(8, 4, 8, 4);

        // Add rounded corners
        statusView.setBackground(getResources().getDrawable(R.drawable.status_border));
    }

    private String formatDate(String dateString) {
        try {
            // Parse the date string and format it
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return dateFormat.format(date);
        } catch (Exception e) {
            // If parsing fails, try without time
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return dateFormat.format(date);
            } catch (Exception ex) {
                return dateString;
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loanHistoryContainer.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}