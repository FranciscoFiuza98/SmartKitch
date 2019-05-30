package com.example.smartkkitch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    private ArrayList<String> mRecipeSteps = new ArrayList<>();

    private RecyclerView mPreparationRecyclerView;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_recipe_preparation, container, false);

        TextView txtIngredients = view.findViewById(R.id.txtIngredients);
        TextView txtPreparation = view.findViewById(R.id.txtPreparation);
        TextView txtSimiliarRecipes = view.findViewById(R.id.txtSimilarRecipes);
        mPreparationRecyclerView = view.findViewById(R.id.preparationRecyclerView);

        //Gets recipe used in home activity
        final Recipe recipe = ((RecipeActivity)getActivity()).getRecipe();

        firestore.collection("Recipes").document(recipe.getId()).collection("Steps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();

                            if (result.size() == 0) {
                                getRecipeSteps(recipe);
                            } else {
                                for (QueryDocumentSnapshot document: result) {
                                    Map<String, Object> recipeStep = document.getData();

                                    String recipeStepNumber = recipeStep.get("stepNumber").toString();
                                    String recipeStepDescription = recipeStep.get("stepDescription").toString();

                                    mRecipeSteps.add(Integer.parseInt(recipeStepNumber) - 1, recipeStepDescription);
                                }

                                //initRecyclerView(mRecipeSteps);
                            }


                        } else {
                            Log.w(TAG, "Error getting documents", task.getException());
                        }
                    }
                });

        txtIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity)getActivity()).setViewPager(0);
            }
        });

        txtPreparation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity)getActivity()).setViewPager(1);
            }
        });

        txtSimiliarRecipes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((RecipeActivity)getActivity()).setViewPager(2);
            }
        });

        return view;
    }

    private void getRecipeSteps(final Recipe recipe) {

        //Url for API request
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/" + recipe.getId() + "/analyzedInstructions?stepBreakdown=true";
        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();


        //TODO change request to receive a json array
        //Volley Request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                try {
                    JSONArray arraySteps = response.getJSONArray("steps");

                    Log.d(TAG, "Steps: " + arraySteps);
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
}