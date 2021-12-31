package com.nikhilparanjape.radiocontrol.utilities

import android.content.Context
import android.util.Log
import java.net.InetAddress
import kotlin.system.measureTimeMillis

/**
 * Created by Nikhil on 01/19/2021.
 *
 * A custom Networking Utility class for RadioControl
 *
 * This "offloads" the networking tasks to this class allowing for easy returns for UI elements
 */
class NetworkUtility {
    companion object{
        fun reachable(context: Context): Boolean {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            val ip = preferences.getString("prefPingIp", "1.0.0.1")
            val address = InetAddress.getByName(ip)
            var reachable = false

            val timeDifference = measureTimeMillis {
                reachable = address.isReachable(1000)
            }
            Log.d("RadioControl-Networking", "Reachable?: $reachable, Time: $timeDifference")

            return reachable
        }
    }
}