package com.example.slavikplamadyala_weightlosstracker;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Handle all SQLite database CRUD functions
 */
public class SQLiteManager extends  SQLiteOpenHelper{

    private SQLiteDatabase database; // Database object
    private static final int DATABASE_VERSION = 8;
    private static final String DATABASE_NAME = "wtusers.db";

    private static final String USER_CREDENTIALS_TABLE_NAME = "userCredentials";
    private static final String USER_LOG_TABLE_NAME = "userLog";

    // Shared between both tables
    private static final String USER_ID_COLUMN_NAME = "userId";

    // USER_CREDENTIALS_TABLE Columns
    private static final String USER_NAME_COLUMN_NAME = "userName";
    private static final String USER_PASSWORD_COLUMN_NAME = "userPassword";
    private static final String USER_GOAL_WEIGHT_COLUMN_NAME = "userGoalWeight";
    private static final String USER_NOTIFY_COLUMN_NAME = "sendMessages";

    // USER_LOG_TABLE Columns
    private static final String USER_LOG_ID_COLUMN_NAME = "logID";
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
        String command = "INSERT INTO " + USER_CREDENTIALS_TABLE_NAME + " (" +
                USER_NAME_COLUMN_NAME + ", " +
                USER_PASSWORD_COLUMN_NAME + ", " +
                USER_GOAL_WEIGHT_COLUMN_NAME  + ", " +
                USER_NOTIFY_COLUMN_NAME +
                ") VALUES ('" +
                userName + "', '" +
                userPassword + "', '0', 'False')";
        try{
            database.execSQL(command);
            return true;
        }catch (SQLException e){
            Log.e("MyApp", "AddUserCredentials", e);
            return false;
        }
    }

    /**
     * Get selected user goal weight
     * @param userName User Name
     * @return Positive numeric value
     */
    public int GetUserGoalWeight(String userName){
        String query = "SELECT * FROM " + USER_CREDENTIALS_TABLE_NAME + " WHERE " + USER_NAME_COLUMN_NAME + " = '" + userName + "'";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    // Select weight goal column
                    cursor.moveToFirst();
                    return Integer.parseInt(cursor.getString(cursor.getColumnIndex(USER_GOAL_WEIGHT_COLUMN_NAME)));
                }
            }
            return -1;
        }
    }

    /**
     * Get selected user last logged weight
     * @param userName User Name
     * @return Positive numeric value
     */
    public int GetUserLastWeight(String userName){
        // Get id of selected user
        int userID = GetUserID(userName);
        if(userID < 0){
            return -1;
        }

        // Construct query to get all records for selected user
        String query = "SELECT * FROM " + USER_LOG_TABLE_NAME + " WHERE " + USER_ID_COLUMN_NAME + " = '" + userID + "' ORDER BY " + USER_LOG_ID_COLUMN_NAME + " DESC LIMIT 1";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    cursor.moveToFirst();
                    // Return weight
                    return Integer.parseInt(cursor.getString(cursor.getColumnIndex(USER_LOG_CURRENT_WEIGHT_COLUMN_NAME)));
                }
            }
            return -1;
        }
    }

    /**
     * Update user current goal weight
     * @param userName User Name
     * @param goalWeight Goal weight
     * @return True if set successfully
     */
    public boolean UpdateGoalWeight(String userName, String goalWeight){
        if(goalWeight.isEmpty()){
            return false;
        }

        // Update selected user goal weight
        String command = "UPDATE " + USER_CREDENTIALS_TABLE_NAME + " SET " +
                USER_GOAL_WEIGHT_COLUMN_NAME + " = '" + goalWeight + "' WHERE " +
                USER_NAME_COLUMN_NAME + " = '" + userName + "'";

        try{
            database.execSQL(command);
            return true;
        }catch (SQLException e){
            Log.e("MyApp", "UpdateGoalWeight", e);
            return false;
        }
    }

    /**
     * Add a log of current weight
     * @param userName User Name
     * @param currentWeight User Current Weight
     * @return True if log added successfully
     */
    public boolean LogCurrentWeight(String userName, String currentWeight){
        if(currentWeight.isEmpty()){
            return false;
        }
        // Get id of selected user
        int userID = GetUserID(userName);
        if(userID < 0){
            return false;
        }

        // Get today date and construct date string
        Calendar calendar = Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("EEEE MMMM F, y");
        String logDate = df.format(calendar.getTime());

        // Check if record already exist for current date and update it
        String query = "SELECT 1 FROM " + USER_LOG_TABLE_NAME + " WHERE " + USER_ID_COLUMN_NAME + " = '" + userID + "' AND " +
                USER_LOG_DATE_COLUMN_NAME + " = '" + logDate + "'";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    // Record exist so lets update it
                    String command = "UPDATE " + USER_LOG_TABLE_NAME + " SET " +
                            USER_LOG_CURRENT_WEIGHT_COLUMN_NAME + " = '" + currentWeight + "' WHERE " +
                            USER_LOG_ID_COLUMN_NAME + " = " + cursor.getInt(0);
                    try{
                        database.execSQL(command);
                        return true;
                    }catch (SQLException e){
                        Log.e("MyApp", "LogCurrentWeight", e);
                        return false;
                    }
                }else{ // Create new record
                    // Insert log row into database
                    String command = "INSERT INTO " + USER_LOG_TABLE_NAME + " (" +
                            USER_ID_COLUMN_NAME + ", " +
                            USER_LOG_CURRENT_WEIGHT_COLUMN_NAME + ", " +
                            USER_LOG_DATE_COLUMN_NAME +
                            ") VALUES ('" +
                            userID + "', '" +
                            currentWeight + "', '" +
                            logDate + "')";
                    try{
                        database.execSQL(command);
                        return true;
                    }catch (SQLException e){
                        Log.e("MyApp", "LogCurrentWeight", e);
                        return false;
                    }
                }
            }
        }



        return true;
    }

    /**
     * Return selected user ID
     * @param userName User Name
     * @return User ID
     */
    private int GetUserID(String userName){
        String query = "SELECT * FROM " + USER_CREDENTIALS_TABLE_NAME + " WHERE " + USER_NAME_COLUMN_NAME + " = '" + userName + "'";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    cursor.moveToFirst();
                    return cursor.getInt(0);
                }
            }
            return -1;
        }
    }

    /**
     * Class to store date and weight
     */
    public class UserLog{
        public String Date;
        public String Weight;

        public UserLog(String date, String weight){
            Date = date;
            Weight = weight;
        }
    }

    /**
     * Gather all latest logs up to 10
     * @param userName User Name
     * @return Array of logs
     */
    public UserLog[] GetLatestLogs(String userName){
        UserLog[] userLog;
        boolean endOfLogs = true;
        // Get id of selected user
        int userID = GetUserID(userName);
        if(userID < 0){
            return null;
        }

        // Construct query to pull up to 10 latest logs
        String query = "SELECT * FROM " + USER_LOG_TABLE_NAME + " WHERE " + USER_ID_COLUMN_NAME + " = '" + userID + "' ORDER BY " + USER_LOG_ID_COLUMN_NAME + " DESC LIMIT 10";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    userLog = new UserLog[cursor.getCount()];
                    cursor.moveToFirst();
                    while(endOfLogs){
                        // Get date and weight
                        userLog[cursor.getPosition()] = new UserLog(cursor.getString(cursor.getColumnIndex(USER_LOG_DATE_COLUMN_NAME)),
                                cursor.getString(cursor.getColumnIndex(USER_LOG_CURRENT_WEIGHT_COLUMN_NAME))) ;

                        // Move to next element
                        endOfLogs = cursor.moveToNext();
                    }
                    return userLog;
                }
            }
            return null;
        }
    }

    /**
     * Clear all selected user logs
     * @param userName User Name
     * @return True if logs deleted successfully
     */
    public boolean ClearLogs(String userName){
        // Get id of selected user
        int userID = GetUserID(userName);
        if(userID < 0){
            return false;
        }

        String command = "DELETE FROM " + USER_LOG_TABLE_NAME + " WHERE " + USER_ID_COLUMN_NAME + " = '" + userID + "'";
        try{
            database.execSQL(command);
            return true;
        }catch (SQLException e){
            Log.e("MyApp", "ClearLogs", e);
            return false;
        }
    }

    /**
     * Indicate if messages should be send to user
     * @param userName User Name
     * @return True if messages should be sent
     */
    public boolean ShouldSendMessages(String userName){
        String query = "SELECT * FROM " + USER_CREDENTIALS_TABLE_NAME + " WHERE " + USER_NAME_COLUMN_NAME + " = '" + userName + "'";
        try (Cursor cursor = database.rawQuery(query, null)) {
            if(cursor!=null) {
                if(cursor.getCount()>0) {
                    cursor.moveToFirst();
                    return cursor.getString(cursor.getColumnIndex(USER_NOTIFY_COLUMN_NAME)).equals("True");
                }
            }
            return false;
        }
    }

    /**
     * Set status if messages should be sent
     * @param userName User Name
     * @param state True/False
     * @return True if message sending status updated successfully
     */
    public boolean SetSendMessages(String userName, String state){
        // Update send message status
        String command = "UPDATE " + USER_CREDENTIALS_TABLE_NAME + " SET " +
                USER_NOTIFY_COLUMN_NAME + " = '" + state + "' WHERE " +
                USER_NAME_COLUMN_NAME + " = '" + userName + "'";
        try{
            database.execSQL(command);
            return true;
        }catch (SQLException e){
            Log.e("MyApp", "LogCurrentWeight", e);
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
                USER_GOAL_WEIGHT_COLUMN_NAME + " TEXT, " +
                USER_NOTIFY_COLUMN_NAME + " TEXT)");

        // Create user log table
        db.execSQL("CREATE TABLE " + USER_LOG_TABLE_NAME + " (" +
                USER_LOG_ID_COLUMN_NAME + " INTEGER PRIMARY KEY," +
                USER_ID_COLUMN_NAME + " TEXT," +
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


