package com.example.disabilityapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText email, password;
    Button loginBtn;
    TextView registerLink;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerLink = findViewById(R.id.registerLink);

        loginBtn.setOnClickListener(v -> {
            String userEmail = email.getText().toString().trim();
            String userPass = password.getText().toString().trim();

            // Validate empty fields
            if (userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check user credentials
            if (db.checkUser(userEmail, userPass)) {
                String userType = db.getUserType(userEmail);
                String firstName = db.getFirstName(userEmail);

                if (userType == null || firstName == null) {
                    Toast.makeText(this, "User data is incomplete", Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN_ERROR", "userType or firstName is null");
                    return;
                }

                // Redirect based on user type
                Intent intent;
                if (userType.equalsIgnoreCase("doctor")) {
                    intent = new Intent(MainActivity.this, DoctorDashboardActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, DisabledDashboardActivity.class);
                }

                intent.putExtra("firstName", firstName);
                startActivity(intent);
                finish();  // Optional: finish login activity

            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        registerLink.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });
    }
}
