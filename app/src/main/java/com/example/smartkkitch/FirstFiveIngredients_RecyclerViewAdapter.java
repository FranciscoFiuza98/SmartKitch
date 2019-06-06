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

public class FirstFiveIngredients_RecyclerViewAdapter extends RecyclerView.Adapter<FirstFiveIngredients_RecyclerViewAdapter.ViewHolder> {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients_Re";

    private String firstFiveIngredientsContext = "com.example.smartkkitch.FirstFiveIngredients";
    private String homeContext = "com.example.smartkkitch.Home";
    private String applicationContext = "android.app.Application";
    private String currentContext;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    //ArrayLists that hold ingredients given in adapter constructor and favoriteIngredients
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Ingredient> favoriteIngredients = new ArrayList<>();

    private int firstBind = 0;

    //Activity context
    private Context context;

    //RecyclerView adapater constructor
    FirstFiveIngredients_RecyclerViewAdapter(Context context, ArrayList<Ingredient> ingredients, FirebaseUser currentUser) {
        this.ingredients = ingredients;
        this.context = context;
        this.currentContext = context.toString().split("@")[0];
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

    /*
    TODO Fix repetetive checking:
        - Find a way to change the button image only for that ingredient (Compare ID).
        - Try putting a TextView in the card and changing it on click to see if its the card's, button or image problem*/

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        //Current ingredient of the iteration
        final Ingredient ingredient = ingredients.get(i);

        //Adds ingredient image to each card
        Glide.with(context)
                .asBitmap()
                .load(ingredient.getImageUrl())
                .into(viewHolder.image);

        //Adds ingredient name to each card
        viewHolder.name.setText(ingredient.getName());

        //Adds ingredient id to the hidden id field in each card
        viewHolder.id.setText(ingredient.getId());

        viewHolder.btnCheckbox.setTag(ingredient.getId());

        //OnClick listener for each card
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (currentContext.equals(homeContext) || currentContext.equals(applicationContext)) {

                    //Ingredient clicked
                    final Ingredient ingredient = ingredients.get(viewHolder.getAdapterPosition());
                    String userEmail = currentUser.getEmail();

                    //Ingredient clicked HashMap object containing ingredient name and ID
                    final Map<String, Object> favoriteIngredient = new HashMap<>();
                    favoriteIngredient.put("name", ingredient.getName());
                    favoriteIngredient.put("imageUrl", ingredient.getImageUrl());

                    ingredients.remove(viewHolder.getAdapterPosition());
                    notifyItemRemoved(viewHolder.getAdapterPosition());
                    notifyItemRangeChanged(viewHolder.getAdapterPosition(), ingredients.size());

                    //Adds ingredient to database
                    assert userEmail != null;
                    firestore.collection("Users").document(userEmail).collection("FavoriteIngredients").document(ingredient.getId())
                            .set(favoriteIngredient)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    Toast.makeText(context, "" + ingredient.getName() + " added to favorite ingredients!", Toast.LENGTH_LONG).show();
                                    incrementIngredientNumberSaves(ingredient);

                                }
                            })

                            //TODO Find a way to force error to test the ingredient addition to the Recycler View
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
                else if (currentContext.equals(firstFiveIngredientsContext)){

                    //Toast.makeText(context, arrayIds.get(i), Toast.LENGTH_LONG).show();

                    //Gets bitmap of the current image of the button
                    final Bitmap btnBitMap = ((BitmapDrawable) viewHolder.btnCheckbox.getDrawable()).getBitmap();

                    //Gets drawables for the "checked" and "notchecked" images
                    Drawable checked = context.getResources().getDrawable(R.drawable.checked);
                    Drawable notChecked = context.getResources().getDrawable(R.drawable.notchecked);

                    //Gets bitmap for the "checked" and "notchecked" images
                    final Bitmap bitMapChecked = ((BitmapDrawable) checked).getBitmap();
                    final Bitmap bitMapNotChecked = ((BitmapDrawable) notChecked).getBitmap();

                    //Checks which image is currently associated to the button, and changes it to the other one (if "checked" changes to "unchecked" and vice versa
                    if (btnBitMap.sameAs(bitMapChecked) && viewHolder.btnCheckbox.getTag().equals(ingredient.getId())) {
                        viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);

                        favoriteIngredients.remove(ingredients.get(i));

                    } else if (btnBitMap.sameAs(bitMapNotChecked) && viewHolder.btnCheckbox.getTag().equals(ingredient.getId())) {
                        viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                        favoriteIngredients.add(ingredients.get(i));
                    }
                }


            }
        });


    }

    private void incrementIngredientNumberSaves(final Ingredient ingredient) {

        firestore.collection("Ingredients").document(ingredient.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> ingredientMap = result.getData();
                            String numberSaves = "";

                            try {
                                numberSaves = ingredientMap.get("numberSaves").toString();

                                int numberSavesInt = Integer.parseInt(numberSaves);

                                numberSavesInt++;

                                final Map<String, Object> numberSavesMap = new HashMap<>();
                                numberSavesMap.put("name", ingredient.getName());
                                numberSavesMap.put("imageUrl", ingredient.getImageUrl());
                                numberSavesMap.put("numberSaves", numberSavesInt);

                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(numberSavesMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Number of saves updated: " + numberSavesMap);
                                            }
                                        });

                            } catch (NullPointerException exception) {

                                final Map<String, Object> firstNumberSave = new HashMap<>();
                                firstNumberSave.put("name", ingredient.getName());
                                firstNumberSave.put("imageUrl", ingredient.getImageUrl());
                                firstNumberSave.put("numberSaves", 1);

                                firestore.collection("Ingredients").document(ingredient.getId())
                                        .set(firstNumberSave)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Ingredients ID: " + ingredient.getId());
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

    ArrayList<Ingredient> getFavoriteIngredients() {
        return favoriteIngredients;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //Variables for the items present in each card
        ImageView image;
        TextView name;
        TextView id;
        TextView txtChecked;
        ImageButton btnCheckbox;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Gets references to the items in each card
            image = itemView.findViewById(R.id.imgImage);
            name = itemView.findViewById(R.id.txtName);
            id = itemView.findViewById(R.id.txtId);
            txtChecked = itemView.findViewById(R.id.txtChecked);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

}
