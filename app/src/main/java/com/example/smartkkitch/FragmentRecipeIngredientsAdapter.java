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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;


public class FragmentRecipeIngredientsAdapter extends RecyclerView.Adapter<FragmentRecipeIngredientsAdapter.ViewHolder>{

    private static final String TAG = "FragmentRecipeIngredien";

    //Ingredients list given in constructor
    private ArrayList<IngredientRecipeAdapter> mIngredients;
    private ArrayList<Ingredient> mFavoriteIngredients = new ArrayList<>();

    //Context given in constructor
    private Context context;

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    //Constructor
    public FragmentRecipeIngredientsAdapter(Context context, ArrayList<IngredientRecipeAdapter> ingredients, ArrayList<Ingredient> mFavoriteIngredients) {
        this.mIngredients= ingredients;
        this.context = context;
        this.mFavoriteIngredients = mFavoriteIngredients;
    }

    //Creates View object with the ingredient information
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the recipe_ingredients layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_ingredients, viewGroup, false);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Returns ViewHolder object with the view created.
        return new ViewHolder(view);
    }



    //Creation of every card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        //Gets IngredientRecipe object
        final IngredientRecipeAdapter ingredientRecipe = mIngredients.get(i);

        final String ingredientId= ingredientRecipe.getId();
        String ingredientName = ingredientRecipe.getName();
        String ingredientAmount  = ingredientRecipe.getAmount();
        String ingredientUnit= ingredientRecipe.getUnit();

        StringTokenizer tokens = new StringTokenizer(ingredientAmount, ".");

        String roundedAmount = tokens.nextToken().trim();


        //Sets ingredient ide, name, amount and unit

        viewHolder.txtRecipeIngredientId.setText(ingredientId);
        viewHolder.txtIngredientName.setText(ingredientName);
        viewHolder.txtIngredientAmount.setText(roundedAmount);
        viewHolder.txtIngredientUnit.setText(ingredientUnit);

        if (ingredientRecipe.isImageChanged()) {
            viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
        } else {
            viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);
        }


        viewHolder.btnCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checks which image is currently associated to the button, and changes it to the other one (if "checked" changes to "unchecked" and vice versa
                if (ingredientRecipe.isImageChanged()) {



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
                                                            ingredientRecipe.setImageChanged(false);

                                                            decrementIngredientNumberSaves(ingredientRecipe);
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
                } else if (!ingredientRecipe.isImageChanged()) {

                    final Map<String, Object> newIngredient = new HashMap<>();
                    newIngredient.put("imageUrl", ingredientRecipe.getImageUrl());
                    newIngredient.put("name", ingredientRecipe.getName());

                    firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientRecipe.getId())
                            .set(newIngredient)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "" + ingredientRecipe.getName() + " added to favorite ingredients", Toast.LENGTH_SHORT).show();

                                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
                                    ingredientRecipe.setImageChanged(true);

                                    incrementIngredientNumberSaves(ingredientRecipe);

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

    private void decrementIngredientNumberSaves(final IngredientRecipeAdapter ingredient) {

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

    private void incrementIngredientNumberSaves(final IngredientRecipeAdapter ingredient) {

        //Gets clicked ingredient from firestore
        firestore.collection("Ingredients").document(ingredient.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            //Gets ingredient object from database
                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> ingredientMap = result.getData();
                            String numberSaves = "";

                            //Tries to get number of saves from ingredient
                            try {
                                //Gets number of ingredient's saves
                                numberSaves = ingredientMap.get("numberSaves").toString();

                                //Parses number of saves to a integer
                                int numberSavesInt = Integer.parseInt(numberSaves);

                                //Increments number of saves
                                numberSavesInt++;

                                //Creates a map object to save to the firestore
                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", ingredient.getName());
                                numberSavesMap.put("imageUrl", ingredient.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                //Saves updated ingredient info to the firestore
                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number of saves Increment: " + numberSavesMap);
                                            }
                                        });

                                //If there is no "numberSaves" field in ingredient document, adds the field with value of 1
                            } catch (NullPointerException exception) {

                                //Creates Map object with 1 save number to add to the database
                                final Map<String, Object> firstNumberSave = new HashMap<>();
                                firstNumberSave.put("name", ingredient.getName());
                                firstNumberSave.put("imageUrl", ingredient.getImageUrl());
                                firstNumberSave.put("numberSaves", 1);

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
