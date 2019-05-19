package com.example.smartkkitch;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class FirstFiveIngredients extends AppCompatActivity {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients";

    //Firebase database reference
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("/");

    //Array lists with names and images of the ingredients
    private ArrayList<String> arrayNames = new ArrayList<>();
    private ArrayList<String> arrayImages = new ArrayList<>();
    private ArrayList<String> arrayIds = new ArrayList<>();

    FirstFiveIngredients_RecyclerViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_five_ingredients);

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

                Log.d(TAG, "Starting Iteration");

                //Iterates the iterable and gets ingredient Name and ID and populates the names array
                for (DataSnapshot ingredient : iterable) {
                    Iterable<DataSnapshot> ingredientNames = ingredient.getChildren();

                    for (DataSnapshot name : ingredientNames) {
                        String key = name.getKey();

                        assert key != null;
                        if (key.equals("name")) {
                            // Log.d(TAG, "Ingredient Name: " + name.getValue());
                            arrayNames.add(name.getValue().toString());
                        } else if (key.equals("ingredientId")) {
                            //Log.d(TAG, "Ingredient ID: " + name.getValue());
                            arrayIds.add(name.getValue().toString());
                        }
                    }
                }

                //TODO add images dynamically
                //Adds images to image array
                arrayImages.add("https://s3.amazonaws.com/pix.iemoji.com/images/emoji/apple/ios-12/256/salt.png");
                arrayImages.add("https://cdn11.bigcommerce.com/s-arl5b/images/stencil/500x659/products/631/7841/OliveOilPuget__96925.1441043323.jpg?c=2&imbypass=on");
                arrayImages.add("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQbeLciKcsVyGdM9M2mPSZD7DftE3Lbz7TwzEEvNHr7uJt2Qe7N");


                //Initializes RecyclerView
                initRecyclerView();

            }

            @Override
            //Throws an error if there is one connecting to the database
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ", databaseError.toException());
            }
        });
    }

    private void initRecyclerView() {
        //LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        //Creates a layout manager
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);

        //Gets RecyclerView instance from View
        RecyclerView recyclerView = findViewById(R.id.recyclerVIew);

        //Sets layout manager to the recycler view
        recyclerView.setLayoutManager(layoutManager);

        //Creates a new adapter object
        adapter = new FirstFiveIngredients_RecyclerViewAdapter(this, arrayNames, arrayImages, arrayIds);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);
    }

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
