package com.nikhilparanjape.radiocontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class SettingsActivity extends Activity implements AdapterView.OnItemSelectedListener {
    private static final String PRIVATE_PREF = "radiocontrol-prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String versionName = BuildConfig.VERSION_NAME;
        String[] themes = new String[]{"Material Light", "Material Dark"};


        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(MainActivity.getDeviceName()).withEmail(versionName)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        //Creates navigation drawer items
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Home").withIcon(GoogleMaterial.Icon.gmd_wifi);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info);

        //Create navigation drawer
        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(false)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item3
                )

                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Log.d("drawer", "The drawer is: " + drawerItem + " position is " + position);
                        //Settings button
                        if (position == 1) {
                            startMainActivity();
                            Log.d("drawer", "Started about activity");
                        }
                        //About button
                        else if (position == 4) {
                            startAboutActivity();
                            Log.d("drawer", "Started about activity");
                        }
                        return false;
                    }
                })
                .build();
        result.setSelection(3);

        //Initialize Timer Spinner
        Spinner spinner = (Spinner) findViewById(R.id.themeSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, themes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        SharedPreferences sp = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        long theme = sp.getLong("themes", 0);
        spinner.setSelection((int) theme);
        spinner.setOnItemSelectedListener(this);
    }





    //starts about activity
    public void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    //starts about activity
    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.d("Spinner","Value set to: " + id);
        SharedPreferences sp = getSharedPreferences(PRIVATE_PREF, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("themes", id);
        editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }

}
