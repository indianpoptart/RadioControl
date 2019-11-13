package com.nikhilparanjape.radiocontrol.listeners

/**
 * Created by admin on 3/4/2017.
 */

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.RootAccess
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.writeLog
import com.topjohnwu.superuser.Shell
import java.util.*

class CustomPhoneStateListener(//private static final String TAG = "PhoneStateChanged";
        private val context: Context //Context to make Toast if required
) : PhoneStateListener() {
    //Root commands which disable cell only
    private var airCmd = arrayOf("su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
    //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
    private val airOffCmd2 = arrayOf("su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"")

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private val isIncoming: Boolean = false

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        Log.d("RadioControl", "PhoneCall State $state")
        Log.d("RadioControl", "Last-State $lastState")
        if(lastState == state){
            //No change, debounce extras
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {

            }
            TelephonyManager.CALL_STATE_OFFHOOK ->{
                //when Off hook i.e in call
                cellChange()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                    }
                    isIncoming -> {}
                    else -> {
                        Log.d("RadioControl", "DoThing")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Utilities.scheduleJob(context)
                        } else {
                            cellChange()
                        }

                    }
                }

            }
        }//when Idle i.e no call
        lastState = state
        //Toast.makeText(context, "Phone state Idle", Toast.LENGTH_LONG).show();
        //Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_LONG).show();
        //when Ringing
        //Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_LONG).show();
    }

    // Highly experimental
    private fun cellChange() {
        val sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        //Utilities util = new Utilities();
        //Log.d("RadioControl", "Active: $activeNetwork")

        val disabledPref = context.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)

        val h = HashSet(listOf("")) //Set default set for SSID check
        val selections = prefs.getStringSet("ssid", h) //Gets stringset, if empty sets default
        val networkAlert = prefs.getBoolean("isNetworkAlive", false)
        prefs.getBoolean("isBatteryOn", true)

        //Check if user wants the app on
        if (sp.getInt("isActive", 0) == 1 && prefs.getBoolean("isPhoneStateCheck", true)) {
            //If airplane mode is on or cell is off
            if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                //If Wifi is not connected
                if(!Utilities.isConnectedWifi(context)){
                    //Runs the alternate root command
                    if (prefs.getBoolean("altRootCommand", false) && !Utilities.isAirplaneMode(context)) {
                        if (Utilities.getCellStatus(context) == 1) {
                            val output = Shell.su("service call phone 27").exec().out
                            writeLog("root accessed: $output", context)
                            Log.d("RadioControl", "Cell Radio has been turned on")
                        }
                    } else {
                        RootAccess.runCommands(airOffCmd2)
                        Log.d("RadioControl", "Airplane mode has been turned off")

                    }
                }
            }
            //If Airplane mode is off, or cell is connected
            else if (!Utilities.isAirplaneMode(context) || Utilities.isConnectedMobile(context)) {
                //If wifi is connected
                if (Utilities.isConnectedWifi(context)){
                    //Disable cell radio
                    if (prefs.getBoolean("altRootCommand", false)) {
                        if (Utilities.getCellStatus(context) == 0) {
                            val output = Shell.su("service call phone 27").exec().out
                            writeLog("root accessed: $output", context)
                            Log.d("RadioControl", "Cell Radio has been turned off")
                        }
                    } else {
                        RootAccess.runCommands(airCmd)
                        Log.d("RadioControl", "Airplane mode has been turned on")

                    }
                }

            }
        }
        if (sp.getInt("isActive", 0) == 1) {
            //Check if we just lost WiFi signal
            if (!Utilities.isConnectedWifi(context)) {
                Log.d("RadioControl", "WiFi signal LOST")
                writeLog("WiFi Signal lost", context)
                if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                    //Runs the alternate root command
                    if (prefs.getBoolean("altRootCommand", true)) {
                        if (Utilities.getCellStatus(context) == 1) {
                            val output = Shell.su("service call phone 27").exec().out
                            writeLog("root accessed: $output", context)
                            Log.d("RadioControl", "Cell Radio has been turned on")
                            writeLog("Cell radio has been turned off", context)
                        }
                    } else {
                        if (prefs.getBoolean("altBTCommand", false)) {
                            val output = Shell.su("settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false").exec().out
                            writeLog("root accessed: $output", context)
                            //RootAccess.runCommands(airOffCmd3)
                            Log.d("RadioControl", "Airplane mode has been turned off(with bt cmd)")
                            writeLog("Airplane mode has been turned off", context)
                        } else {
                            val output = Shell.su("settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"").exec().out
                            writeLog("root accessed: $output", context)
                            //RootAccess.runCommands(airOffCmd2)
                            Log.d("RadioControl", "Airplane mode has been turned off")
                            writeLog("Airplane mode has been turned off", context)
                        }

                    }
                }
            }

            //If network is connected and airplane mode is off or Cell is on
            if (Utilities.isConnectedWifi(context) && !Utilities.isAirplaneMode(context)) {
                //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
                //Check the list of disabled networks
                if (!disabledPref.contains(Utilities.getCurrentSsid(context))) {
                    Log.d("RadioControl", "The current SSID was not found in the disabled list")

                    //Checks if the user doesn't want network alerts
                    if (!networkAlert) {
                        //Runs the alternate root command
                        if (prefs.getBoolean("altRootCommand", false)) {

                            if (Utilities.getCellStatus(context) == 0) {
                                val output = Shell.su("service call phone 27").exec().out
                                writeLog("root accessed: $output", context)
                                Log.d("RadioControl", "Cell Radio has been turned off")
                            } else if (Utilities.getCellStatus(context) == 1) {
                                Log.d("RadioControl", "Cell Radio is already off")
                            }

                        } else {
                            val output = Shell.su("settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true").exec().out
                            writeLog("root accessed: $output", context)
                            //RootAccess.runCommands(airCmd)
                            Log.d("RadioControl", "Airplane mode has been turned on")
                            writeLog("Airplane mode has been turned on", context)
                        }

                    }
                } else if (selections!!.contains(Utilities.getCurrentSsid(context))) {
                    Log.d("RadioControl", "The current SSID was blocked from list $selections")
                    writeLog("The current SSID was blocked from list $selections", context)
                }//Pauses because WiFi network is in the list of disabled SSIDs
            }

        }
    }

    companion object {
        private const val PRIVATE_PREF = "prefs"
    }

}