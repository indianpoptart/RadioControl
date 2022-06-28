package com.nikhilparanjape.radiocontrol.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nikhilparanjape.radiocontrol.BuildConfig
import com.topjohnwu.superuser.Shell

/**
 * Created by Nikhil on 4/24/2016.
 *
 * @author Nikhil Paranjape
 *
 *
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}