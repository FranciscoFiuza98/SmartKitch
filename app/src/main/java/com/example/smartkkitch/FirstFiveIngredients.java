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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FirstFiveIngredients extends AppCompatActivity {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients";

    //RequestQueue object used to make HTTP requests
    private RequestQueue mQueue;

    //Header variables used in the RapidApi requests
    private String apiHost = "spoonacular-recipe-food-nutrition-v1.p.rapidapi.com";
    private String apiKey = "890724bd25msh8626e74253b368cp16308cjsnf290bae6aa08";

    //Api url to get images, image name is needed after the end of this string so the correct image is loaded
    private String imageUrl = "https://spoonacular.com/cdn/ingredients_100x100/";

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_five_ingredients);

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

                            arrayNames.add(Objects.requireNonNull(name.getValue()).toString());
                        } else if (key.equals("ingredientId")) {
                            String ingredientId = Objects.requireNonNull(name.getValue()).toString();

                            arrayIds.add(ingredientId);
                        }
                    }
                }

                //Sends API request to get image for each ingredient ID
                for (String idIngrediente: arrayIds) {
                    getIngredientImage(idIngrediente);
                }


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
                Log.d(TAG, "Count: " + count);

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

        //Creates a layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);

        //Gets RecyclerView instance from View
        RecyclerView recyclerView = findViewById(R.id.recyclerVIew);

        //Sets layout manager to the recycler view
        recyclerView.setLayoutManager(layoutManager);

        //Creates a new adapter object
        adapter = new FirstFiveIngredients_RecyclerViewAdapter(this, arrayNames, arrayImagesUrl, arrayIds);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);
    }

    //Function called on button click
    public void start(View view) {
        ArrayList<String> favoriteIngredientsNames = adapter.getFavoriteIngredientsNames();
        ArrayList<String> favoriteIngredientsIds = adapter.getFavoriteIngredientsIds();

        //TODO change to 5 when possible
        if (favoriteIngredientsNames.size() < 3) {
            Toast.makeText(this, "Choose at least 5 ingredients!", Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "Ingredients Names: " + favoriteIngredientsNames);
        Log.d(TAG, "Ingredients Ids: " + favoriteIngredientsIds);
    }
}
