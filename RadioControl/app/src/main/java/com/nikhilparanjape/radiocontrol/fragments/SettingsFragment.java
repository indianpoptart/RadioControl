package com.nikhilparanjape.radiocontrol.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.appyvet.rangebar.RangeBar;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.ScheduledAirplaneService;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Nikhil on 4/5/2016.
 */
public class SettingsFragment extends PreferenceFragment implements TimePickerDialog.OnTimeSetListener{

    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        final Context c = getActivity();

        if(Utilities.isConnectedWifi(c)){
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
                editor.apply();
                return true;
            }
        });

        final CheckBoxPreference serviceCheckbox = (CheckBoxPreference) getPreferenceManager().findPreference("isAirplaneService");
        serviceCheckbox.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("interval_prefs", "120");
                editor.apply();

                String intervalTimeString = preferences.getString("interval_prefs","10");
                int intervalTime = Integer.parseInt(intervalTimeString);
                boolean airplaneService = preferences.getBoolean("isAirplaneService", false);

                if(intervalTime != 0 && airplaneService){
                    Intent i= new Intent(c, ScheduledAirplaneService.class);
                    c.startService(i);
                    Log.d("RadioControl", "Service launched");
                }

                return true;
            }
        });

        if(!getPreferenceScreen().findPreference("isAirplaneService").isEnabled()){
            getPreferenceScreen().findPreference("interval_prefs").setEnabled(false);
        }
        else{
            getPreferenceScreen().findPreference("interval_prefs").setEnabled(true);
        }

        //Initialize time picker
        Calendar now = Calendar.getInstance();
        final TimePickerDialog tpd = TimePickerDialog.newInstance(
                SettingsFragment.this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        );
        tpd.setThemeDark(true);
        tpd.setAccentColor(R.color.mdtp_accent_color);

        Preference night_mode = findPreference("night-mode-service");
        night_mode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                tpd.show(getFragmentManager(), "Timepickerdialog");
                return false;
            }
        });



    }
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String hourStringEnd = hourOfDayEnd < 10 ? "0"+hourOfDayEnd : ""+hourOfDayEnd;
        String minuteStringEnd = minuteEnd < 10 ? "0"+minuteEnd : ""+minuteEnd;
        String time = "You picked the following time: From - "+hourString+"h"+minuteString+" To - "+hourStringEnd+"h"+minuteStringEnd;
        Log.d("RadioControl", "Night Mode: " + time);
    }

    //Method for the ssid list clear button
    public void ssidClearButton(){
        Context c = getActivity();
        final SharedPreferences pref = c.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(getActivity(),
                R.string.reset_ssid, Toast.LENGTH_LONG).show();
    }


}