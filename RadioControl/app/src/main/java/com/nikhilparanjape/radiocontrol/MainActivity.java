package com.nikhilparanjape.radiocontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.mikepenz.materialdrawer.util.IdDistributor;

import java.io.DataOutputStream;
import java.io.IOException;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Integer[] seconds_array = new Integer[]{0,1,2,3,4,5,6,7,8,9,10,
                11,12,13,14,15,16,17,18,19,20,
                21,22,23,24,25,26,27,28,29,30,
                31,32,33,34,35,36,37,38,39,40,
                41,42,43,44,45,46,47,48,49,50,
                51,52,53,54,55,56,57,58,59,60};
        //Initialize Spinner
        Spinner spinner = (Spinner) findViewById(R.id.secSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter <Integer> adapter = new ArrayAdapter<Integer>( this,android.R.layout.simple_spinner_item, seconds_array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        SharedPreferences sp = getSharedPreferences("spinnerPref", Context.MODE_PRIVATE);
        long secondsValue = sp.getLong("seconds_spinner", 10);
        spinner.setSelection((int) secondsValue);
        spinner.setOnItemSelectedListener(this);

        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(getDeviceName()).withEmail("v1.2 - Beta")
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
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_help);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName("About").withIcon(GoogleMaterial.Icon.gmd_info);

        //Create navigation drawer
        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item3
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        return false;
                    }
                })
                .build();

    }
    //Grab device make and model for drawer
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }
    //Capitalizes names for devices. Used by getDeviceName()
    private String capitalize(String s) {
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        Log.d("Spinner","Value set to: " + id);
        SharedPreferences sp = getSharedPreferences("spinnerPref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("seconds_spinner", id);
        editor.commit();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
