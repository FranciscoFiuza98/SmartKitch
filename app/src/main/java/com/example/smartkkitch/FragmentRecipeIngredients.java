package com.example.smartkkitch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FragmentRecipeIngredients extends Fragment {
    private static final String TAG = "FragmentRecipeIngredien";

    private String imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/";

    //Header variables used in the RapidApi requests
    private String apiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
    private String apiKey = "890724bd25msh8626e74253b368cp16308cjsnf290bae6aa08";
    private RequestQueue mQueue;

    private ArrayList<IngredientRecipe> mIngredientsList = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Inflates Recipe Ingredient Fragment layout and adds it to the view
        View view = inflater.inflate(R.layout.fragment_recipe_ingredients, container, false);

        //Firebase instances
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        mQueue = Volley.newRequestQueue((Objects.requireNonNull(getActivity())));

        //Gets reference of objects in fragment
        TextView txtIngredients = view.findViewById(R.id.txtIngredients);
        TextView txtPreparation = view.findViewById(R.id.txtPreparation);
        TextView txtSimiliarRecipes = view.findViewById(R.id.txtSimilarRecipes);
        mRecyclerView = view.findViewById(R.id.fragmentIngredientsRecyclerView);

        //Gets recipe used in home activity
        final Recipe recipe = ((RecipeActivity)getActivity()).getRecipe();

        firestore.collection("Recipes").document(recipe.getId()).collection("Ingredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot result = task.getResult();

                            if (result.size() == 0) {

                                getRecipeIngredients(recipe);

                            }else {
                                for (QueryDocumentSnapshot document: result) {
                                    Map<String, Object> ingredient = document.getData();

                                    String ingredientId = document.getId();
                                    String ingredientName = ingredient.get("name").toString();
                                    String ingredientAmount = ingredient.get("amount").toString();
                                    String ingredientImageUrl = ingredient.get("imageUrl").toString();
                                    String ingredientUnit = ingredient.get("unit").toString();

                                    IngredientRecipe ingredientRecipe = new IngredientRecipe(ingredientId, ingredientName, ingredientImageUrl, ingredientAmount, ingredientUnit);

                                    mIngredientsList.add(ingredientRecipe);

                                }

                                initRecyclerView();
                            }

                        }else {
                            Log.w(TAG, "Error getting documents", task.getException());
                        }
                    }
                });






        //Ingredient text on click listener
        txtIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity)getActivity()).setViewPager(0);
            }
        });

        //Preparation text on click listener
        txtPreparation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity)getActivity()).setViewPager(1);
            }
        });

        //Similar text on click listener
        txtSimiliarRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity)getActivity()).setViewPager(2);
            }
        });

        return view;
    }

    private void getRecipeIngredients(final Recipe recipe) {

        //Url for API request
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/" + recipe.getId() + "/information";
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        //Volley Request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    //Gets ingredients used in recipe from response
                    JSONArray arrayIngredients = response.getJSONArray("extendedIngredients");

                    //TODO Fix amount and unit overlapping, try rounding up the amount number
                    //Iterates over ingredients array and adds its information to the database
                    for (int i = 0; i < arrayIngredients.length(); i++) {

                        JSONObject ingredient = arrayIngredients.getJSONObject(i);

                        String ingredientId = ingredient.getString("id");
                        final String ingredientName = ingredient.getString("name");
                        String ingredientImageUrl = imageUrl + ingredient.getString("image");
                        JSONObject measures = ingredient.getJSONObject("measures");
                        JSONObject metric = measures.getJSONObject("metric");
                        Double ingredientAmount = metric.getDouble("amount");
                        String amountUnit = metric.getString("unitShort");


                        Map<String, Object> ingredientMap = new HashMap<>();
                        ingredientMap.put("name", ingredientName);
                        ingredientMap.put("imageUrl", ingredientImageUrl);
                        ingredientMap.put("amount", ingredientAmount);
                        ingredientMap.put("unit", amountUnit);

                        firestore.collection("Recipes").document(recipe.getId()).collection("Ingredients").document(ingredientId)
                                .set(ingredientMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Ingredient added: " + ingredientName);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "onFailure: ", e);
                                    }
                                });

                        IngredientRecipe ingredientRecipe = new IngredientRecipe(ingredientId, ingredientName, ingredientImageUrl, ingredientAmount.toString(), amountUnit);

                        mIngredientsList.add(ingredientRecipe);

                    }

                    initRecyclerView();

                } catch (JSONException e) {
                    e.printStackTrace();
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

        //Adds request to queue
        mQueue.add(request);

    }

    private void initRecyclerView() {

        //TODO fix repetetive adding ingredients

        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        FragmentRecipeIngredientsAdapter adapter = new FragmentRecipeIngredientsAdapter(getActivity(), mIngredientsList, user);
        mRecyclerView.setAdapter(adapter);

    }
}