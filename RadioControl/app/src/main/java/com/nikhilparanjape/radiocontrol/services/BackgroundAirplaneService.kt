package com.nikhilparanjape.radiocontrol.services

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.preference.PreferenceManager
import android.text.format.DateFormat
import android.util.Log
import androidx.core.app.NotificationCompat
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.utilities.RootAccess
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.*


/**
 * Created by admin on 8/20/2016.
 *
 * @author Nikhil Paranjape
 */
class BackgroundAirplaneService : IntentService("BackgroundAirplaneService") {

    //Root commands which disable cell only
    private var airCmd = arrayOf("su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
    //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
    private var airOffCmd2 = arrayOf("su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"")
    private var airOffCmd3 = arrayOf("su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")

    internal var util = Utilities() //Network and other related utilities
    internal var alarmUtil = AlarmSchedulers()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createBackgroundNotification("Airplane Service", "Background Process Running...")
        Log.d("RadioControl-background", "Notified")

    }

    private fun createBackgroundNotification(title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val builder = Notification.Builder(this, "airplane")
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_info_white_24dp)
                    .setContentText(message)
                    .setAutoCancel(true)

            val notification = builder.build()
            startForeground(1, notification)

        } else {

            @Suppress("DEPRECATION") //For backwards compatibility
            val builder = NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setSmallIcon(R.drawable.ic_info_white_24dp)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)

            val notification = builder.build()

            startForeground(1, notification)
        }


    }
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Airplane Service"
            val description = getString(R.string.summary_airplane_service_notification)
            val importance = NotificationManager.IMPORTANCE_NONE
            val channel = NotificationChannel("airplane", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            Log.d("RadioControl", "Get action-bas: " + intent.action)
        }
        // Start Alarm Task
        Log.d("RadioControl", "Background Service running")
        val context = applicationContext
        val sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val disabledPref = context.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)

        val h = HashSet(listOf("")) //Set default set for SSID check
        val selections = prefs.getStringSet("ssid", h) //Gets stringset, if empty sets default
        val networkAlert = prefs.getBoolean("isNetworkAlive", false)
        prefs.getBoolean("isBatteryOn", true)

        //Log.i("RadioControl","Battery Optimized");
        //Check if user wants the app on
        if (sp.getInt("isActive", 0) == 1) {
            //Check if we just lost WiFi signal
            if (!Utilities.isConnectedWifi(context)) {
                Log.d("RadioControl", "WiFi signal LOST")
                writeLog("WiFi Signal lost", context)
                if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                    //Checks that user is not in call
                    if (!util.isCallActive(context)) {
                        //Runs the alternate root command
                        if (prefs.getBoolean("altRootCommand", true)) {
                            if (Utilities.getCellStatus(context) == 1) {
                                val cellIntent = Intent(context, CellRadioService::class.java)
                                context.startService(cellIntent)
                                Log.d("RadioControl", "Cell Radio has been turned on")
                                writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context)!!, context)
                            }
                        } else {
                            if (prefs.getBoolean("altBTCommand", false)) {
                                RootAccess.runCommands(airOffCmd3)
                                Log.d("RadioControl", "Airplane mode has been turned off(with bt cmd)")
                                writeLog("Airplane mode has been turned off", context)
                            } else {
                                RootAccess.runCommands(airOffCmd2)
                                Log.d("RadioControl", "Airplane mode has been turned off")
                                writeLog("Airplane mode has been turned off", context)
                            }

                        }
                    } else if (util.isCallActive(context)) {
                        while (util.isCallActive(context)) {
                            waitFor(1000)//Wait for call to end
                            Log.d("RadioControl", "waiting for call to end")
                        }
                        //Utilities.scheduleJob(context)
                    }//Checks that user is currently in call and pauses execution till the call ends
                }
            }

            //If network is connected and airplane mode is off
            if (Utilities.isConnectedWifi(context) && !Utilities.isAirplaneMode(context)) {
                //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
                //Check the list of disabled networks
                if (!disabledPref.contains(Utilities.getCurrentSsid(context))) {
                    Log.d("RadioControl", Utilities.getCurrentSsid(context)!! + " was not found in the disabled list")
                    //Checks that user is not in call
                    if (!util.isCallActive(context)) {
                        //Checks if the user doesn't want network alerts
                        if (!networkAlert) {
                            //Runs the alternate root command
                            if (prefs.getBoolean("altRootCommand", false)) {

                                if (Utilities.getCellStatus(context) == 0) {
                                    val cellIntent = Intent(context, CellRadioService::class.java)
                                    context.startService(cellIntent)
                                    Log.d("RadioControl", "Cell Radio has been turned off")
                                    writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context)!!, context)
                                } else if (Utilities.getCellStatus(context) == 1) {
                                    Log.d("RadioControl", "Cell Radio is already off")
                                }

                            } else {
                                RootAccess.runCommands(airCmd)
                                Log.d("RadioControl", "Airplane mode has been turned on")
                                writeLog("Airplane mode has been turned on, SSID: " + Utilities.getCurrentSsid(context)!!, context)
                            }

                        } else {
                            pingTask(context)
                        }//The user does want network alert notifications

                    } else if (util.isCallActive(context)) {
                        while (util.isCallActive(context)) {
                            waitFor(1000)//Wait for call to end
                            Log.d("RadioControl", "waiting for call to end")
                        }
                    }//Checks that user is currently in call and pauses execution till the call ends
                } else if (selections!!.contains(Utilities.getCurrentSsid(context))) {
                    Log.d("RadioControl", Utilities.getCurrentSsid(context) + " was blocked from list " + selections)
                    writeLog(Utilities.getCurrentSsid(context) + " was blocked from list " + selections, context)
                }//Pauses because WiFi network is in the list of disabled SSIDs
            }

        }
        if (sp.getInt("isActive", 0) == 0) {
            Log.d("RadioControl", "RadioControl has been disabled")
            if (networkAlert) {
                pingTask(context)
            }
            //Adds wifi signal lost log for nonrooters
            if (!Utilities.isConnectedWifi(context)) {
                Log.d("RadioControl", "WiFi signal LOST")
                writeLog("WiFi Signal lost", context)
            }
        }

    }

    private fun writeLog(data: String, c: Context) {
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
                Log.d("RadioControl", "There was an error saving the log: $e")
            }

        }
    }

    private fun pingTask(context: Context) {
        doAsync {
            try {
                //Wait for network to be connected fully
                while (!Utilities.isConnected(context)) {
                    Thread.sleep(1000)
                }
                val address = InetAddress.getByName("1.1.1.1")
                val reachable = address.isReachable(4000)
                Log.d("RadioControl", "Reachable?: $reachable")

                val sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)

                val alertPriority = prefs.getBoolean("networkPriority", false)//Setting for network notifier
                val alertSounds = prefs.getBoolean("networkSound", false)
                val alertVibrate = prefs.getBoolean("networkVibrate", false)


                if (sp.getInt("isActive", 0) == 0) {
                    //If the connection can't reach Google
                    if (!reachable) {
                        Utilities.sendNote(context, context.getString(R.string.not_connected_alert), alertVibrate, alertSounds, alertPriority)
                        writeLog("Not connected to the internet", context)
                    }
                } else if (sp.getInt("isActive", 0) == 1) {
                    //If the connection can't reach Google
                    if (!reachable) {
                        Utilities.sendNote(context, context.getString(R.string.not_connected_alert), alertVibrate, alertSounds, alertPriority)
                        writeLog("Not connected to the internet", context)
                    } else {
                        //Runs the alternate root command
                        if (prefs.getBoolean("altRootCommand", false)) {
                            val cellIntent = Intent(context, CellRadioService::class.java)
                            context.startService(cellIntent)
                            alarmUtil.scheduleRootAlarm(context)
                            Log.d("RadioControl", "Cell Radio has been turned off")
                            writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context)!!, context)
                        } else if (!prefs.getBoolean("altRootCommand", false)) {
                            RootAccess.runCommands(airCmd)
                            Log.d("RadioControl", "Airplane mode has been turned on")
                            writeLog("Airplane mode has been turned on, SSID: " + Utilities.getCurrentSsid(context)!!, context)
                        }
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }

    private fun waitFor(timer: Long) {
        try {
            Thread.sleep(timer)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val airplaneService = preferences.getBoolean("isAirplaneService", false)
        if (airplaneService) {
            alarmUtil.scheduleAlarm(applicationContext)
        }

        Log.d("RadioControl", "Background Service destroyed")
    }

    companion object {

        private const val PRIVATE_PREF = "prefs"
    }

}