@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import androidx.legacy.content.WakefulBroadcastReceiver

import com.nikhilparanjape.radiocontrol.services.BackgroundJobService

class TimedAlarmReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, BackgroundJobService::class.java)
        context.startService(i)
    }

    companion object {
        const val REQUEST_CODE = 142456
    }
}
