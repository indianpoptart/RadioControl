package com.nikhilparanjape.radiocontrol.activities

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.nikhilparanjape.radiocontrol.R
import kotlinx.android.synthetic.main.activity_troubleshooting.*

import com.nikhilparanjape.radiocontrol.fragments.AppFragment
import com.nikhilparanjape.radiocontrol.fragments.CellFragment
import com.nikhilparanjape.radiocontrol.fragments.WLANFragment


class TroubleshootingActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_troubleshooting)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }


}