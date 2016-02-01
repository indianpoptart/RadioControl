package com.nikhilparanjape.radiocontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.provider.Settings.Global.AIRPLANE_MODE_ON;


public class SettingsActivity extends PreferenceActivity {
    //private static final String PRIVATE_PREF = "radiocontrol-prefs";
    Drawable icon;
    private static final String PRIVATE_PREF = "prefs";
    public static ArrayList<String> ssidList = new ArrayList<String>();
    String versionName = BuildConfig.VERSION_NAME;


    protected void onCreate(Context context, final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        //wifiSaver();
        //drawerCreate();

        //connectivity manager for wifi check
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Check active networks
        NetworkInfo activeNetwork = conMan.getActiveNetworkInfo();
        //Check if the network is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        //Check for airplane mode
        boolean isEnabled = Settings.Global.getInt(context.getContentResolver(), AIRPLANE_MODE_ON, 0) == 1;

        final MultiSelectListPreference listPreference = (MultiSelectListPreference) findPreference("ssid");
        // THIS IS REQUIRED IF YOU DON'T HAVE 'entries' and 'entryValues' in your XML
        setListPreferenceData(context,listPreference);


        Preference clearPref = (Preference) findPreference("clear-ssid");
        clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ssidClearButton();
                return false;
            }
        });

    }
    public static class SettingsFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);

        }
    }

    protected void setListPreferenceData(Context context, MultiSelectListPreference lp) {

        //connectivity manager for wifi check
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Check active networks
        NetworkInfo activeNetwork = conMan.getActiveNetworkInfo();
        //Check if the network is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        //Check for airplane mode
        boolean isEnabled = Settings.Global.getInt(context.getContentResolver(), AIRPLANE_MODE_ON, 0) == 1;

        if(isConnected && !isEnabled){
            //Run this if everything is connected
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            if(isWiFi){
                WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

                SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                String arrayString = pref.getString("disabled_networks", "1");
                Set<String> valuesSet = new HashSet<>();

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                if(list.isEmpty())
                {
                    Log.e("Connection Setup","Empty list returned");
                }
                String ssid = null;
                for( WifiConfiguration i : list ) {
                    if(i.SSID != null) {
                        ssid = i.SSID;
                        ssid = ssid.substring(1, ssid.length()-1);
                        Log.e("RadioControl",ssid+" network listed");
                        //Check if the list contains the entered SSID
                        if (!arrayString.contains(ssid)) {
                            ssidList.add(ssid);
                            Collections.addAll(valuesSet, ssid);
                            // pair the value in text field with the key
                        }

                    }
                }

                CharSequence[] cs = ssidList.toArray(new CharSequence[ssidList.size()]);
                lp.setValues(valuesSet);
                lp.setEntries(cs);
                lp.setEntryValues(cs);
            }
            else{
                Toast.makeText(SettingsActivity.this,
                        "Enable WiFi first", Toast.LENGTH_LONG).show();
            }
        }


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
    //Show WiFi List
    public void wifiPicker(){
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_ssidchooser, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog

        builder.setView(view).setTitle("SSID Chooser")//sets title
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    //Method for the ssid list clear button
    public void ssidClearButton(){
        SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("disabled_networks");
        Toast.makeText(SettingsActivity.this,
                "Disabled SSID list cleared", Toast.LENGTH_LONG).show();
        editor.apply();
    }

    public void wifiSaver() {
        SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String arrayString = pref.getString("saved_networks", "1");

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);


        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        if(list.isEmpty())
        {
            Log.e("Connection Setup","Empty list returned");
        }
        String ssid = null;
        for( WifiConfiguration i : list ) {
            if(i.SSID != null) {
                ssid = i.SSID;
                ssid = ssid.substring(1, ssid.length()-1);
                Log.e("RadioControl",ssid+" network listed");
                //Check if the list contains the entered SSID
                if (!arrayString.contains(ssid)) {
                    ssidList.add(ssid);
                    // pair the value in text field with the key
                    editor.putString("saved_networks", ssidList.toString());
                }

            }
            editor.commit();
        }

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback

    }


}
