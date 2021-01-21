package com.nikhilparanjape.radiocontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers

/**
 * Created by Nikhil on 10/12/2018.
 *
 * A class that receives when night mode is activated
 */

class NightModeReceiver : BroadcastReceiver() {
    private var alarmUtil = AlarmSchedulers()

    override fun onReceive(context: Context, intent: Intent) {
        alarmUtil.cancelAlarm(context)

        Log.d("RadioControl-Night", "Night Mode started")
    }
    companion object {

        const val REQUEST_CODE = 17545
    }
}
