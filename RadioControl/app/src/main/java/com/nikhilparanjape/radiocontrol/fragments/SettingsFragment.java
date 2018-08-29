package com.nikhilparanjape.radiocontrol.fragments;

import android.Manifest;
import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.folderselector.FolderChooserDialog;
import com.borax12.materialdaterangepicker.time.RadialPickerLayout;
import com.borax12.materialdaterangepicker.time.TimePickerDialog;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.activities.MainActivity;
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver;
import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.PersistenceService;

import java.io.File;
import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Nikhil on 4/5/2016.
 */
public class SettingsFragment extends PreferenceFragment implements TimePickerDialog.OnTimeSetListener, FolderChooserDialog.FolderCallback {

    Context c;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        if (android.os.Build.VERSION.SDK_INT >= 24){
            getPreferenceScreen().findPreference("workMode").setEnabled(true);
            editor.putBoolean("workMode",true);
            editor.apply();
        }


        c = getActivity();
        Utilities util = new Utilities();

        if(Utilities.isWifiOn(c)){
            getPreferenceScreen().findPreference("ssid").setEnabled(true);
        }
        else{
            getPreferenceScreen().findPreference("ssid").setEnabled(false);
        }


        Preference ssidListPref = findPreference("ssid");
        ssidListPref.setOnPreferenceClickListener(preference -> false);


        Preference clearPref = findPreference("clear-ssid");
        clearPref.setOnPreferenceClickListener(preference -> {
            ssidClearButton();
            return false;
        });
        Preference notificationPref = findPreference("button_network_key");
        notificationPref.setOnPreferenceClickListener(preference -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, c.getPackageName());
                startActivity(intent);
            }
            return false;
        });
        Preference airplaneResetPref = findPreference("reset-airplane");
        airplaneResetPref.setOnPreferenceClickListener(preference -> {
            new MaterialDialog.Builder(getActivity())
                    .iconRes(R.mipmap.wifi_off)
                    .limitIconToDefaultSize()
                    .title("Please disable WiFi and Airplane mode and make sure the cell radio is on before pressing OK")
                    .positiveText("OK")
                    .negativeText("Cancel")
                    .backgroundColorRes(R.color.material_drawer_dark_background)
                    .onPositive((dialog, which) -> {
                        String[] airOffCmd2 = {"su", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};
                        RootAccess.runCommands(airOffCmd2);
                        Toast.makeText(getActivity(),
                                "Airplane mode reset", Toast.LENGTH_LONG).show();
                    })
                    .onNegative((dialog, which) -> {

                    })
                    .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                    .show();


            return false;
        });

        Preference logDirPref = findPreference("logDir");
        logDirPref.setOnPreferenceClickListener(preference -> {
            logDirectoryButton();
            return false;
        });
        Preference logDelPref = findPreference("logDel");
        logDelPref.setOnPreferenceClickListener(preference -> {
            logDeleteButton();
            return false;
        });

        CheckBoxPreference batteryOptimizePref = (CheckBoxPreference) getPreferenceManager().findPreference("isBatteryOn");
        CheckBoxPreference workModePref = (CheckBoxPreference) getPreferenceManager().findPreference("workMode");
        workModePref.setOnPreferenceChangeListener((preference, newValue) -> {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(c);
            if(newValue.toString().equals("true")) {
                if(batteryOptimizePref.isChecked()){
                    Log.i("RadioControl","true-ischecked");
                    if (pref.getBoolean("workMode", true)) {
                        if(Build.VERSION.SDK_INT>=26) {
                            getActivity().startForegroundService(new Intent(getActivity(), PersistenceService.class));
                        }else{
                            getActivity().startService(new Intent(getActivity(), PersistenceService.class));
                        }
                    } else {
                        registerForBroadcasts(c);
                    }
                } else{
                    new MaterialDialog.Builder(getActivity())
                            .iconRes(R.mipmap.ic_launcher)
                            .limitIconToDefaultSize()
                            .title(getString(R.string.permissionIntelligent))
                            .positiveText("Allow")
                            .negativeText("Deny")
                            .backgroundColorRes(R.color.material_drawer_dark_background)
                            .onPositive((dialog, which) -> {
                                batteryOptimizePref.setChecked(true);
                                if (pref.getBoolean("workMode", true)) {
                                    if(Build.VERSION.SDK_INT>=26) {
                                        getActivity().startForegroundService(new Intent(getActivity(), PersistenceService.class));
                                    }else{
                                        getActivity().startService(new Intent(getActivity(), PersistenceService.class));
                                    }
                                } else {
                                    registerForBroadcasts(c);
                                }
                            })
                            .onNegative((dialog, which) -> {
                                if (pref.getBoolean("workMode", true)) {
                                    if(Build.VERSION.SDK_INT>=26) {
                                        getActivity().startForegroundService(new Intent(getActivity(), PersistenceService.class));
                                    }else{
                                        getActivity().startService(new Intent(getActivity(), PersistenceService.class));
                                    }
                                } else {
                                    registerForBroadcasts(c);
                                }
                            })
                            .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                            .show();
                }

            }else{
                getPreferenceScreen().findPreference("altRootCommand").setEnabled(true);
                getActivity().stopService(new Intent(getActivity(), PersistenceService.class));
            }
            Log.i("RadioControl","workMode");

            return true;
        });

        CheckBoxPreference checkboxPref = (CheckBoxPreference) getPreferenceManager().findPreference("enableLogs");
        checkboxPref.setOnPreferenceChangeListener((preference, newValue) -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor1 = preferences.edit();
            if(newValue.toString().equals("true")){
                //Request storage permissions if on MM or greater
                if (Build.VERSION.SDK_INT >= 23) {
                    String[] perms = {"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};

                    int permsRequestCode = 200;

                    requestPermissions(perms, permsRequestCode);
                    editor1.putBoolean("enableLogs", true);
                    Log.d("RadioControl", "Logging enabled");

                }
                else{
                    editor1.putBoolean("enableLogs", true);
                    Log.d("RadioControl", "Logging enabled");
                }
            }
            else{
                checkboxPref.setChecked(false);
                editor1.putBoolean("enableLogs", false);
                Log.d("RadioControl", "Logging disabled");
                File log = new File("radiocontrol.log");
                if (log.exists()) {
                    log.delete();
                }
            }
            editor1.apply();
            return true;
        });
        CheckBoxPreference altRootCommand = (CheckBoxPreference) getPreferenceManager().findPreference("altRootCommand");
        altRootCommand.setOnPreferenceChangeListener((preference, newValue) -> {
            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 200);
            } else {
                //altRootCommand.setChecked(false);
            }

            return true;
        });



        CheckBoxPreference dozeSetting = (CheckBoxPreference) getPreferenceManager().findPreference("isDozeOff");
        dozeSetting.setOnPreferenceChangeListener((preference, newValue) -> {
            if(newValue.toString().equals("true")){
                Log.i("RadioControl","true-new");
                if (Build.VERSION.SDK_INT >= 23) {
                    Intent intent = new Intent();
                    String packageName = c.getPackageName();
                    PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                    if (pm.isIgnoringBatteryOptimizations(packageName))
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    else {
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        c.startActivity(intent);
                        return false;
                    }
                    dozeSetting.setChecked(true);
                    c.startActivity(intent);
                }
            }
            else{
                dozeSetting.setChecked(false);
                Log.i("RadioControl","false");
                if (Build.VERSION.SDK_INT >= 23) {
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0);
                    /*Intent intent = new Intent();
                    String packageName = c.getPackageName();
                    PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    c.startActivity(intent);*/
                    return false;
                }
            }

            return true;
        });

        if(altRootCommand.isChecked() || batteryOptimizePref.isChecked()){
            final SharedPreferences pref = c.getSharedPreferences("batteryOptimizePref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor2 = pref.edit();
            editor2.clear();
            editor2.apply();
            getPreferenceScreen().findPreference("altRootCommand").setEnabled(false);
        }
        else if(!batteryOptimizePref.isChecked()){
            getPreferenceScreen().findPreference("altRootCommand").setEnabled(true);
            getActivity().stopService(new Intent(getActivity(), PersistenceService.class));
        }

        CheckBoxPreference eulaShow = (CheckBoxPreference) getPreferenceManager().findPreference("eulaShow");
        eulaShow.setOnPreferenceChangeListener((preference, newValue) -> {
            if(newValue.toString().equals("true")){
                new MaterialDialog.Builder(getActivity())
                        .iconRes(R.mipmap.ic_launcher)
                        .limitIconToDefaultSize()
                        .title(Html.fromHtml(getString(R.string.permissionSampleFirebase, getString(R.string.app_name))))
                        .positiveText("Allow")
                        .negativeText("Deny")
                        .backgroundColorRes(R.color.material_drawer_dark_background)
                        .onPositive((dialog, which) -> FirebaseAnalytics.getInstance(c).setAnalyticsCollectionEnabled(true))
                        .onNegative((dialog, which) -> {
                            eulaShow.setChecked(false);
                            FirebaseAnalytics.getInstance(c).setAnalyticsCollectionEnabled(true);
                        })
                        .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                        .show();
            }
            else{
                FirebaseAnalytics.getInstance(c).setAnalyticsCollectionEnabled(false);
                eulaShow.setChecked(false);
            }

            return true;
        });

        CheckBoxPreference fabricCrashlyticsPref = (CheckBoxPreference) getPreferenceManager().findPreference("fabricCrashlytics");
        fabricCrashlyticsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor12 = preferences.edit();

            if(newValue.toString().equals("true")){
                new MaterialDialog.Builder(getActivity())
                        .iconRes(R.mipmap.ic_launcher)
                        .limitIconToDefaultSize()
                        .title(Html.fromHtml(getString(R.string.permissionSampleFabric, getString(R.string.app_name))))
                        .positiveText("Allow")
                        .negativeText("Deny")
                        .backgroundColorRes(R.color.material_drawer_dark_background)
                        .onPositive((dialog, which) -> {
                            editor12.putBoolean("allowFabric", true);
                            editor12.apply();
                        })
                        .onNegative((dialog, which) -> {
                            fabricCrashlyticsPref.setChecked(false);
                            editor12.putBoolean("allowFabric", false);
                            editor12.apply();

                        })
                        .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                        .show();

            }
            else{
                editor12.putBoolean("allowFabric", false);
                editor12.apply();
            }


            return true;
        });

        CheckBoxPreference callingCheck = (CheckBoxPreference) getPreferenceManager().findPreference("isPhoneStateCheck");
        callingCheck.setOnPreferenceChangeListener((preference, newValue) -> {
            int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, 200);

            } else {
                callingCheck.setChecked(false);
            }

            return true;
        });

        CheckBoxPreference serviceCheckbox = (CheckBoxPreference) getPreferenceManager().findPreference("isAirplaneService");
        serviceCheckbox.setOnPreferenceChangeListener((preference, newValue) -> {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor13 = preferences.edit();

            if(newValue.toString().equals("true")){
                editor13.putBoolean("isAirplaneService", true);
                editor13.apply();

                String intervalTimeString = preferences.getString("interval_prefs","60");
                int intervalTime = Integer.parseInt(intervalTimeString);
                boolean airplaneService = preferences.getBoolean("isAirplaneService", false);

                if(intervalTime != 0 && airplaneService){
                    Log.d("RadioControl", "Alarm Scheduled");
                    util.scheduleAlarm(c);
                }
            }
            else{
                Log.d("RadioControl", "Alarm Cancelled");
                util.cancelAlarm(c);
            }


            return true;
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
        night_mode.setOnPreferenceClickListener(preference -> {
            tpd.show(getFragmentManager(), "Timepickerdialog");
            return false;
        });

    }



    public void registerForBroadcasts(Context context) {
        ComponentName component = new ComponentName(context, WifiReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int hourOfDayEnd, int minuteEnd) {
        final Utilities util = new Utilities();
        Context c = getActivity();
        String hourString = hourOfDay < 10 ? "0"+hourOfDay : ""+hourOfDay;
        String minuteString = minute < 10 ? "0"+minute : ""+minute;
        String hourStringEnd = hourOfDayEnd < 10 ? "0"+hourOfDayEnd : ""+hourOfDayEnd;
        String minuteStringEnd = minuteEnd < 10 ? "0"+minuteEnd : ""+minuteEnd;
        String time = "You picked the following time: From - "+hourString+"h"+minuteString+" To - "+hourStringEnd+"h"+minuteStringEnd;

        util.cancelNightAlarm(c,hourOfDay, minute);

        util.scheduleNightWakeupAlarm(c, hourOfDayEnd, minuteEnd);
        Log.d("RadioControl", "Night Mode: " + time);
        Toast.makeText(getActivity(), "Night mode set from " + hourOfDay + ":" + minuteString + " to " + hourOfDayEnd + ":" + minuteStringEnd, Toast.LENGTH_LONG).show();
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
    public void logDirectoryButton(){
        Toast.makeText(getActivity(),
                "Coming Soon!", Toast.LENGTH_LONG).show();
    }
    public void logDeleteButton(){
        File log = new File(c.getFilesDir(), "radiocontrol.log");
        boolean deleted = log.delete();
        Toast.makeText(getActivity(),
                "Log Deleted", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onFolderSelection(@NonNull FolderChooserDialog dialog, @NonNull File folder) {
        // TODO
        final String tag = dialog.getTag(); // gets tag set from Builder, if you use multiple dialogs
        Toast.makeText(getActivity(),
                tag, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFolderChooserDismissed(@NonNull FolderChooserDialog dialog) {
        // TODO
        final String tag = dialog.getTag(); // gets tag set from Builder, if you use multiple dialogs
        Toast.makeText(getActivity(),
                tag, Toast.LENGTH_LONG).show();
    }
}