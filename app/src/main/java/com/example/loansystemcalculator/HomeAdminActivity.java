package com.example.loansystemcalculator;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class HomeAdminActivity extends AppCompatActivity {

    private TextView tvWelcomeAdmin, tvAdminId;
    private CardView cardProcessLoans, cardViewLoans, cardViewUsers, cardLoanAnalytics;
    private ImageButton btnLogout;

    private DatabaseHelper dbHelper;
    private String adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        dbHelper = new DatabaseHelper(this);

        // Get admin ID from intent or use default
        adminId = getIntent().getStringExtra("ADMIN_ID");
        if (adminId == null) {
            adminId = "admin"; // fallback to default admin
        }

        setupAdminInfo();
        setupCardClickListeners();
        setupLogoutButton();
    }

    private void initializeViews() {
        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvAdminId = findViewById(R.id.tvAdminId);

        cardProcessLoans = findViewById(R.id.cardProcessLoans);
        cardViewLoans = findViewById(R.id.cardViewLoans);
        cardViewUsers = findViewById(R.id.cardViewUsers);
        cardLoanAnalytics = findViewById(R.id.cardLoanAnalytics);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupAdminInfo() {
        // Update welcome message and admin ID display
        tvWelcomeAdmin.setText("Welcome, Admin!");
        tvAdminId.setText("Admin ID: " + adminId);
    }

    private void setupCardClickListeners() {
        // Process Loans Card - Navigate to AdminProcessLoanActivity
        cardProcessLoans.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, AdminProcessLoanActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        // View Loans Card - Navigate to AdminLoanViewerActivity
        cardViewLoans.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, AdminLoanViewerActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        // View Users Card - Navigate to AdminEmployeeViewerActivity
        cardViewUsers.setOnClickListener(v -> {
            Intent intent = new Intent(HomeAdminActivity.this, AdminEmployeeViewerActivity.class);
            intent.putExtra("ADMIN_ID", adminId);
            startActivity(intent);
        });

        // Loan Analytics Card - Show loan statistics dialog
        cardLoanAnalytics.setOnClickListener(v -> {
            showLoanAnalytics();
        });
    }

    private void setupLogoutButton() {
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void showLoanAnalytics() {
        // Show loan application statistics in a dialog
        StringBuilder analytics = new StringBuilder();

        try {
            // Get counts for different statuses
            android.database.Cursor allCursor = dbHelper.getAllLoanApplications();
            android.database.Cursor pendingCursor = dbHelper.getPendingLoanApplications();
            android.database.Cursor approvedCursor = dbHelper.getApprovedLoanApplications();
            android.database.Cursor deniedCursor = dbHelper.getDeniedLoanApplications();

            int totalApplications = allCursor != null ? allCursor.getCount() : 0;
            int pendingApplications = pendingCursor != null ? pendingCursor.getCount() : 0;
            int approvedApplications = approvedCursor != null ? approvedCursor.getCount() : 0;
            int deniedApplications = deniedCursor != null ? deniedCursor.getCount() : 0;

            // Close cursors
            if (allCursor != null) allCursor.close();
            if (pendingCursor != null) pendingCursor.close();
            if (approvedCursor != null) approvedCursor.close();
            if (deniedCursor != null) deniedCursor.close();

            analytics.append("📊 Loan Application Statistics\n\n")
                    .append("Total Applications: ").append(totalApplications).append("\n")
                    .append("Pending: ").append(pendingApplications).append("\n")
                    .append("Approved: ").append(approvedApplications).append("\n")
                    .append("Denied: ").append(deniedApplications).append("\n\n");

            if (totalApplications > 0) {
                double approvalRate = (double) approvedApplications / totalApplications * 100;
                double pendingRate = (double) pendingApplications / totalApplications * 100;
                double deniedRate = (double) deniedApplications / totalApplications * 100;

                analytics.append(String.format("Approval Rate: %.1f%%\n", approvalRate))
                        .append(String.format("Pending Rate: %.1f%%\n", pendingRate))
                        .append(String.format("Denial Rate: %.1f%%", deniedRate));
            } else {
                analytics.append("No loan applications found.");
            }
        } catch (Exception e) {
            analytics.append("Error loading analytics. Please try again.");
            e.printStackTrace();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Loan Analytics")
                .setMessage(analytics.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> logout())
                .setNegativeButton("No", null)
                .show();
    }

    private void logout() {
        // Navigate back to admin login screen
        Intent intent = new Intent(HomeAdminActivity.this, LandingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh admin info when returning to this activity
        setupAdminInfo();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog when back button is pressed
        showExitConfirmation();
    }

    private void showExitConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit Application")
                .setMessage("Are you sure you want to exit the application?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Close the app
                    finishAffinity();
                    System.exit(0);
                })
                .setNegativeButton("No", null)
                .show();
    }
}