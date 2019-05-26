package com.example.smartkkitch;

import android.content.Intent;
import android.media.Image;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.nio.BufferUnderflowException;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";

    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;

    ImageView imgRecipeImage;
    TextView txtRecipeName;
    String recipeId;
    String recipeName;
    String recipeImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        //Gets intent and extras from previous activvity
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        //Gets recipe image and name references
        imgRecipeImage = findViewById(R.id.imgRecipeImage);
        txtRecipeName = findViewById(R.id.txtRecipeName);

        //Changes the recipe image and name using the extras
        if (extras != null) {
            recipeImageUrl = extras.getString("recipeImage");
            recipeName = extras.getString("recipeName");
            recipeId = extras.getString("recipeId");

            Picasso.get().load(recipeImageUrl).into(imgRecipeImage);
            txtRecipeName.setText(recipeName);
        }

        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.recipeIngredientsPager);

        setupViewPager(mViewPager);

    }

    public void setViewPager(int fragmentNumber) {
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private void setupViewPager(ViewPager viewPager) {

        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new FragmentRecipeIngredients(), "Recipe Ingredients"); //    index 0
        adapter.addFragment(new FragmentRecipePreparation(), "Recipe Preparation"); //    index 1
        adapter.addFragment(new FragmentRecipeSimilarRecipes(), "Recipe Similar Recipes"); // index 2
        viewPager.setAdapter(adapter);

    }

    public Recipe getRecipe() {

     Recipe recipe = new Recipe(recipeId, recipeName, recipeImageUrl);

     return recipe;


    }

}
