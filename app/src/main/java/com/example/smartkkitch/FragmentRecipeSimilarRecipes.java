package com.example.smartkkitch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FragmentRecipeSimilarRecipes extends Fragment {

    private static final String TAG = "FragmentRecipeSimilarRe";

    private RecyclerView mSimilarRecyclerView;

    //Header variables used in the RapidApi requests
    private String apiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
    private String apiKey = "890724bd25msh8626e74253b368cp16308cjsnf290bae6aa08";
    private String recipeUrl = "https://spoonacular.com/recipeImages/";
    private RequestQueue mQueue;

    private ArrayList<Recipe> mRecipes = new ArrayList<>();

    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Gets similar recipes fragment to display
        View view = inflater.inflate(R.layout.fragment_recipe_similar_recipes, container, false);

        //Gets current recipe shown
        final Recipe currentRecipe = ((RecipeActivity)getActivity()).getRecipe();

        //Initializes queue
        mQueue = Volley.newRequestQueue(getActivity());

        firestore = FirebaseFirestore.getInstance();

        //Gets reference of all objects in the fragment
        TextView txtIngredients = view.findViewById(R.id.txtIngredients);
        TextView txtPreparation = view.findViewById(R.id.txtPreparation);
        TextView txtSimiliarRecipes = view.findViewById(R.id.txtSimilarRecipes);
        mSimilarRecyclerView = view.findViewById(R.id.similarRecyclerView);

        firestore.collection("Recipes").document(currentRecipe.getId()).collection("Similar")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();

                            if (result.size() == 0) {
                                //Gets similar recipes
                                getSimilarRecipes(currentRecipe);
                                Log.d(TAG, "Getting similar recipes from API");
                            } else {
                                Log.d(TAG, "Getting similar recipes from Firebase");

                                for (QueryDocumentSnapshot document : result) {
                                    Map<String, Object> similarRecipe = document.getData();

                                    String recipeId = document.getId();
                                    String recipeName = similarRecipe.get("name").toString();
                                    String recipeImageUrl = similarRecipe.get("imageUrl").toString();

                                    Recipe newSimilarRecipe = new Recipe(recipeId, recipeName, recipeImageUrl);

                                    mRecipes.add(newSimilarRecipe);
                                }

                                initRecyclerView(mRecipes);

                            }
                        }
                    }
                });



        //Onclick listeners for the diferent fragments
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

    private void getSimilarRecipes(final Recipe currentRecipe){

        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/recipes/" + currentRecipe.getId() + "/similar";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for (int i = 0; i < response.length(); i++) {

                    try {
                        JSONObject similarRecipe = response.getJSONObject(i);

                        int recipeId = similarRecipe.getInt("id");
                        String recipeName = similarRecipe.getString("title");
                        String recipeImageUrl = recipeUrl + similarRecipe.getString("image");

                        Recipe newSimilarRecipe = new Recipe(Integer.toString(recipeId), recipeName, recipeImageUrl);

                        mRecipes.add(newSimilarRecipe);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                for (Recipe recipe : mRecipes) {

                    String recipeId = recipe.getId();
                    String recipeName = recipe.getName();
                    String recipeImageUrl = recipe.getImageUrl();

                    final HashMap<String, Object> newSimilarRecipe = new HashMap<>();
                    newSimilarRecipe.put("id", recipeId);
                    newSimilarRecipe.put("name", recipeName);
                    newSimilarRecipe.put("imageUrl", recipeImageUrl);

                    firestore.collection("Recipes").document(currentRecipe.getId()).collection("Similar").document(recipeId)
                            .set(newSimilarRecipe)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Similar Recipe Added: " + newSimilarRecipe);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "onFailure: ", e);
                                }
                            });

                }

                initRecyclerView(mRecipes);


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

    private void initRecyclerView(ArrayList<Recipe> arrayRecipes) {

        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mSimilarRecyclerView.setLayoutManager(layoutManager);
        HomeRecipeRecyclerViewAdapter adapter = new HomeRecipeRecyclerViewAdapter(getActivity(), arrayRecipes);
        mSimilarRecyclerView.setAdapter(adapter);

    }
}