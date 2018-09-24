package com.nikhilparanjape.radiocontrol.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.adapters.RecyclerAdapter

/**
 * Created by Nikhil on 07/31/2018.
 */

class BatteryFragment : Fragment() {
    private var batteryTrouble = arrayOf("Blacklist", "Crisis", "Gotham", "Banshee", "Breaking Bad")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.battery_fragment, container, false)

        val rv = rootView.findViewById<RecyclerView>(R.id.batteryRV)
        rv.layoutManager = LinearLayoutManager(this.activity)

        val adapter = RecyclerAdapter(this.activity!!, batteryTrouble)
        rv.adapter = adapter

        return rootView
    }

}
