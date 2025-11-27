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
    private static final int DATABASE_VERSION = 2;

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

    // Admin table fields
    private static final String TABLE_ADMIN = "Admin";
    private static final String COLUMN_ADMIN_ID = "adminId";
    private static final String COLUMN_ADMIN_PASSWORD_HASH = "passwordHash";

    // LoanType table fields
    private static final String TABLE_LOAN_TYPE = "LoanType";
    private static final String COLUMN_LOAN_TYPE_ID = "loanTypeId";
    private static final String COLUMN_TYPE_NAME = "typeName";
    private static final String COLUMN_MIN_AMOUNT = "minAmount";
    private static final String COLUMN_MAX_AMOUNT = "maxAmount";
    private static final String COLUMN_SERVICE_CHARGE_RATE = "serviceChargeRate";

    // InterestRate table fields
    private static final String TABLE_INTEREST_RATE = "InterestRate";
    private static final String COLUMN_RATE_ID = "rateId";
    private static final String COLUMN_MIN_MONTHS = "minMonths";
    private static final String COLUMN_MAX_MONTHS = "maxMonths";
    private static final String COLUMN_INTEREST_RATE = "interestRate";

    // LoanApplication table fields
    private static final String TABLE_LOAN_APPLICATION = "LoanApplication";
    private static final String COLUMN_LOAN_ID = "loanId";
    private static final String COLUMN_REQUESTED_AMOUNT = "requestedAmount";
    private static final String COLUMN_MONTHS_TO_PAY = "monthsToPay";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_INTEREST_RATE_APPLIED = "interestRateApplied";
    private static final String COLUMN_INTEREST_AMOUNT = "interestAmount";
    private static final String COLUMN_SERVICE_CHARGE_AMOUNT = "serviceChargeAmount";
    private static final String COLUMN_TAKE_HOME_LOAN = "takeHomeLoan";
    private static final String COLUMN_TOTAL_AMOUNT_DUE = "totalAmountDue";
    private static final String COLUMN_PROCESSED_BY_ADMIN_ID = "processedByAdminId";
    private static final String COLUMN_APPLICATION_DATE = "applicationDate";

    /**----------
     Create table statements
     ----------**/

    // Create Employee table
    private static final String CREATE_EMPLOYEE_TABLE = "CREATE TABLE " + TABLE_EMPLOYEE + "("
            + COLUMN_EMPLOYEE_ID + " VARCHAR(15) PRIMARY KEY,"
            + COLUMN_EMPLOYEE_FIRST_NAME + " NVARCHAR(100) NOT NULL,"
            + COLUMN_EMPLOYEE_MIDDLE_INITIAL + " CHAR(1),"
            + COLUMN_EMPLOYEE_LAST_NAME + " NVARCHAR(100) NOT NULL,"
            + COLUMN_EMPLOYEE_DATE_HIRED + " DATE NOT NULL,"
            + COLUMN_EMPLOYEE_PASSWORD_HASH + " VARCHAR(255) NOT NULL,"
            + COLUMN_EMPLOYEE_BASIC_SALARY + " DECIMAL(10,2) NOT NULL"
            + ")";

    // Create Admin table
    private static final String CREATE_ADMIN_TABLE = "CREATE TABLE " + TABLE_ADMIN + "("
            + COLUMN_ADMIN_ID + " VARCHAR(15) PRIMARY KEY,"
            + COLUMN_ADMIN_PASSWORD_HASH + " VARCHAR(255) NOT NULL"
            + ")";

    // Create LoanType table
    private static final String CREATE_LOAN_TYPE_TABLE = "CREATE TABLE " + TABLE_LOAN_TYPE + "("
            + COLUMN_LOAN_TYPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TYPE_NAME + " NVARCHAR(50) NOT NULL UNIQUE,"
            + COLUMN_MIN_AMOUNT + " DECIMAL(10,2),"
            + COLUMN_MAX_AMOUNT + " DECIMAL(10,2),"
            + COLUMN_SERVICE_CHARGE_RATE + " DECIMAL(5,4)"
            + ")";

    // Create InterestRate table
    private static final String CREATE_INTEREST_RATE_TABLE = "CREATE TABLE " + TABLE_INTEREST_RATE + "("
            + COLUMN_RATE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_LOAN_TYPE_ID + " INTEGER NOT NULL,"
            + COLUMN_MIN_MONTHS + " INTEGER NOT NULL,"
            + COLUMN_MAX_MONTHS + " INTEGER NOT NULL,"
            + COLUMN_INTEREST_RATE + " DECIMAL(5,4) NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_LOAN_TYPE_ID + ") REFERENCES " + TABLE_LOAN_TYPE + "(" + COLUMN_LOAN_TYPE_ID + ")"
            + ")";

    // Create LoanApplication table
    private static final String CREATE_LOAN_APPLICATION_TABLE = "CREATE TABLE " + TABLE_LOAN_APPLICATION + "("
            + COLUMN_LOAN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_EMPLOYEE_ID + " VARCHAR(15) NOT NULL,"
            + COLUMN_LOAN_TYPE_ID + " INTEGER NOT NULL,"
            + COLUMN_REQUESTED_AMOUNT + " DECIMAL(10,2) NOT NULL,"
            + COLUMN_MONTHS_TO_PAY + " INTEGER NOT NULL,"
            + COLUMN_STATUS + " NVARCHAR(50) NOT NULL DEFAULT 'Pending',"
            + COLUMN_INTEREST_RATE_APPLIED + " DECIMAL(5,4),"
            + COLUMN_INTEREST_AMOUNT + " DECIMAL(10,2),"
            + COLUMN_SERVICE_CHARGE_AMOUNT + " DECIMAL(10,2),"
            + COLUMN_TAKE_HOME_LOAN + " DECIMAL(10,2),"
            + COLUMN_TOTAL_AMOUNT_DUE + " DECIMAL(10,2),"
            + COLUMN_PROCESSED_BY_ADMIN_ID + " VARCHAR(15),"
            + COLUMN_APPLICATION_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_EMPLOYEE_ID + ") REFERENCES " + TABLE_EMPLOYEE + "(" + COLUMN_EMPLOYEE_ID + "),"
            + "FOREIGN KEY(" + COLUMN_LOAN_TYPE_ID + ") REFERENCES " + TABLE_LOAN_TYPE + "(" + COLUMN_LOAN_TYPE_ID + "),"
            + "FOREIGN KEY(" + COLUMN_PROCESSED_BY_ADMIN_ID + ") REFERENCES " + TABLE_ADMIN + "(" + COLUMN_ADMIN_ID + ")"
            + ")";

    /**----------
     Abstract class methods (SQLiteOpenHelper)
     ----------**/

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EMPLOYEE_TABLE);
        db.execSQL(CREATE_ADMIN_TABLE);
        db.execSQL(CREATE_LOAN_TYPE_TABLE);
        db.execSQL(CREATE_INTEREST_RATE_TABLE);
        db.execSQL(CREATE_LOAN_APPLICATION_TABLE);

        // Insert default admin account
        insertDefaultAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Create Admin table for version 2
            db.execSQL(CREATE_ADMIN_TABLE);
            // Insert default admin account
            insertDefaultAdmin(db);
        }
    }

    /**----------
     Employee Methods
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

    // Register employee
    // Register employee with auto-generated ID
    public boolean registerEmployee(String firstName, String middleInitial,
                                    String lastName, String dateHired, String password, double basicSalary) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Generate employee ID
        String employeeId = generateEmployeeId(firstName, lastName, middleInitial);

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMPLOYEE_ID, employeeId);
        values.put(COLUMN_EMPLOYEE_FIRST_NAME, firstName);
        values.put(COLUMN_EMPLOYEE_MIDDLE_INITIAL, middleInitial);
        values.put(COLUMN_EMPLOYEE_LAST_NAME, lastName);
        values.put(COLUMN_EMPLOYEE_DATE_HIRED, dateHired);
        values.put(COLUMN_EMPLOYEE_PASSWORD_HASH, hashPassword(password));
        values.put(COLUMN_EMPLOYEE_BASIC_SALARY, basicSalary);

        long result = db.insert(TABLE_EMPLOYEE, null, values);
        return result != -1;
    }

    private boolean isEmployeeExists(String employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_EMPLOYEE_ID};
        String selection = COLUMN_EMPLOYEE_ID + " = ?";
        String[] selectionArgs = {employeeId};

        Cursor cursor = db.query(TABLE_EMPLOYEE, columns, selection, selectionArgs, null, null, null);

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Validate employee login
    public boolean validateEmployeeLogin(String employeeId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_EMPLOYEE_PASSWORD_HASH};
        String selection = COLUMN_EMPLOYEE_ID + " = ?";
        String[] selectionArgs = {employeeId};

        Cursor cursor = db.query(TABLE_EMPLOYEE, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMPLOYEE_PASSWORD_HASH));
            cursor.close();
            return storedHash.equals(hashPassword(password));
        }
        cursor.close();
        return false;
    }

    // Get employee date hired
    public LocalDate getEmployeeDateHired(String employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_EMPLOYEE_DATE_HIRED};
        String selection = COLUMN_EMPLOYEE_ID + " = ?";
        String[] selectionArgs = {employeeId};

        Cursor cursor = db.query(TABLE_EMPLOYEE, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String dateString = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMPLOYEE_DATE_HIRED));
            cursor.close();
            // Parse the date string to LocalDate
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return LocalDate.parse(dateString, formatter);
        }
        cursor.close();
        return null;
    }

    // Get employee basic salary
    public double getEmployeeBasicSalary(String employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_EMPLOYEE_BASIC_SALARY};
        String selection = COLUMN_EMPLOYEE_ID + " = ?";
        String[] selectionArgs = {employeeId};

        Cursor cursor = db.query(TABLE_EMPLOYEE, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            double salary = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EMPLOYEE_BASIC_SALARY));
            cursor.close();
            return salary;
        }
        cursor.close();
        return 0.0;
    }

    // Generate employee ID
    public String generateEmployeeId(String firstName, String lastName, String middleInitial) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get first 3 letters of last name (uppercase)
        String lastPart = lastName.length() >= 3 ?
                lastName.substring(0, 3).toUpperCase() :
                String.format("%-3s", lastName).toUpperCase().trim();

        // Get first letter of first name (uppercase)
        String firstPart = firstName.length() >= 1 ?
                firstName.substring(0, 1).toUpperCase() :
                "X";

        // Get middle initial (uppercase) or "X" if null/empty
        String middlePart = (middleInitial != null && !middleInitial.isEmpty()) ?
                middleInitial.toUpperCase() : "X";

        String baseId = lastPart + firstPart + middlePart;
        String employeeId = baseId + "001"; // Start with 001

        // Check if this ID already exists and increment if needed
        int counter = 1;
        while (isEmployeeExists(employeeId)) {
            counter++;
            employeeId = baseId + String.format("%03d", counter);

            // Safety check to prevent infinite loop
            if (counter > 999) {
                throw new RuntimeException("Too many employees with similar names");
            }
        }

        return employeeId;
    }

    /**----------
     Admin Methods
     ----------**/

    // Insert default admin account
    private void insertDefaultAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ADMIN_ID, "admin");
        values.put(COLUMN_ADMIN_PASSWORD_HASH, hashPassword("admin123"));
        db.insert(TABLE_ADMIN, null, values);
    }

    // Validate admin login
    public boolean validateAdminLogin(String adminId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ADMIN_PASSWORD_HASH};
        String selection = COLUMN_ADMIN_ID + " = ?";
        String[] selectionArgs = {adminId};

        Cursor cursor = db.query(TABLE_ADMIN, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADMIN_PASSWORD_HASH));
            cursor.close();
            return storedHash.equals(hashPassword(password));
        }
        cursor.close();
        return false;
    }

    // Get all employees (for admin records viewer)
    public Cursor getAllEmployees() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {
                COLUMN_EMPLOYEE_ID,
                COLUMN_EMPLOYEE_FIRST_NAME,
                COLUMN_EMPLOYEE_MIDDLE_INITIAL,
                COLUMN_EMPLOYEE_LAST_NAME,
                COLUMN_EMPLOYEE_DATE_HIRED,
                COLUMN_EMPLOYEE_BASIC_SALARY
        };

        return db.query(TABLE_EMPLOYEE, columns, null, null, null, null,
                COLUMN_EMPLOYEE_LAST_NAME + ", " + COLUMN_EMPLOYEE_FIRST_NAME);
    }
}