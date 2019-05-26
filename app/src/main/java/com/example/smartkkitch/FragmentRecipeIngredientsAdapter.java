package com.example.smartkkitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class FragmentRecipeIngredientsAdapter extends RecyclerView.Adapter<FragmentRecipeIngredientsAdapter.ViewHolder>{

    private static final String TAG = "FragmentRecipeIngredien";

    //Ingredients list given in constructor
    private ArrayList<IngredientRecipe> mIngredients;

    //Context given in constructor
    private Context context;

    //Constructor
    public FragmentRecipeIngredientsAdapter(Context context, ArrayList<IngredientRecipe> ingredients) {
        this.mIngredients= ingredients;
        this.context = context;
    }

    //Creates View object with the ingredient information
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the recipe_ingredients layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_ingredients, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }


    //Creation of every card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        //Gets IngredientRecipe object
        final IngredientRecipe ingredientRecipe = mIngredients.get(i);

        //Sets ingredient name, amount and unit
        viewHolder.txtIngredientName.setText(ingredientRecipe.getName());
        viewHolder.txtIngredientAmount.setText(ingredientRecipe.getAmount());
        viewHolder.txtIngredientUnit.setText(ingredientRecipe.getUnit());

        //Criates OnClickListener for each card
        viewHolder.recipeIngredientCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Gets bitmap of the current image of the button
                final Bitmap btnBitMap = ((BitmapDrawable) viewHolder.btnCheckbox.getDrawable()).getBitmap();

                //Gets drawables for the "checked" and "notchecked" images
                Drawable checked = context.getResources().getDrawable(R.drawable.checked);
                Drawable notChecked = context.getResources().getDrawable(R.drawable.notchecked);

                //Gets bitmap for the "checked" and "notchecked" images
                final Bitmap bitMapChecked = ((BitmapDrawable) checked).getBitmap();
                final Bitmap bitMapNotChecked = ((BitmapDrawable) notChecked).getBitmap();

                //Checks which image is currently associated to the button, and changes it to the other one (if "checked" changes to "unchecked" and vice versa
                if (btnBitMap.sameAs(bitMapChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);

                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
                }

            }
        });

        viewHolder.btnCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Gets bitmap of the current image of the button
                final Bitmap btnBitMap = ((BitmapDrawable) viewHolder.btnCheckbox.getDrawable()).getBitmap();

                //Gets drawables for the "checked" and "notchecked" images
                Drawable checked = context.getResources().getDrawable(R.drawable.checked);
                Drawable notChecked = context.getResources().getDrawable(R.drawable.notchecked);

                //Gets bitmap for the "checked" and "notchecked" images
                final Bitmap bitMapChecked = ((BitmapDrawable) checked).getBitmap();
                final Bitmap bitMapNotChecked = ((BitmapDrawable) notChecked).getBitmap();

                //Checks which image is currently associated to the button, and changes it to the other one (if "checked" changes to "unchecked" and vice versa
                if (btnBitMap.sameAs(bitMapChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);

                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
                }
            }
        });

    }

    //Returns adapter item count
    @Override
    public int getItemCount() {
        return mIngredients.size();
    }

    //Card structure
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtIngredientName;
        TextView txtIngredientAmount;
        TextView txtIngredientUnit;
        CardView recipeIngredientCard;
        ImageButton btnCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtIngredientName = itemView.findViewById(R.id.txtIngredientName);
            txtIngredientAmount = itemView.findViewById(R.id.txtIngredientAmount);
            txtIngredientUnit = itemView.findViewById(R.id.txtIngredientUnit);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            recipeIngredientCard = itemView.findViewById(R.id.recipeIngredientCard);
        }
    }
}
