package com.example.smartkkitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class GeneratedRecipesAdapter extends RecyclerView.Adapter<GeneratedRecipesAdapter.ViewHolder> {

    private static final String TAG = "GeneratedRecipesAdapter";

    //Generated recipes list given in constructor
    private ArrayList<GeneratedRecipe> mGeneratedRecipes;

    //Context given in constructor
    private Context context;

    //Constructor
    public GeneratedRecipesAdapter(Context context, ArrayList<GeneratedRecipe> mGeneratedRecipes) {
        this.mGeneratedRecipes = mGeneratedRecipes;
        this.context = context;
    }

    //Creates View object with the ingredient information
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the recipe_ingredients layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.generated_recipe, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }


    //Creation of every card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {


        //Gets Generated Recipe object
        final GeneratedRecipe generatedRecipe = mGeneratedRecipes.get(i);

        //Recipe info
        final String recipeId = generatedRecipe.getId();
        final String recipeName = generatedRecipe.getName();
        final String recipeImage = generatedRecipe.getImageUrl();
        String usedIngredients = generatedRecipe.getUsedIngredients();
        String missedIngredients = generatedRecipe.getMissedIngredients();

        //Sets recipe information to the respective objects in the card
        viewHolder.generatedRecipeId.setText(recipeId);
        viewHolder.generatedRecipeName.setText(recipeName);
        viewHolder.ingredientsUsed.setText(usedIngredients);
        viewHolder.ingredientsMissed.setText(missedIngredients);

        Glide.with(context)
                .asBitmap()
                .load(recipeImage)
                .into(viewHolder.generatedRecipeImage);

        //On click listener for the card
        viewHolder.generatedRecipeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, RecipeActivity.class);
                intent.putExtra("recipeId", recipeId);
                intent.putExtra("recipeImage", recipeImage);
                intent.putExtra("recipeName", recipeName);

                context.startActivity(intent);

            }
        });

    }

    //Returns adapter item count
    @Override
    public int getItemCount() {
        return mGeneratedRecipes.size();
    }


    //Card structure
    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView generatedRecipeCard;
        ImageView generatedRecipeImage;
        TextView ingredientsUsed, ingredientsMissed, generatedRecipeName, generatedRecipeId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            generatedRecipeCard = itemView.findViewById(R.id.generatedRecipeCard);
            generatedRecipeImage = itemView.findViewById(R.id.imgRecipeImage);
            ingredientsUsed = itemView.findViewById(R.id.txtIngredientsUsed);
            ingredientsMissed = itemView.findViewById(R.id.txtIngredientsMissed);
            generatedRecipeName = itemView.findViewById(R.id.txtRecipeName);
            generatedRecipeId = itemView.findViewById(R.id.txtRecipeId);

        }
    }
}
