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
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {


        //Gets Generated Recipe object
        final Ingredient ingredient = mIngredients.get(i);

        //Recipe info
        final String ingredientId = ingredient.getId();
        final String ingredientName = ingredient.getName();

        //Sets recipe information to the respective objects in the card
        viewHolder.ingredientName.setText(ingredientName);
        viewHolder.btnCross.setImageResource(R.drawable.cross);

        //On click listener for the card
        viewHolder.btnCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot result = task.getResult();

                                    if (result.size() > 5) {

                                        mIngredients.remove(viewHolder.getAdapterPosition());
                                        notifyItemRemoved(viewHolder.getAdapterPosition());
                                        notifyItemRangeChanged(viewHolder.getAdapterPosition(), mIngredients.size());


                                        firestore.collection("Users").document(currentUser.getEmail()).collection("FavoriteIngredients").document(ingredientId)
                                                .delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(context, "" + ingredientName + " removed from favorite ingredients.", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else {
                                                            Toast.makeText(context, "Could not delete ingredient", Toast.LENGTH_SHORT).show();
                                                        }

                                                    }
                                                });
                                    }else {
                                        Toast.makeText(context, "You can't have less than 5 ingredients", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });

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
        ImageButton btnCross;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ingredientName= itemView.findViewById(R.id.generateIngredientName);
            btnCross = itemView.findViewById(R.id.btnCheckbox);
        }
    }
}