@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.services.CellRadioService
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers

/**
 * Created by admin on 9/24/2016.
 *
 * @author Nikhil Paranjape
 *
 *
 */

class RootServiceReceiver : WakefulBroadcastReceiver() {
    private var alarmUtil = AlarmSchedulers()

    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, CellRadioService::class.java)

        context.stopService(i)
        Log.d(TAG, "CellService Stopped")
        alarmUtil.cancelRootAlarm(context)
        Log.d(TAG, "RootClock cancelled")
    }

    companion object {
        const val REQUEST_CODE = 35718
        private const val TAG = "RadioControl-RSR"
    }
}
