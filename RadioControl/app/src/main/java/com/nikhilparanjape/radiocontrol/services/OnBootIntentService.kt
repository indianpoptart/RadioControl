package com.nikhilparanjape.radiocontrol.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat

import com.nikhilparanjape.radiocontrol.R

/**
 * Created by admin on 10/12/2018.
 *
 * @author Nikhil Paranjape
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
    }

    override fun onHandleWork(intent: Intent) {

    }

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