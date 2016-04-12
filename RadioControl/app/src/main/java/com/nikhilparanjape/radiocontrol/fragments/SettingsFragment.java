package com.nikhilparanjape.radiocontrol.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;

import java.io.File;

/**
 * Created by Nikhil on 4/5/2016.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        Utilities util = new Utilities();
        final Context c = getActivity();

        if(util.isConnectedWifi(c)){
            getPreferenceScreen().findPreference("ssid").setEnabled(true);
            //Listen for changes, I'm not sure if this is how it's meant to work, but it does :/


        }
        else{
            getPreferenceScreen().findPreference("ssid").setEnabled(false);
        }
        Preference ssidListPref = findPreference("ssid");
        ssidListPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return false;
            }
        });

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
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor editor = preferences.edit();
                if(newValue.toString().equals("true")){
                    //Request storage permissions if on MM or greater
                    if (Build.VERSION.SDK_INT >= 23) {
                        String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

                        int permsRequestCode = 200;

                        requestPermissions(perms, permsRequestCode);
                        editor.putBoolean("enableLogs", true);
                        Log.d("RadioControl", "Logging enabled");

                    }
                    else{
                        editor.putBoolean("enableLogs", true);
                        Log.d("RadioControl", "Logging enabled");
                    }
                }
                else{
                    editor.putBoolean("enableLogs", false);
                    Log.d("RadioControl", "Logging disabled");
                    File log = new File("radiocontrol.log");
                    if (log.exists()) {
                        log.delete();
                    }
                }

                return true;
            }
        });
    }
    //Method for the ssid list clear button
    public void ssidClearButton(){
        Context c = getActivity();
        final SharedPreferences pref = c.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        Toast.makeText(getActivity(),
                R.string.reset_ssid, Toast.LENGTH_LONG).show();
    }
}