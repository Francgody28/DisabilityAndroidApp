package com.example.disabilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DoctorDashboardActivity extends AppCompatActivity {

    TextView doctorWelcome;
    Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Bind views
        doctorWelcome = findViewById(R.id.doctorWelcome);
        logoutBtn = findViewById(R.id.logoutBtn);

        // Get first name from intent and show welcome message
        String firstName = getIntent().getStringExtra("firstName");
        doctorWelcome.setText("Welcome Dr. " + firstName);

        // Handle logout
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear history
            startActivity(intent);
            finish(); // close this activity
        });
    }
}
