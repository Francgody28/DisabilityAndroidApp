package com.example.disabilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DisabledDashboardActivity extends AppCompatActivity {

    TextView disabledWelcome;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_dashboard);

        // Bind views
        disabledWelcome = findViewById(R.id.disabledWelcome);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Set welcome text
        String firstName = getIntent().getStringExtra("firstName");
        disabledWelcome.setText("Welcome " + firstName);

        // Handle logout
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DisabledDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
