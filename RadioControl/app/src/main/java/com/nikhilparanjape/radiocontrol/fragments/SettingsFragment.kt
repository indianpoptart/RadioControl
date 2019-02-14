package com.nikhilparanjape.radiocontrol.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.preference.CheckBoxPreference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.borax12.materialdaterangepicker.time.RadialPickerLayout
import com.borax12.materialdaterangepicker.time.TimePickerDialog
import com.google.android.material.snackbar.Snackbar
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver
import com.nikhilparanjape.radiocontrol.services.PersistenceService
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import com.nikhilparanjape.radiocontrol.utilities.RootAccess
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import java.io.File
import java.util.*


/**
 * Created by Nikhil on 4/5/2016.
 */
class SettingsFragment : PreferenceFragment(), TimePickerDialog.OnTimeSetListener {

    private lateinit var c: Context
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        val sp = preferenceScreen.sharedPreferences
        val editor = sp.edit()
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            preferenceScreen.findPreference("workMode").isEnabled = true
            editor.putBoolean("workMode", true)
            editor.apply()
        }

        c = activity
        val alarmUtil = AlarmSchedulers()

        preferenceScreen.findPreference("ssid").isEnabled = Utilities.isWifiOn(c)


        val ssidListPref = findPreference("ssid")
        ssidListPref.setOnPreferenceClickListener { false }


        val pingIpPref = findPreference("prefPingIp")
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val ip = preferences.getString("prefPingIp",null)
        pingIpPref.summary = ip
        pingIpPref.setOnPreferenceChangeListener { _, _ ->
            pingIpPref.summary = ip
            true
        }

        val clearPref = findPreference("clear-ssid")
        clearPref.setOnPreferenceClickListener {
            ssidClearButton()
            false
        }
        val notificationPref = findPreference("button_network_key")
        notificationPref.setOnPreferenceClickListener {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, c.packageName)
                startActivity(intent)
            }
            false
        }
        val airplaneResetPref = findPreference("reset-airplane")
        airplaneResetPref.setOnPreferenceClickListener {
            MaterialDialog(activity)
                    .icon(R.mipmap.wifi_off)
                    .message(R.string.title_airplane_reset)
                    .positiveButton(R.string.text_ok) {
                        val airOffCmd2 = arrayOf("su", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"")
                        RootAccess.runCommands(airOffCmd2)
                        Toast.makeText(activity,
                                "Airplane mode reset", Toast.LENGTH_LONG).show()
                    }
                    .negativeButton(R.string.text_cancel)
                    .show()
            false
        }

        val logDirPref = findPreference("logDir")
        logDirPref.setOnPreferenceClickListener {
            logDirectoryButton()
            false
        }
        val logDelPref = findPreference("logDel")
        logDelPref.setOnPreferenceClickListener {
            logDeleteButton()
            false
        }

        val batteryOptimizePref = preferenceManager.findPreference("isBatteryOn") as CheckBoxPreference
        val workModePref = preferenceManager.findPreference("workMode") as CheckBoxPreference
        workModePref.setOnPreferenceChangeListener { _, newValue ->
            val pref = PreferenceManager.getDefaultSharedPreferences(c)
            if (newValue.toString() == "true") {
                if (batteryOptimizePref.isChecked) {
                    Log.i("RadioControl", "true-ischecked")
                    if (pref.getBoolean("workMode", true)) {
                        if (Build.VERSION.SDK_INT >= 26) {
                            activity.startForegroundService(Intent(activity, PersistenceService::class.java))
                        } else {
                            activity.startService(Intent(activity, PersistenceService::class.java))
                        }
                    } else {
                        registerForBroadcasts(c)
                    }
                } else {
                    MaterialDialog(activity)
                            .icon(R.mipmap.ic_launcher)
                            .title(R.string.title_intelligent_mode)
                            .message(R.string.permissionIntelligent)
                            .positiveButton(R.string.button_text_allow) {
                                batteryOptimizePref.isChecked = true
                                if (pref.getBoolean("workMode", true)) {
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        activity.startForegroundService(Intent(activity, PersistenceService::class.java))
                                    } else {
                                        activity.startService(Intent(activity, PersistenceService::class.java))
                                    }
                                } else {
                                    registerForBroadcasts(c)
                                }
                            }
                            .negativeButton(R.string.button_text_deny) {
                                if (pref.getBoolean("workMode", true)) {
                                    if (Build.VERSION.SDK_INT >= 26) {
                                        activity.startForegroundService(Intent(activity, PersistenceService::class.java))
                                    } else {
                                        activity.startService(Intent(activity, PersistenceService::class.java))
                                    }
                                } else {
                                    registerForBroadcasts(c)
                                }
                            }
                            .show()
                }

            } else {
                activity.stopService(Intent(activity, PersistenceService::class.java))
            }
            Log.i("RadioControl", "workMode")

            true
        }

        val checkboxPref = preferenceManager.findPreference("enableLogs") as CheckBoxPreference
        checkboxPref.setOnPreferenceChangeListener { _, newValue ->
            val preference = PreferenceManager.getDefaultSharedPreferences(c)
            val editor1 = preference.edit()
            if (newValue.toString() == "true") {
                //Request storage permissions if on MM or greater
                if (Build.VERSION.SDK_INT >= 23) {
                    val perms = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")

                    val permsRequestCode = 200

                    requestPermissions(perms, permsRequestCode)
                    editor1.putBoolean("enableLogs", true)
                    Log.d("RadioControl", "Logging enabled")

                } else {
                    editor1.putBoolean("enableLogs", true)
                    Log.d("RadioControl", "Logging enabled")
                }
            } else {
                checkboxPref.isChecked = false
                editor1.putBoolean("enableLogs", false)
                Log.d("RadioControl", "Logging disabled")
                val log = File("radiocontrol.log")
                if (log.exists()) {
                    log.delete()
                }
            }
            editor1.apply()
            true
        }
        val altRootCommand = preferenceManager.findPreference("altRootCommand") as CheckBoxPreference
        altRootCommand.setOnPreferenceChangeListener { _, newValue ->
            if (newValue.toString() == "true") {
                val permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION)

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 200)
                } else {
                    //altRootCommand.setChecked(false);
                }
            } else {

            }

            true
        }
        altRootCommand.setOnPreferenceClickListener {
            if (!altRootCommand.isEnabled){
                Snackbar.make(view, "Disable Intelligent mode first", Snackbar.LENGTH_LONG)
                        .show()
            }
            true
        }

        //Setting for enabling/disabling doze
        val dozeSetting = preferenceManager.findPreference("isDozeOff") as CheckBoxPreference
        dozeSetting.setOnPreferenceChangeListener { _, newValue ->
            if (newValue.toString() == "true") {
                Log.i("RadioControl", "doze-bypassed")
                if (Build.VERSION.SDK_INT >= 23) {
                    val intent = Intent()
                    val packageName = c.packageName
                    val pm = c.getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (pm.isIgnoringBatteryOptimizations(packageName)) {
                        Log.i("RadioControl", "ignoring")
                        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        intent.data = Uri.parse("package:$packageName")
                        c.startActivity(intent)
                    } else {
                        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        intent.data = Uri.parse("package:$packageName")
                        c.startActivity(intent)
                    }
                    dozeSetting.isChecked = true
                }
            } else {
                dozeSetting.isChecked = false
                Log.i("RadioControl", "false")
                if (Build.VERSION.SDK_INT >= 23) {
                    startActivityForResult(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0)
                    /*Intent intent = new Intent();
                    String packageName = c.getPackageName();
                    PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    c.startActivity(intent);*/
                }
            }
            true
        }

        /*if (altRootCommand.isChecked || batteryOptimizePref.isChecked) {
            val pref = c.getSharedPreferences("batteryOptimizePref", Context.MODE_PRIVATE)
            val editor2 = pref.edit()
            editor2.clear()
            editor2.apply()
        } else */
        if (!batteryOptimizePref.isChecked) {
            activity.stopService(Intent(activity, PersistenceService::class.java))
        }

        val fabricCrashlyticsPref = preferenceManager.findPreference("fabricCrashlytics") as CheckBoxPreference
        fabricCrashlyticsPref.setOnPreferenceChangeListener { _, newValue ->
            val preference = PreferenceManager.getDefaultSharedPreferences(c)
            val editor12 = preference.edit()

            if (newValue.toString() == "true") {
                MaterialDialog(activity)
                        .icon(R.mipmap.ic_launcher)
                        .title(R.string.title_allow_fabric)
                        .message(R.string.permissionSampleFabric)
                        .positiveButton(R.string.button_text_allow) {
                            editor12.putBoolean("allowFabric", true)
                            editor12.apply()
                        }
                        .negativeButton(R.string.button_text_deny) {
                            fabricCrashlyticsPref.isChecked = false
                            editor12.putBoolean("allowFabric", false)
                            editor12.apply()
                        }
                        .show()
            } else {
                editor12.putBoolean("allowFabric", false)
                editor12.apply()
            }
            editor12.apply()
            true
        }

        val callingCheck = preferenceManager.findPreference("isPhoneStateCheck") as CheckBoxPreference
        callingCheck.setOnPreferenceChangeListener { _, newValue ->
            if (newValue.toString() == "true") {
                val permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_PHONE_STATE), 200)

                } else {
                    callingCheck.isChecked = false
                }
            } else {

            }


            true
        }

        val serviceCheckbox = preferenceManager.findPreference("isAirplaneService") as CheckBoxPreference
        serviceCheckbox.setOnPreferenceChangeListener { _, newValue ->
            val preference = PreferenceManager.getDefaultSharedPreferences(c)
            val editor13 = preference.edit()

            if (newValue.toString() == "true") {
                editor13.putBoolean("isAirplaneService", true)
                editor13.apply()

                val intervalTimeString = preferences.getString("interval_prefs", "60")
                val intervalTime = Integer.parseInt(intervalTimeString)
                val airplaneService = preferences.getBoolean("isAirplaneService", false)

                if (intervalTime != 0 && airplaneService) {
                    Log.d("RadioControl", "Alarm Scheduled")
                    alarmUtil.scheduleAlarm(c)
                }
            } else {
                Log.d("RadioControl", "Alarm Cancelled")
                alarmUtil.cancelAlarm(c)
            }
            true
        }


        preferenceScreen.findPreference("interval_prefs").isEnabled = preferenceScreen.findPreference("isAirplaneService").isEnabled

        //Initialize time picker
        val now = Calendar.getInstance()
        val tpd = TimePickerDialog.newInstance(
                this,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                false
        )
        tpd.isThemeDark = true
        tpd.setAccentColor(R.color.mdtp_accent_color)

        val nightMode = findPreference("night-mode-service")
        nightMode.setOnPreferenceClickListener {
            tpd.show(fragmentManager, "Timepickerdialog")
            false
        }
    }

    private fun registerForBroadcasts(context: Context) {
        val component = ComponentName(context, ConnectivityReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    override fun onTimeSet(view: RadialPickerLayout, hourOfDay: Int, minute: Int, hourOfDayEnd: Int, minuteEnd: Int) {
        val alarmUtil = AlarmSchedulers()
        val c = activity
        val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
        val minuteString = if (minute < 10) "0$minute" else "" + minute
        val hourStringEnd = if (hourOfDayEnd < 10) "0$hourOfDayEnd" else "" + hourOfDayEnd
        val minuteStringEnd = if (minuteEnd < 10) "0$minuteEnd" else "" + minuteEnd
        val time = "You picked the following time: From - " + hourString + "h" + minuteString + " To - " + hourStringEnd + "h" + minuteStringEnd

        alarmUtil.cancelNightAlarm(c, hourOfDay, minute)

        alarmUtil.scheduleNightWakeupAlarm(c, hourOfDayEnd, minuteEnd)
        Log.d("RadioControl", "Night Mode: $time")
        Toast.makeText(activity, "Night mode set from $hourOfDay:$minuteString to $hourOfDayEnd:$minuteStringEnd", Toast.LENGTH_LONG).show()
    }

    //Method for the ssid list clear button
    private fun ssidClearButton() {
        val c = activity
        val pref = c.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.clear()
        editor.apply()
        Toast.makeText(activity,
                R.string.reset_ssid, Toast.LENGTH_LONG).show()
    }

    private fun logDirectoryButton() {
        Toast.makeText(activity,
                "Coming Soon!", Toast.LENGTH_LONG).show()
    }

    private fun logDeleteButton() {
        val log = File(c.filesDir, "radiocontrol.log")
        log.delete()
        Toast.makeText(activity,
                "Log Deleted", Toast.LENGTH_LONG).show()
    }
}