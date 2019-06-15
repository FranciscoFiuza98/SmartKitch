package com.example.smartkkitch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MyIngredientsAdapter extends RecyclerView.Adapter<MyIngredientsAdapter.ViewHolder> {

    private static final String TAG = "MyIngredientsAdapter";

    //Generated recipes list given in constructor
    private ArrayList<Ingredient> mIngredients;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Context given in constructor
    private Context context;

    //Constructor
    public MyIngredientsAdapter(Context context, ArrayList<Ingredient> mIngredients) {
        this.mIngredients= mIngredients;
        this.context = context;
    }

    //Creates View object with the ingredient information
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the recipe_ingredients layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.generate_ingredient, viewGroup, false);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }

    //Creation of every card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position) {


        //Gets Generated Recipe object
        final Ingredient ingredient = mIngredients.get(position);

        //Recipe info
        final String ingredientId = ingredient.getId();
        final String ingredientName = ingredient.getName();

        //Sets recipe information to the respective objects in the card
        viewHolder.ingredientName.setText(ingredientName);
        viewHolder.btnCross.setImageResource(R.drawable.cross);

        if (position%2 == 0) {
            viewHolder.generateIngredientCard.setBackgroundColor(Color.parseColor("#F7F7F7"));
        } else {
            viewHolder.generateIngredientCard.setBackgroundColor(Color.WHITE);
        }

        //FIXME Fix crash when spamming remove button

        //On click listener for the button
        viewHolder.btnCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mIngredients.size() > 5) {

                    mIngredients.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mIngredients.size());

                    firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientId)
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(context, "" + ingredientName + " removed from favorite ingredients.", Toast.LENGTH_SHORT).show();
                                        decrementIngredientNumberSaves(ingredient);
                                    }
                                    else {
                                        Toast.makeText(context, "Could not delete ingredient", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                } else {
                    Toast.makeText(context, "Cannot have less than 5 ingredients", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void decrementIngredientNumberSaves(final Ingredient ingredient) {

        firestore.collection("Ingredients").document(ingredient.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> ingredientMap = result.getData();

                            try {
                                String numberSaves = ingredientMap.get("numberSaves").toString();
                                int numberSavesInt = Integer.parseInt(numberSaves);

                                Log.d(TAG, "Current number: " + numberSaves);

                                numberSavesInt--;

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", ingredient.getName());
                                numberSavesMap.put("imageUrl", ingredient.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number of saves Decrement: " + numberSavesMap);
                                            }
                                        });

                            } catch (NullPointerException exception) {

                                //Creates Map object with 1 save number to add to the database
                                final Map<String, Object> firstNumberSave = new HashMap<>();
                                firstNumberSave.put("name", ingredient.getName());
                                firstNumberSave.put("imageUrl", ingredient.getImageUrl());
                                firstNumberSave.put("numberSaves", 0);

                                //Adds ingredient info to the firestore
                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(firstNumberSave)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "First number save added: " + firstNumberSave);
                                            }
                                        });

                            }


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

        TextView ingredientName;
        CardView generateIngredientCard;
        ImageButton btnCross;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ingredientName= itemView.findViewById(R.id.generateIngredientName);
            btnCross = itemView.findViewById(R.id.btnCheckbox);
            generateIngredientCard = itemView.findViewById(R.id.generateIngredientCard);
        }
    }
}
