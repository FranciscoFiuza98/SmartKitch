package com.example.smartkkitch;

import android.content.Context;
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

    private static final String TAG = "HomeRecipeRecyclerViewA";

    private ArrayList<Recipe> mRecipes;

    private Context context;

    public HomeRecipeRecyclerViewAdapter(Context context, ArrayList<Recipe> mRecipes) {
        this.mRecipes = mRecipes;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the ingredient layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final Recipe recipe = mRecipes.get(i);

        Glide.with(context)
                .asBitmap()
                .load(recipe.getImageUrl())
                .into(viewHolder.imgRecipeImage);

        viewHolder.txtRecipeName.setText(recipe.getName());
        viewHolder.txtRecipeId.setText(recipe.getId());

        viewHolder.recipeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked: " + recipe);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

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
