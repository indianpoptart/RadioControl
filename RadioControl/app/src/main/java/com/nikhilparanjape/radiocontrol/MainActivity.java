package com.nikhilparanjape.radiocontrol;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
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


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize the process
        Process p;
        //Root commands for airplane mode
        String[] airplaneCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable"};
        //Root commands if there is an active bluetooth connection
        String[] bluetoothCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable","service call bluetooth_manager 6"};
        //Root commands if there is a wifi connection. NOT USED
        String[] wifiCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        Context context = this;
        //Initialize network settings
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Check if the device is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //booleans for radios

        //Check for bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check for airplane mode
        boolean isEnabled = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        String test = Boolean.toString(isEnabled); // A test string, not needed

        if (isConnected && isEnabled == false) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            //Check for wifi
            if(isWiFi) {
                //If the bluetooth connection is on
                if(bluetoothAdapter.isEnabled()) {
                    try {
                        p = Runtime.getRuntime().exec("su");
                        DataOutputStream os = new DataOutputStream(p.getOutputStream());
                        for (String tmpCmd : bluetoothCmd) {
                            os.writeBytes(tmpCmd + "\n");
                        }
                        os.writeBytes("exit\n");
                        os.flush();
                    } catch (IOException e) {

                    }
                    Log.d("Wifi", "wifi is on" + test);
                }
                //If bluetooth is off, run the standard root request
                else if(bluetoothAdapter == null){
                    try {
                        p = Runtime.getRuntime().exec("su"); //Request SU
                        DataOutputStream os = new DataOutputStream(p.getOutputStream()); //Used for terminal
                        for (String tmpCmd : airplaneCmd) {
                            os.writeBytes(tmpCmd + "\n"); //Sends commands to the terminal
                        }
                        os.writeBytes("exit\n"); //Quits the terminal session
                        os.flush(); //Ends datastream
                    } catch (IOException e) {
                        Log.d("Root", "There was an error with root");
                    }
                    Log.d("Wifi", "wifi is on" + test);
                }
            }
        //Check if airplane mode is on, and wifi is off
        //This means user is in a no WiFi and no cell range
        } else if(isConnected == false && isEnabled == true){
            Log.d("Radio","I didn't have to do anything, yay");
        }

        else {
            Log.d("Else","something is different");
        }

        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(getDeviceName()).withEmail("v1.0.1-ALPHA")
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
