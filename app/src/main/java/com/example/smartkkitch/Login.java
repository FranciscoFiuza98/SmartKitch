package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    //Creates EditText objects for email and password
    EditText txtEmail;
    EditText txtPassword;

    //Creates strings for email and  password
    String email, password;

    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Instantiates Firebase
        mAuth = FirebaseAuth.getInstance();

        //Gets instance of email and password inputs
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);

    }

    //Function called on Sign In button click
    public void SignIn(View view) {

        //Gets values from the email and password inputs
        email = txtEmail.getText().toString();
        password = txtPassword.getText().toString();

        //Attempts to sign in user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If the sign up is successful makes a toast informing the user and starts the WelcomeScreen Activity
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;
                            String name = user.getDisplayName();
                            Intent intent = new Intent(getApplicationContext(), WelcomeScreen.class);
                            intent.putExtra("name", name);
                            intent.putExtra("email", email);

                            startActivity(intent);

                            //TODO finish checking if user exists in database
                            /*FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                            firestore.collection("Users")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                                    Log.d(TAG, "onComplete: " + document);
                                                }
                                            } else {
                                                Log.w(TAG, "Error getting documents.", task.getException());
                                            }
                                        }
                                    });*/

                            //If the sign in was not successful, makes a toast for the user with the failure reason
                        } else {

                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    //Function called on Register button click
    public void Register(View view) {
        //Starts Register Activity
        Intent intent = new Intent(getApplicationContext(), Register.class);
        startActivity(intent);
    }
}
