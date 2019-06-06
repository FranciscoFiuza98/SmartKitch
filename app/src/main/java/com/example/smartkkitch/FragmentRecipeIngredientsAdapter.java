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
import android.widget.Toast;

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


public class FragmentRecipeIngredientsAdapter extends RecyclerView.Adapter<FragmentRecipeIngredientsAdapter.ViewHolder>{

    private static final String TAG = "FragmentRecipeIngredien";

    //Ingredients list given in constructor
    private ArrayList<IngredientRecipe> mIngredients;

    //Context given in constructor
    private Context context;

    private FirebaseUser currentUser;

    //Constructor
    public FragmentRecipeIngredientsAdapter(Context context, ArrayList<IngredientRecipe> ingredients, FirebaseUser currentUser) {
        this.mIngredients= ingredients;
        this.context = context;
        this.currentUser = currentUser;
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

        final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        //Gets IngredientRecipe object
        final IngredientRecipe ingredientRecipe = mIngredients.get(i);

        final String ingredientId= ingredientRecipe.getId();
        String ingredientName = ingredientRecipe.getName();
        String ingredientAmount = ingredientRecipe.getAmount();
        String ingredientUnit= ingredientRecipe.getUnit();

        //Sets ingredient ide, name, amount and unit

        viewHolder.txtRecipeIngredientId.setText(ingredientId);
        viewHolder.txtIngredientName.setText(ingredientName);
        viewHolder.txtIngredientAmount.setText(ingredientAmount);
        viewHolder.txtIngredientUnit.setText(ingredientUnit);

        //Gets user's favorite ingredients
        firestore.collection("Users").document(Objects.requireNonNull(currentUser.getEmail())).collection("FavoriteIngredients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {

                                //Favorite ingredient data from database
                                Map<String, Object> favoriteIngredient = document.getData();

                                //Gets ingredient ID
                                String favoriteIngredientId = document.getId();

                                //If the ingredient ID is the same as the current Ingredient ID, marks it as checked
                                if (favoriteIngredientId.equals(ingredientId)) {
                                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

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

                    //Deletes ingredient from user's favorite ingredients collection
                    firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientRecipe.getId())
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Ingredient deleted from favorites: " + ingredientRecipe.getId());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });


                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                    final Map<String, Object> newIngredient = new HashMap<>();
                    newIngredient.put("imageUrl", ingredientRecipe.getImageUrl());
                    newIngredient.put("name", ingredientRecipe.getName());

                    firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientRecipe.getId())
                            .set(newIngredient)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "New ingredient added: " + newIngredient);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "onFailure: ", e);
                                }
                            });
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

                    firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {

                                        QuerySnapshot result = task.getResult();

                                        if (result.size() > 5) {
                                            //Deletes ingredient from user's favorite ingredients collection
                                            firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientRecipe.getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Toast.makeText(context, "" + ingredientRecipe.getName() + " removed from favorite ingredients", Toast.LENGTH_SHORT).show();
                                                            viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);
                                                            Log.d(TAG, "Ingredient deleted from favorites: " + ingredientRecipe.getId());
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    });
                                        }else {
                                            Toast.makeText(context, "Can't have less than 5 favorite ingredients.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }
                            });
                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                    final Map<String, Object> newIngredient = new HashMap<>();
                    newIngredient.put("imageUrl", ingredientRecipe.getImageUrl());
                    newIngredient.put("name", ingredientRecipe.getName());

                    firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientRecipe.getId())
                            .set(newIngredient)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "" + ingredientRecipe.getName() + " added to favorite ingredients", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "New ingredient added: " + newIngredient);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "onFailure: ", e);
                                }
                            });
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
        TextView txtRecipeIngredientId;
        CardView recipeIngredientCard;
        ImageButton btnCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtRecipeIngredientId = itemView.findViewById(R.id.txtRecipeIngredientId);
            txtIngredientName = itemView.findViewById(R.id.txtIngredientName);
            txtIngredientAmount = itemView.findViewById(R.id.txtIngredientAmount);
            txtIngredientUnit = itemView.findViewById(R.id.txtIngredientUnit);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            recipeIngredientCard = itemView.findViewById(R.id.recipeIngredientCard);
        }
    }
}
