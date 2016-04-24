package com.nikhilparanjape.radiocontrol.fragments;

/**
 * Created by Nikhil on 4/24/2016.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nikhilparanjape.radiocontrol.R;

public class FirstSlide extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.intro1, container, false);
        return v;
    }
}