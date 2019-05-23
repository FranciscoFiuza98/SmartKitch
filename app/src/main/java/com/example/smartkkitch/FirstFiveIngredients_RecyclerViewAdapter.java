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

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class FirstFiveIngredients_RecyclerViewAdapter extends RecyclerView.Adapter<FirstFiveIngredients_RecyclerViewAdapter.ViewHolder> {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients_Re";

    private String firstFiveIngredientsContext = "com.example.smartkkitch.FirstFiveIngredients";
    private String homeContext = "com.example.smartkkitch.Home";
    private String currentContext;

    //ArrayLists that hold ingredients given in adapter constructor and favoriteIngredients
    private ArrayList<Ingredient> ingredients;
    private ArrayList<Ingredient> favoriteIngredients = new ArrayList<>();
    private ArrayList<String> homeFavoriteIngredientNames = new ArrayList<>();
    private ArrayList<String> homeFavoriteIngredientIds = new ArrayList<>();

    //Activity context
    private Context context;

    //RecyclerView adapater constructor
    FirstFiveIngredients_RecyclerViewAdapter(Context context, ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
        this.context = context;
        this.currentContext = context.toString().split("@")[0];
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //Inflates layout with the ingredient layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.first_five_ingredients, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }

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

        //OnClick listener for each card
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //TODO add new favorite ingredient to database before removing from RecyclerView
                if (currentContext.equals(homeContext)) {
                    ingredients.remove(viewHolder.getAdapterPosition());
                    notifyItemRemoved(viewHolder.getAdapterPosition());
                    notifyItemRangeChanged(viewHolder.getAdapterPosition(), ingredients.size());
                }
                else {
                    String firstFiveIngredientsContext = "com.example.smartkkitch.FirstFiveIngredients";
                    String homeContext = "com.example.smartkkitch.Home";
                    String currentContext = context.toString().split("@")[0];

                    if (currentContext.equals(firstFiveIngredientsContext)) {
                        Log.d(TAG, "First five");
                    }
                    if (currentContext.equals(homeContext)){
                        Log.d(TAG, "Home");
                    }

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
                    if (btnBitMap.sameAs(bitMapChecked)) {
                        viewHolder.btnCheckbox.setImageResource(R.drawable.notchecked);

                        favoriteIngredients.remove(ingredients.get(i));

                    } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                        viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                        favoriteIngredients.add(ingredients.get(i));
                    }
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public ArrayList<Ingredient> getFavoriteIngredients() {
        return favoriteIngredients;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //Variables for the items present in each card
        ImageView image;
        TextView name;
        TextView id;
        ImageButton btnCheckbox;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Gets references to the items in each card
            image = itemView.findViewById(R.id.imgImage);
            name = itemView.findViewById(R.id.txtName);
            id = itemView.findViewById(R.id.txtId);
            btnCheckbox = itemView.findViewById(R.id.btnCheckbox);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

}
