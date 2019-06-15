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

public class FragmentRecipePreparationAdapter extends RecyclerView.Adapter<FragmentRecipePreparationAdapter.ViewHolder>{

    private static final String TAG = "PreparationAdapter";

    //Ingredients list given in constructor
    private ArrayList<RecipeStep> mRecipeSteps;

    //Context given in constructor
    private Context context;

    private FirebaseUser currentUser;

    //Constructor
    public FragmentRecipePreparationAdapter(Context context, ArrayList<RecipeStep> recipeSteps, FirebaseUser currentUser) {
        this.mRecipeSteps = recipeSteps;
        this.context = context;
        this.currentUser = currentUser;
    }

    //Creates View object with the ingredient information
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //Inflates layout with the recipe_ingredients layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_preparation, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }


    //Creation of every card
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        RecipeStep recipeStep = new RecipeStep("", "");

        //Orders recipe steps
        for (RecipeStep currentStep: mRecipeSteps) {

            int stepNumber = Integer.parseInt(currentStep.getNumber());

            if (stepNumber == i + 1) {
                recipeStep = currentStep;
                break;
            }

        }

        //Gets recipe step number and description from recipe step object
        String recipeNumber = recipeStep.getNumber();
        String recipeDescription = recipeStep.getDescription();

        //Sets step number and step description

        viewHolder.txtStepNumber.setText("Step " + recipeNumber + ".");
        viewHolder.txtStepDescription.setText(recipeDescription);

        if (i%2 == 0) {
            viewHolder.stepCard.setBackgroundColor(Color.parseColor("#F7F7F7"));
        } else {
            viewHolder.stepCard.setBackgroundColor(Color.WHITE);
        }


    }

    //Returns adapter item count
    @Override
    public int getItemCount() {
        return mRecipeSteps.size();
    }

    //Card structure
    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView txtStepNumber, txtStepDescription;
        CardView stepCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtStepNumber = itemView.findViewById(R.id.txtStepNumber);
            txtStepDescription = itemView.findViewById(R.id.txtStepDescription);
            stepCard = itemView.findViewById(R.id.stepCard);
        }
    }
}
