package com.example.loansystemcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Locale;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseHelper extends SQLiteOpenHelper {
    /**
     * ----------
     * Database Properties
     * ----------
     **/
    private static final String DATABASE_NAME = "LoanSystemCalculator.db";
    private static final int DATABASE_VERSION = 2;

    /**
     * ----------
     * Table Properties
     * ----------
     **/

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

    /**
     * ----------
     * Create table statements
     * ----------
     **/



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

    /**
     * ----------
     * Abstract class methods (SQLiteOpenHelper)
     * ----------
     **/

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

        insertDefaultAdmin(db);
        insertDefaultLoanData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Create Admin table
            db.execSQL(CREATE_ADMIN_TABLE);

            // Insert default admin account
            insertDefaultAdmin(db);

            // Initialize loan data when upgrading
            insertDefaultLoanData(db);
        }
    }

    /**
     * ----------
     * Employee Methods
     * ----------
     **/

    // Generate employee ID
    public String generateEmployeeId(String middleInitial) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Get the initial from the first name (first character, uppercase)
        String firstInitial = middleInitial.length() >= 1 ?
                middleInitial.substring(0, 1).toUpperCase() : "X";

        // Generate 5-digit random number (00000 to 99999)
        String randomDigits;
        String baseId;
        int attempts = 0;

        do {
            // Generate random 5-digit number
            int randomNum = (int)(Math.random() * 100000);
            randomDigits = String.format("%05d", randomNum);

            // Combine initial with random digits
            baseId = firstInitial + randomDigits;

            // Check if this ID already exists
            attempts++;

            // Safety check to prevent infinite loop
            if (attempts > 1000) {
                throw new RuntimeException("Unable to generate unique employee ID after 1000 attempts");
            }

        } while (isEmployeeExists(baseId));

        return baseId;
    }


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


    // Register employee with auto-generated ID
    public boolean registerEmployee(String firstName, String middleInitial,
                                    String lastName, String dateHired, String password, double basicSalary) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Generate employee ID
        String employeeId = generateEmployeeId(middleInitial);

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

    // Register employee with pre-generated ID (for RegisterActivity)
    public boolean registerEmployeeWithId(String employeeId, String firstName, String middleInitial,
                                          String lastName, String dateHired, String password, double basicSalary) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if employee ID already exists
        if (isEmployeeExists(employeeId)) {
            return false; // ID already exists
        }

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

    /**
     * ----------
     * Admin Methods
     * ----------
     **/

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

    /**
     * ----------
     * Loan Methods
     * ----------
     **/

    // Insert loan types method (Emergency, Special, Regular)
    private void insertDefaultLoanData(SQLiteDatabase db) {
        // Insert loan types
        ContentValues emergency = new ContentValues();
        emergency.put(COLUMN_TYPE_NAME, "Emergency");
        emergency.put(COLUMN_MIN_AMOUNT, 5000.00);
        emergency.put(COLUMN_MAX_AMOUNT, 25000.00);
        emergency.put(COLUMN_SERVICE_CHARGE_RATE, 0.01);
        db.insert(TABLE_LOAN_TYPE, null, emergency);

        ContentValues special = new ContentValues();
        special.put(COLUMN_TYPE_NAME, "Special");
        special.put(COLUMN_MIN_AMOUNT, 50000.00);
        special.put(COLUMN_MAX_AMOUNT, 100000.00);
        special.put(COLUMN_SERVICE_CHARGE_RATE, 0.00);
        db.insert(TABLE_LOAN_TYPE, null, special);

        ContentValues regular = new ContentValues();
        regular.put(COLUMN_TYPE_NAME, "Regular");
        regular.putNull(COLUMN_MIN_AMOUNT);
        regular.putNull(COLUMN_MAX_AMOUNT);
        regular.put(COLUMN_SERVICE_CHARGE_RATE, 0.02);
        db.insert(TABLE_LOAN_TYPE, null, regular);

        // Insert interest rates for Emergency (only 6 months)
        ContentValues emergencyRate = new ContentValues();
        emergencyRate.put(COLUMN_LOAN_TYPE_ID, 1);
        emergencyRate.put(COLUMN_MIN_MONTHS, 6);
        emergencyRate.put(COLUMN_MAX_MONTHS, 6);
        emergencyRate.put(COLUMN_INTEREST_RATE, 0.0060);
        db.insert(TABLE_INTEREST_RATE, null, emergencyRate);

        // Insert interest rates for Special
        String[] specialRates = {"1,6,0.0060", "7,12,0.0062", "13,18,0.0065"};
        for (String rate : specialRates) {
            String[] parts = rate.split(",");
            ContentValues values = new ContentValues();
            values.put(COLUMN_LOAN_TYPE_ID, 2);
            values.put(COLUMN_MIN_MONTHS, Integer.parseInt(parts[0]));
            values.put(COLUMN_MAX_MONTHS, Integer.parseInt(parts[1]));
            values.put(COLUMN_INTEREST_RATE, Double.parseDouble(parts[2]));
            db.insert(TABLE_INTEREST_RATE, null, values);
        }

        // Insert interest rates for Regular
        String[] regularRates = {"1,5,0.0062", "6,10,0.0065", "11,15,0.0068", "16,20,0.0075", "21,24,0.0080"};
        for (String rate : regularRates) {
            String[] parts = rate.split(",");
            ContentValues values = new ContentValues();
            values.put(COLUMN_LOAN_TYPE_ID, 3);
            values.put(COLUMN_MIN_MONTHS, Integer.parseInt(parts[0]));
            values.put(COLUMN_MAX_MONTHS, Integer.parseInt(parts[1]));
            values.put(COLUMN_INTEREST_RATE, Double.parseDouble(parts[2]));
            db.insert(TABLE_INTEREST_RATE, null, values);
        }
    }

    // Create loan application
    public boolean applyForLoan(String employeeId, int loanTypeId, double requestedAmount,
                                int monthsToPay, double totalAmountDue, double interestRateApplied,
                                double interestAmount, double serviceChargeAmount, double takeHomeLoan) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMPLOYEE_ID, employeeId);
        values.put(COLUMN_LOAN_TYPE_ID, loanTypeId);
        values.put(COLUMN_REQUESTED_AMOUNT, requestedAmount);
        values.put(COLUMN_MONTHS_TO_PAY, monthsToPay);
        values.put(COLUMN_STATUS, "Pending");
        values.put(COLUMN_INTEREST_RATE_APPLIED, interestRateApplied);
        values.put(COLUMN_INTEREST_AMOUNT, interestAmount);
        values.put(COLUMN_SERVICE_CHARGE_AMOUNT, serviceChargeAmount);
        values.put(COLUMN_TAKE_HOME_LOAN, takeHomeLoan);
        values.put(COLUMN_TOTAL_AMOUNT_DUE, totalAmountDue);

        long result = db.insert(TABLE_LOAN_APPLICATION, null, values);
        return result != -1;
    }

    // Process loan as approved or denied
    public boolean updateLoanStatus(int loanId, String status, String adminId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, status);
        values.put(COLUMN_PROCESSED_BY_ADMIN_ID, adminId);

        String whereClause = COLUMN_LOAN_ID + " = ?";
        String[] whereArgs = {String.valueOf(loanId)};

        int result = db.update(TABLE_LOAN_APPLICATION, values, whereClause, whereArgs);
        return result > 0;
    }

    // Getters
    public double getInterestRate(int loanTypeId, int monthsToPay) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_INTEREST_RATE};
        String selection = COLUMN_LOAN_TYPE_ID + " = ? AND " + COLUMN_MIN_MONTHS + " <= ? AND " + COLUMN_MAX_MONTHS + " >= ?";
        String[] selectionArgs = {String.valueOf(loanTypeId), String.valueOf(monthsToPay), String.valueOf(monthsToPay)};

        Cursor cursor = db.query(TABLE_INTEREST_RATE, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            double rate = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_INTEREST_RATE));
            cursor.close();
            return rate;
        }
        cursor.close();
        return 0.0;
    }

    // Add this method to DatabaseHelper.java in the Loan Methods section

    // Get all loan applications for a specific employee
    public Cursor getEmployeeLoanApplications(String employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT la." + COLUMN_LOAN_ID + ", " +
                "lt." + COLUMN_TYPE_NAME + " as loanType, " +
                "la." + COLUMN_REQUESTED_AMOUNT + ", " +
                "la." + COLUMN_MONTHS_TO_PAY + ", " +
                "la." + COLUMN_STATUS + ", " +
                "la." + COLUMN_APPLICATION_DATE + ", " +
                "la." + COLUMN_TOTAL_AMOUNT_DUE + ", " +
                "la." + COLUMN_TAKE_HOME_LOAN + " " +
                "FROM " + TABLE_LOAN_APPLICATION + " la " +
                "INNER JOIN " + TABLE_LOAN_TYPE + " lt ON la." + COLUMN_LOAN_TYPE_ID + " = lt." + COLUMN_LOAN_TYPE_ID + " " +
                "WHERE la." + COLUMN_EMPLOYEE_ID + " = ? " +
                "ORDER BY la." + COLUMN_APPLICATION_DATE + " DESC";

        return db.rawQuery(query, new String[]{employeeId});
    }

    public Cursor getAllLoanApplications() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT la." + COLUMN_LOAN_ID + ", " +
                "e." + COLUMN_EMPLOYEE_FIRST_NAME + " || ' ' || e." + COLUMN_EMPLOYEE_LAST_NAME + " as employeeName, " +
                "lt." + COLUMN_TYPE_NAME + " as loanType, " +
                "la." + COLUMN_REQUESTED_AMOUNT + ", " +
                "la." + COLUMN_STATUS + ", " +
                "la." + COLUMN_APPLICATION_DATE + " " +
                "FROM " + TABLE_LOAN_APPLICATION + " la " +
                "INNER JOIN " + TABLE_EMPLOYEE + " e ON la." + COLUMN_EMPLOYEE_ID + " = e." + COLUMN_EMPLOYEE_ID + " " +
                "INNER JOIN " + TABLE_LOAN_TYPE + " lt ON la." + COLUMN_LOAN_TYPE_ID + " = lt." + COLUMN_LOAN_TYPE_ID + " " +
                "ORDER BY la." + COLUMN_APPLICATION_DATE + " DESC";

        return db.rawQuery(query, null);
    }

    public Cursor getPendingLoanApplications() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT la.*, " +
                "e." + COLUMN_EMPLOYEE_FIRST_NAME + " || ' ' || e." + COLUMN_EMPLOYEE_LAST_NAME + " as employeeName, " +
                "lt." + COLUMN_TYPE_NAME + " as loanTypeName " +
                "FROM " + TABLE_LOAN_APPLICATION + " la " +
                "INNER JOIN " + TABLE_EMPLOYEE + " e ON la." + COLUMN_EMPLOYEE_ID + " = e." + COLUMN_EMPLOYEE_ID + " " +
                "INNER JOIN " + TABLE_LOAN_TYPE + " lt ON la." + COLUMN_LOAN_TYPE_ID + " = lt." + COLUMN_LOAN_TYPE_ID + " " +
                "WHERE la." + COLUMN_STATUS + " = 'Pending' " +
                "ORDER BY la." + COLUMN_APPLICATION_DATE + " ASC";

        return db.rawQuery(query, null);
    }

    public Cursor getApprovedLoanApplications() {
        return getLoanApplicationsByStatus("Approved");
    }

    public Cursor getDeniedLoanApplications() {
        return getLoanApplicationsByStatus("Denied");
    }

    private Cursor getLoanApplicationsByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT la." + COLUMN_LOAN_ID + ", " +
                "e." + COLUMN_EMPLOYEE_FIRST_NAME + " || ' ' || e." + COLUMN_EMPLOYEE_LAST_NAME + " as employeeName, " +
                "lt." + COLUMN_TYPE_NAME + " as loanType, " +
                "la." + COLUMN_REQUESTED_AMOUNT + ", " +
                "la." + COLUMN_STATUS + ", " +
                "la." + COLUMN_APPLICATION_DATE + " " +
                "FROM " + TABLE_LOAN_APPLICATION + " la " +
                "INNER JOIN " + TABLE_EMPLOYEE + " e ON la." + COLUMN_EMPLOYEE_ID + " = e." + COLUMN_EMPLOYEE_ID + " " +
                "INNER JOIN " + TABLE_LOAN_TYPE + " lt ON la." + COLUMN_LOAN_TYPE_ID + " = lt." + COLUMN_LOAN_TYPE_ID + " " +
                "WHERE la." + COLUMN_STATUS + " = ? " +
                "ORDER BY la." + COLUMN_APPLICATION_DATE + " DESC";

        return db.rawQuery(query, new String[]{status});
    }

    /**
     * ----------
     * Public method to initialize loan data if needed
     * ----------
     **/

// Check if loan types are initialized, and initialize if not
    public void initializeLoanDataIfNeeded() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Check if LoanType table has any data
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_LOAN_TYPE, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();

            if (count == 0) {
                // Loan types not initialized, insert them
                SQLiteDatabase writableDb = this.getWritableDatabase();
                insertDefaultLoanData(writableDb);
            }
        }
    }

    // Add this helper method to get loan type ID by name
    public int getLoanTypeIdByName(String typeName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_LOAN_TYPE_ID};
        String selection = COLUMN_TYPE_NAME + " = ?";
        String[] selectionArgs = {typeName};

        Cursor cursor = db.query(TABLE_LOAN_TYPE, columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LOAN_TYPE_ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1; // Not found
    }
}