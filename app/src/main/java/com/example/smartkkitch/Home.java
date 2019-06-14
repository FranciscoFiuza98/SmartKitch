package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
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
import java.util.Objects;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

    //Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    //Fragment controllers
    private ViewPager mViewPager;

    private ArrayList<IngredientAdapter> mIngredients = new ArrayList<>();
    private ArrayList<IngredientAdapter> mFavoriteIngredients = new ArrayList<>();

    private HomeIngredientsAdapter adapter;

    //TODO Find a way to update ingredients and recipes recycler view on activity resume
    //TODO Add "View All ingredients"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Fragment setup
        mViewPager = findViewById(R.id.container);
        setupViewPager(mViewPager);

        //Firebase references
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        getIngredients();

    }


    public void setViewPager(int fragmentNumber) {
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentForYou(), "For You"); // index 0
        //adapter.addFragment(new FragmentMeat(), "Meat"); //      index 1
        viewPager.setAdapter(adapter);

    }

    private void getIngredients() {

        //Gets all ingredients from database and adds them to the mIngredients array
        firestore.collection("Ingredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                String ingredientId = document.getId();
                                String ingredientName = document.get("name").toString();
                                String ingredientImageUrl = document.get("imageUrl").toString();

                                IngredientAdapter ingredient = new IngredientAdapter(ingredientId, ingredientName, ingredientImageUrl,false);


                                mIngredients.add(ingredient);


                            }

                            //Filters user's favorite ingredients
                            filterFavoriteIngredients();
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

    private void filterFavoriteIngredients() {

        //Gets user's favorite ingredients
        firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                String ingredientId = document.getId();
                                String ingredientName = document.get("name").toString();
                                Log.d(TAG, "Favorite Ingredient ID: " + ingredientId);
                                String ingredientImageUrl = document.get("imageUrl").toString();

                                IngredientAdapter favoriteIngredient = new IngredientAdapter(ingredientId, ingredientName, ingredientImageUrl, false);

                                mFavoriteIngredients.add(favoriteIngredient);
                            }

                            //Removes ingredientes from the mIngredients array list if there are the same as the user's favorite ingredients
                            for (int i = 0; i < mIngredients.size(); i++) {
                                for (int j = 0; j < mFavoriteIngredients.size(); j++) {

                                    String ingredientId = mIngredients.get(i).getId();
                                    String favoriteIngredientId = mFavoriteIngredients.get(j).getId();

                                    if (ingredientId.equals(favoriteIngredientId)) {
                                        mIngredients.remove(i);
                                    }
                                }
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

    //TODO Show relevant ingredients (Most liked). Think of logic when there are no most liked ingredients to show.
    private void initRecyclerView() {

        while (mIngredients.size() > 30) {
            mIngredients.remove(mIngredients.size() - 1);
        }

        //Creates a layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        //Gets RecyclerView instance from View
        RecyclerView recyclerView = findViewById(R.id.recommendedRecyclerView);

        //Sets layout manager to the recycler view
        recyclerView.setLayoutManager(layoutManager);

        //Creates a new adapter object
        adapter = new HomeIngredientsAdapter(this, mIngredients, currentUser);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);

    }

    //Bottom navigation on item select listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //Switches between the item selected and starts corresponding activity
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_recipes:
                    Intent myRecipesIntent = new Intent(getApplicationContext(), MyRecipesActivity.class);
                    startActivity(myRecipesIntent);
                    return true;
                case R.id.navigation_generate:
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
}
