package com.example.slavikplamadyala_weightlosstracker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Handle all SQLite database CRUD functions
 */
public class SQLiteManager extends  SQLiteOpenHelper{

    private SQLiteDatabase database; // Database object
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "wtusers.db";

    private static final String USER_CREDENTIALS_TABLE_NAME = "userCredentials";
    private static final String USER_LOG_TABLE_NAME = "userLog";

    // Shared between both tables
    private static final String USER_ID_COLUMN_NAME = "userId";

    // USER_CREDENTIALS_TABLE Columns
    private static final String USER_NAME_COLUMN_NAME = "userName";
    private static final String USER_PASSWORD_COLUMN_NAME = "userPassword";
    private static final String USER_GOAL_WEIGHT_COLUMN_NAME = "userGoalWeight";

    // USER_LOG_TABLE Columns
    private static final String USER_LOG_DATE_COLUMN_NAME = "date";
    private static final String USER_LOG_CURRENT_WEIGHT_COLUMN_NAME = "userWeight";

    /**
     * Initialize database connection
     * @param context
     */
    public SQLiteManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    /**
     * Check if user exists
     * @param userName User name
     * @return True if user exists
     */
    public boolean CheckUser(String userName){
        String query = "SELECT 1 FROM " + USER_CREDENTIALS_TABLE_NAME + " WHERE " + USER_NAME_COLUMN_NAME + " = '" + userName + "'";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Check if user credentials exist inside database
     * @param userName User Name
     * @param userPassword Password
     * @return True if user credentials matches
     */
    public boolean CheckUserCredentials(String userName, String userPassword){
        String query = "SELECT 1 FROM " + USER_CREDENTIALS_TABLE_NAME + " WHERE " + USER_NAME_COLUMN_NAME + " = '" + userName +
                "' AND " + USER_PASSWORD_COLUMN_NAME + " = '" + userPassword + "'";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Add user credentials inside database
     * @param userName User Name
     * @param userPassword Password
     * @return True if successfully added inside database
     */
    public boolean AddUserCredentials(String userName, String userPassword){
        String query = "INSERT INTO " + USER_CREDENTIALS_TABLE_NAME + " (" +
                USER_NAME_COLUMN_NAME + ", " +
                USER_PASSWORD_COLUMN_NAME + ", " +
                USER_GOAL_WEIGHT_COLUMN_NAME +
                ") VALUES ('" +
                userName + "', '" +
                userPassword + "', '0')";
        try{
            database.execSQL(query);
            return true;
        }catch (SQLException e){
            Log.e("MyApp", "AddUserCredentials", e);
            return false;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user credentials and goal table
        db.execSQL("CREATE TABLE " + USER_CREDENTIALS_TABLE_NAME + " (" +
                USER_ID_COLUMN_NAME + " INTEGER PRIMARY KEY," +
                USER_NAME_COLUMN_NAME + " TEXT," +
                USER_PASSWORD_COLUMN_NAME + " TEXT, " +
                USER_GOAL_WEIGHT_COLUMN_NAME + " TEXT)");

        // Create user log table
        db.execSQL("CREATE TABLE " + USER_LOG_TABLE_NAME + " (" +
                USER_LOG_CURRENT_WEIGHT_COLUMN_NAME + " TEXT," +
                USER_LOG_DATE_COLUMN_NAME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_CREDENTIALS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + USER_LOG_TABLE_NAME);
        onCreate(db);
    }
}
