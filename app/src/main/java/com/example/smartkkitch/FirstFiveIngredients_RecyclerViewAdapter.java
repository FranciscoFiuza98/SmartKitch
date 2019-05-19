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

import java.util.ArrayList;

public class FirstFiveIngredients_RecyclerViewAdapter extends RecyclerView.Adapter<FirstFiveIngredients_RecyclerViewAdapter.ViewHolder> {

    //Tag for debugging
    private static final String TAG = "FirstFiveIngredients_Re";

    //Arraylists with ingredient names and images
    private ArrayList<String> arrayNames;
    private ArrayList<String> arrayImages;
    private ArrayList<String> arrayIds;
    private ArrayList<String> arrayFavoriteIngredientsNames = new ArrayList<>();
    private ArrayList<String> getArrayFavoriteIngredientsIds = new ArrayList<>();

    //Activity context
    private Context context;

    //RecyclerView adapater constructor
    FirstFiveIngredients_RecyclerViewAdapter(Context context, ArrayList<String> arrayNames, ArrayList<String> arrayImages, ArrayList<String> arrayIds) {
        this.arrayNames = arrayNames;
        this.arrayImages = arrayImages;
        this.arrayIds = arrayIds;
        this.context = context;
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

        //Adds ingredient image to each card
        Glide.with(context)
                .asBitmap()
                .load(arrayImages.get(i))
                .into(viewHolder.image);

        //Adds ingredient name to each card
        viewHolder.name.setText(arrayNames.get(i));

        //Adds ingredient id to the hidden id field in each card
        viewHolder.id.setText(arrayIds.get(i));

        //OnClick listener for each card
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    //Removes ingredient name and id from the respective "favorite" arrays
                    arrayFavoriteIngredientsNames.remove(arrayNames.get(i));
                    getArrayFavoriteIngredientsIds.remove(arrayIds.get(i));
                } else if (btnBitMap.sameAs(bitMapNotChecked)) {
                    viewHolder.btnCheckbox.setImageResource(R.drawable.checked);

                    //Adds ingredient name and id to the respective "favorite" arrays
                    arrayFavoriteIngredientsNames.add(arrayNames.get(i));
                    getArrayFavoriteIngredientsIds.add(arrayIds.get(i));
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return arrayNames.size();
    }

    ArrayList<String> getFavoriteIngredientsNames() {
        return arrayFavoriteIngredientsNames;
    }

    ArrayList<String> getFavoriteIngredientsIds() {
        return getArrayFavoriteIngredientsIds;
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
