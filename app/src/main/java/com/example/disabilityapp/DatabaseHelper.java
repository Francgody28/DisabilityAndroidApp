package com.example.disabilityapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "disability.db";
    private static final int DB_VERSION = 2;  // increment when schema changes

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fname TEXT, " +
                "mname TEXT, " +
                "lname TEXT, " +
                "dob TEXT, " +
                "email TEXT UNIQUE, " +
                "phone TEXT, " +
                "password TEXT, " +
                "disabilityType TEXT, " +  // only for disabled users
                "specialist TEXT, " +      // only for doctors
                "userType TEXT)";          // "doctor" or "disabled"
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old table and recreate
        db.execSQL("DROP TABLE IF EXISTS users");
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

        long result = db.insert("users", null, cv);
        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert user for email: " + email);
            return false;
        }
        return true;
    }

    // Check if user exists with email & password (login)
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Get user type (doctor or disabled) by email
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
}
