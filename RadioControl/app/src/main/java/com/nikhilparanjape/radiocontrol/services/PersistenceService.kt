package com.nikhilparanjape.radiocontrol.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver
import com.nikhilparanjape.radiocontrol.utilities.Utilities

/**
 * Created by admin on 7/9/2017.
 *
 * @author Nikhil Paranjape
 *
 * @description This class allows RadioControl to keep itself awake in the background.
 */
class PersistenceService : Service() {
    private val myConnectivityReceiver = ConnectivityReceiver()
    var context: Context = this
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, getString(R.string.log_persistence_created))
        createNotificationChannel()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(myConnectivityReceiver, filter)
        createNotification(1)

    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, getString(R.string.log_persistence_started))
        createNotification(2)

        return START_STICKY
    }
    private fun createNotification(id: Int){
        Log.i(TAG, "Notification Created")
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)

        try {
            applicationContext.registerReceiver(myConnectivityReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Registration Failed")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getPrefs.getInt("isActive", 0) == 1) {
            Utilities.scheduleJob(context)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val builder = Notification.Builder(this, "persistence")
                    .setContentTitle("Persistence")
                    .setSmallIcon(R.drawable.ic_memory_24px)
                    .setContentText("This keeps RadioControl functioning in the background")
                    .setAutoCancel(true)

            val notification = builder.build()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                startForeground(1, notification)
            } else {
                startForeground(1, notification,FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE)
            }
            //startForeground(1, notification) //TODO Fix this error by adding foreground service


        } else {
            //Run this code if the device is older (With a probably better OS :))
            @Suppress("DEPRECATION") //For backwards compatibility
            val builder = NotificationCompat.Builder(this)
                    .setContentTitle("Persistence")
                    .setSmallIcon(R.drawable.ic_memory_24px)
                    .setContentText("This keeps RadioControl functioning in the background")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setAutoCancel(true)

            val notification = builder.build()
            startForeground(id, notification)

        }
    }
    private fun createNotificationChannel() {
        Log.i("RadioControl-persist", "Notification channel created")
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Persistence"
            val description = getString(R.string.persistence_description)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel("persistence", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
    override fun onBind(intent: Intent): IBinder? {
        Log.i(TAG, getString(R.string.log_persistence_bound))

        return null
    }
    override fun onDestroy() {
        unregisterReceiver(myConnectivityReceiver)
        super.onDestroy()
    }
    companion object {

        private const val TAG = "RadioControl-Persist"
        /*private const val PRIVATE_PREF = "prefs"*/

    }

}
