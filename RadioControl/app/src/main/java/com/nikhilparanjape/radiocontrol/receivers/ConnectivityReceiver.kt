package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService
import android.net.ConnectivityManager

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
class ConnectivityReceiver : WakefulBroadcastReceiver() {

    private val mConnRecListener = ConnectivityReceiver.Companion

    override fun onReceive(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        Log.d("RadioControl", "Get action-ConRec: " + intent.action!!)

        val i = Intent(context, BackgroundAirplaneService::class.java)
        context.startService(i)


    }
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
    interface ConnectivityReceiverListener

    companion object {
        fun onNetworkConnectionChanged(isConnected: Boolean) {

        }
    }
}
