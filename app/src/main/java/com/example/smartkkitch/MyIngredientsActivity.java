package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyIngredientsActivity extends AppCompatActivity {

    private static final String TAG = "MyIngredientsActivity";

    private ArrayList<Ingredient> mIngredients = new ArrayList<>();

    private RecyclerView myIngredientsRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ingredients);

        myIngredientsRecyclerView = findViewById(R.id.myIngredientsRecyclerView);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        getIngredients();
    }

    private void getIngredients() {

        firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for(QueryDocumentSnapshot document: task.getResult()) {

                                String id = document.getId();
                                String name = document.get("name").toString();
                                String imageUrl = document.get("imageUrl").toString();

                                Ingredient ingredient = new Ingredient(id, name, imageUrl);

                                mIngredients.add(ingredient);

                            }

                            initRecyclerView();

                        }
                    }
                });
    }

    private void initRecyclerView() {

        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        myIngredientsRecyclerView.setLayoutManager(layoutManager);
        GenerateAdapter adapter = new GenerateAdapter(this, mIngredients);
        myIngredientsRecyclerView.setAdapter(adapter);
    }

    //Bottom navigation on item select listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //Switches between the item selected and starts corresponding activity
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent homeIntent = new Intent(getApplicationContext(), Home.class);
                    startActivity(homeIntent);
                    return true;
                case R.id.navigation_dashboard:
                    Intent myRecipesIntent = new Intent(getApplicationContext(), MyRecipesActivity.class);
                    startActivity(myRecipesIntent);
                    return true;
                case R.id.navigation_notifications:
                    Intent generateIntent = new Intent(getApplicationContext(), GenerateActivity.class);
                    startActivity(generateIntent);
                    return true;
                case R.id.navigation_ingredients:
                    return true;
            }
            return false;
        }
    };
}
