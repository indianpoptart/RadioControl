package com.nikhilparanjape.radiocontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Nikhil Paranjape on 12/16/2015.
 */

public class SettingsActivity extends PreferenceActivity {
    private static final String PRIVATE_PREF = "prefs";
    public static ArrayList<String> ssidList = new ArrayList<String>();


    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        Utilities util = new Utilities();
        Context c = getApplicationContext();
        getPreferenceScreen().findPreference("ssid-curr").setEnabled(false);
        getPreferenceScreen().findPreference("clear-ssid").setEnabled(false);

        final MultiSelectListPreference listPreference = (MultiSelectListPreference) findPreference("ssid-curr");
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object o) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                Set<String> selections = preferences.getStringSet("ssid-curr", null);
                String[] selected= selections.toArray(new String[] {});
                for (int j = 0; j < selected.length ; j++){
                    System.out.println("\ntest" + j +" : " + selected[j]);
                    Log.d("RadioControl", "\ntest" + j +" : " + selected[j]);
                }
                Log.d("RadioControl", "Object: " + o);
                PreferenceManager
                        .getDefaultSharedPreferences(SettingsActivity.this)
                        .edit()
                        .putStringSet("ssid",selections)
                        .commit();

                return true;
            }
        });
        if(util.isConnectedWifi(c)){
            //getPreferenceScreen().findPreference("ssid-curr").setEnabled(true);
            //Listen for changes, I'm not sure if this is how it's meant to work, but it does :/

            //setListPreferenceData(listPreference);
        }
        else{
            getPreferenceScreen().findPreference("ssid-curr").setEnabled(false);
        }

        Preference clearPref = findPreference("clear-ssid");
        clearPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                ssidClearButton();
                return false;
            }
        });
        final CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("enableLogs");
        checkboxPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Request storage permissions if on MM or greater
                if (Build.VERSION.SDK_INT >= 23) {
                    String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

                    int permsRequestCode = 200;

                    requestPermissions(perms, permsRequestCode);
                }
                return true;
            }
        });


    }

    protected void setListPreferenceData(MultiSelectListPreference lp) {
        //Run this if everything is connected

        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        Set<String> valuesSet = new HashSet<>();
        Set<String> arrayString = pref.getStringSet("ssid", valuesSet);
        Set<String> ssidCurrSet = pref.getStringSet("ssid-curr", valuesSet);

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
                if (!ssidCurrSet.contains(ssid) && !arrayString.contains(ssid)) {
                    ssidList.add(ssid);
                    Collections.addAll(valuesSet, ssid);
                    // pair the value in text field with the key
                }
            }
        }

        CharSequence[] cs = ssidList.toArray(new CharSequence[ssidList.size()]);
        //CharSequence[] ce = arrayString.toArray(new CharSequence[valuesSet.size()]);
        Log.d("RadioControl", "CS: " + cs);
        //lp.setValues((Set<String>) ssidList);
        lp.setEntries(cs);
        lp.setEntryValues(cs);

    }
    //Method for the ssid list clear button
    public void ssidClearButton(){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("ssid");
        Toast.makeText(SettingsActivity.this,
                "Disabled SSID list cleared", Toast.LENGTH_LONG).show();
        editor.apply();
    }


}