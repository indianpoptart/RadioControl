package com.nikhilparanjape.radiocontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nikhilparanjape.radiocontrol.services.OnBootIntentService
import com.nikhilparanjape.radiocontrol.utilities.Utilities

/**
 * Created by Nikhil on Fillin.
 *
 * @author
 * A service starter
 */

class JobServiceStarter : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, OnBootIntentService::class.java))
        } else {
            context.startService(Intent(context, OnBootIntentService::class.java))
        }
        Utilities.scheduleJob(context)
    }
}