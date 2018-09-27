package com.nikhilparanjape.radiocontrol.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem

import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.nikhilparanjape.radiocontrol.fragments.AboutFragment

/**
 * Created by Nikhil Paranjape on 12/16/2015.
 */

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.onPostCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, AboutFragment()).commit()

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.TOOLBAR_ICON_SIZE).paddingDp(IconicsDrawable.TOOLBAR_ICON_PADDING))
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "About"
        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
