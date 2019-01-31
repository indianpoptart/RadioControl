package com.nikhilparanjape.radiocontrol.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver


/**
 * Created by admin on 7/9/2017.
 *
 * @author Nikhil Paranjape
 *
 * @description This class allows RadioControl to keep itself awake in the background.
 */
class PersistenceService : Service() {
    private val myBroadcast = ConnectivityReceiver()
    var context: Context = this
    override fun onCreate() {
        super.onCreate()
        Log.i("RadioControl", getString(R.string.log_persistence_created))
        createNotificationChannel()
    }
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i("RadioControl", getString(R.string.log_persistence_started))
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
           /* val job = dispatcher.newJobBuilder().setService(Updater::class.java)
                    .setTag("connectivity-job").setLifetime(Lifetime.FOREVER).setRetryStrategy(RetryStrategy.DEFAULT_LINEAR)
                    .setRecurring(true).setReplaceCurrent(true).setTrigger(Trigger.executionWindow(0, 0)).build()
            dispatcher.mustSchedule(job)*/
        }

        try {
            this.registerReceiver(myBroadcast, filter)
        } catch (e: Exception) {
            Log.e("RadioControl-register", "Registration Failed")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val builder = Notification.Builder(this, "persistence")
                    .setContentTitle("Persistence")
                    .setSmallIcon(R.drawable.ic_memory_24px)
                    .setContentText("This keeps RadioControl functioning in the background")
                    .setAutoCancel(true)

            val notification = builder.build()
            startForeground(1, notification)

        } else {

            @Suppress("DEPRECATION") //For backwards compatibility
            val builder = NotificationCompat.Builder(this)
                    .setContentTitle("Persistence")
                    .setSmallIcon(R.drawable.ic_memory_24px)
                    .setContentText("This keeps RadioControl functioning in the background")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)

            val notification = builder.build()

            startForeground(1, notification)
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Persistence"
            val description = getString(R.string.persistence_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel("persistence", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    fun createNotificationChannelInput(name: String,description: String,  importance: Int, channelName: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelName, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("RadioControl", getString(R.string.log_persistence_bound))

        return null
    }

    override fun onDestroy() {
        unregisterReceiver(myBroadcast)
        super.onDestroy()

    }

}
