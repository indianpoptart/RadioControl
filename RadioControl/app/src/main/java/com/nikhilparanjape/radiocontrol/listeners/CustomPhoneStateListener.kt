package com.nikhilparanjape.radiocontrol.listeners

/**
 * Created by admin on 3/4/2017.
 *
 * @author Nikhil Paranjape
 *
 *
 */

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.Utilities



class CustomPhoneStateListener(private val context: Context) : PhoneStateListener() { //Context for making a Toast if applicable
    //Root commands which disable cell only
    //private var airCmd = arrayOf("su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")

    //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
    //private val airOffCmd2 = arrayOf("su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"")

    private var lastState = TelephonyManager.CALL_STATE_IDLE // We assume the last state of the phone call is idle for now
    private var isIncoming: Boolean = false //Boolean for if there is an incoming call

    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
        super.onCallStateChanged(state, "1") // Do not pull phone number for privacy
        Log.d(TAG, "PhoneCall State $state")
        Log.d(TAG, "Last-State $lastState")
        if (lastState == state) {
            //No change, debounce extras
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true // If the phone is ringing, set isIncoming to true
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                //when Off hook i.e in call
                //doThingChangeNameLater()
                Log.d(TAG,"Outgoing Call Starting")
                isIncoming = false
                lastState = state
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                when {
                    lastState == TelephonyManager.CALL_STATE_RINGING -> {
                        // When phone is idle and the last state was ringing do nothing for now
                    }
                    isIncoming -> {
                        // When there
                    }
                    else -> {
                        Log.d(TAG, "Job Time")
                        Utilities.scheduleJob(context)
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
    companion object {
        private const val TAG = "RadioControl-phoneSL"
    }
}