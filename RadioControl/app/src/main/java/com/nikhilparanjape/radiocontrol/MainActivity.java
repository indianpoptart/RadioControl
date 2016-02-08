package com.nikhilparanjape.radiocontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Nikhil Paranjape on 11/3/2015.
 */

public class MainActivity extends Activity {
    private static final String PRIVATE_PREF = "prefs";
    private static final String VERSION_KEY = "version_number";
    Drawable icon;
    String versionName = BuildConfig.VERSION_NAME;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();//initializes the whats new dialog
        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        final TextView statusText = (TextView)findViewById(R.id.statusText);
        final TextView linkText = (TextView)findViewById(R.id.linkSpeed);
        final TextView connectionStatusText = (TextView) findViewById(R.id.pingStatus);
        Switch toggle = (Switch) findViewById(R.id.enableSwitch);

        rootInit();
        //LinkSpeed Button
        Button linkSpeedButton = (Button) findViewById(R.id.linkSpeedButton);
        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isEasterEgg",false)){
            linkSpeedButton.setVisibility(View.GONE);
            linkText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isEasterEgg",false)){
            linkSpeedButton.setVisibility(View.VISIBLE);
            linkText.setVisibility(View.VISIBLE);
        }

        linkSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int linkspeed = WifiReceiver.linkSpeed(getApplicationContext());
                if(linkspeed == -1){
                    linkText.setText("Unknown network detected");
                }
                else{
                    linkText.setText("Link speed = " + linkspeed + "Mbps");
                }


            }

        });

        //Connection Test button
        Button conn = (Button) findViewById(R.id.pingTestButton);
        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isEasterEgg",false)){
            conn.setVisibility(View.GONE);
            connectionStatusText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isEasterEgg",false)){
            conn.setVisibility(View.VISIBLE);
            connectionStatusText.setVisibility(View.VISIBLE);
        }

        conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utilities.isOnline()){
                    if(Utilities.isConnectedWifi(getApplicationContext())){
                        connectionStatusText.setText("Connected to the internet thru WIFI");
                        connectionStatusText.setTextColor(getResources().getColor(R.color.status_activated));
                    }
                    else if(Utilities.isConnectedMobile(getApplicationContext())){
                        if(Utilities.isConnectedFast(getApplicationContext())){
                            connectionStatusText.setText("Connected to the internet thru FAST CELL");
                            connectionStatusText.setTextColor(getResources().getColor(R.color.status_activated));
                        }
                        else if(!Utilities.isConnectedFast(getApplicationContext())){
                            connectionStatusText.setText("Connected to the internet thru SLOW CELL");
                            connectionStatusText.setTextColor(getResources().getColor(R.color.status_activated));
                        }

                    }

                }
                else{
                    if(Utilities.isAirplaneMode(getApplicationContext()) && !Utilities.isConnected(getApplicationContext())){
                        connectionStatusText.setText("Airplane mode is on");
                        connectionStatusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                    }
                    else{
                        connectionStatusText.setText("Unable to connect to the internet");
                        connectionStatusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                    }

                }

            }

        });

        drawerCreate(); //Initalizes Drawer
        //rootInit();//Checks for root

        if(rootInit() == false){
            toggle.setClickable(false);
            statusText.setText("couldn't get root");
            statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
        }

        if(sharedPref.getInt("isActive",1) == 1){
            if(rootInit() == false){
                toggle.setClickable(false);
                statusText.setText("couldn't get root");
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
            }
            else{
                statusText.setText("is Enabled");
                statusText.setTextColor(getResources().getColor(R.color.status_activated));
                toggle.setChecked(true);
            }

        }
        else if(sharedPref.getInt("isActive",1) == 0){
            if(rootInit() == false){
                toggle.setClickable(false);
                statusText.setText("couldn't get root");
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
            }
            else{
                statusText.setText("is Disabled");
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                toggle.setChecked(false);
            }

        }

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    editor.putInt("isActive",0);
                    statusText.setText("is Disabled");
                    statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                    editor.commit();

                } else {
                    editor.putInt("isActive",1);
                    statusText.setText("is Enabled");
                    statusText.setTextColor(getResources().getColor(R.color.status_activated));
                    editor.commit();
                }
            }
        });

    }

    //Initialize method for the Whats new dialog
    private void init() {
        SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int currentVersionNumber = 0;

        int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception e) {}

        if (currentVersionNumber > savedVersionNumber) {
            showWhatsNewDialog();
            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.commit();
        }
    }

    //Method to create the Navigation Drawer
    public void drawerCreate(){
        //Drawable lg = getResources().getDrawable(R.mipmap.lg);
        if(getDeviceName().contains("Nexus 6P")){
            icon = getResources().getDrawable(R.mipmap.huawei);
        }
        else if(getDeviceName().contains("Motorola")){
            icon = getResources().getDrawable(R.mipmap.moto2);
        }
        else if(getDeviceName().contains("LG")){
            icon = getResources().getDrawable(R.mipmap.lg);
        }
        else{
            icon = getResources().getDrawable(R.mipmap.ic_launcher);
        }
        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(getDeviceName()).withEmail("v" + versionName).withIcon(icon)
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
                        //About button
                        if (position == 3) {
                            startSettingsActivity();
                            Log.d("drawer", "Started settings activity");
                        } else if (position == 4) {
                            startAboutActivity();
                            Log.d("drawer", "Started about activity");
                        }
                        return false;
                    }
                })
                .build();
        result.setSelection(item1);

    }

    //starts about activity
    public void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    //starts settings activity
    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    //whats new dialog
    private void showWhatsNewDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog

        builder.setView(view).setTitle("Whats New")//sets title
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    //Grab device make and model for drawer
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    //Capitalizes names for devices. Used by getDeviceName()
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    public void boolPrefEditor(String key, boolean value){
        SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key,value);
    }
    public boolean rootInit(){
        try{
            Process p = Runtime.getRuntime().exec("su");
            return true;
        }catch (IOException e){
            return false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);

        //Connection Test button
        Button conn = (Button) findViewById(R.id.pingTestButton);
        //Ping text
        TextView connectionStatusText = (TextView) findViewById(R.id.pingStatus);

        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isEasterEgg",false)){
            conn.setVisibility(View.GONE);
            connectionStatusText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isEasterEgg",false)){
            conn.setVisibility(View.VISIBLE);
            connectionStatusText.setVisibility(View.VISIBLE);
        }

        //LinkSpeed Button
        Button btn3 = (Button) findViewById(R.id.linkSpeedButton);
        final TextView linkText = (TextView)findViewById(R.id.linkSpeed);
        //LinkSpeed button and text
        if(!sharedPref.getBoolean("isEasterEgg",false)){
            btn3.setVisibility(View.GONE);
            linkText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isEasterEgg",false)){
            btn3.setVisibility(View.VISIBLE);
            linkText.setVisibility(View.VISIBLE);
        }
        TextView statusText = (TextView)findViewById(R.id.statusText);
        Switch toggle = (Switch) findViewById(R.id.enableSwitch);

        if(sharedPref.getInt("isActive",1) == 1){
            statusText.setText("is Enabled");
            statusText.setTextColor(getResources().getColor(R.color.status_activated));
            toggle.setChecked(true);

        }
        else if(sharedPref.getInt("isActive",1) == 0){
            if(rootInit() == false){
                toggle.setClickable(false);
                statusText.setText("couldn't get root");
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
            }
            else{
                statusText.setText("is Disabled");
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                toggle.setChecked(false);
            }

        }
    }
}
