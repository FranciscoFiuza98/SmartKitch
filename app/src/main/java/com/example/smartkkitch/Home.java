package com.example.smartkkitch;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Home extends AppCompatActivity {

    private static final String TAG = "Home";

    //Firebase instances
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    //Arrays with ingredient information
    private ArrayList<String> arrayNames = new ArrayList<>();
    private ArrayList<String> arrayImagesUrl = new ArrayList<>();
    private ArrayList<String> arrayIds = new ArrayList<>();

    //Fragment controllers
    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    //TODO Find a way to update the ingredients recyclerview after backing up from another activity to not show repeated ingredients
    //TODO Add "View All ingredients"
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);

        setupViewPager(mViewPager);

        mAuth = FirebaseAuth.getInstance();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Menu menu = navView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);

        fillArrays();




    }

    public void setViewPager(int fragmentNumber) {
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentForYou(), "For You"); // index 0
        adapter.addFragment(new FragmentMeat(), "Meat"); //      index 1
        viewPager.setAdapter(adapter);

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

                //Gets current user's favorite ingredients so that the RecyclerView doesn't show ingredients that the user already added. This fucntion also initializes RecyclerView
                filterFavoriteIngredients();

            }

            @Override
            //Throws an error if there is one connecting to the database
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: ", databaseError.toException());
            }
        });
    }

    private void filterFavoriteIngredients() {

        //Firebase instances
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        //Array Lists that hold user's favorite ingredients and ingredients that will be shown in the RecyclerView
        final ArrayList<String> currentFavoriteIngredientsIds = new ArrayList<>();
        final ArrayList<Ingredient> arrayIngredients = new ArrayList<>();

        //Gets user's favorite ingredients from database
        assert currentUser != null;
        firestore.collection("Users").document(Objects.requireNonNull(currentUser.getEmail())).collection("FavoriteIngredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                //Favorite ingredient data from database
                                Map<String, Object> favoriteIngredient = document.getData();

                                //Iterates over ingredient and adds it's ID to the currentFavoriteIngredientsId ArrayList
                                for (String key : favoriteIngredient.keySet()) {
                                    String value = (String) favoriteIngredient.get(key);

                                    if (key.equals("ingredientId")) {
                                        currentFavoriteIngredientsIds.add(value);
                                    }
                                }
                            }

                            //Iterates over arrayIds, arrayNames and arrayImagesUrl arrays
                            for (int i = 0; i < arrayIds.size(); i++) {
                                String id = arrayIds.get(i);
                                String name = arrayNames.get(i);
                                String imageUrl = arrayImagesUrl.get(i);
                                boolean favorite = false;

                                //Checks if current ingredients is in the currentFavoriteIngredientsId ArrayList
                                for (String favoriteIngredientId : currentFavoriteIngredientsIds) {

                                    if (id.equals(favoriteIngredientId)) {

                                        favorite = true;
                                    }

                                }

                                //If ingredient doesn't exist in currentFavoriteIngredientIds ArrayList, creates Ingredient Object with given information and adds it to the arrayIngredients ArrayList
                                if (!favorite) {
                                    Ingredient ingredient = new Ingredient(id, name, imageUrl);
                                    arrayIngredients.add(ingredient);
                                }
                            }

                            //Initalizes RecyclerView
                            initRecyclerView(currentUser, arrayIngredients);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });


    }

    private void initRecyclerView(FirebaseUser currentUser, ArrayList<Ingredient> arrayIngredients) {

        //Creates a layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        //Gets RecyclerView instance from View
        RecyclerView recyclerView = findViewById(R.id.recommendedRecyclerView);

        //Sets layout manager to the recycler view
        recyclerView.setLayoutManager(layoutManager);

        //Creates a new adapter object
        //TODO Show relevant ingredients (Most liked). Think of logic when there are no most liked ingredients to show.
        FirstFiveIngredients_RecyclerViewAdapter adapter = new FirstFiveIngredients_RecyclerViewAdapter(this, arrayIngredients, currentUser);

        //Sets adapter to RecyclerView
        recyclerView.setAdapter(adapter);

    }

    //Bottom navigation on item select listener
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            //Switches between the item selected and starts corresponding activity
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    return true;
                case R.id.navigation_dashboard:
                    Intent myRecipesIntent = new Intent(getApplicationContext(), MyRecipesActivity.class);
                    startActivity(myRecipesIntent);
                    return true;
                case R.id.navigation_notifications:
                    Intent generateIntent = new Intent(getApplicationContext(), GenerateActivity.class);
                    startActivity(generateIntent);
                    return true;
            }
            return false;
        }
    };
}
