package com.example.loansystemcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseHelper extends SQLiteOpenHelper {
    /**----------
     Database Properties
     ----------**/
    private static final String DATABASE_NAME = "LoanSystemCalculator.db";
    private static final int DATABASE_VERSION = 1;

    /**----------
     Table Properties
     ----------**/

    // Employee table fields
    private static final String TABLE_EMPLOYEE = "Employee";
    private static final String COLUMN_EMPLOYEE_ID = "employeeId";
    private static final String COLUMN_EMPLOYEE_FIRST_NAME = "firstName";
    private static final String COLUMN_EMPLOYEE_MIDDLE_INITIAL = "middleInitial";
    private static final String COLUMN_EMPLOYEE_LAST_NAME = "lastName";
    private static final String COLUMN_EMPLOYEE_DATE_HIRED = "dateHired";
    private static final String COLUMN_EMPLOYEE_PASSWORD_HASH = "passwordHash";
    private static final String COLUMN_EMPLOYEE_BASIC_SALARY = "basicSalary";

    // Create Employee table
    private static final String CREATE_EMPLOYEE_TABLE = "CREATE TABLE " + TABLE_EMPLOYEE + "("
            + COLUMN_EMPLOYEE_ID + " VARCHAR(15) PRIMARY KEY,"
            + COLUMN_EMPLOYEE_FIRST_NAME + " NVARCHAR(100) NOT NULL,"
            + COLUMN_EMPLOYEE_MIDDLE_INITIAL + " CHAR(1),"
            + COLUMN_EMPLOYEE_LAST_NAME + " NVARCHAR(100) NOT NULL,"
            + COLUMN_EMPLOYEE_DATE_HIRED + " DATE NOT NULL"
            + COLUMN_EMPLOYEE_PASSWORD_HASH + " VARCHAR(255) NOT NULL"
            + COLUMN_EMPLOYEE_BASIC_SALARY + " DECIMAL(10,2) NOT NULL"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EMPLOYEE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
        onCreate(db);
    }

    /**----------
     Methods
     ----------**/

    // Hash password using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
