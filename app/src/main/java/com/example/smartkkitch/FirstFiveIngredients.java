package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1beta1.Document;
import com.google.firestore.v1beta1.WriteResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FirstFiveIngredients extends AppCompatActivity {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients";

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    ArrayList<IngredientAdapter> mIngredients = new ArrayList<>();

    //RecyclerView adapter class
    FirstFiveIngredients_RecyclerViewAdapter adapter;

    //TODO add search


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_five_ingredients);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        firestore = FirebaseFirestore.getInstance();

        //Calls function to fill the names and images arrays, this functions initiates the RecylcerView as well.
        getIngredients();
    }

    private void getIngredients() {

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

    //Initializes Recycler View
    private void initRecyclerView() {

        /*while (mIngredients.size() > 20) {
            mIngredients.remove(mIngredients.size() - 1);
            Log.d(TAG, "Size: " + mIngredients.size());
        }*/

        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Creates layout manager and adapter and sets them to the RecyclerView
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        RecyclerView recyclerView = findViewById(R.id.recyclerVIew);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FirstFiveIngredients_RecyclerViewAdapter(this, mIngredients, currentUser);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);
    }

    //Function called on Start button click
    public void start(View view) {

        ArrayList<IngredientAdapter> favoriteIngredients = adapter.getFavoriteIngredients();

        //Checks if user has chosen 5 ingredients, if not, makes a toast warning him
        if (favoriteIngredients.size() < 5) {
            Toast.makeText(this, "Choose at least 5 ingredients!", Toast.LENGTH_LONG).show();
        } else {

            final Map<String, Object> user = new HashMap<>();
            user.put("name", currentUser.getDisplayName());

            //Adds user to the Users collection in the database
            firestore.collection("Users").document(currentUser.getEmail())
                    .set(user)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "User added:" + user);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "onFailure: ", e);
                        }
                    });

            //Adds user's favorite ingredients to his FavoriteIngredients collection in the database
            for (final IngredientAdapter ingredient : favoriteIngredients) {
                final Map<String, Object> favoriteIngredient = new HashMap<>();
                favoriteIngredient.put("name", ingredient.getName());
                favoriteIngredient.put("imageUrl", ingredient.getImageUrl());

                firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredient.getId())
                        .set(favoriteIngredient)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Favorite ingredient added: " + favoriteIngredient);

                                incrementIngredientNumberSaves(ingredient);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "onFailure: ", e);
                            }
                        });
            }

            //Starts Home Activity
            Intent intent = new Intent(getApplicationContext(), Home.class);
            Toast.makeText(getApplicationContext(), "Favorite ingredients saved!", Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
        }


    }

    private void incrementIngredientNumberSaves(final IngredientAdapter ingredient) {

        //Gets clicked ingredient from firestore
        firestore.collection("Ingredients").document(ingredient.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            //Gets ingredient object from database
                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> ingredientMap = result.getData();
                            String numberSaves = "";

                            //Tries to get number of saves from ingredient
                            try {
                                //Gets number of ingredient's saves
                                numberSaves = ingredientMap.get("numberSaves").toString();

                                //Parses number of saves to a integer
                                int numberSavesInt = Integer.parseInt(numberSaves);

                                //Increments number of saves
                                numberSavesInt++;

                                //Creates a map object to save to the firestore
                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", ingredient.getName());
                                numberSavesMap.put("imageUrl", ingredient.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                //Saves updated ingredient info to the firestore
                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "ID: " + ingredient.getId());
                                                Log.d(TAG, "Number of saves updated: " + numberSavesMap);
                                            }
                                        });

                                //If there is no "numberSaves" field in ingredient document, adds the field with value of 1
                            } catch (NullPointerException exception) {

                                //Creates Map object with 1 save number to add to the database
                                final Map<String, Object> firstNumberSave = new HashMap<>();
                                firstNumberSave.put("name", ingredient.getName());
                                firstNumberSave.put("imageUrl", ingredient.getImageUrl());
                                firstNumberSave.put("numberSaves", 1);

                                //Adds ingredient info to the firestore
                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(firstNumberSave)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "ID: " + ingredient.getId());
                                                Log.d(TAG, "First number save added: " + firstNumberSave);
                                            }
                                        });


                            }
                        }
                    }
                });

    }
}
