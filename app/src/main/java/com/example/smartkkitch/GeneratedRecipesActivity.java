package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generated_recipes);

        //Gets intent and extras from GenerateActivity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Gets selected ingredients from intent extras
        mSelectedIngredients = extras.getStringArrayList("selectedIngredients");

        mQueue = Volley.newRequestQueue(this);

        generateRecipes();
    }

    private void generateRecipes() {

        String example = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/findByIngredients?number=5&ranking=1&ignorePantry=false&ingredients=bacon%2Cpork+tenderloins%2C+green+grapes%2C+egg";
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/findByIngredients?number=20&ranking=1&ignorePantry=false&ingredients=";
        StringBuilder urlIngredients = new StringBuilder();


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

        String newUrlIngredients = urlIngredients.toString();

        newUrlIngredients = newUrlIngredients.replace(' ', '+');

        url += newUrlIngredients;

        getGeneratedRecipes(url);

    }

    private void getGeneratedRecipes(String url) {

        Log.d(TAG, "Url: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(final JSONArray response) {

                Log.d(TAG, "Got Response");

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject generatedRecipe = response.getJSONObject(i);

                        Log.d(TAG, "Response: " + generatedRecipe);

                        int recipeId = generatedRecipe.getInt("id");
                        String recipeName = generatedRecipe.getString("title");
                        String recipeImageUrl = generatedRecipe.getString("image");
                        int recipeUsedIngredients = generatedRecipe.getInt("usedIngredientCount");
                        int recipeMissedIngredients = mSelectedIngredients.size() - recipeUsedIngredients;
                        //int recipeMissedIngredients = generatedRecipe.getInt("missedIngredientCount");
                        int recipeLikes = generatedRecipe.getInt("likes");

                        GeneratedRecipe newGeneratedRecipe = new GeneratedRecipe(Integer.toString(recipeId), recipeName, recipeImageUrl, Integer.toString(recipeUsedIngredients), Integer.toString(recipeMissedIngredients), Integer.toString(recipeLikes));

                        mGeneratedRecipes.add(newGeneratedRecipe);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

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

        mQueue.add(request);

    }
}
