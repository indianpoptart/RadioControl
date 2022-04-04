package com.nikhilparanjape.radiocontrol.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.holders.RecyclerHolder

/**
 * Created by Nikhil on 10/12/2018.
 *
 * @author Nikhil Paranjape
 *
 * An adapter class for TroubleShooting Activity
 */
class RecyclerAdapter(private var troubleshoot: Array<String>) : RecyclerView.Adapter<RecyclerHolder>() {

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
