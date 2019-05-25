package com.example.smartkkitch;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class HomeRecipeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecipeRecyclerViewAdapter.ViewHolder>{

    //Debugging tag
    private static final String TAG = "HomeRecipeRecyclerViewA";

    //Recipe list given in constructor
    private ArrayList<Recipe> mRecipes;

    //Context given in constructor
    private Context context;

    //Constructor
    public HomeRecipeRecyclerViewAdapter(Context context, ArrayList<Recipe> mRecipes) {
        this.mRecipes = mRecipes;
        this.context = context;
    }

    //Creates View object with the recipe layout and activity context and returns it
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the ingredient layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }


    //Creation of evey card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        //Gets Recipe object
        final Recipe recipe = mRecipes.get(i);

        //Fills image
        Glide.with(context)
                .asBitmap()
                .load(recipe.getImageUrl())
                .into(viewHolder.imgRecipeImage);

        //Sets recipe name and ID
        viewHolder.txtRecipeName.setText(recipe.getName());
        viewHolder.txtRecipeId.setText(recipe.getId());

        //Criates OnClickListener for each card that starts the Recipe Activity
        viewHolder.recipeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RecipeActivity.class);
                intent.putExtra("recipeId", recipe.getId());
                intent.putExtra("recipeImage", recipe.getImageUrl());

                context.startActivity(intent);
            }
        });

    }

    //Returns adapter item count
    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    //Card structure
    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgRecipeImage;
        TextView txtRecipeName;
        TextView txtRecipeId;
        CardView recipeCardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            recipeCardView = itemView.findViewById(R.id.recipeCard);
            txtRecipeId = itemView.findViewById(R.id.txtRecipeId);
            imgRecipeImage = itemView.findViewById(R.id.imgRecipeImage);
            txtRecipeName = itemView.findViewById(R.id.txtRecipeName);
        }
    }
}
