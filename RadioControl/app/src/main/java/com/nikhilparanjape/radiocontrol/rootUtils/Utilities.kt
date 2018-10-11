package com.nikhilparanjape.radiocontrol.rootUtils

import android.annotation.SuppressLint
import android.app.*
import android.app.AlarmManager.RTC
import android.app.AlarmManager.RTC_WAKEUP
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.*
import com.nikhilparanjape.radiocontrol.services.TestJobService
import java.io.File
import java.io.IOException
import java.util.*


/**
 * Created by Nikhil on 2/3/2016.
 *
 * A custom Utilities class for RadioControl
 */
class Utilities {

    fun scheduleWakeupAlarm(c: Context, hour: Int) {
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR, hour)
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, WakeupReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, ActionReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)

    }

    fun scheduleNightWakeupAlarm(c: Context, hourofDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay)
        cal.add(Calendar.MINUTE, minute)
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, TimedAlarmReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)

    }

    // Setup a recurring alarm every 15,30,60 minutes
    fun scheduleAlarm(c: Context) {

        val preferences = PreferenceManager.getDefaultSharedPreferences(c)
        val intervalTimeString = preferences.getString("interval_prefs", "10")
        val intervalTime = Integer.parseInt(intervalTimeString)
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.MINUTE, intervalTime)
        Log.d("RadioControl", "Interval: " + intervalTime + cal.time)

        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, TimedAlarmReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val interval = cal.timeInMillis
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        Log.d("RadioControl", "Time: " + (cal.timeInMillis - firstMillis))
        //alarm.setInexactRepeating(RTC, firstMillis,
        //cal.getTimeInMillis(), pIntent);

        alarm.setInexactRepeating(RTC, firstMillis + interval, interval, pIntent)

    }

    fun scheduleRootAlarm(c: Context) {
        val cal = Calendar.getInstance()
        // start 10 seconds
        cal.add(Calendar.SECOND, 30)

        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, RootServiceReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every X seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        alarm.setInexactRepeating(RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)
        Log.d("RadioControl", "RootClock enabled for " + cal.time)

    }

    fun cancelRootAlarm(c: Context) {
        val intent = Intent(c, RootServiceReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
        Log.d("RadioControl", "RootClock cancelled")
    }

    fun cancelAlarm(c: Context) {
        val intent = Intent(c, TimedAlarmReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
    }

    fun cancelNightAlarm(c: Context, hourofDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay)
        cal.add(Calendar.MINUTE, minute)
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, NightModeReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, NightModeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)
    }

    fun cancelWakeupAlarm(c: Context) {
        val intent = Intent(c, WakeupReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(c, WakeupReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
    }

    /**
     * Enable WiFi without root
     * @param context
     */
    fun enableWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true

    }

    /**
     * Disable WiFi without root
     * @param context
     */
    fun disableWifi(context: Context) {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = false

    }

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
            if (networkInfo.isConnected) {
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
            val preferences = PreferenceManager.getDefaultSharedPreferences(c)
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
            val serviceComponent = ComponentName(context, TestJobService::class.java)
            val builder = JobInfo.Builder(0, serviceComponent)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val intervalTimeString = preferences.getString("interval_prefs", "10")
            val intervalTime = Integer.parseInt(intervalTimeString)

            builder.setMinimumLatency((intervalTime * 1000).toLong()) // wait at least
            builder.setOverrideDeadline((intervalTime * 1000).toLong()) // maximum delay
            //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
            //builder.setRequiresDeviceIdle(true); // device should be idle
            //builder.setRequiresCharging(false); // we don't care if the device is charging or not
            var jobScheduler: JobScheduler? = null
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                jobScheduler = context.getSystemService(JobScheduler::class.java)
            }
            jobScheduler!!.schedule(builder.build())
        }

        fun frequency(c: Context): Int {
            val wifiManager = c.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val freq = wifiManager.connectionInfo.frequency
            val gHz = freq / 1000
            if (gHz == 2) {
                Log.d("RadioControl", "Frequency = " + freq + "MHz")
                return 2
            } else if (gHz == 5) {
                Log.d("RadioControl", "Frequency = " + freq + "MHz")
                return 5
            } else
                return 0

        }

        fun getPingStats(s: String): String {
            try {
                val status: String
                when {
                    s.contains("0% packet loss") -> {
                        val start = s.indexOf("/mdev = ")
                        val end = s.indexOf(" ms\n", start)
                        s.substring(start + 8, end)
                        val stats = s.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        return stats[2]
                    }
                    s.contains("100% packet loss") -> {
                        status = "100% packet loss"
                        return status

                    }
                    s.contains("50% packet loss") -> {
                        status = "50% packet loss"
                        return status
                    }
                    s.contains("25% packet loss") -> {
                        status = "25% packet loss"
                        return status
                    }
                    s.contains("unknown host") -> {
                        status = "unknown host"
                        return status
                    }
                    else -> {
                        status = "unknown error in getPingStats"
                        return status
                    }
                }
            } catch (e: StringIndexOutOfBoundsException) {
                return "An error occurred"
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

                notificationManager?.notify(notificationID, notification)


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
        fun getNetworkInfo(context: Context): NetworkInfo? {
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

        fun getNetworkType(context: Context): String {
            try {
                val info = getNetworkInfo(context)
                val type = info!!.type
                val subType = info.subtype

                return if (type == ConnectivityManager.TYPE_WIFI) {
                    "WIFI"
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    when (subType) {
                        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT" // ~ 50-100 kbps
                        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA" // ~ 14-64 kbps
                        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE" // ~ 50-100 kbps
                        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO_0" // ~ 400-1000 kbps
                        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO_A" // ~ 600-1400 kbps
                        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS" // ~ 100 kbps
                        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA" // ~ 2-14 Mbps
                        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA" // ~ 700-1700 kbps
                        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA" // ~ 1-23 Mbps
                        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS" // ~ 400-7000 kbps
                        TelephonyManager.NETWORK_TYPE_EHRPD // API level 11
                        -> "EHRPD" // ~ 1-2 Mbps
                        TelephonyManager.NETWORK_TYPE_EVDO_B // API level 9
                        -> "EVDO_B" // ~ 5 Mbps
                        TelephonyManager.NETWORK_TYPE_HSPAP // API level 13
                        -> "HSPAP" // ~ 10-20 Mbps
                        TelephonyManager.NETWORK_TYPE_IDEN // API level 8
                        -> "IDEN" // ~25 kbps
                        TelephonyManager.NETWORK_TYPE_LTE // API level 11
                        -> "LTE" // ~ 10+ Mbps
                        // Unknown
                        TelephonyManager.NETWORK_TYPE_UNKNOWN -> "UNKNOWN"
                        else -> "UNKNOWN"
                    }
                } else {
                    "UNKNOWN"
                }
            } catch (e: NullPointerException) {
                return "CELL"
            }

        }

        /**
         * Check if the connection is fast
         * @param type
         * @param subType
         * @return
         */
        fun isConnectionFast(type: Int, subType: Int): Boolean {
            return if (type == ConnectivityManager.TYPE_WIFI) {
                true
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                when (subType) {
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
            } else {
                false
            }
        }
    }
}
