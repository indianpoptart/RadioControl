package com.nikhilparanjape.radiocontrol.fragments

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.borax12.materialdaterangepicker.time.RadialPickerLayout
import com.borax12.materialdaterangepicker.time.TimePickerDialog
import com.google.android.material.snackbar.Snackbar
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver
import com.nikhilparanjape.radiocontrol.services.PersistenceService
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import com.nikhilparanjape.radiocontrol.utilities.RootAccess
import java.io.File
import java.util.*


/**
 * Created by Nikhil on 4/5/2016.
 */
class SettingsFragment : PreferenceFragmentCompat(), TimePickerDialog.OnTimeSetListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        val sp = preferenceScreen.sharedPreferences
        val workModePref = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("workMode")
        if (Build.VERSION.SDK_INT >= 24) {
            preferenceScreen.findPreference<androidx.preference.CheckBoxPreference>("workMode")?.isEnabled  = true
            workModePref?.isChecked = true
        }

        val pingIpPref = findPreference<EditTextPreference>("prefPingIp")
        val ip = sp.getString("prefPingIp",null)
        pingIpPref?.summary = ip


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPreferenceTreeClick(preference: androidx.preference.Preference): Boolean {
        val sp = preferenceScreen.sharedPreferences
        //val editor = sp.edit()
        val batteryOptimizePref = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("isBatteryOn")
        val dozeSetting = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("isDozeOff")
        val workModePref = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("workMode")
        val altRootCommandPref = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("altRootCommand")
        val fabricCrashlyticsPref = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("fabricCrashlytics")
        val callingCheck = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("isPhoneStateCheck")
        val serviceCheckbox = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("isAirplaneService")
        val checkboxPref = preferenceManager.findPreference<androidx.preference.CheckBoxPreference>("enableLogs")
        val pingIpPref = findPreference<EditTextPreference>("prefPingIp")
        val ip = sp.getString("prefPingIp",null)
        val alarmUtil = AlarmSchedulers()
        //editor.apply()
        return when (preference.key) {
            getString(R.string.key_preference_settings_intelligent) -> {
                if ((preference as androidx.preference.CheckBoxPreference).isChecked) {
                    if (workModePref!!.isChecked) {
                        Log.i("RadioControl", "true-ischecked")
                        if (sp.getBoolean("workMode", true)) {
                            if (Build.VERSION.SDK_INT >= 26) {
                                activity?.startForegroundService(Intent(activity, PersistenceService::class.java))
                            } else {
                                activity?.startService(Intent(activity, PersistenceService::class.java))
                            }
                        } else {
                            registerForBroadcasts(requireContext())
                        }
                    } else {
                        MaterialDialog(requireContext())
                                .icon(R.mipmap.ic_launcher)
                                .title(R.string.title_intelligent_mode)
                                .message(R.string.permissionIntelligent)
                                .positiveButton(R.string.button_text_allow) {
                                    workModePref.isChecked = true
                                    if (sp.getBoolean("workMode", true)) {
                                        if (Build.VERSION.SDK_INT >= 26) {
                                            activity?.startForegroundService(Intent(activity, PersistenceService::class.java))
                                        } else {
                                            activity?.startService(Intent(activity, PersistenceService::class.java))
                                        }
                                    } else {
                                        registerForBroadcasts(requireContext())
                                    }
                                }
                                .negativeButton(R.string.button_text_deny) {
                                    if (sp.getBoolean("workMode", true)) {
                                        if (Build.VERSION.SDK_INT >= 26) {
                                            activity?.startForegroundService(Intent(activity, PersistenceService::class.java))
                                        } else {
                                            activity?.startService(Intent(activity, PersistenceService::class.java))
                                        }
                                    } else {
                                        registerForBroadcasts(requireContext())
                                    }
                                }
                                .show()
                    }

                } else {
                    activity?.stopService(Intent(activity, PersistenceService::class.java))
                }
                Log.i("RadioControl", "workMode")

                false
            }
            getString(R.string.key_preference_settings_clear_ssid) -> {
                ssidClearButton()
                false
            }
            getString(R.string.key_preference_settings_reset_airplane) -> {
                MaterialDialog(requireContext())
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
            getString(R.string.key_preference_settings_doze) -> {
                if ((preference as androidx.preference.CheckBoxPreference).isChecked) {
                    Log.i("RadioControl", "doze-bypassed")
                    if (Build.VERSION.SDK_INT >= 23) {
                        val intent = Intent()
                        val packageName = requireContext().packageName
                        val pm = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
                        if (pm.isIgnoringBatteryOptimizations(packageName)) {
                            Log.i("RadioControl", "ignoring")
                            view?.let { it1 ->
                                Snackbar.make(it1, "RadioControl is already excluded from Doze", Snackbar.LENGTH_LONG)
                                        .show()
                            }
                        } else {
                            intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                            intent.data = Uri.parse("package:$packageName")
                            requireContext().startActivity(intent)
                        }
                        dozeSetting?.isChecked = true
                    }
                } else {
                    dozeSetting?.isChecked = false
                    Log.i("RadioControl", "false")
                    if (Build.VERSION.SDK_INT >= 23) {
                        startActivityForResult(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS), 0)
                        /*Intent intent = new Intent();
                        String packageName = c.getPackageName();
                        PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        c.startActivity(intent);*/
                    }
                }
                false
            }
            getString(R.string.key_preference_settings_battery_opimization) -> {
                false
            }
            getString(R.string.key_preference_settings_alternate_command) -> {
                val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 200)
                    //altRootCommandPref?.isChecked = true
                } else {
                    //altRootCommandPref?.isChecked = false
                }

                false
            }
            getString(R.string.key_preference_settings_fabric) -> {

                if ((preference as androidx.preference.CheckBoxPreference).isChecked) {
                    MaterialDialog(requireContext())
                            .icon(R.mipmap.ic_launcher)
                            .title(R.string.title_allow_fabric)
                            .message(R.string.permissionSampleFabric)
                            .positiveButton(R.string.button_text_allow) {}
                            .negativeButton(R.string.button_text_deny) {
                                fabricCrashlyticsPref?.isChecked = false

                            }
                            .show()
                }
                false
            }
            getString(R.string.key_preference_settings_phone_state) -> {
                if ((preference as androidx.preference.CheckBoxPreference).isChecked) {
                    val permissionCheck = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)

                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_PHONE_STATE), 200)
                        callingCheck?.isChecked = true
                    } else {
                        callingCheck?.isChecked = false
                    }
                }

                false
            }
            getString(R.string.key_preference_settings_airplane_service) -> {
                if ((preference as androidx.preference.CheckBoxPreference).isChecked) {


                    val intervalTimeString = sp.getString("interval_prefs", "60")
                    val intervalTime = Integer.parseInt(intervalTimeString.toString())
                    val airplaneService = sp.getBoolean("isAirplaneService", false)

                    if (intervalTime != 0 && airplaneService) {
                        Log.d("RadioControl", "Alarm Scheduled")
                        alarmUtil.scheduleAlarm(requireContext())
                    }
                } else {
                    Log.d("RadioControl", "Alarm Cancelled")
                    alarmUtil.cancelAlarm(requireContext())
                }
                false
            }
            getString(R.string.key_preference_settings_night_mode) -> {
                //Initialize time picker
                val now = Calendar.getInstance()
                val tpd = TimePickerDialog.newInstance(
                        this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                )
                tpd.isThemeDark = true
                tpd.setAccentColor(R.color.mdtp_accent_color_dark)
                tpd.show(requireActivity(), "Timepickerdialog")
                false
            }
            getString(R.string.key_preference_settings_notification) -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                    startActivity(intent)
                }
                false
            }
            getString(R.string.key_preference_settings_logging) -> {
                if ((preference as androidx.preference.CheckBoxPreference).isChecked) {
                    //Request storage permissions if on MM or greater
                    if (Build.VERSION.SDK_INT >= 23) {
                        val perms = arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")

                        val permsRequestCode = 200

                        requestPermissions(perms, permsRequestCode)
                        Log.d("RadioControl", "Logging enabled")

                    } else {
                        Log.d("RadioControl", "Logging enabled")
                    }
                } else {
                    checkboxPref?.isChecked = false
                    Log.d("RadioControl", "Logging disabled")

                    MaterialDialog(requireContext())
                            .icon(R.mipmap.ic_launcher)
                            .title(R.string.title_delete_logs)
                            .message(R.string.summary_delete_log)
                            .positiveButton(R.string.button_yes) {
                                logDeleteButton()
                            }
                            .negativeButton(R.string.button_no) {}
                            .show()
                }
                false
            }
            getString(R.string.key_preference_settings_log_directory) -> {
                logDirectoryButton()
                false
            }
            getString(R.string.key_preference_settings_log_delete) -> {
                logDeleteButton()
                false
            }
            getString(R.string.key_preference_settings_ping_ip) -> {
                pingIpPref?.summary = ip
                false
            }
            else -> {
                super.onPreferenceTreeClick(preference)
            }
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
        val hourString = if (hourOfDay < 10) "0$hourOfDay" else "" + hourOfDay
        val minuteString = if (minute < 10) "0$minute" else "" + minute
        val hourStringEnd = if (hourOfDayEnd < 10) "0$hourOfDayEnd" else "" + hourOfDayEnd
        val minuteStringEnd = if (minuteEnd < 10) "0$minuteEnd" else "" + minuteEnd
        val time = "You picked the following time: From - " + hourString + "h" + minuteString + " To - " + hourStringEnd + "h" + minuteStringEnd

        alarmUtil.cancelNightAlarm(requireContext(), hourOfDay, minute)

        alarmUtil.scheduleNightWakeupAlarm(requireContext(), hourOfDayEnd, minuteEnd)
        Log.d("RadioControl", "Night Mode: $time")
        Toast.makeText(activity, "Night mode set from $hourOfDay:$minuteString to $hourOfDayEnd:$minuteStringEnd", Toast.LENGTH_LONG).show()
    }

    //Method for the ssid list clear button
    private fun ssidClearButton() {
        val pref = requireContext().getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)
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
        val log = File(requireContext().filesDir, "radiocontrol.log")
        log.delete()
        Toast.makeText(activity,
                "Log Deleted", Toast.LENGTH_LONG).show()
    }
}

private fun TimePickerDialog.show(requireActivity: FragmentActivity, s: String) {

}
