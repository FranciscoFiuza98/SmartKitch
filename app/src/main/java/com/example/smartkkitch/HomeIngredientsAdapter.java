package com.example.smartkkitch;

import android.content.Context;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeIngredientsAdapter extends RecyclerView.Adapter<HomeIngredientsAdapter.ViewHolder> {

    //Tag for debugging
    private static final String TAG = "HomeIngredientsAdapter";

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    //ArrayLists that hold ingredients given in adapter constructor and favoriteIngredients
    private ArrayList<IngredientAdapter> ingredients;

    //Activity context
    private Context context;

    //RecyclerView adapater constructor
    HomeIngredientsAdapter(Context context, ArrayList<IngredientAdapter> ingredients, FirebaseUser currentUser) {
        this.ingredients = ingredients;
        this.context = context;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //Inflates layout with the ingredient layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_five_ingredients, viewGroup, false);

        firestore = FirebaseFirestore.getInstance();

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        //Current ingredient of the iteration
        final IngredientAdapter ingredient = ingredients.get(i);

        Log.d(TAG, "Ingredient Image Changed: " + ingredient.isImageChanged());

        //Adds ingredient image to each card
        Glide.with(context)
                .asBitmap()
                .load(ingredient.getImageUrl())
                .into(viewHolder.image);

        //Adds ingredient name to each card
        viewHolder.name.setText(ingredient.getName());

        //Adds ingredient id to the hidden id field in each card
        viewHolder.id.setText(ingredient.getId());


        if (ingredient.isImageChanged()) {
            viewHolder.btnCheckbox.setImageResource(R.drawable.checkedfilled);
        } else {
            viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);
        }
        //OnClick listener for each card
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ingredient.setImageChanged(true);

                Log.d(TAG, "Image Changed: " + ingredient.isImageChanged());

                viewHolder.btnCheckbox.setImageResource(R.drawable.checkedfilled);

                //Ingredient clicked
                final IngredientAdapter ingredient = ingredients.get(viewHolder.getAdapterPosition());
                String userEmail = currentUser.getEmail();

                //Ingredient clicked HashMap object containing ingredient name and ID
                final Map<String, Object> favoriteIngredient = new HashMap<>();
                favoriteIngredient.put("name", ingredient.getName());
                favoriteIngredient.put("imageUrl", ingredient.getImageUrl());

                //Adds ingredient to database
                assert userEmail != null;
                firestore.collection("Users").document(userEmail).collection("FavoriteIngredients").document(ingredient.getId())
                        .set(favoriteIngredient)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                ingredients.remove(viewHolder.getAdapterPosition());
                                notifyItemRemoved(viewHolder.getAdapterPosition());
                                notifyItemRangeChanged(viewHolder.getAdapterPosition(), ingredients.size());

                                Toast.makeText(context, "" + ingredient.getName() + " added to favorite ingredients!", Toast.LENGTH_LONG).show();
                                incrementIngredientNumberSaves(ingredient);

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                ingredients.add(ingredient);
                                notifyItemInserted(viewHolder.getAdapterPosition());
                                notifyItemRangeChanged(viewHolder.getAdapterPosition(), ingredients.size());

                                Log.w(TAG, "onFailure: ", e);
                                Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                            }
                        });

            }
        });


    }

    private void incrementIngredientNumberSaves(final IngredientAdapter ingredient) {

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
                                                Log.d(TAG, "Number of saves updated: " + numberSavesMap);
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

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //Variables for the items present in each card
        ImageView image;
        TextView name;
        TextView id;
        TextView txtChecked;
        ImageButton btnCheckbox;
        CardView cardView;
        boolean imageChanged;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Gets references to the items in each card
            image = itemView.findViewById(R.id.imgImage);
            name = itemView.findViewById(R.id.txtName);
            id = itemView.findViewById(R.id.txtId);
            txtChecked = itemView.findViewById(R.id.txtChecked);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            cardView = itemView.findViewById(R.id.cardView);

            imageChanged = false;
        }
    }

}
