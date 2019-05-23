package com.example.smartkkitch;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

    //Arrays with ingredient information
    private ArrayList<String> arrayNames = new ArrayList<>();
    private ArrayList<String> arrayImagesUrl = new ArrayList<>();
    private ArrayList<String> arrayIds = new ArrayList<>();

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

                Log.d(TAG, "Names: " + arrayNames.size());
                Log.d(TAG, "IDS: " + arrayIds.size());
                Log.d(TAG, "Images " + arrayImagesUrl.size());

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

        ArrayList<Ingredient> arrayIngredients = new ArrayList<>();

        for(int i = 0; i < arrayIds.size(); i++) {
            String id = arrayIds.get(i);
            String name = arrayNames.get(i);
            String imageUrl = arrayImagesUrl.get(i);

            Ingredient ingredient = new Ingredient(id, name, imageUrl);

            arrayIngredients.add(ingredient);

        }

        //Creates a layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        //Gets RecyclerView instance from View
        RecyclerView recyclerView = findViewById(R.id.recommendedRecyclerView);

        //Sets layout manager to the recycler view
        recyclerView.setLayoutManager(layoutManager);

        //Creates a new adapter object
        FirstFiveIngredients_RecyclerViewAdapter adapter = new FirstFiveIngredients_RecyclerViewAdapter(this, arrayIngredients);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);
    }
}
