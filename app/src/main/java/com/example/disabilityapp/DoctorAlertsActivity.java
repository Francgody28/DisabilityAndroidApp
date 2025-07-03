package com.example.disabilityapp;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.disabilityapp.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class DoctorAlertsActivity extends AppCompatActivity {

    ListView alertListView;
    DatabaseHelper db;
    String doctorEmail;
    List<Alert> alertList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_alerts);

        alertListView = findViewById(R.id.alertListView);
        db = new DatabaseHelper(this);

        doctorEmail = getIntent().getStringExtra("email");

        // ✅ FIXED: use the detailed method that returns List<Alert>
        alertList = db.getAlertsForDoctorDetailed(doctorEmail);

        List<String> alertStrings = new ArrayList<>();
        for (Alert alert : alertList) {
            String s = "Patient: " + alert.getPatientFirstName() + " " + alert.getPatientLastName() +
                    "\nCondition: " + alert.getCondition() +
                    "\nLocation: " + alert.getLatitude() + ", " + alert.getLongitude();
            alertStrings.add(s);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, alertStrings);
        alertListView.setAdapter(adapter);
    }
}
