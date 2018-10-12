package com.nikhilparanjape.radiocontrol.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nikhilparanjape.radiocontrol.R


/**
 * Created by Nikhil on 10/12/2018.
 *
 * An holder class for TroubleShooting Activity
 */
class RecyclerHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var nametxt: TextView = itemView.findViewById(R.id.nameTxt)

}