package com.nikhilparanjape.radiocontrol.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.nikhilparanjape.radiocontrol.R
import java.io.File
import java.io.IOException


/**
 * Created by admin on 10/12/2018.
 *
 * @author Nikhil Paranjape
 *
 * @description This class is supposed to wake up RadioControl when the device first boots up. But its pretty much a hit or miss
 * It should probably have some things for actually calling the background airplane service so it starts in the background. Maybe work mode? we'll see...
 */
class OnBootIntentService : JobIntentService() {


    override fun onHandleWork(@NonNull intent: Intent) {
        createNotificationChannel(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, "Startup")
                    .setSmallIcon(R.drawable.ic_radiocontrol_main)
                    .setContentTitle("Startup Operations")
                    .setContentText("Running startup operations...")
                    .build()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val i = Intent(applicationContext, PersistenceService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                applicationContext.startForegroundService(i)
            } else {
                applicationContext.startService(i)
            }
            Log.d(TAG, "persist Service launched")
        }
        "boot procedure handled".writeLog(applicationContext)
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
    private fun String.writeLog(c: Context) {
        val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(c)
        if (preferences.getBoolean("enableLogs", false)) {
            try {
                val h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString()
                val log = File(c.filesDir, "radiocontrol.log")
                if (!log.exists()) {
                    log.createNewFile()
                }
                val logPath = "radiocontrol.log"
                val string = "\n$h: $this"

                val fos = c.openFileOutput(logPath, Context.MODE_APPEND)
                fos.write(string.toByteArray())
                fos.close()
            } catch (e: IOException) {
                Log.d(TAG, "There was an error saving the log: $e")
            }

        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }
    companion object {
        private const val TAG = "RadioControl-Boot"
    }
}