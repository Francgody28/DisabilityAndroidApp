package com.example.disabilityapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DoctorDashboardActivity extends AppCompatActivity {

    TextView doctorWelcome;
    ListView patientListView, alertListView;
    Button logoutBtn, viewAlertsBtn;
    DatabaseHelper db;
    String doctorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_dashboard);

        // Bind views
        doctorWelcome = findViewById(R.id.doctorWelcome);
        patientListView = findViewById(R.id.patientListView);
        alertListView = findViewById(R.id.alertListView);
        logoutBtn = findViewById(R.id.logoutBtn);
        viewAlertsBtn = findViewById(R.id.viewAlertsBtn);
        db = new DatabaseHelper(this);

        // Get doctor data from Intent
        String firstName = getIntent().getStringExtra("firstName");
        doctorEmail = getIntent().getStringExtra("email");

        // Set welcome message
        doctorWelcome.setText("Welcome Dr. " + firstName);

        // Load assigned patients
        List<String> patientList = db.getPatientsForDoctor(doctorEmail);
        ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, patientList);
        patientListView.setAdapter(patientAdapter);

        // Load alerts
        List<String> alerts = db.getAlertsForDoctor(doctorEmail);
        if (!alerts.isEmpty()) {
            ArrayAdapter<String> alertAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alerts);
            alertListView.setAdapter(alertAdapter);

            // Show popup if latest alert is "Bad"
            String latestAlert = alerts.get(0);
            if (latestAlert.toLowerCase().contains("bad")) {
                new AlertDialog.Builder(this)
                        .setTitle("Urgent Alert")
                        .setMessage(latestAlert)
                        .setPositiveButton("OK", null)
                        .show();
            }
        } else {
            Toast.makeText(this, "No alerts yet", Toast.LENGTH_SHORT).show();
        }

        // 🔹 View Full Alert List
        viewAlertsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, DoctorAlertsActivity.class);
            intent.putExtra("email", doctorEmail);
            startActivity(intent);
        });

        // 🔸 Logout
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
