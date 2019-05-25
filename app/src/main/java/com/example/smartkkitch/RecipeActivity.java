package com.example.smartkkitch;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.nio.BufferUnderflowException;

public class RecipeActivity extends AppCompatActivity {

    private static final String TAG = "RecipeActivity";

    ImageView imgRecipeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        imgRecipeImage = findViewById(R.id.imgRecipeImage);

        if (extras != null) {
            String recipeImage = extras.getString("recipeImage");

            Picasso.get().load(recipeImage).into(imgRecipeImage);
        }



    }
}
