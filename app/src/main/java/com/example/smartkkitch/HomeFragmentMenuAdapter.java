package com.example.smartkkitch;

import android.content.Context;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragmentMenuAdapter extends RecyclerView.Adapter<HomeFragmentMenuAdapter.ViewHolder> {

    //Tag for debugging
    private static final String TAG = "HomeIngredientsAdapter";

    private ArrayList<String> mMenuTitles;

    //Activity context
    private Context context;

    //RecyclerView adapater constructor
    HomeFragmentMenuAdapter(Context context, ArrayList<String> mMenuTitles) {
        this.context = context;
        this.mMenuTitles = mMenuTitles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //Inflates layout with the ingredient layout
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.home_fragment_menu, viewGroup, false);

        //Returns ViewHolder object with the view created
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {


       final String menuTitle = mMenuTitles.get(i);

       viewHolder.txtMenuTitle.setText(menuTitle);

        //OnClick listener for each card
        viewHolder.txtMenuTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!menuTitle.equals("For you")) {

                    Toast.makeText(context, "Comming Soon!", Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return mMenuTitles.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        //Variables for the items present in each card
        TextView txtMenuTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Gets references to the items in each card
            txtMenuTitle = itemView.findViewById(R.id.txtMenuTitle);
        }
    }

}
