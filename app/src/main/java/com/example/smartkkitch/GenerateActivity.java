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
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class GenerateActivity extends AppCompatActivity {

    private static final String TAG = "GenerateActivity";

    private ArrayList<Ingredient> mFavoriteIngredients = new ArrayList<>();

    FirebaseFirestore firestore;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    GenerateAdapter adapter;

    Button btnGenerate;
    RecyclerView generateRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);

        //Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //Gets Firestore and FirebaseAuth instances and gets current user
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Gets Recycler View reference
        generateRecyclerView = findViewById(R.id.generateRecyclerView);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRecipe();
            }
        });

        //Gets user's favorite ingredients and adds them to the recycler view
        getUserFavoriteIngredients();
    }

    private void generateRecipe() {

        ArrayList<String> selectedIngredients = adapter.getSelectedIngredients();

        Intent intent = new Intent(this, GeneratedRecipesActivity.class);
        intent.putExtra("selectedIngredients", selectedIngredients);

        startActivity(intent);

    }

    private void getUserFavoriteIngredients() {

        firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                //Favorite ingredient data from database
                                Map<String, Object> data = document.getData();

                                String ingredientId = document.getId();
                                String ingredientName = data.get("name").toString();
                                String ingredientImageUrl = data.get("imageUrl").toString();

                                Ingredient favoriteIngredient = new Ingredient(ingredientId, ingredientName, ingredientImageUrl);

                                mFavoriteIngredients.add(favoriteIngredient);
                            }

                            initRecyclerView();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

    }


    private void initRecyclerView() {

        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        generateRecyclerView.setLayoutManager(layoutManager);
        adapter = new GenerateAdapter(this, mFavoriteIngredients);
        generateRecyclerView.setAdapter(adapter);

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
                case R.id.navigation_recipes:
                    Intent myRecipesIntent = new Intent(getApplicationContext(), MyRecipesActivity.class);
                    startActivity(myRecipesIntent);
                    return true;
                case R.id.navigation_generate:
                    return true;
                case R.id.navigation_ingredients:
                    Intent ingredientsIntent = new Intent(getApplicationContext(), MyIngredientsActivity.class);
                    startActivity(ingredientsIntent);
                    return true;
            }
            return false;
        }
    };
}
