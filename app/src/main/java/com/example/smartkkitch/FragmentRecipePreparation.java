package com.example.smartkkitch;

import android.content.Intent;
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
import com.android.volley.toolbox.JsonArrayRequest;
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

public class FragmentRecipePreparation extends Fragment {
    private static final String TAG = "FragmentRecipePreparati";

    //Header variables used in the RapidApi requests
    private String apiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
    private String apiKey = "890724bd25msh8626e74253b368cp16308cjsnf290bae6aa08";
    private RequestQueue mQueue;

    private ArrayList<RecipeStep> mRecipeSteps = new ArrayList<>();

    private RecyclerView mPreparationRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        mQueue = Volley.newRequestQueue(getActivity());

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_recipe_preparation, container, false);

        TextView txtIngredients = view.findViewById(R.id.txtIngredients);
        TextView txtPreparation = view.findViewById(R.id.txtPreparation);
        TextView txtSimiliarRecipes = view.findViewById(R.id.txtSimilarRecipes);
        mPreparationRecyclerView = view.findViewById(R.id.preparationRecyclerView);

        //Gets recipe used in home activity
        final Recipe recipe = ((RecipeActivity) getActivity()).getRecipe();

        firestore.collection("Recipes").document(recipe.getId()).collection("Steps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();

                            if (result.size() == 0) {
                                getRecipeSteps(recipe);

                                Log.d(TAG, "Getting Steps from API");
                            } else {
                                Log.d(TAG, "Getting Steps from Firebase");
                                for (QueryDocumentSnapshot document : result) {
                                    Map<String, Object> recipeStep = document.getData();

                                    String recipeStepNumber = document.getId();
                                    String recipeStepDescription = recipeStep.get("description").toString();

                                    RecipeStep newRecipeStep = new RecipeStep(recipeStepNumber, recipeStepDescription);

                                    mRecipeSteps.add(newRecipeStep);
                                }

                                initRecyclerView(mRecipeSteps);
                            }


                        } else {
                            Log.w(TAG, "Error getting documents", task.getException());
                        }
                    }
                });

        txtIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity) getActivity()).setViewPager(0);
            }
        });

        txtPreparation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity) getActivity()).setViewPager(1);
            }
        });

        txtSimiliarRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity) getActivity()).setViewPager(2);
            }
        });

        return view;
    }

    private void getRecipeSteps(final Recipe recipe) {

        //Url for API request
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/" + recipe.getId() + "/analyzedInstructions?stepBreakdown=true";
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Url: " + url);

        final Recipe currentRecipe = ((RecipeActivity) getActivity()).getRecipe();
        Log.d(TAG, "Recipe ID: " + currentRecipe.getId());


        //Volley Request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {

                    JSONObject responseObject = response.getJSONObject(0);

                    JSONArray steps = responseObject.getJSONArray("steps");

                    for (int i = 0; i < steps.length(); i++) {
                        JSONObject currentStep = (JSONObject) steps.get(i);

                        int stepNumber = currentStep.getInt("number");
                        String stepDescription = currentStep.getString("step");

                        RecipeStep newRecipeStep = new RecipeStep(Integer.toString(stepNumber), stepDescription);

                        mRecipeSteps.add(newRecipeStep);
                    }


                    //Iterates over all steps
                    for (RecipeStep recipeStep : mRecipeSteps) {

                        //Gets step number and description
                        String stepNumber = recipeStep.getNumber();
                        String stepDescription = recipeStep.getDescription();

                        //Creates StepDescription HashMap
                        final HashMap<String, Object> hashStepDescription = new HashMap<>();
                        hashStepDescription.put("description", stepDescription);

                        //Adds step description to database
                        firestore.collection("Recipes").document(currentRecipe.getId()).collection("Steps").document(stepNumber)
                                .set(hashStepDescription)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "Description added: " + hashStepDescription);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "onFailure: ", e);
                                    }
                                });

                    }


                    initRecyclerView(mRecipeSteps);

                    //TODO put an empty state in the preparation fragment when there are no steps to show
                } catch (JSONException e) {
                    e.printStackTrace();

                    RecipeStep emptyRecipeStep = new RecipeStep("1", "No steps to show");

                    mRecipeSteps.add(emptyRecipeStep);

                    final HashMap<String, Object> hashStepDescription = new HashMap<>();
                    hashStepDescription.put("description", emptyRecipeStep.getDescription());

                    firestore.collection("Recipes").document(currentRecipe.getId()).collection("Steps").document(emptyRecipeStep.getNumber())
                            .set(hashStepDescription)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Description added: " + hashStepDescription);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "onFailure: ", e);
                                }
                            });

                    initRecyclerView(mRecipeSteps);
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

    private void initRecyclerView(ArrayList<RecipeStep> arrayRecipeSteps) {

        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mPreparationRecyclerView.setLayoutManager(layoutManager);
        FragmentRecipePreparationAdapter adapter = new FragmentRecipePreparationAdapter(getActivity(), arrayRecipeSteps, user);
        mPreparationRecyclerView.setAdapter(adapter);

    }
}