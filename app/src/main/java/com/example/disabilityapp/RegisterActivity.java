package com.example.disabilityapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    EditText fname, mname, lname, dob, email, phone, password, confirmPassword, specialist;
    CheckBox visual, hearing, mobility, others;
    Button registerBtn;
    RadioGroup roleGroup;
    RadioButton radioDisabled, radioDoctor;
    LinearLayout disabilitySection, specialistSection;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Bind views
        fname = findViewById(R.id.fname);
        mname = findViewById(R.id.mname);
        lname = findViewById(R.id.lname);
        dob = findViewById(R.id.dob);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        specialist = findViewById(R.id.specialist);
        visual = findViewById(R.id.visual);
        hearing = findViewById(R.id.hearing);
        mobility = findViewById(R.id.mobility);
        others = findViewById(R.id.others);
        registerBtn = findViewById(R.id.registerBtn);
        roleGroup = findViewById(R.id.roleGroup);
        radioDisabled = findViewById(R.id.radioDisabled);
        radioDoctor = findViewById(R.id.radioDoctor);
        disabilitySection = findViewById(R.id.disabilitySection);
        specialistSection = findViewById(R.id.specialistSection);
        db = new DatabaseHelper(this);

        // Toggle visibility based on role
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioDisabled) {
                disabilitySection.setVisibility(View.VISIBLE);
                specialistSection.setVisibility(View.GONE);
            } else {
                disabilitySection.setVisibility(View.GONE);
                specialistSection.setVisibility(View.VISIBLE);
            }
        });

        // Date Picker for DOB
        dob.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this,
                    (view, y, m, d) -> dob.setText(d + "/" + (m + 1) + "/" + y),
                    year, month, day);
            dialog.show();
        });

        // Register button logic
        registerBtn.setOnClickListener(v -> {
            String userType = radioDoctor.isChecked() ? "doctor" : "disabled";
            String f = fname.getText().toString().trim();
            String m = mname.getText().toString().trim();
            String l = lname.getText().toString().trim();
            String birth = dob.getText().toString().trim();
            String e = email.getText().toString().trim();
            String p = phone.getText().toString().trim();
            String pass = password.getText().toString().trim();
            String confirm = confirmPassword.getText().toString().trim();
            String spec = specialist.getText().toString().trim();

            if (f.isEmpty() || l.isEmpty() || birth.isEmpty() || e.isEmpty() || p.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            String disabilityType = "";

            if (userType.equals("disabled")) {
                if (!visual.isChecked() && !hearing.isChecked() && !mobility.isChecked() && !others.isChecked()) {
                    Toast.makeText(this, "Please select at least one disability type", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (visual.isChecked()) disabilityType += "Visual ";
                if (hearing.isChecked()) disabilityType += "Hearing ";
                if (mobility.isChecked()) disabilityType += "Mobility ";
                if (others.isChecked()) disabilityType += "Others ";
            } else {
                if (spec.isEmpty()) {
                    Toast.makeText(this, "Enter specialist field", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Save to DB
            boolean inserted = db.insertUser(
                    f, m, l, birth, e, p, pass,
                    userType.equals("disabled") ? disabilityType.trim() : "",
                    userType.equals("doctor") ? spec : "",
                    userType
            );

            if (inserted) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                finish(); // go back to login
            } else {
                Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
