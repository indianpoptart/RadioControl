package com.nikhilparanjape.radiocontrol.fragments

/**
 * Created by Nikhil on 4/24/2016.
 */

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.nikhilparanjape.radiocontrol.R

class FirstSlide : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.intro1, container, false)
    }
}