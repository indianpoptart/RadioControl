package com.nikhilparanjape.radiocontrol.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.adapters.RecyclerAdapter

/**
 * Created by Nikhil on 07/31/2018.
 */

class CellFragment : Fragment() {
    private var cellTrouble = arrayOf("Blacklist", "Crisis", "Gotham", "Banshee", "Breaking Bad")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.cell_fragment, container, false)

        val rv = rootView.findViewById<RecyclerView>(R.id.cellRV)
        rv.layoutManager = LinearLayoutManager(this.activity)

        val adapter = RecyclerAdapter(cellTrouble)
        rv.adapter = adapter

        return rootView
    }
}
