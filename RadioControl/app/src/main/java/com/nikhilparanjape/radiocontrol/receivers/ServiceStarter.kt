package com.nikhilparanjape.radiocontrol.receivers

/**
 * Created by admin on 4/23/2017.
 */

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nikhilparanjape.radiocontrol.services.BackgroundJobService

class ServiceStarter : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver") // Google bad
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(Intent(context, BackgroundJobService::class.java))
        } else {
            context.startService(Intent(context, BackgroundJobService::class.java))
        }
    }
}