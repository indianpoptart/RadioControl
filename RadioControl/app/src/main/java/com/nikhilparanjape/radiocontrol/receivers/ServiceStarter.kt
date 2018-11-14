package com.nikhilparanjape.radiocontrol.receivers

/**
 * Created by admin on 4/23/2017.
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService

class ServiceStarter : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, BackgroundAirplaneService::class.java))
        } else {
            context.startService(Intent(context, BackgroundAirplaneService::class.java))
        }
    }
}