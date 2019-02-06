package com.nikhilparanjape.radiocontrol.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat

import com.nikhilparanjape.radiocontrol.R

/**
 * Created by admin on 10/12/2018.
 *
 * @author Nikhil Paranjape
 *
 * @description This class is supposed to wake up RadioControl when the device first boots up. But its pretty much a hit or miss
 * It should probably have some things for actually calling the background airplane service so it starts in the background. Maybe work mode? we'll see...
 */
class OnBootIntentService : JobIntentService() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, "Startup")
                    .setSmallIcon(R.drawable.ic_radiocontrol_main)
                    .setContentTitle("Startup Operations")
                    .setContentText("Running startup operations...")
                    .build()
        }
        val getPrefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val airplaneService = getPrefs.getBoolean(getString(R.string.preference_airplane_service), false)

        //Begin background service
        if (airplaneService) {
            val i = Intent(applicationContext, BackgroundJobService::class.java)
            applicationContext.startService(i)
            Log.d("RadioControl", "background Service launched")
        }
        if (getPrefs.getBoolean(getString(R.string.preference_work_mode), true)) {
            val i = Intent(applicationContext, PersistenceService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                applicationContext.startForegroundService(i)
            } else {
                applicationContext.startService(i)
            }
            Log.d("RadioControl", "persist Service launched")
        }

    }

    override fun onHandleWork(intent: Intent) { }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Startup"
            val description = "Channel for startup related notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("startup", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }
}