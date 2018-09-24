@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver

import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService

class TimedAlarmReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, BackgroundAirplaneService::class.java)
        context.startService(i)
    }

    companion object {
        val REQUEST_CODE = 12345
        val ACTION = "com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService"
    }
}
