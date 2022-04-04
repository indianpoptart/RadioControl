@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.listeners.CustomPhoneStateListener

/**
 * Created by Nikhil on 10/12/2018.
 *
 * @author Nikhil Paranjape
 *
 * An adapter class for TroubleShooting Activity
 */
class PhoneStateBroadcastReceiver : WakefulBroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver") // No one cares Google, stop keeping the toys to yourself
    override fun onReceive(context: Context, intent: Intent) {

        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE)
        Log.d("RadioControl-phone-BR", "PhoneCall ENGAGED")

    }
}