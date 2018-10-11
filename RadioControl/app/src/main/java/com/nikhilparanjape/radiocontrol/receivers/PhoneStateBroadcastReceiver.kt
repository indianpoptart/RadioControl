@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

/**
 * Created by admin on 3/4/2017.
 */

import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.listeners.CustomPhoneStateListener

class PhoneStateBroadcastReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE)
        Log.d("RadioControl", "PhoneCall ENGAGED")

    }
}