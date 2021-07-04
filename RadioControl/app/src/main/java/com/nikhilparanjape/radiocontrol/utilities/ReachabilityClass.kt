package com.nikhilparanjape.radiocontrol.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.MutableStateFlow

class ReachabilityClass (private val context: Context){
    val isNetworkConnected = MutableStateFlow(false)

    private val connectivityManager: ConnectivityManager? = null
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Connected")
            isNetworkConnected.value = true
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Disconnected")
            isNetworkConnected.value = false
        }
    }

    fun start() {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            }
        } catch (e: Exception) {
            Log.d(TAG, "Could not start: ${e.message.toString()}")
            e.printStackTrace()
            isNetworkConnected.value = false
        }
    }

    fun stop() {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }
    companion object{
        private const val TAG = "RadioControl-NetReach"
    }
}