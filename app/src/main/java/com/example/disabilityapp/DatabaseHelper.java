package com.example.disabilityapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "disability.db";
    private static final int DB_VERSION = 5;  // Incremented for location fields in alerts

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        String createUsersTable = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fname TEXT, " +
                "mname TEXT, " +
                "lname TEXT, " +
                "dob TEXT, " +
                "email TEXT UNIQUE, " +
                "phone TEXT, " +
                "password TEXT, " +
                "disabilityType TEXT, " +       // only for disabled users
                "specialist TEXT, " +           // only for doctors
                "userType TEXT, " +             // "doctor" or "disabled"
                "assignedDoctorEmail TEXT)";    // only for disabled users
        db.execSQL(createUsersTable);

        // Create alerts table with location columns and timestamp
        String createAlertsTable = "CREATE TABLE alerts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "disabledEmail TEXT, " +
                "doctorEmail TEXT, " +
                "condition TEXT, " +
                "latitude REAL, " +             // New latitude column
                "longitude REAL, " +            // New longitude column
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createAlertsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables and recreate (simple migration)
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS alerts");
        onCreate(db);
    }

    // Insert user (doctor or disabled)
    public boolean insertUser(String fname, String mname, String lname, String dob,
                              String email, String phone, String password,
                              String disabilityType, String specialist, String userType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("fname", fname);
        cv.put("mname", mname);
        cv.put("lname", lname);
        cv.put("dob", dob);
        cv.put("email", email);
        cv.put("phone", phone);
        cv.put("password", password);
        cv.put("disabilityType", disabilityType);
        cv.put("specialist", specialist);
        cv.put("userType", userType);
        cv.put("assignedDoctorEmail", "");  // initially empty for disabled

        long result = db.insert("users", null, cv);
        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert user for email: " + email);
            return false;
        }
        return true;
    }

    // Check login credentials
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Get userType by email (doctor/disabled)
    public String getUserType(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT userType FROM users WHERE email = ?",
                new String[]{email});
        String userType = null;
        if (cursor.moveToFirst()) {
            userType = cursor.getString(0);
        }
        cursor.close();
        return userType;
    }

    // Get first name by email
    public String getFirstName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT fname FROM users WHERE email = ?",
                new String[]{email});
        String firstName = "";
        if (cursor.moveToFirst()) {
            firstName = cursor.getString(0);
        }
        cursor.close();
        return firstName;
    }

    // Get list of all doctors
    public List<String> getAllDoctors() {
        List<String> doctorList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT fname, email FROM users WHERE userType = 'doctor'", null);

        while (cursor.moveToNext()) {
            String full = cursor.getString(0) + " (" + cursor.getString(1) + ")";
            doctorList.add(full);
        }
        cursor.close();
        return doctorList;
    }

    // Assign doctor to disabled user
    public boolean assignDoctorToDisabled(String disabledEmail, String doctorEmail) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("assignedDoctorEmail", doctorEmail);
        long result = db.update("users", cv, "email = ?", new String[]{disabledEmail});
        return result != -1;
    }

    // Get all patients (disabled users) assigned to a specific doctor
    public List<String> getPatientsForDoctor(String doctorEmail) {
        List<String> patients = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT fname, email FROM users WHERE assignedDoctorEmail = ?",
                new String[]{doctorEmail});

        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String email = cursor.getString(1);
            patients.add(name + " (" + email + ")");
        }
        cursor.close();
        return patients;
    }

    // Get assigned doctor details (name and specialist) for a disabled user
    public String[] getAssignedDoctorDetails(String disabledEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT fname, specialist FROM users WHERE email = (" +
                        "SELECT assignedDoctorEmail FROM users WHERE email = ?)",
                new String[]{disabledEmail});

        if (cursor.moveToFirst()) {
            String[] details = new String[2];
            details[0] = cursor.getString(0); // fname
            details[1] = cursor.getString(1); // specialist
            cursor.close();
            return details;
        }
        cursor.close();
        return null;
    }

    // Get assigned doctor email by disabled user's email
    public String getAssignedDoctorEmail(String disabledEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT assignedDoctorEmail FROM users WHERE email = ?",
                new String[]{disabledEmail});
        if (cursor.moveToFirst()) {
            String email = cursor.getString(0);
            cursor.close();
            return email;
        }
        cursor.close();
        return null;
    }

    // Insert alert with location (disabled user reports condition)
    public boolean insertAlert(String disabledEmail, String doctorEmail, String condition,
                               double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("disabledEmail", disabledEmail);
        cv.put("doctorEmail", doctorEmail);
        cv.put("condition", condition);
        cv.put("latitude", latitude);
        cv.put("longitude", longitude);

        long result = db.insert("alerts", null, cv);
        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert alert from " + disabledEmail);
            return false;
        }
        return true;
    }

    // Get alerts as formatted strings for doctor (with location info)
    public List<String> getAlertsForDoctor(String doctorEmail) {
        List<String> alerts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT u.fname, a.condition, a.latitude, a.longitude, a.timestamp " +
                        "FROM alerts a JOIN users u ON a.disabledEmail = u.email " +
                        "WHERE a.doctorEmail = ? ORDER BY a.timestamp DESC",
                new String[]{doctorEmail});

        while (cursor.moveToNext()) {
            String patientName = cursor.getString(0);
            String condition = cursor.getString(1);
            double lat = cursor.getDouble(2);
            double lon = cursor.getDouble(3);
            String timestamp = cursor.getString(4);

            alerts.add(patientName + " reported \"" + condition + "\" at " + timestamp +
                    " (Location: " + lat + ", " + lon + ")");
        }
        cursor.close();
        return alerts;
    }

    // New method: Get alerts as Alert objects (your requested method)
    public List<Alert> getAlertsForDoctorDetailed(String doctorEmail) {
        List<Alert> alerts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT a.id, a.condition, a.latitude, a.longitude, u.fname, u.lname, a.disabledEmail, a.doctorEmail " +
                "FROM alerts a JOIN users u ON a.disabledEmail = u.email " +
                "WHERE a.doctorEmail = ? ORDER BY a.id DESC";

        Cursor cursor = db.rawQuery(query, new String[]{doctorEmail});

        while (cursor.moveToNext()) {
            Alert alert = new Alert();
            alert.id = cursor.getInt(0);
            alert.condition = cursor.getString(1);
            alert.latitude = cursor.getDouble(2);
            alert.longitude = cursor.getDouble(3);
            alert.patientFirstName = cursor.getString(4);
            alert.patientLastName = cursor.getString(5);
            alert.disabledEmail = cursor.getString(6);
            alert.doctorEmail = cursor.getString(7);
            alerts.add(alert);
        }
        cursor.close();
        return alerts;
    }
}
