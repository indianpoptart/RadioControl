package com.nikhilparanjape.radiocontrol.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.adapters.RecyclerAdapter;

/**
 * Created by Nikhil on 07/31/2018.
 */

public class CellFragment extends Fragment{
    String[] cellTrouble = {"Blacklist","Crisis","Gotham","Banshee","Breaking Bad"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView=inflater.inflate(R.layout.cell_fragment,container,false);

        RecyclerView rv= rootView.findViewById(R.id.cellRV);
        rv.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        RecyclerAdapter adapter=new RecyclerAdapter(this.getActivity(),cellTrouble);
        rv.setAdapter(adapter);

        return rootView;
    }
}
