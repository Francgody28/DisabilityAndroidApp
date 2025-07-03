package com.example.disabilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class DisabledDashboardActivity extends AppCompatActivity {

    TextView welcomeText, doctorDetails;
    Spinner doctorSpinner;
    Button assignDoctorBtn, logoutBtn, sendAlertBtn;
    RadioGroup conditionGroup;
    View doctorSelectionSection, doctorInfoSection;
    DatabaseHelper db;
    String userEmail;  // Logged-in user's email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disabled_dashboard);

        // Bind views
        welcomeText = findViewById(R.id.disabledWelcome);
        doctorSpinner = findViewById(R.id.doctorSpinner);
        assignDoctorBtn = findViewById(R.id.assignDoctorBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        sendAlertBtn = findViewById(R.id.sendAlertBtn);
        conditionGroup = findViewById(R.id.conditionGroup);
        doctorDetails = findViewById(R.id.doctorDetails);
        doctorSelectionSection = findViewById(R.id.doctorSelectionSection);
        doctorInfoSection = findViewById(R.id.doctorInfoSection);

        db = new DatabaseHelper(this);

        // Get user data from Intent
        String firstName = getIntent().getStringExtra("firstName");
        userEmail = getIntent().getStringExtra("email"); // Passed from login

        // Set welcome text
        welcomeText.setText("Welcome " + firstName);

        // Check if user already assigned a doctor
        String[] doctorInfo = db.getAssignedDoctorDetails(userEmail);

        if (doctorInfo != null) {
            // Doctor already assigned — hide selection, show doctor info and condition options
            doctorSelectionSection.setVisibility(View.GONE);
            doctorInfoSection.setVisibility(View.VISIBLE);
            conditionGroup.setVisibility(View.VISIBLE);
            doctorDetails.setText("Your doctor is Dr. " + doctorInfo[0] + " (" + doctorInfo[1] + ")");
        } else {
            // Load list of doctors only if no doctor assigned
            List<String> doctorList = db.getAllDoctors();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            doctorSpinner.setAdapter(adapter);

            // Doctor info and condition group are hidden by default
            doctorInfoSection.setVisibility(View.GONE);
            conditionGroup.setVisibility(View.GONE);
            sendAlertBtn.setVisibility(View.GONE);
        }

        // Assign doctor button logic
        assignDoctorBtn.setOnClickListener(v -> {
            String selected = (String) doctorSpinner.getSelectedItem();
            if (selected != null) {
                String doctorEmail = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")")); // extract email
                boolean success = db.assignDoctorToDisabled(userEmail, doctorEmail);
                if (success) {
                    Toast.makeText(this, "Doctor Assigned Successfully", Toast.LENGTH_SHORT).show();
                    // Refresh activity to update UI
                    recreate();
                } else {
                    Toast.makeText(this, "Assignment Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Condition selection changes
        conditionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.badRadio) {
                sendAlertBtn.setVisibility(View.VISIBLE);
            } else {
                sendAlertBtn.setVisibility(View.GONE);
            }
        });

        // Send alert button logic
        sendAlertBtn.setOnClickListener(v -> {
            String doctorEmail = db.getAssignedDoctorEmail(userEmail);
            if (doctorEmail != null) {
                boolean alertInserted = db.insertAlert(userEmail, doctorEmail, "Bad");
                if (alertInserted) {
                    Toast.makeText(this, "Alert sent to Doctor", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No assigned doctor found!", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout button logic
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DisabledDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });
    }
}
