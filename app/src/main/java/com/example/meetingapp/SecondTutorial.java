package com.example.meetingapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class SecondTutorial extends Fragment {
    private String title;
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static SecondTutorial newInstance(int page, String title) {
        SecondTutorial fragment = new SecondTutorial();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second_tutorial, container, false);
        TextView tvLabel = (TextView) view.findViewById(R.id.editText2);
        tvLabel.setText(page + " -- " + title);
        return view;
    }
}
