package com.nikhilparanjape.radiocontrol.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.holders.RecyclerHolder

class RecyclerAdapter(internal var c: Context, internal var troubleshoot: Array<String>) : RecyclerView.Adapter<RecyclerHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.model, parent, false)

        return RecyclerHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {
        holder.nametxt.text = troubleshoot[position]

    }

    override fun getItemCount(): Int {
        return troubleshoot.size
    }
}
