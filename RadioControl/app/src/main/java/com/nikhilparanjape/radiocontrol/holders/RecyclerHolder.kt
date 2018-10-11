package com.nikhilparanjape.radiocontrol.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nikhilparanjape.radiocontrol.R

class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var nametxt: TextView = itemView.findViewById(R.id.nameTxt)

}