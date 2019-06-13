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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ObjectStreamException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GeneratedRecipesActivity extends AppCompatActivity {

    private static final String TAG = "GeneratedRecipesActivit";

    //Header variables used in the RapidApi requests
    private String apiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
    private String apiKey = "890724bd25msh8626e74253b368cp16308cjsnf290bae6aa08";
    private RequestQueue mQueue;

    private ArrayList<String> mSelectedIngredients;
    private ArrayList<GeneratedRecipe> mGeneratedRecipes = new ArrayList<>();

    private RecyclerView generatedRecipesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_recipes);

        //Gets intent and extras from GenerateActivity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Bottom Navigation Bar
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);

        //Gets selected ingredients from intent extras
        mSelectedIngredients = extras.getStringArrayList("selectedIngredients");

        mQueue = Volley.newRequestQueue(this);

        generatedRecipesRecyclerView = findViewById(R.id.generatedRecipesRecyclerView);

        generateRecipes();
    }

    private void generateRecipes() {

        //Base url for generating recipe
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/findByIngredients?number=20&ranking=1&ignorePantry=false&ingredients=";

        //Selected ingredients to be added to the url
        StringBuilder urlIngredients = new StringBuilder();

        //Adds all selected ingredients to the urlIngredients
        for (int i = 0; i < mSelectedIngredients.size(); i++)
        {
            String selectedIngredient = mSelectedIngredients.get(i);

            if (i != mSelectedIngredients.size() - 1) {
                urlIngredients.append(selectedIngredient).append("%2C");
            }
            else {
                urlIngredients.append(selectedIngredient);
            }

        }

        //Switches spaces with "+" in urlIngredients string
        String newUrlIngredients = urlIngredients.toString();
        newUrlIngredients = newUrlIngredients.replace(' ', '+');

        //Adds urlIngredients to the base url
        url += newUrlIngredients;

        Log.d(TAG, "Url: " + url);

        //Sends api request to the URL
        getGeneratedRecipes(url);

    }

    private void getGeneratedRecipes(String url) {

        //Sends Api request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {

                //Iterates over generated recipes
                for (int i = 0; i < response.length(); i++) {

                    try {
                        //Generated recipe Json object
                        JSONObject generatedRecipe = response.getJSONObject(i);

                        //Recipe information
                        int recipeId = generatedRecipe.getInt("id");
                        String recipeName = generatedRecipe.getString("title");
                        String recipeImageUrl = generatedRecipe.getString("image");
                        int recipeUsedIngredients = generatedRecipe.getInt("usedIngredientCount");
                        int recipeMissedIngredients = mSelectedIngredients.size() - recipeUsedIngredients;
                        int recipeLikes = generatedRecipe.getInt("likes");

                        //Generated recipe object
                        GeneratedRecipe newGeneratedRecipe = new GeneratedRecipe(Integer.toString(recipeId), recipeName, recipeImageUrl, Integer.toString(recipeUsedIngredients), Integer.toString(recipeMissedIngredients), Integer.toString(recipeLikes));

                        //Adds generated recipe to the array
                        mGeneratedRecipes.add(newGeneratedRecipe);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                //Saves each generated recipe in the database
                for(GeneratedRecipe generatedRecipe: mGeneratedRecipes) {

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    String recipeId = generatedRecipe.getId();
                    String recipeName = generatedRecipe.getName();
                    String recipeImageUrl= generatedRecipe.getImageUrl();
                    String recipeLikes = generatedRecipe.getLikes();

                    final Map<String, Object> generatedRecipeMap = new HashMap<>();
                    generatedRecipeMap.put("name", recipeName);
                    generatedRecipeMap.put("imageUrl", recipeImageUrl);
                    generatedRecipeMap.put("likes", recipeLikes);

                    firestore.collection("Recipes").document(recipeId)
                            .set(generatedRecipeMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Recipe info saved: " + generatedRecipeMap);
                                }
                            });

                }

                initRecyclerView();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("X-RapidAPI-Host", apiHost);
                params.put("X-RapidAPI-Key", apiKey);

                return params;
            }
        };

        //Adds request to the queue
        mQueue.add(request);

    }

    private void initRecyclerView() {

        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        generatedRecipesRecyclerView.setLayoutManager(layoutManager);
        GeneratedRecipesAdapter adapter = new GeneratedRecipesAdapter(this, mGeneratedRecipes);
        generatedRecipesRecyclerView.setAdapter(adapter);
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
