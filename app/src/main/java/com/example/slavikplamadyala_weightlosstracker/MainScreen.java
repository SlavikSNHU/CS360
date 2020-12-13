package com.example.slavikplamadyala_weightlosstracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainScreen extends AppCompatActivity {
    private static final int PERMISSION_SEND_SMS = 123;
    private SQLiteManager db;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        userName = getIntent().getStringExtra("userName");

        // Connect to database
        db = new SQLiteManager(this);

        // Set permission to send messages
        RequestSmsPermission();

        // Update UI to match user current progress
        UpdateMainUI();

        // Configure all main screen UI events
        ConfigureControlEvents(this);
    }

    /**
     * Prompt user to allow app to send messages
     */
    private void RequestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        }
    }

    private void PopulateGridComponents(SQLiteManager.UserLog[] userLogs) {
        GridLayout gl = findViewById(R.id.mainLayout);
        for (int i = 0; i < gl.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) gl.getChildAt(i);

            // Check bounds and set control visibility
            if (i >= userLogs.length) {
                layout.setVisibility(View.INVISIBLE);
                continue;
            }

            // Make control visible
            layout.setVisibility(View.VISIBLE);

            // Set date
            ((TextView) layout.getChildAt(0)).setText(userLogs[i].Date);

            // Set weight
            ((TextView) layout.getChildAt(1)).setText(userLogs[i].Weight + "lb");
        }
    }

    private void HideLogs() {
        // Set all logs invisible
        GridLayout gl = findViewById(R.id.mainLayout);
        for (int i = 0; i < gl.getChildCount(); i++) {
            LinearLayout layout = (LinearLayout) gl.getChildAt(i);
            // Make control visible
            layout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Update current UI with data from database
     */
    private void UpdateMainUI() {
        // Update progress bar based on current goal and weight
        ProgressBar pb = findViewById(R.id.pbWeightGoal);
        TextView goalTextView = findViewById(R.id.textCurrentGoal);

        // Get goal and last logged weight from database
        int currentGoal = db.GetUserGoalWeight(userName);
        int lastWeight = db.GetUserLastWeight(userName);

        pb.setMax(0);
        pb.setProgress(0);

        // Make sure correct values been gathered from database
        if (currentGoal < 0 || lastWeight < 0) {
            HideLogs();
            return;
        }

        pb.setMin(0);

        // Determine if goal has been reached
        if (currentGoal < lastWeight) {
            pb.setMax(lastWeight);
            pb.setProgress(currentGoal);
        } else {
            // Goal achieved!
            pb.setMax(currentGoal);
            pb.setProgress(currentGoal);

            // Check if SMS should be sent
            if (db.ShouldSendMessages(userName)) {
                // Get total number of days that took to reach goal
                int numberOfDays = db.GetTotalNumberOfDays(userName);
                if(numberOfDays > 0){
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("555-521-5554", null, "Congratulation You Have Reached Goal Weight of " + currentGoal + "lb After " + numberOfDays + " Days!", null, null);
                }
            }

            // Display congratulation message
            new AlertDialog.Builder(this)
                    .setTitle("Goal Achieved!")
                    .setMessage("Congratulations Your Weight Goal is Achieved!")

                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Start sending messages
                        }
                    })

                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();

            // Clear all records
            if(!db.ClearLogs(userName)){
                return;
            }
        }

        // Update goal text
        goalTextView.setText(String.valueOf(currentGoal));

        // Get array of all recent logs
        SQLiteManager.UserLog[] userLogs = db.GetLatestLogs(userName);
        if(userLogs == null){
            Log.e("MyApp", "No User Logs Returned");
            HideLogs();
            return;
        }

        // Populate grid
        PopulateGridComponents(userLogs);
    }

    /**
     * Configure all UI element events
     */
    private void ConfigureControlEvents(Context context){
        Button addWeightButton = findViewById(R.id.btnAddWeight);
        Button setGoalWeightButton = findViewById(R.id.btnAddGoal);
        Button logOut = findViewById(R.id.btnLogOut);

        // Update user goal weight
        setGoalWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter New Goal Weight");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Update goal text
                        TextView goalTextView = findViewById(R.id.textCurrentGoal);
                        goalTextView.setText(input.getText().toString());

                        // Attempt to set new goal weight
                        if(db.UpdateGoalWeight(userName, goalTextView.getText().toString())){
                            UpdateMainUI();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        // Log user current weight
        addWeightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Enter Current Weight");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Attempt to set new goal weight
                        if(db.LogCurrentWeight(userName, input.getText().toString())){
                            UpdateMainUI();
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        // Go back to log in screen
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToLoginScreen();
            }
        });
    }

    /**
     * Prompt user if they want to receive weight goal achieved messages
     * @param view
     */
    public void SetMessageAccess(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Message Permission")
                .setMessage("Allow app sending messages when goal is achieved?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Start sending messages
                        db.SetSendMessages(userName, "True");
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Stop sending messages
                        db.SetSendMessages(userName, "False");
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Switch to login screen
     */
    private void GoToLoginScreen(){
        db.close();
        Intent intent = new Intent(MainScreen.this, LoginScreen.class );
        startActivity(intent);
    }
}
