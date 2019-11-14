package com.nikhilparanjape.radiocontrol.utilities

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.services.BackgroundJobService
import java.io.File
import java.io.IOException

/**
 * Created by Nikhil on 2/3/2016.
 *
 * A custom Utilities class for RadioControl
 */
class Utilities {

    /**
     * Check if there is any active call
     * @param context
     * @return
     */
    fun isCallActive(context: Context): Boolean {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return manager.mode == AudioManager.MODE_IN_CALL
    }

    companion object {

        /**
         * gets network ssid
         * @param context
         * @return
         */
        fun getCurrentSsid(context: Context): String? {
            var ssid: String? = null
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (networkInfo!!.isConnected) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val connectionInfo = wifiManager.connectionInfo
                ssid = connectionInfo.ssid
                ssid = ssid!!.substring(1, ssid.length - 1)
            } else if (!networkInfo.isConnected) {
                ssid = "Not Connected"
            }
            return ssid
        }

        fun getCellStatus(c: Context): Int {
            var z = 0
            try {
                val tm = c.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val cellInfoList = tm.allCellInfo
                //This means cell is off
                if (cellInfoList.isEmpty()) {
                    z = 1
                }
                return z
            } catch (e: SecurityException) {
                Log.e("RadioControl", "Unable to get Location Permission", e)
            } catch (e: NullPointerException) {
                Log.e("RadioControl", "NullPointer ", e)
            }

            return z
        }

        /**
         * Checks link speed
         * @param c
         * @return
         */
        fun linkSpeed(c: Context): Int {
            val wifiManager = c.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val linkSpeed = wifiManager.connectionInfo.linkSpeed
            Log.d("RadioControl", "Link speed = " + linkSpeed + "Mbps")
            return linkSpeed
        }

        /**
         * Writes logs
         * @param c
         * @return
         */
        fun writeLog(data: String, c: Context) {
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(c)
            if (preferences.getBoolean("enableLogs", false)) {
                try {
                    val h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString()
                    val log = File(c.filesDir, "radiocontrol.log")
                    if (!log.exists()) {
                        log.createNewFile()
                    }
                    val logPath = "radiocontrol.log"
                    val string = "\n$h: $data"

                    val fos = c.openFileOutput(logPath, Context.MODE_APPEND)
                    fos.write(string.toByteArray())
                    fos.close()
                } catch (e: IOException) {
                    Log.e("RadioControl", "Error writing log")
                }
            }
        }

        // Schedule the start of the service every 10 - 30 seconds
        fun scheduleJob(context: Context) {
            val serviceComponent = ComponentName(context, BackgroundJobService::class.java)
            val builder = JobInfo.Builder(1, serviceComponent)
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
            val intervalTime = preferences.getString("interval_prefs", "10")?.toInt()
            //val intervalTime = Integer.parseInt(intervalTimeString)
            //val mJobScheduler = context as JobScheduler

            (intervalTime?.times(1000))?.toLong()?.let { builder.setMinimumLatency(it) } // wait at least
            (intervalTime?.times(1000))?.toLong()?.let { builder.setOverrideDeadline(it) } // maximum delay
            builder.setPersisted(true) // Persist at boot
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // require any network

            //mJobScheduler.schedule(builder.build())

            //(getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
        }


        fun frequency(c: Context): Int {
            val wifiManager = c.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val freq = wifiManager.connectionInfo.frequency
            val gHz = freq / 1000
            return when (gHz) {
                2 -> {
                    Log.d("RadioControl", "Frequency = " + freq + "MHz")
                    2
                }
                5 -> {
                    Log.d("RadioControl", "Frequency = " + freq + "MHz")
                    5
                }
                else -> 0
            }

        }

        @SuppressLint("ByteOrderMark")
                /**
         * Makes a network alert
         * @param context
         * @return
         */
        fun sendNote(context: Context, mes: String, vibrate: Boolean, sound: Boolean, heads: Boolean) {

            val notificationID = 102
            createNotificationChannel(context)
            val pi = PendingIntent.getActivity(context, 1, Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), 0)
            //Resources r = getResources();
            if (Build.VERSION.SDK_INT >= 26) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notification = Notification.Builder(context, "NetworkAlert")
                        .setContentTitle("Network Alert")
                        .setSmallIcon(R.drawable.ic_network_check_white_48dp)
                        .setContentIntent(pi)
                        .setContentText("Your WiFi connection is not functioning")
                        .setAutoCancel(true)
                        .build()

                notificationManager.notify(notificationID, notification)


            } else {
                val builder = androidx.core.app.NotificationCompat.Builder(context)
                        .setContentTitle("Network Alert")
                        .setSmallIcon(R.drawable.ic_network_check_white_48dp)
                        .setContentIntent(pi)
                        .setContentText("Your WiFi connection is not functioning")
                        .setPriority(-1)
                        .setAutoCancel(true)
                        .build()
            }
        }

        private fun createNotificationChannel(context: Context) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Network Alert"
                val description = "Channel for network related alerts"
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel("networkalert", name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager!!.createNotificationChannel(channel)
            }
        }

        /**
         * Get the network info
         * @param context
         * @return
         */
        private fun getNetworkInfo(context: Context): NetworkInfo? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo
        }

        /**
         * Check if there is any connectivity to a Wifi network
         * @param context
         * @return
         */
        fun isConnectedWifi(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting && info.type == ConnectivityManager.TYPE_WIFI
        }

        fun isWifiOn(context: Context): Boolean {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }

        /**
         * Check if there is any connectivity
         * @param context
         * @return
         */
        fun isConnected(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting
        }

        /**
         * Check if there is any connectivity to a mobile network
         * @param context
         * @return
         */
        fun isConnectedMobile(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting && info.type == ConnectivityManager.TYPE_MOBILE
        }

        /**
         * Check if there is fast connectivity
         * @param context
         * @return
         */
        fun isConnectedFast(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting && isConnectionFast(info.type, info.subtype)
        }

        fun isAirplaneMode(context: Context): Boolean {
            return Settings.Global.getInt(context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        }



        /**
         * Check if the connection is fast
         * @param type
         * @param subType
         * @return
         */
        private fun isConnectionFast(type: Int, subType: Int): Boolean {
            return when (type) {
                ConnectivityManager.TYPE_WIFI -> true
                ConnectivityManager.TYPE_MOBILE -> when (subType) {
                    TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                    TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> true // ~ 400-1000 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> true // ~ 600-1400 kbps
                    TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                    TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPA -> true // ~ 700-1700 kbps
                    TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                    TelephonyManager.NETWORK_TYPE_UMTS -> true // ~ 400-7000 kbps
                    TelephonyManager.NETWORK_TYPE_EHRPD // API level 11
                    -> true // ~ 1-2 Mbps
                    TelephonyManager.NETWORK_TYPE_EVDO_B // API level 9
                    -> true // ~ 5 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPAP // API level 13
                    -> true // ~ 10-20 Mbps
                    TelephonyManager.NETWORK_TYPE_IDEN // API level 8
                    -> false // ~25 kbps
                    TelephonyManager.NETWORK_TYPE_LTE // API level 11
                    -> true // ~ 10+ Mbps
                    // Unknown
                    TelephonyManager.NETWORK_TYPE_UNKNOWN -> false
                    else -> false
                }
                else -> false
            }
        }
    }
}
