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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
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

    //RequestQueue object used to make HTTP requests
    private RequestQueue mQueue;

    private FirebaseAuth mAuth;

    //Header variables used in the RapidApi requests
    private String apiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
    private String apiKey = "890724bd25msh8626e74253b368cp16308cjsnf290bae6aa08";

    //Api url to get images, image name is needed after the end of this string so the correct image is loaded
    private String imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/";

    private String userEmail;
    private String userName;

    //Holds number of API requests (for Debugging)
    private int count = 0;

    //Firebase database reference
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("/");

    //Array lists with names and images of the ingredients
    private ArrayList<String> arrayNames = new ArrayList<>();
    private ArrayList<String> arrayImagesUrl = new ArrayList<>();
    private ArrayList<String> arrayIds = new ArrayList<>();

    //RecyclerView adapter class
    FirstFiveIngredients_RecyclerViewAdapter adapter;

    //TODO add search


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_five_ingredients);

        mAuth = FirebaseAuth.getInstance();

        //Gets intent sent from previous activity and gets intent extras
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            userEmail = extras.getString("email");
            userName = extras.getString("name");
        }

        //Queue used to send requests
        mQueue = Volley.newRequestQueue(this);

        //Calls function to fill the names and images arrays, this functions initiates the RecylcerView as well.
        fillArrays();
    }

    private void fillArrays() {
        //Connects to database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            //For every item on the database creates a DataSnapshot object
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //Creates an iterable of each item
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();

                //Iterates the iterable and gets ingredient Name and ID and populates the names array
                for (DataSnapshot ingredient : iterable) {
                    Iterable<DataSnapshot> ingredientNames = ingredient.getChildren();

                    for (DataSnapshot name : ingredientNames) {
                        String key = name.getKey();

                        assert key != null;
                        if (key.equals("name")) {

                            String ingredientName = Objects.requireNonNull(name.getValue()).toString();
                            arrayNames.add(ingredientName);

                        } else if (key.equals("ingredientId")) {
                            String ingredientId = Objects.requireNonNull(name.getValue()).toString();
                            arrayIds.add(ingredientId);
                        }
                    }
                }

                //Hardcoded images to save API requests
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/brown-onion.png");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/egg.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/bacon.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/garlic.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/yellow-bell-pepper.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/carrots.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/whole-chicken.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/tomato.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/beef-cubes-raw.png");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/potatoes-yukon-gold.jpg");
                arrayImagesUrl.add("https://spoonacular.com/cdn/ingredients_100x100/shrimp.jpg");


                initRecyclerView();


                //Sends API request to get image for each ingredient ID
                /*for (String idIngrediente: arrayIds) {
                    getIngredientImage(idIngrediente);
                }*/


            }

            @Override
            //Throws an error if there is one connecting to the database
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ", databaseError.toException());
            }
        });
    }

    private void getIngredientImage(String ingredientID) {

        //Api url that gives information about an ingredient
        String url = "https://spoonacular-recipe-food-nutrition-v1.p.rapidapi.com/food/ingredients/" + ingredientID + "/information?amount=100&unit=gram";

        //Volley Request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Increments count each time a response is received (For debugging)
                count++;
                Log.d(TAG, "Api Requests: " + count);

                try {

                    //Gets image name from response JSON object
                    String image = response.getString("image");

                    //Adds image name to API url that holds all images, and adds the result string to the images array
                    arrayImagesUrl.add(imageUrl + image);

                    //When the number of image links is the same as the number of ingredient IDs, initializes Recycler View
                    if (arrayImagesUrl.size() == arrayIds.size()) {

                        initRecyclerView();
                    }

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

    //Initializes Recycler View
    private void initRecyclerView() {

        FirebaseUser currentUser = mAuth.getCurrentUser();
        ArrayList<Ingredient> ingredients = new ArrayList<>();

        for (int i = 0; i < arrayIds.size(); i++) {
            String id = arrayIds.get(i);
            String name = arrayNames.get(i);
            String imageUrl = arrayImagesUrl.get(i);

            Ingredient ingredient = new Ingredient(id, name, imageUrl);

            ingredients.add(ingredient);
        }

        //Creates a layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);

        //Gets RecyclerView instance from View
        RecyclerView recyclerView = findViewById(R.id.recyclerVIew);

        //Sets layout manager to the recycler view
        recyclerView.setLayoutManager(layoutManager);

        //Creates a new adapter object
        adapter = new FirstFiveIngredients_RecyclerViewAdapter(this, ingredients, currentUser);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);
    }

    //Function called on Start button click
    public void start(View view) {
        ArrayList<Ingredient> favoriteIngredients = adapter.getFavoriteIngredients();

        //Checks if user has chosen 5 ingredients, if not, makes a toast warning him
        if (favoriteIngredients.size() < 5) {
            Toast.makeText(this, "Choose at least 5 ingredients!", Toast.LENGTH_LONG).show();
        } else {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            final Map<String, Object> user = new HashMap<>();
            user.put("name", userName);

            firestore.collection("Users").document(userEmail)
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

            for (Ingredient ingredient : favoriteIngredients) {
                final Map<String, Object> favoriteIngredient = new HashMap<>();
                favoriteIngredient.put("ingredientId", ingredient.getId());
                favoriteIngredient.put("name", ingredient.getName());

                firestore.collection("Users").document(userEmail).collection("FavoriteIngredients").document()
                        .set(favoriteIngredient)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Favorite ingredient added: " + favoriteIngredient);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "onFailure: ", e);
                            }
                        });

                //Gets ID of last ingredient
                String lastIngredientId = favoriteIngredients.get(favoriteIngredients.size() - 1).getId();

                //If the current ingredient ID is the same as lastIngredientId, meaning that it is the last iteration of the ArrayList, starts Home activity
                if (ingredient.getId().equals(lastIngredientId)) {
                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    Toast.makeText(getApplicationContext(), "Favorite ingredients saved!", Toast.LENGTH_LONG).show();
                    startActivity(intent);
                    finish();
                }
            }
        }


    }
}
