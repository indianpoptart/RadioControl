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
import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities
import com.nikhilparanjape.radiocontrol.services.CellRadioService

class CustomPhoneStateListener(//private static final String TAG = "PhoneStateChanged";
        private val context: Context //Context to make Toast if required
) : PhoneStateListener() {
    //Root commands which disable cell only
    internal var airCmd = arrayOf("su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
    //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
    private val airOffCmd2 = arrayOf("su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"")
    internal var airOffCmd3 = arrayOf("su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        super.onCallStateChanged(state, incomingNumber)
        Log.d("RadioControl", "PhoneCall State $state")
        when (state) {
            TelephonyManager.CALL_STATE_IDLE -> {
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //when Off hook i.e in call
                cellChange()
            TelephonyManager.CALL_STATE_RINGING -> {
            }
            else -> {
            }
        }//when Idle i.e no call
        //Toast.makeText(context, "Phone state Idle", Toast.LENGTH_LONG).show();
        //Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_LONG).show();
        //when Ringing
        //Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_LONG).show();
    }

    private fun cellChange() {
        val sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        //Utilities util = new Utilities();

        //Check if user wants the app on
        if (sp.getInt("isActive", 0) == 1 && prefs.getBoolean("isPhoneStateCheck", true)) {
            if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
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
    }

    companion object {
        private val PRIVATE_PREF = "prefs"
    }

}