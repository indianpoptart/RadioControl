package com.nikhilparanjape.radiocontrol.listeners

/**
 * Created by admin on 3/4/2017.
 */

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.RootAccess

import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.nikhilparanjape.radiocontrol.services.CellRadioService

class CustomPhoneStateListener(//private static final String TAG = "PhoneStateChanged";
        private val context: Context //Context to make Toast if required
) : PhoneStateListener() {
    //Root commands which disable cell only
    internal var airCmd = arrayOf("su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
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
                        cellChange()
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
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        //Utilities util = new Utilities();

        //Check if user wants the app on
        if (sp.getInt("isActive", 0) == 1 && prefs.getBoolean("isPhoneStateCheck", true)) {
            //If airplane mode is on or cell is off
            if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                //If Wifi is not connected
                if(!Utilities.isConnectedWifi(context)){
                    //Runs the alternate root command
                    if (prefs.getBoolean("altRootCommand", false) && !Utilities.isAirplaneMode(context)) {
                        if (Utilities.getCellStatus(context) == 1) {
                            val cellIntent = Intent(context, CellRadioService::class.java)
                            //util.cancelAlarm(context);
                            context.startService(cellIntent)
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
                            val cellIntent = Intent(context, CellRadioService::class.java)
                            //util.cancelAlarm(context);
                            context.startService(cellIntent)
                            Log.d("RadioControl", "Cell Radio has been turned off")
                        }
                    } else {
                        RootAccess.runCommands(airCmd)
                        Log.d("RadioControl", "Airplane mode has been turned on")

                    }
                }

            }
        }
    }

    companion object {
        private const val PRIVATE_PREF = "prefs"
    }

}