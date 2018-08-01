package com.nikhilparanjape.radiocontrol.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.holders.RecyclerHolder;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerHolder> {

    Context c;
    String[] troubleshoot;

    public RecyclerAdapter(Context c, String[] troubleshoot) {
        this.c = c;
        this.troubleshoot = troubleshoot;
    }

    @Override
    public RecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.model,parent,false);

        return new RecyclerHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerHolder holder, int position) {
        holder.nametxt.setText(troubleshoot[position]);

    }

    @Override
    public int getItemCount() {
        return troubleshoot.length;
    }
}
