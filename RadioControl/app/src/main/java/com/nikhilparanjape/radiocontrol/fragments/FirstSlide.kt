package com.nikhilparanjape.radiocontrol.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.nikhilparanjape.radiocontrol.R

/**
 * Created by Nikhil on 4/24/2016.
 *
 * The first slide in the Tutorial
 */

class FirstSlide : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.intro1, container, true)
    }
}