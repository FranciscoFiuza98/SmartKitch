package com.example.smartkkitch;

import android.content.Intent;
import android.graphics.Color;
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
import java.util.Objects;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;


    private ImageView imgRecipeImage;
    private TextView txtRecipeName;
    private String recipeId, recipeName, recipeImageUrl;
    private Button btnSaveRecipe;

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
                recipeAction();
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

        //Firebase Instances
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        checkIfRecipeSaved();

        //Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        //Fragments View Pager
        mViewPager = findViewById(R.id.recipeIngredientsPager);
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

    private void recipeAction() {

        String buttonText = btnSaveRecipe.getText().toString();

        if (buttonText.equals("Save Recipe")) {
            saveRecipe();
        } else if (buttonText.equals("Remove Recipe")) {
            removeRecipe();
        }

        Log.d(TAG, "Button Text: " + buttonText);

    }

    private void removeRecipe() {

        final Recipe currentRecipe = getRecipe();

        firestore.collection("Users").document(currentUser.getEmail()).collection("SavedRecipes").document(currentRecipe.getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(RecipeActivity.this, "Recipe Removed", Toast.LENGTH_SHORT).show();
                            btnSaveRecipe.setText("Save Recipe");
                            btnSaveRecipe.setBackgroundColor(Color.GREEN);
                            recipeNumberSavesChange(currentRecipe, "decrement");

                        }
                    }
                });

    }

    //Saves recipe to the user collection
    private void saveRecipe() {

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

                            //Saves recipe to user's SavedRecipes collection
                            firestore.collection("Users").document(currentUser.getEmail()).collection("SavedRecipes").document(currentRecipe.getId())
                                    .set(saveRecipe)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(RecipeActivity.this, "Recipe Saved", Toast.LENGTH_SHORT).show();
                                            btnSaveRecipe.setText("Remove Recipe");
                                            btnSaveRecipe.setBackgroundColor(Color.RED);
                                            recipeNumberSavesChange(currentRecipe, "increment");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "onFailure: ", e);
                                        }
                                    });

                            //Checks if saved recipe exists in Recipes collection or has information about it, if not, adds it
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

/*    private void incrementRecipeNumberSaves(final Recipe recipe) {

        Log.d(TAG, "ID: " + recipe.getId());

        firestore.collection("Recipes").document(recipe.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> savedRecipe = result.getData();

                            try{
                                String numberSaves = savedRecipe.get("numberSaves").toString();
                                int numberSavesInt = Integer.parseInt(numberSaves);
                                numberSavesInt++;

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", recipe.getName());
                                numberSavesMap.put("imageUrl", recipe.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                firestore.collection("Recipes").document(recipe.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number Saves increment: " + numberSavesMap);
                                            }
                                        });

                            }catch (NullPointerException exception) {

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", recipe.getName());
                                numberSavesMap.put("imageUrl", recipe.getImageUrl());
                                numberSavesMap.put("numberSaves", 1);

                                firestore.collection("Recipes").document(recipe.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number Saves new: " + numberSavesMap);
                                            }
                                        });

                            }

                        }
                    }
                });

    }*/

    private void recipeNumberSavesChange(final Recipe recipe, final String type) {

        Log.d(TAG, "ID: " + recipe.getId());

        firestore.collection("Recipes").document(recipe.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> savedRecipe = result.getData();

                            try{
                                String numberSaves = savedRecipe.get("numberSaves").toString();

                                Log.d(TAG, "Current Number of saves: " + numberSaves);

                                int numberSavesInt = Integer.parseInt(numberSaves);
                                if (type.equals("increment")) {
                                    numberSavesInt++;
                                } else if (type.equals("decrement")) {
                                    numberSavesInt--;
                                }

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", recipe.getName());
                                numberSavesMap.put("imageUrl", recipe.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                firestore.collection("Recipes").document(recipe.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number Saves increment: " + numberSavesMap);
                                            }
                                        });

                            }catch (NullPointerException exception) {

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", recipe.getName());
                                numberSavesMap.put("imageUrl", recipe.getImageUrl());

                                if (type.equals("increment")) {
                                    numberSavesMap.put("numberSaves", 1);
                                } else if (type.equals("decrement")) {
                                    numberSavesMap.put("numberSaves", 0);
                                }

                                firestore.collection("Recipes").document(recipe.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number Saves new: " + numberSavesMap);
                                            }
                                        });

                            }

                        }
                    }
                });

    }


/*
    private void decrementRecipeNumberSaves(final Recipe recipe) {

        Log.d(TAG, "ID: " + recipe.getId());

        firestore.collection("Recipes").document(recipe.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> savedRecipe = result.getData();

                            try{
                                String numberSaves = savedRecipe.get("numberSaves").toString();
                                int numberSavesInt = Integer.parseInt(numberSaves);
                                numberSavesInt--;

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", recipe.getName());
                                numberSavesMap.put("imageUrl", recipe.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                firestore.collection("Recipes").document(recipe.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number Saves increment: " + numberSavesMap);
                                            }
                                        });

                            }catch (NullPointerException exception) {

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", recipe.getName());
                                numberSavesMap.put("imageUrl", recipe.getImageUrl());
                                numberSavesMap.put("numberSaves", 1);

                                firestore.collection("Recipes").document(recipe.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number Saves new: " + numberSavesMap);
                                            }
                                        });

                            }

                        }
                    }
                });

    }
*/




    private void checkIfRecipeSaved() {

        Recipe currentRecipe = getRecipe();

        Log.d(TAG, "Email: " + currentUser.getEmail());

        firestore.collection("Users").document(currentUser.getEmail()).collection("SavedRecipes").document(currentRecipe.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot result = task.getResult();

                            Map<String, Object> savedRecipe = result.getData();

                            if (savedRecipe == null) {
                                btnSaveRecipe.setText("Save Recipe");
                                btnSaveRecipe.setBackgroundColor(Color.GREEN);
                            }else {
                                btnSaveRecipe.setText("Remove Recipe");
                                btnSaveRecipe.setBackgroundColor(Color.RED);
                            }

                        }
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
