package com.example.smartkkitch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentForYou extends Fragment{
    private static final String TAG = "FragmentForYou";

    private ArrayList<String> mArrayRecipeIds = new ArrayList<>();
    private ArrayList<String> mArrayRecipeNames = new ArrayList<>();
    private ArrayList<String> mArrayRecipeImageUrls = new ArrayList<>();
    private ArrayList<Recipe> mRecipes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        //Gets instances for each object inside fragment
        Button btnForYou = view.findViewById(R.id.btnForYou);
        Button btnMeat = view.findViewById(R.id.btnMeat);
        RecyclerView recipeRecyclerView = view.findViewById(R.id.recipeRecyclerView);

        //Fills Recipe IDs array
        mArrayRecipeIds.add("556768");
        mArrayRecipeIds.add("855089");
        mArrayRecipeIds.add("107697");
        mArrayRecipeIds.add("760573");
        mArrayRecipeIds.add("44446");

        //Fills Recipe Names array
        mArrayRecipeNames.add("Orange Carrot Ginger Juice {No Added Sugar, Dairy & Gluten Free}");
        mArrayRecipeNames.add("Rainbow Vegetable Hummus Box");
        mArrayRecipeNames.add("Yummy to the Tummy Clock");
        mArrayRecipeNames.add("Edamame Fried Rice");
        mArrayRecipeNames.add("Radish Slaw with New York Deli Dressing");

        //Fills recipe Image Urls array
        mArrayRecipeImageUrls.add("https://spoonacular.com/recipeImages/556768-312x231.jpg");
        mArrayRecipeImageUrls.add("https://spoonacular.com/recipeImages/855089-312x231.jpg");
        mArrayRecipeImageUrls.add("https://spoonacular.com/recipeImages/107697-312x231.jpg");
        mArrayRecipeImageUrls.add("https://spoonacular.com/recipeImages/760573-312x231.jpg");
        mArrayRecipeImageUrls.add("https://spoonacular.com/recipeImages/44446-312x231.jpg");

        //Creates Recipe object with the information given in each array and adds object to mRecipes array
        for (int i = 0; i < mArrayRecipeIds.size(); i++) {
            String recipeId = mArrayRecipeIds.get(i);
            String recipeName = mArrayRecipeNames.get(i);
            String recipeImageUrl = mArrayRecipeImageUrls.get(i);

            Recipe recipe = new Recipe(recipeId, recipeName, recipeImageUrl);

            mRecipes.add(recipe);
        }

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


