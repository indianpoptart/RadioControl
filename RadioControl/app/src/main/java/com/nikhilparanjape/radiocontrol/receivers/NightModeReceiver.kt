package com.nikhilparanjape.radiocontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities

class NightModeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val util = Utilities()
        util.cancelAlarm(context)

        Log.d("RadioControl", "Night Mode started")

    }

    companion object {

        val REQUEST_CODE = 12345
        val ACTION = "com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService"
    }
}