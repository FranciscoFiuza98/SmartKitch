package com.example.smartkkitch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirestoreRegistrar;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentForYou extends Fragment{
    private static final String TAG = "FragmentForYou";

    FirebaseFirestore firestore;

    //Lists for recipe information and Recipe object
    private ArrayList<Recipe> mRecipes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        firestore = FirebaseFirestore.getInstance();

        //Gets instances for each object inside fragment
        Button btnForYou = view.findViewById(R.id.btnForYou);
        Button btnMeat = view.findViewById(R.id.btnMeat);
        RecyclerView recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView);

        
        //TODO Recommend recipes based on other user's saved recipes

        firestore.collection("Recipes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document: Objects.requireNonNull(task.getResult())) {
                                String recipeId = document.getId();
                                String recipeName = document.get("name").toString();
                                String recipeImageUrl = document.get("imageUrl").toString();

                                Recipe recipe = new Recipe(recipeId, recipeName, recipeImageUrl);

                                mRecipes.add(recipe);
                            }
                        }
                    }
                });


        //Creates layout manager, adapter and sets them to the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recipeRecyclerView.setLayoutManager(layoutManager);
        HomeRecipeRecyclerViewAdapter adapter = new HomeRecipeRecyclerViewAdapter(getActivity(), mRecipes);
        recipeRecyclerView.setAdapter(adapter);



        //On click listener for "For You" button
        btnForYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to FragmentForYou", Toast.LENGTH_SHORT).show();

                ((Home) Objects.requireNonNull(getActivity())).setViewPager(0);
            }
        });

        //On click listener for "Meat" button
        btnMeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to FragmentMeat", Toast.LENGTH_SHORT).show();

                ((Home) Objects.requireNonNull(getActivity())).setViewPager(1);
            }
        });

        return view;
    }
}


