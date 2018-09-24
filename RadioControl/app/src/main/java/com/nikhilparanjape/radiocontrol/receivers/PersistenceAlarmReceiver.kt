@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.support.v4.content.WakefulBroadcastReceiver

import com.nikhilparanjape.radiocontrol.services.PersistenceService

/**
 * Created by admin on 7/9/2017.
 */

class PersistenceAlarmReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val service = Intent(context, PersistenceService::class.java)
        WakefulBroadcastReceiver.startWakefulService(context, service)
    }

    companion object {
        val REQUEST_CODE = 25912
    }
}
