package com.nikhilparanjape.radiocontrol.utilities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import androidx.core.content.ContextCompat
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.services.BackgroundJobService
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 * Created by Nikhil on 2/3/2016.
 *
 * A custom Utilities class for RadioControl
 *
 * @author Nikhil Paranjape
 *
 *
 */
class Utilities {

    companion object {
        /**
         * Check if there is any active call
         *
         * @param context allows access to application-specific resources and classes
         * @return true if the device is in an active call
         */
        fun isCallActive(context: Context): Boolean {
            val manager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            return manager.mode == AudioManager.MODE_IN_CALL //Checks if android's audiomanager is in curently in an active call
        }

        /**
         * gets network ssid
         * @version 1.0
         * @param context allows access to application-specific resources and classes
         * @return the current ssid the device is connected to as a string
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
                // TODO Check if this can use local isConnectedWifi method to reduce networkInfo/ConnectivityMananger usage
            }
            return ssid
        }
        /**
         * Gets the current network SSID
         *
         * After Android 8.0+ You now need the COARSE_LOCATION of the device to run these commands
         *
         * @version 2.0-alpha01
         * @param context allows access to application-specific resources and classes
         * @return the current ssid the device is connected to as a string
         **/
        fun getCurrentSSID(context: Context): String{
            var ssid: String? = null
            val mWifiManager =
                (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            val info = mWifiManager.connectionInfo
            return info.ssid
        }
        /**
         * Return the status of the cellular radio
         * @param context allows access to application-specific resources and classes
         * @return int  A value between 0-3.
         *              0 and 1 are good values,
         *              returning a 2 or 3 means there is some kind of error
         */
        fun getCellStatus(context: Context): Int {
            var z = 0 // Initially, we forcibly assume the cell radio is connected
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            /**
             * Old variables that are now deprecated
                    //val connMgr = c.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                            - This used to be the original connectivity manager, until that functionality was moved out of the utility
                            - This removal allows the Utilities class to remain a relatively simple method
                    //var isWifiConn: Boolean = false
                            - Was used with connMgr
                    //var isMobileConn: Boolean = false
                            - Was used with connMgr
            **/
            try{
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                    val cellInfoList = tm.allCellInfo

                    Log.d("Radiocontrol-Util","Cell list: $cellInfoList")
                    //This means cell is off
                    if (cellInfoList.isEmpty()) {
                        z = 1
                    }
                }
            } catch (e: SecurityException) {
                Log.e("RadioControl-util", "Unable to get Location Permission", e)
                return 2
            } catch (e: NullPointerException) {
                Log.e("RadioControl-util", "NullPointer: ", e)
                return 3
            }

            return z //Any value above 1 means some kind of error
        }



        //@Throws(java.lang.Exception::class)
        private fun getTransactionCode(context: Context): String {
            return try {
                val mTelephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val mTelephonyClass = Class.forName(mTelephonyManager.javaClass.name)
                val mTelephonyMethod: Method = mTelephonyClass.getDeclaredMethod("getITelephony")
                mTelephonyMethod.isAccessible = true
                val mTelephonyStub: Any = mTelephonyMethod.invoke(mTelephonyManager)
                val mTelephonyStubClass = Class.forName(mTelephonyStub.javaClass.name)
                val mClass = mTelephonyStubClass.declaringClass
                val field: Field = mClass!!.getDeclaredField("TRANSACTION_setDataEnabled")
                field.isAccessible = true
                java.lang.String.valueOf(field.getInt(null))
            } catch (e: java.lang.Exception) {
                // The "TRANSACTION_setDataEnabled" field is not available,
                // or named differently in the current API level, so we throw
                // an exception and inform users that the method is not available.
                throw e
            }
        }
        internal fun enableNetworks(context: Context, prefs: SharedPreferences){
            // Ensures that Airplane mode is on, or that the cell radio is off
            if (isAirplaneMode(context) || !isConnectedMobile(context)) {

                //Runs the alt cellular mode, otherwise, run the standard airplane mode
                if (prefs.getBoolean("altRootCommand", false)) {
                    if (getCellStatus(context) == 1) {
                        val output = Shell.cmd("service call phone 27").exec()
                        writeLog("root accessed: $output", context)
                        Log.d(BackgroundJobService.TAG, BackgroundJobService.CELL_RADIO_ON)
                        writeLog(BackgroundJobService.CELL_RADIO_ON, context)
                    }
                } else {
                    val output = Shell.cmd("settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false").exec()
                    writeLog("root accessed: $output", context)
                    //RootAccess.runCommands(airOffCmd3) *Here for legacy purposes
                    Log.d(BackgroundJobService.TAG, BackgroundJobService.AIRPLANE_MODE_OFF)
                    writeLog(BackgroundJobService.AIRPLANE_MODE_OFF, context)
                }
            }
        }
        internal fun disableNetworks(prefs: SharedPreferences, context: Context){
            //Runs the cellular mode, otherwise, run default airplane mode
            if (prefs.getBoolean("altRootCommand", false)) {

                when {
                    getCellStatus(context) == 0 -> {
                        val output = Shell.cmd("service call phone 27").exec()
                        writeLog("root accessed: $output", context)
                        Log.d(BackgroundJobService.TAG, BackgroundJobService.CELL_RADIO_OFF)
                        writeLog(BackgroundJobService.CELL_RADIO_OFF, context)
                    }
                    getCellStatus(context) == 1 -> {
                        Log.d(BackgroundJobService.TAG, "Cell Radio is already off")
                    }
                    getCellStatus(context) == 2 -> {
                        Log.e(BackgroundJobService.TAG, "Location can't be accessed, try alt method")
                        setMobileNetworkFromLollipop(context)
                    }
                }

            } else {
                val output = Shell.cmd("settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true").exec()
                writeLog("root accessed: $output", context)
                //RootAccess.runCommands(airCmd)
                Log.d(BackgroundJobService.TAG, BackgroundJobService.AIRPLANE_MODE_ON)
                writeLog(BackgroundJobService.AIRPLANE_MODE_ON, context)
            }
        }

        //An old function used for checking of we are on lollipop and whether mobile data can be turned on/off
        private fun isMobileDataEnabledFromLollipop(context: Context): Boolean {
            return Settings.Global.getInt(context.contentResolver, "mobile_data", 0) == 1
        }

        /**
         * Runs a root level command to control the mobile network on device
         *
         * This method always requires transactionCodes that are device specific from getTransactionCode()
         *
         * @param context allows access to application-specific resources and classes
         */
        @Throws(Exception::class)
        fun setMobileNetworkFromLollipop(context: Context) {
            var command: String? = null
            var state = 0
            try {
                // Set the current state of the mobile network.
                state = Settings.Global.getInt(context.contentResolver, "mobile_data", 0)
                // Get the value of the "TRANSACTION_setDataEnabled" field.
                val transactionCode: String = getTransactionCode(context)
                // Android 5.1+ (API 22) and later.
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    val mSubscriptionManager: SubscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                    // Loop through the subscription list i.e. SIM list.
                    for (i in 0 until mSubscriptionManager.activeSubscriptionInfoCountMax) {
                        if (transactionCode.isNotEmpty()) {
                            // Get the active subscription ID for a given SIM card.
                            val subscriptionId: Int = mSubscriptionManager.activeSubscriptionInfoList[i].subscriptionId
                            // Execute the command via `su` to turn off
                            // mobile network for a subscription service.
                            command = "service call phone $transactionCode i32 $subscriptionId i32 $state"
                            val output = Shell.su(command).exec().out
                        }
                    }
                } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
                    // Android 5.0 (API 21) only.
                    if (transactionCode.isNotEmpty()) {
                        // Execute the command via `su` to turn off mobile network.
                        command = "service call phone $transactionCode i32 $state"
                        val output = Shell.su(command).exec().out
                    }
                }
            } catch (e: Exception) {
                // Oops! Something went wrong, so we throw the exception here.
                Log.e("RadioControl-util", "An unknown error occurred", e)
            } catch (e: SecurityException) {
                Log.e("RadioControl-util", "Unable to get Phone State Permission", e)

            } catch (e: NullPointerException) {
                Log.e("RadioControl-util", "NullPointer: ", e)
            }
        }

        /**
         * Checks link speed
         * @param c allows access to application-specific resources and classes
         * @return linkSpeed of the current wifi network in mbps
         */
        fun linkSpeed(c: Context): Int {
            val wifiManager = c.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val linkSpeed = wifiManager.connectionInfo.linkSpeed
            Log.d("RadioControl-util", "Link speed = " + linkSpeed + "Mbps")
            return linkSpeed
        }

        /**
         * Write a private log for the Statistics Activity
         *
         * Sets the date in yyyy-MM-dd HH:mm:ss format
         *
         * This method always requires appropriate context
         *
         * @param data The data to be written to the log file radiocontrol.log
         * @param c allows access to application-specific resources and classes
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
                    Log.e("RadioControl-util", "Error writing log")
                }
            }
        }
        /**
         * Function to write data to the log. It will also execute functions defined by Log.* (DEBUG,INFO,VERBOSE,etc)
         *
         * @param data      This is the data that needs to be written to the log
         * @param c         Allows access to application-specific resources and classes
         * @param t         The type of Log level requested
         */
        fun writeLog(tag: String, data: String, c: Context, t: Int) {
            println("$tag : $t  : $data")
            val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(c)
            if (preferences.getBoolean("enableLogs", false)) {
                try {
                    val h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString()
                    val log = File(c.filesDir, "radiocontrol.log")
                    if (!log.exists()) { //If the log don't exist
                        log.createNewFile()
                    }
                    val logPath = "radiocontrol.log" //TODO Change this to grab what the user desires
                    val loggedData = "\n$h: TYPE: $t: DATA: $data"

                    val fos = c.openFileOutput(logPath, Context.MODE_APPEND) // Appends the log data
                    fos.write(loggedData.toByteArray())                      // writes the log data to the log file above
                    fos.close()                                              // Closes the file buffer
                } catch (e: IOException) {
                    Log.e("RadioControl-UTIL", "Error writing log")
                }
            }
        }
        /**
         * Schedule the start of the BackgroundJobService every 10 - 30 seconds
         * This ensures that the device is properly connected
         *
         * @param context allows access to application-specific resources and classes
         */
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
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // set required to any network

            builder.build()
            //mJobScheduler.schedule(builder.build())

            //(getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
        }


        /**
         * Gets and returns the frequency of the connected WiFi network (eg. 2.4 or 5 GHz)
         *
         * @param c allows access to application-specific resources and classes
         *
         * @return signal frequency of WiFi network (2.4GHz or 5GHz)
         */
        fun frequency(c: Context): Int {
            val wifiManager = c.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val freq = wifiManager.connectionInfo.frequency
            return when (freq / 1000) {
                2 -> {
                    Log.d("RadioControl-util", "Frequency = " + freq + "MHz")
                    2
                }
                5 -> {
                    Log.d("RadioControl-util", "Frequency = " + freq + "MHz")
                    5
                }
                else -> 0
            }

        }

        @SuppressLint("ByteOrderMark")
        /**
         * Makes a basic network alert notification
         *
         * @param context allows access to application-specific resources and classes
         * @param mes The main body of the notification
         * @param vibrate Sets if the notification will vibrate
         * @param sound Sets if the notification will make a sound
         * @param heads Should the notification appear in a floating window
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
                        .setPriority(3)
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
         * Get the currently active network info
         * @param context allows access to application-specific resources and classes
         * @return ActiveNetwork info
         */
        private fun getNetworkInfo(context: Context): NetworkInfo? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo
        }

        /**
         * Check if there is any connectivity via WiFi
         *
         * @param context allows access to application-specific resources and classes
         * @return true if connected to a WiFi network. False otherwise
         */
        fun isConnectedWifi(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting && info.type == ConnectivityManager.TYPE_WIFI
        }
        /**
         * Check if the WiFi module is enabled.
         *
         * @param context allows access to application-specific resources and classes
         * @return True if WiFi is enabled, but not connected. False if otherwise
         */
        fun isWifiOn(context: Context): Boolean {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            return wifiManager.isWifiEnabled
        }

        /**
         * Check if there is any connectivity at all(local or internet)
         *
         * @param context allows access to application-specific resources and classes
         * @return true if connected to any network
         */
        fun isConnected(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting
        }

        /**
         * Check if there is any connectivity to a mobile network
         * @param context
         * @return return true if the device is connected to a cellular network
         */
        fun isConnectedMobile(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting && info.type == ConnectivityManager.TYPE_MOBILE
        }

        /**
         * Check if there is fast connectivity
         * @param context
         * @return true if the connection is fast wifi/mobile
         */
        fun isConnectedFast(context: Context): Boolean {
            val info = getNetworkInfo(context)
            return info != null && info.isConnectedOrConnecting && isConnectionFast(info.type, info.subtype)
        }
        /**
         * Return whether airplane mode is on or off
         * @param context allows access to application-specific resources and classes
         * @return true if airplane mode is enabled, false if otherwise
         */
        fun isAirplaneMode(context: Context): Boolean {
            return Settings.Global.getInt(context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0
        }

        /**
         * Check if the connection is fast
         * @param type Type of the current network (WiFi/Mobile)
         * @param subType A subtype of mobile networks (eg. CDMA, HSPA, UMTS, LTE)
         * @return true if the connection is faster than 1 Mbps(Or Better than HSPA)
         * TODO Use transport types: https://developer.android.com/reference/kotlin/android/net/NetworkCapabilities
         */
        private fun isConnectionFast(type: Int, subType: Int): Boolean {
            return when (type) {
                ConnectivityManager.TYPE_WIFI -> true //Assume WiFi is fast. TODO possible check for fast internet (but that may take too much CPU time to be efficient)
                ConnectivityManager.TYPE_MOBILE -> when (subType) {
                    TelephonyManager.NETWORK_TYPE_1xRTT -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_CDMA -> false // ~ 14-64 kbps
                    TelephonyManager.NETWORK_TYPE_EDGE -> false // ~ 50-100 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_0 -> false // ~ 400-1000 kbps
                    TelephonyManager.NETWORK_TYPE_EVDO_A -> false // ~ 600-1400 kbps
                    TelephonyManager.NETWORK_TYPE_GPRS -> false // ~ 100 kbps
                    TelephonyManager.NETWORK_TYPE_HSDPA -> true // ~ 2-14 Mbps
                    TelephonyManager.NETWORK_TYPE_HSPA -> false // ~ 700-1700 kbps
                    TelephonyManager.NETWORK_TYPE_HSUPA -> true // ~ 1-23 Mbps
                    TelephonyManager.NETWORK_TYPE_UMTS -> false // ~ 400-7000 kbps
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
