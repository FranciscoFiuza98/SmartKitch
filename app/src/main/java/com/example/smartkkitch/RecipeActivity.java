package com.example.smartkkitch;

import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    ImageView imgRecipeImage;
    TextView txtRecipeName;
    String recipeId;
    String recipeName;
    String recipeImageUrl;
    Button btnSaveRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        //Gets intent and extras from previous activvity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Gets recipe image and name references
        imgRecipeImage = findViewById(R.id.imgRecipeImage);
        txtRecipeName = findViewById(R.id.txtRecipeName);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        btnSaveRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipe();
            }
        });

        //Changes the recipe image and name using the extras
        if (extras != null) {
            recipeImageUrl = extras.getString("recipeImage");
            recipeName = extras.getString("recipeName");
            recipeId = extras.getString("recipeId");

            Picasso.get().load(recipeImageUrl).into(imgRecipeImage);
            txtRecipeName.setText(recipeName);
        }

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.recipeIngredientsPager);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        setupViewPager(mViewPager);

    }

    public void setViewPager(int fragmentNumber) {
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupViewPager(ViewPager viewPager) {

        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentRecipeIngredients(), "Recipe Ingredients"); //    index 0
        adapter.addFragment(new FragmentRecipePreparation(), "Recipe Preparation"); //    index 1
        adapter.addFragment(new FragmentRecipeSimilarRecipes(), "Recipe Similar Recipes"); // index 2
        viewPager.setAdapter(adapter);

    }

    public Recipe getRecipe() {
     Recipe recipe = new Recipe(recipeId, recipeName, recipeImageUrl);
     return recipe;
    }

    private void saveRecipe() {
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        final Recipe currentRecipe = getRecipe();

        firestore.collection("Users").document(currentUser.getEmail()).collection("SavedRecipes").document(currentRecipe.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot result = task.getResult();

                        Map<String, Object> recipe = result.getData();

                        if (recipe == null) {
                            final HashMap<String, Object> saveRecipe = new HashMap<>();
                            saveRecipe.put("name", currentRecipe.getName());
                            saveRecipe.put("imageUrl", currentRecipe.getImageUrl());

                            //TODO change button when recipe is saved
                            //Saves recipe to user's SavedRecipes collection
                            firestore.collection("Users").document(currentUser.getEmail()).collection("SavedRecipes").document(currentRecipe.getId())
                                    .set(saveRecipe)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RecipeActivity.this, "Recipe Saved", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "onFailure: ", e);
                                        }
                                    });

                            //Checks if saved recipe has exists or has information about it, if not, adds it
                            firestore.collection("Recipes").document(currentRecipe.getId())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            DocumentSnapshot result = task.getResult();

                                            final Map<String, Object> savedRecipe = result.getData();

                                            if(savedRecipe == null) {
                                                firestore.collection("Recipes").document(currentRecipe.getId())
                                                        .set(saveRecipe)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "Recipe Saved: " + saveRecipe);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                e.printStackTrace();
                                                            }
                                                        });
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
                        else {
                            Toast.makeText(RecipeActivity.this, "You already saved this recipe.", Toast.LENGTH_SHORT).show();
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
            }
            return false;
        }
    };

}
