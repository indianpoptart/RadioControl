package com.nikhilparanjape.radiocontrol.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.github.paolorotolo.appintro.AppIntro2;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.fragments.SlideFragment;

/**
 * Created by Nikhil on 4/24/2016.
 */
public class TutorialActivity extends AppIntro2 {
    // Please DO NOT override onCreate. Use init.
    @Override
    public void init(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        hideSystemUI();

        // Add your slide's fragments here.
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(SlideFragment.newInstance(R.layout.intro1));
        addSlide(SlideFragment.newInstance(R.layout.intro2));
        addSlide(SlideFragment.newInstance(R.layout.intro3));
        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        //addSlide(AppIntroFragment.newInstance("Welcome to RadioControl", "Use this app to auto toggle the cell radio when you join a WiFi network", R.mipmap.ic_launcher, R.color.colorPrimary));

        // SHOW or HIDE the statusbar
        showStatusBar(false);
        setDepthAnimation();
    }
    // This snippet hides the system bars.
    private void hideSystemUI() {
        Window w = this.getWindow();
        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void onSkipPressed() {
        // Do something when users tap on Skip button.
    }
    private void loadMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


    @Override
    public void onDonePressed() {
        loadMainActivity();
    }

    @Override
    public void onSlideChanged() {
        // Do something when the slide changes.
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }
}
