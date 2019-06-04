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

public class MyRecipesActivity extends AppCompatActivity {

    private static final String TAG = "MyRecipesActivity";

    private ArrayList<Recipe> mSavedRecipes = new ArrayList<>();

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore firestore;

    RecyclerView myRecipesRecyclerView;

    //TODO Add empty state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        //Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        myRecipesRecyclerView = findViewById(R.id.myRecipesRecyclerView);

        getSavedRecipes();
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
                    return true;
                case R.id.navigation_notifications:
                    Intent generateIntent = new Intent(getApplicationContext(), GenerateActivity.class);
                    startActivity(generateIntent);
                    return true;
                case R.id.navigation_ingredients:
                    Intent ingredientsIntent = new Intent(getApplicationContext(), MyIngredientsActivity.class);
                    startActivity(ingredientsIntent);
                    return true;
            }
            return false;
        }
    };

    private void getSavedRecipes() {

        firestore.collection("Users").document(currentUser.getEmail()).collection("SavedRecipes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                //Favorite ingredient data from database
                                Map<String, Object> savedRecipe = document.getData();

                                String recipeId = document.getId();
                                String recipeName = "";
                                String recipeImageUrl = "";

                                //Iterates over ingredient and adds it's ID to the currentFavoriteIngredientsId ArrayList
                                for (String key : savedRecipe.keySet()) {
                                    String value = (String) savedRecipe.get(key);


                                    if (key.equals("name")) {
                                        recipeName = value;
                                    }
                                    else if (key.equals("imageUrl")) {
                                        recipeImageUrl = value;
                                    }
                                }

                                Recipe recipe = new Recipe(recipeId, recipeName, recipeImageUrl);

                                mSavedRecipes.add(recipe);
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
        myRecipesRecyclerView.setLayoutManager(layoutManager);
        HomeRecipeRecyclerViewAdapter adapter = new HomeRecipeRecyclerViewAdapter(this, mSavedRecipes);
        myRecipesRecyclerView.setAdapter(adapter);

    }
}
