package com.nikhilparanjape.radiocontrol.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.nikhilparanjape.radiocontrol.R

/**
 * Created by Nikhil Paranjape on 04/11/2016.
 *
 * @author Nikhil Paranjape
 *
 * Renders the static changelog into a scrolling view
 */

class ChangeLogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_changelog)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).apply {
                colorInt = Color.WHITE
                sizeDp = 24
                paddingDp = 1
            })
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Changelog"
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
