package com.example.loansystemcalculator;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.view.View;

import androidx.cardview.widget.CardView;

public class LandingActivity extends AppCompatActivity {

    private CardView cardRegister, cardLogin, cardAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.initializeLoanDataIfNeeded();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        cardRegister = findViewById(R.id.cardRegister);
        cardLogin = findViewById(R.id.cardLogin);
        cardAdminLogin = findViewById(R.id.btnAdminLogin);
    }

    private void setupClickListeners() {

        // Register Card
        cardRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Login Card
        cardLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Admin Login Card
        cardAdminLogin.setOnClickListener(v -> {
            Intent intent = new Intent(LandingActivity.this, LoginAdminActivity.class);
            startActivity(intent);
        });
    }
}
