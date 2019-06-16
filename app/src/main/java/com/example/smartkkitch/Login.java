package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class Login extends AppCompatActivity {

    //Creates firebase object
    private FirebaseAuth mAuth;
    private boolean userExists = false;

    //Creates EditText objects for email and password
    EditText txtEmail;
    EditText txtPassword;

    //Creates strings for email and  password
    String email, password;

    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_login);

        //Instantiates Firebase
        mAuth = FirebaseAuth.getInstance();

        //Gets instance of email and password inputs
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtPassword.setTransformationMethod(new PasswordTransformationMethod());

    }

    //Function called on Sign In button click
    public void SignIn(View view) {

        //Gets values from the email and password inputs
        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();

        Log.d(TAG, "Email: " + email);
        Log.d(TAG, "Password: " + password);

        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Enter your email and password", Toast.LENGTH_SHORT).show();
        } else {
            //Attempts to sign in user
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //If the sign up is successful makes a toast informing the user and starts the WelcomeScreen Activity
                            if (task.isSuccessful()) {

                                final FirebaseUser user = mAuth.getCurrentUser();

                                //Gets firestore instance
                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                //Gets Users collection from firestore and checks if user exists in the collection
                                firestore.collection("Users")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                                        //Checks if user exists in collection, if exists starts Home activity
                                                        String userEmail = document.getId();
                                                        if (email.equals(userEmail)) {
                                                            userExists = true;
                                                            Intent intent = new Intent(getBaseContext(), Home.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }

                                                    }

                                                    //If user doesn't exist in firebase collection, starts WelcomeScreen activity
                                                    assert user != null;
                                                    if (!userExists) {
                                                        String name = user.getDisplayName();
                                                        Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
                                                        intent.putExtra("name", name);
                                                        intent.putExtra("email", email);

                                                        startActivity(intent);
                                                        finish();
                                                    }

                                                } else {
                                                    Log.w(TAG, "Error getting documents.", task.getException());
                                                }
                                            }
                                        });




                                //If the sign in was not successful, makes a toast for the user with the failure reason
                            } else {

                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    //Function called on Register button click
    public void Register(View view) {
        //Starts Register Activity
        Intent intent = new Intent(getApplicationContext(), Register.class);
        startActivity(intent);
    }
}
