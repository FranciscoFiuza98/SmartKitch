package com.example.smartkkitch;

import android.content.Context;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FirstFiveIngredients_RecyclerViewAdapter extends RecyclerView.Adapter<FirstFiveIngredients_RecyclerViewAdapter.ViewHolder> {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients_Re";

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    //ArrayLists that hold ingredients given in adapter constructor and favoriteIngredients
    private ArrayList<IngredientAdapter> ingredients;
    private ArrayList<IngredientAdapter> favoriteIngredients = new ArrayList<>();

    //Activity context
    private Context context;

    //RecyclerView adapater constructor
    FirstFiveIngredients_RecyclerViewAdapter(Context context, ArrayList<IngredientAdapter> ingredients, FirebaseUser currentUser) {
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

        if (ingredient.isImageChanged()) {
            viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
        } else {
            viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);
        }

        //OnClick listener for each card
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Checks which image is currently associated to the button, and changes it to the other one (if "checked" changes to "unchecked" and vice versa
                if (ingredient.isImageChanged()) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);
                    ingredient.setImageChanged(false);
                    favoriteIngredients.remove(ingredients.get(i));

                } else if (!ingredient.isImageChanged()) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);
                    ingredient.setImageChanged(true);
                    favoriteIngredients.add(ingredients.get(i));
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    ArrayList<IngredientAdapter> getFavoriteIngredients() {
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
