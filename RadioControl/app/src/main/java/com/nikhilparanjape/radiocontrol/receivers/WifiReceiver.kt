package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService
import com.nikhilparanjape.radiocontrol.services.CellRadioService
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import com.nikhilparanjape.radiocontrol.utilities.RootAccess
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.topjohnwu.superuser.Shell
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.*


@Suppress("DEPRECATION")
/**
 * Created by Nikhil Paranjape on 11/8/2015.
 *
 * This file will get deprecated soon :( Sad, as it's the backbone of this app
 *
 * No longer the "backbone" testing if it still is
 *
 * @author Nikhil Paranjape
 */

//This file is kept for backwards compatibility
class WifiReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        Log.d("RadioControl", "Get action-wfr: " + intent.action!!)

        val i = Intent(context, BackgroundAirplaneService::class.java)
        context.startService(i)


    }
}
