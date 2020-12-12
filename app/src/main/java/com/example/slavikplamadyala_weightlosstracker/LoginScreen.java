package com.example.slavikplamadyala_weightlosstracker;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // Configure all login screen UI events
        ConfigureControlEvents();
    }

    /**
     * Configure all UI element events
     */
    private void ConfigureControlEvents(){
        /* Configure "Login" button to check username information from SQLite Database */
        Button loginButton = findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check username information

            }
        });

        /* Configure "Register" button to check if currently entered user information exist
        inside SQLite database. If not then add username information inside database. */
        Button registerButton = findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get entered username and password
                String username = ((EditText)findViewById(R.id.editTextTextPersonName)).getText().toString();
                String password = ((EditText)findViewById(R.id.editTextTextPassword)).getText().toString();

                // Check if user is already registered
                if(SQLiteManager.CheckUser(username, password)){
                    // If already registered then display information notifying user
                    DisplayInfo(username + " Is Already Registered. Use Login Button.", Color.YELLOW, true);
                }else{
                    // If not then add to database and go to main screen

                    // Add user credentials inside SQLite database

                    // Notify user
                    DisplayInfo(username + " Has Been Registered.", Color.GREEN, true);

                    // Small delay to allow user to see message before moving to next screen
                    Delay(1000);

                    // Go to Main screen
                    startActivity(new Intent(LoginScreen.this, MainScreen.class ));
                }
            }
        });

        // Disable both buttons on application startup
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        /* Configure username and password text entry to enable/disable login and register buttons
         when no text has been entered */
        EditText usernameEditText = findViewById(R.id.editTextTextPersonName);
        EditText passwordEditText = findViewById(R.id.editTextTextPassword);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if password has been entered
                if(usernameEditText.getText().length() > 0 && passwordEditText.getText().length() > 0){
                    // Enable login and register buttons
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                }else{
                    // Disable login register buttons
                    loginButton.setEnabled(false);
                    registerButton.setEnabled(false);
                }

                if(usernameEditText.getText().length() > 0) {
                    //After user is done entering the text check if username exist inside database
                    if (SQLiteManager.CheckUser(usernameEditText.getText().toString(), passwordEditText.getText().toString())) {
                        // Let user know that username exists
                        DisplayInfo(usernameEditText.getText().toString() + " Exists", Color.GREEN, true);
                    } else {
                        // Let user know that username does not exists
                        DisplayInfo(usernameEditText.getText().toString() + " Does Not Exist", Color.RED, true);
                    }
                }else{
                    // Hide info view from user
                    DisplayInfo("", 0, false);
                }
            }
        });
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Check if username has been entered
                if(usernameEditText.getText().length() > 0 && passwordEditText.getText().length() > 0){
                    // Enable login and register buttons
                    loginButton.setEnabled(true);
                    registerButton.setEnabled(true);
                }else{
                    // Disable login register buttons
                    loginButton.setEnabled(false);
                    registerButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * Display information to user and control visible state
     * @param infoText Text to be displayed inside UI
     * @param infoColor Text color
     * @param visible Set UI visibility
     */
    private void DisplayInfo(String infoText, int infoColor, boolean visible){
        // Get reference to info UI
        TextView info = findViewById(R.id.information);
        if(visible)
        {
            // Change UI visibility so user can see it
            info.setVisibility(View.VISIBLE);
            // Set text color
            info.setTextColor(infoColor);
            // Set text for user to see
            info.setText(infoText);
        }else{
            // Change UI visibility so user can't see it
            info.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Hold thread of function that get executed for selected amount of milliseconds
     * @param milliseconds Delay in milliseconds
     */
    private void Delay(long milliseconds) {
        Handler handler = new Handler();
        handler.post(() -> runOnUiThread(() -> {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }));
    }
}



