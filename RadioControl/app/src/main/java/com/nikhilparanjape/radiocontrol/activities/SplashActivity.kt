package com.nikhilparanjape.radiocontrol.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

import com.nikhilparanjape.radiocontrol.R

/**
 * Created by Nikhil on 4/24/2016.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}