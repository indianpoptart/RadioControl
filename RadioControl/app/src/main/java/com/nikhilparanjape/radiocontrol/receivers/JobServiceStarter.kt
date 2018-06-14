package com.nikhilparanjape.radiocontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities

class JobServiceStarter : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Utilities.scheduleJob(context)
    }
}