package com.example.loansystemcalculator;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class LoanListFragment extends Fragment {

    private static final String ARG_FILTER_TYPE = "filter_type";
    private static final String ARG_DB_HELPER_REF = "db_helper_ref";

    private String filterType;
    private DatabaseHelper dbHelper;
    private LinearLayout loanListContainer;

    public static LoanListFragment newInstance(String filterType, DatabaseHelper dbHelper) {
        LoanListFragment fragment = new LoanListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_FILTER_TYPE, filterType);
        // Note: We can't pass DatabaseHelper directly, so we'll create a new instance
        fragment.dbHelper = dbHelper;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filterType = getArguments().getString(ARG_FILTER_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loan_list, container, false);
        loanListContainer = view.findViewById(R.id.loanListContainer);
        loadLoans();
        return view;
    }

    private void loadLoans() {
        loanListContainer.removeAllViews();

        Cursor cursor = getCursorForFilter();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                LoanApplication loan = extractLoanFromCursor(cursor);
                View loanItem = createLoanItemView(loan);
                loanListContainer.addView(loanItem);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            showNoDataMessage();
        }
    }

    private Cursor getCursorForFilter() {
        switch (filterType) {
            case "pending":
                return dbHelper.getPendingLoanApplications();
            case "approved":
                return dbHelper.getApprovedLoanApplications();
            case "denied":
                return dbHelper.getDeniedLoanApplications();
            default: // "all"
                return dbHelper.getAllLoanApplications();
        }
    }

    private LoanApplication extractLoanFromCursor(Cursor cursor) {
        LoanApplication loan = new LoanApplication();

        try {
            loan.setLoanId(cursor.getInt(cursor.getColumnIndexOrThrow("loanId")));
            loan.setEmployeeName(cursor.getString(cursor.getColumnIndexOrThrow("employeeName")));
            loan.setLoanType(cursor.getString(cursor.getColumnIndexOrThrow("loanType")));
            loan.setRequestedAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("requestedAmount")));
            loan.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
            loan.setApplicationDate(cursor.getString(cursor.getColumnIndexOrThrow("applicationDate")));

            // Try to get monthsToPay if available
            try {
                loan.setMonthsToPay(cursor.getInt(cursor.getColumnIndexOrThrow("monthsToPay")));
            } catch (Exception e) {
                loan.setMonthsToPay(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return loan;
    }

    private View createLoanItemView(LoanApplication loan) {
        View itemView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_loan_application, loanListContainer, false);

        TextView tvLoanId = itemView.findViewById(R.id.tvLoanId);
        TextView tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
        TextView tvLoanType = itemView.findViewById(R.id.tvLoanType);
        TextView tvAmount = itemView.findViewById(R.id.tvAmount);
        TextView tvStatus = itemView.findViewById(R.id.tvStatus);
        TextView tvDate = itemView.findViewById(R.id.tvDate);

        tvLoanId.setText(String.valueOf(loan.getLoanId()));
        tvEmployeeName.setText(loan.getEmployeeName());
        tvLoanType.setText(loan.getLoanType());

        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        format.setMaximumFractionDigits(2);
        tvAmount.setText(format.format(loan.getRequestedAmount()));

        tvStatus.setText(loan.getStatus());
        tvDate.setText(formatDate(loan.getApplicationDate()));

        // Set status color
        switch (loan.getStatus().toLowerCase()) {
            case "approved":
                tvStatus.setTextColor(Color.GREEN);
                break;
            case "denied":
                tvStatus.setTextColor(Color.RED);
                break;
            case "pending":
                tvStatus.setTextColor(Color.parseColor("#FFA500")); // Orange
                break;
            default:
                tvStatus.setTextColor(Color.GRAY);
        }

        // Set click listener
        itemView.setOnClickListener(v -> showLoanDetails(loan));

        return itemView;
    }

    private void showNoDataMessage() {
        TextView noDataText = new TextView(getContext());
        noDataText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        noDataText.setText("No loan applications found");
        noDataText.setTextSize(16);
        noDataText.setTextColor(Color.GRAY);
        noDataText.setPadding(0, 50, 0, 0);
        noDataText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        loanListContainer.addView(noDataText);
    }

    private void showLoanDetails(LoanApplication loan) {
        // Create a dialog or navigate to details activity
        // This is a simplified version
        String details = String.format(
                "Loan ID: %d\n" +
                        "Employee: %s\n" +
                        "Type: %s\n" +
                        "Amount: %s\n" +
                        "Status: %s\n" +
                        "Date: %s\n" +
                        "Months: %d",
                loan.getLoanId(),
                loan.getEmployeeName(),
                loan.getLoanType(),
                NumberFormat.getCurrencyInstance(new Locale("en", "PH")).format(loan.getRequestedAmount()),
                loan.getStatus(),
                formatDate(loan.getApplicationDate()),
                loan.getMonthsToPay()
        );

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Loan Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateString));
        } catch (Exception e) {
            return dateString;
        }
    }

    public void refreshData() {
        if (isAdded() && getView() != null) {
            loadLoans();
        }
    }

    // Helper class for loan application data
    private static class LoanApplication {
        private int loanId;
        private String employeeName;
        private String loanType;
        private double requestedAmount;
        private String status;
        private String applicationDate;
        private int monthsToPay;

        public int getLoanId() { return loanId; }
        public void setLoanId(int loanId) { this.loanId = loanId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getLoanType() { return loanType; }
        public void setLoanType(String loanType) { this.loanType = loanType; }

        public double getRequestedAmount() { return requestedAmount; }
        public void setRequestedAmount(double requestedAmount) { this.requestedAmount = requestedAmount; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getApplicationDate() { return applicationDate; }
        public void setApplicationDate(String applicationDate) { this.applicationDate = applicationDate; }

        public int getMonthsToPay() { return monthsToPay; }
        public void setMonthsToPay(int monthsToPay) { this.monthsToPay = monthsToPay; }
    }
}