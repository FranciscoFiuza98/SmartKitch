package com.example.smartkkitch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.Objects;

public class FragmentForYou extends Fragment{
    private static final String TAG = "FragmentForYou";

    private Button btnForYou;
    private Button btnMeat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        btnForYou = view.findViewById(R.id.btnForYou);
        btnMeat = view.findViewById(R.id.btnMeat);

        btnForYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Going to FragmentForYou", Toast.LENGTH_SHORT).show();

                ((Home) Objects.requireNonNull(getActivity())).setViewPager(0);
            }
        });

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


