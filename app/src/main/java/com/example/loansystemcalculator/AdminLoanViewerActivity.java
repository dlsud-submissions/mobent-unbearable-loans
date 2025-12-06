package com.example.loansystemcalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class AdminLoanViewerActivity extends AppCompatActivity {

    private TextView textViewAdminId, textViewFilter;
    private TextView textViewTotalLoans, textViewApprovedLoans, textViewPendingLoans;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private Button btnBack;
    private ImageButton btnRefresh;
    private ProgressBar progressBar;
    private CardView cardStatistics;

    private DatabaseHelper dbHelper;
    private String adminId;
    private LoanPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_loan_viewer);
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

        setupUI();
        setupButtonListeners();
        loadLoanStatistics();
    }

    private void initializeViews() {
        textViewAdminId = findViewById(R.id.textViewAdminId);
        textViewFilter = findViewById(R.id.textViewFilter);
        textViewTotalLoans = findViewById(R.id.textViewTotalLoans);
        textViewApprovedLoans = findViewById(R.id.textViewApprovedLoans);
        textViewPendingLoans = findViewById(R.id.textViewPendingLoans);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        progressBar = findViewById(R.id.progressBar);
        cardStatistics = findViewById(R.id.cardStatistics);
    }

    private void setupUI() {
        textViewAdminId.setText(adminId);

        // Setup ViewPager with TabLayout
        setupViewPager();
    }

    private void setupViewPager() {
        pagerAdapter = new LoanPagerAdapter(this, dbHelper);
        viewPager.setAdapter(pagerAdapter);

        // Tab titles
        String[] tabTitles = {"All", "Pending", "Approved", "Denied"};

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();

        // Update filter text based on selected tab
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                String[] filters = {"All Applications", "Pending Applications",
                        "Approved Applications", "Denied Applications"};
                textViewFilter.setText(filters[position]);
            }
        });
    }

    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnRefresh.setOnClickListener(v -> {
            refreshData();
        });
    }

    private void loadLoanStatistics() {
        new Thread(() -> {
            int total = 0, pending = 0, approved = 0, denied = 0;
            try {
                Cursor allCursor = dbHelper.getAllLoanApplications();
                Cursor pendingCursor = dbHelper.getPendingLoanApplications();
                Cursor approvedCursor = dbHelper.getApprovedLoanApplications();
                Cursor deniedCursor = dbHelper.getDeniedLoanApplications();

                total = allCursor != null ? allCursor.getCount() : 0;
                pending = pendingCursor != null ? pendingCursor.getCount() : 0;
                approved = approvedCursor != null ? approvedCursor.getCount() : 0;
                denied = deniedCursor != null ? deniedCursor.getCount() : 0;

                // Close cursors
                if (allCursor != null) allCursor.close();
                if (pendingCursor != null) pendingCursor.close();
                if (approvedCursor != null) approvedCursor.close();
                if (deniedCursor != null) deniedCursor.close();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error, perhaps show a toast or log
                runOnUiThread(() -> Toast.makeText(AdminLoanViewerActivity.this, "Error loading statistics", Toast.LENGTH_SHORT).show());
            }

            int finalTotal = total;
            int finalPending = pending;
            int finalApproved = approved;
            runOnUiThread(() -> {
                textViewTotalLoans.setText("Total: " + finalTotal);
                textViewApprovedLoans.setText("Approved: " + finalApproved);
                textViewPendingLoans.setText("Pending: " + finalPending);

                // Show statistics card if there are loans
                cardStatistics.setVisibility(finalTotal > 0 ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void refreshData() {
        showLoading(true);

        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate loading time
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                showLoading(false);

                // Refresh only the current fragment to avoid crashes if other fragments are not created
                if (pagerAdapter != null) {
                    pagerAdapter.refreshFragment(viewPager.getCurrentItem());
                }

                // Reload statistics
                loadLoanStatistics();

                Toast.makeText(AdminLoanViewerActivity.this,
                        "Data refreshed", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            viewPager.setAlpha(0.5f);
        } else {
            viewPager.setAlpha(1.0f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        refreshData();
    }
}