package com.nikhilparanjape.radiocontrol.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.databinding.ActivityTroubleshootingBinding
import com.nikhilparanjape.radiocontrol.fragments.AppFragment
import com.nikhilparanjape.radiocontrol.fragments.CellFragment
import com.nikhilparanjape.radiocontrol.fragments.WLANFragment


class TroubleshootingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTroubleshootingBinding


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val wlanFragment = WLANFragment()
                supportFragmentManager.beginTransaction().replace(R.id.content_id, wlanFragment).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                val appFragment = AppFragment()
                supportFragmentManager.beginTransaction().replace(R.id.content_id, appFragment).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                val cellFragment = CellFragment()
                supportFragmentManager.beginTransaction().replace(R.id.content_id, cellFragment).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false



    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTroubleshootingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }


}
