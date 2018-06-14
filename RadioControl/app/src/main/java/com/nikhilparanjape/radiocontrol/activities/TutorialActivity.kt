package com.nikhilparanjape.radiocontrol.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window

import com.github.paolorotolo.appintro.AppIntro2
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.fragments.SlideFragment

/**
 * Created by Nikhil on 4/24/2016.
 */
class TutorialActivity : AppIntro2() {
    // Please DO NOT override onCreate. Use init.
    override fun init(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        hideSystemUI()

        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(SlideFragment.newInstance(R.layout.intro1))
        addSlide(SlideFragment.newInstance(R.layout.intro2))
        addSlide(SlideFragment.newInstance(R.layout.intro3))
        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        //addSlide(AppIntroFragment.newInstance("Welcome to RadioControl", "Use this app to auto toggle the cell radio when you join a WiFi network", R.mipmap.ic_launcher, R.color.colorPrimary));

        // SHOW or HIDE the statusbar

        showStatusBar(false)
        setDepthAnimation()
    }

    // This snippet hides the system bars.
    private fun hideSystemUI() {
        val w = this.window
        w.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    fun onSkipPressed() {
        // Do something when users tap on Skip button.
    }

    private fun loadMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    override fun onDonePressed() {
        loadMainActivity()
    }

    override fun onSlideChanged() {
        // Do something when the slide changes.
    }

    override fun onNextPressed() {
        // Do something when users tap on Next button.
    }
}
