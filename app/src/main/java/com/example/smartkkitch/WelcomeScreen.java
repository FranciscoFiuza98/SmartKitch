package com.example.smartkkitch;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

public class WelcomeScreen extends AppCompatActivity {

    //Variables
    TextView txtWelcome;

    String name;
    String welcomeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        //Instantiates the welcome title so we can add the user name
        txtWelcome = findViewById(R.id.txtWelcome);

        //Gets intent sent from previous activity and gets intent extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Updates the welcome message with the user's name
        if (extras != null) {
            name = extras.getString("name");
            welcomeMessage = "Welcome " + name + "!";
            txtWelcome.setText(welcomeMessage);

        }
    }

    //Starts next activity when "Lets get started" button is pressed
    public void getStarted(View view) {
        Intent intent = new Intent(getApplicationContext(), FirstFiveIngredients.class);
        startActivity(intent);
    }
}
