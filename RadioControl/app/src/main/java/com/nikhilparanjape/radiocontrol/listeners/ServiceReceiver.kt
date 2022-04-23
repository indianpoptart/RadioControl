package com.nikhilparanjape.radiocontrol.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getMainExecutor

/**
 *
 *
 * Sourced from: https://stackoverflow.com/questions/69571012/telephonymanager-deprecated-listen-call-state-ringing-on-android-12
 */


class ServiceReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context?, intent: Intent?) {

        registerCustomTelephonyCallback(context)

    }

    @RequiresApi(Build.VERSION_CODES.S)
    class CustomTelephonyCallback(private val func: (state: Int) -> Unit) :
        TelephonyCallback(),
        TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            func(state)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun registerCustomTelephonyCallback(context: Context?) {

        var callback: CustomTelephonyCallback? = null

        (context?.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)?.registerTelephonyCallback(
            getMainExecutor(context),
            CustomTelephonyCallback {
                //
                var state = it


            }.also {
                callback = it
            }
        )
    }
}