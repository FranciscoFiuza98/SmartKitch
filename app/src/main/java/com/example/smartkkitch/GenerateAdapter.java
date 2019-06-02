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


public class GenerateAdapter extends RecyclerView.Adapter<GenerateAdapter.ViewHolder> {

    private static final String TAG = "GenerateAdapter";

    //Ingredients list given in constructor
    private ArrayList<Ingredient> mIngredients;
    private ArrayList<String> mSelectedIngredients = new ArrayList<>();

    //Context given in constructor
    private Context context;

    //Constructor
    public GenerateAdapter(Context context, ArrayList<Ingredient> ingredients) {
        this.mIngredients = ingredients;
        this.context = context;
    }

    //Creates View object with the ingredient information
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the recipe_ingredients layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.generate_ingredient, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }


    //Creation of every card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {


        //Gets IngredientRecipe object
        final Ingredient ingredient = mIngredients.get(i);

        //Ingredient name
        final String ingredientName = ingredient.getName();

        //Changes sets the textview's text to the ingredient name in the layout
        viewHolder.generateIngredientName.setText(ingredientName);

        //On click listener for the card
        viewHolder.generateIngredientCard.setOnClickListener(new View.OnClickListener() {
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

                //If the image is checked, removes the ingredient from the selected ingredients array and changes the image
                if (btnBitMap.sameAs(bitMapChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);

                    mSelectedIngredients.remove(ingredientName);

                 //If the image is not checked, adds the ingredient to the selected ingredients array and changes the image
                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                    mSelectedIngredients.add(ingredientName);


                }
            }
        });

        //On click listener for the image button
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

                //If the image is checked, removes the ingredient from the selected ingredients array and changes the image
                if (btnBitMap.sameAs(bitMapChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);

                    mSelectedIngredients.remove(ingredientName);

                    //If the image is not checked, adds the ingredient to the selected ingredients array and changes the image
                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                    mSelectedIngredients.add(ingredientName);

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
    public class ViewHolder extends RecyclerView.ViewHolder {

        CardView generateIngredientCard;
        TextView generateIngredientName;
        ImageButton btnCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            generateIngredientCard = itemView.findViewById(R.id.generateIngredientCard);
            generateIngredientName = itemView.findViewById(R.id.generateIngredientName);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
        }
    }
}
