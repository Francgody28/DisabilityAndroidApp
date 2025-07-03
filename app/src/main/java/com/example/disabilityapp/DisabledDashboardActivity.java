package com.example.disabilityapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class DisabledDashboardActivity extends AppCompatActivity {

    TextView welcomeText, doctorDetails;
    Spinner doctorSpinner;
    Button assignDoctorBtn, logoutBtn, sendAlertBtn;
    RadioGroup conditionGroup;
    View doctorSelectionSection, doctorInfoSection;
    DatabaseHelper db;
    String userEmail;  // Logged-in user's email

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private double latitude = 0.0;
    private double longitude = 0.0;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

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

        // Location client init
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        // Get user data from Intent
        String firstName = getIntent().getStringExtra("firstName");
        userEmail = getIntent().getStringExtra("email"); // Passed from login

        // Set welcome text
        welcomeText.setText("Welcome " + firstName);

        // Check if user already assigned a doctor
        String[] doctorInfo = db.getAssignedDoctorDetails(userEmail);

        if (doctorInfo != null) {
            // Doctor assigned — hide selection, show info & condition options
            doctorSelectionSection.setVisibility(View.GONE);
            doctorInfoSection.setVisibility(View.VISIBLE);
            conditionGroup.setVisibility(View.VISIBLE);
            doctorDetails.setText("Your doctor is Dr. " + doctorInfo[0] + " (" + doctorInfo[1] + ")");
        } else {
            // Load doctors list if no doctor assigned
            List<String> doctorList = db.getAllDoctors();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, doctorList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            doctorSpinner.setAdapter(adapter);

            doctorInfoSection.setVisibility(View.GONE);
            conditionGroup.setVisibility(View.GONE);
            sendAlertBtn.setVisibility(View.GONE);
        }

        // Assign doctor button
        assignDoctorBtn.setOnClickListener(v -> {
            String selected = (String) doctorSpinner.getSelectedItem();
            if (selected != null) {
                String doctorEmail = selected.substring(selected.indexOf("(") + 1, selected.indexOf(")")); // Extract email
                boolean success = db.assignDoctorToDisabled(userEmail, doctorEmail);
                if (success) {
                    Toast.makeText(this, "Doctor Assigned Successfully", Toast.LENGTH_SHORT).show();
                    recreate(); // Refresh UI
                } else {
                    Toast.makeText(this, "Assignment Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Condition selection change
        conditionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.badRadio) {
                sendAlertBtn.setVisibility(View.VISIBLE);
            } else {
                sendAlertBtn.setVisibility(View.GONE);
            }
        });

        // Send alert button
        sendAlertBtn.setOnClickListener(v -> {
            String doctorEmail = db.getAssignedDoctorEmail(userEmail);
            if (doctorEmail != null) {
                boolean alertInserted = db.insertAlert(userEmail, doctorEmail, "Bad", latitude, longitude);
                if (alertInserted) {
                    Toast.makeText(this, "Alert with location sent to Doctor", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No assigned doctor found!", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout button
        logoutBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DisabledDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
