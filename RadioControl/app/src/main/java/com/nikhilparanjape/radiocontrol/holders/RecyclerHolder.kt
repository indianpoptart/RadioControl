package com.nikhilparanjape.radiocontrol.holders

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView

import com.nikhilparanjape.radiocontrol.R

class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var nametxt: TextView = itemView.findViewById(R.id.nameTxt)

}