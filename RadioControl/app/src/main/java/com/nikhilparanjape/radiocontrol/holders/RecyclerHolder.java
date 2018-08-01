package com.nikhilparanjape.radiocontrol.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.nikhilparanjape.radiocontrol.R;

public class RecyclerHolder extends RecyclerView.ViewHolder {

    public TextView nametxt;

    public RecyclerHolder(View itemView) {
        super(itemView);

        nametxt= itemView.findViewById(R.id.nameTxt);
    }
}