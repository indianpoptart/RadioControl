@file:Suppress("DEPRECATION")

package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities
import com.nikhilparanjape.radiocontrol.services.CellRadioService

/**
 * Created by admin on 9/24/2016.
 */

class RootServiceReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, CellRadioService::class.java)
        val util = Utilities()

        context.stopService(i)
        Log.d("RadioControl", "CellService Stopped")
        util.cancelRootAlarm(context)
        Log.d("RadioControl", "RootClock cancelled")
    }

    companion object {
        const val REQUEST_CODE = 35718
    }
}
