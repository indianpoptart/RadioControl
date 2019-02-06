package com.nikhilparanjape.radiocontrol.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import android.net.ConnectivityManager
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.net.NetworkRequest
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.NetworkInfo
import android.preference.PreferenceManager
import android.text.format.DateFormat
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import com.topjohnwu.superuser.Shell
import org.jetbrains.anko.doAsync
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.util.HashSet

/**
 * This service starts the BackgroundAirplaneService as a foreground service if on Android Oreo or higher.
 *
 *
 *
 * @author Nikhil Paranjape
 */
class BackgroundJobService : JobService(), ConnectivityReceiver.ConnectivityReceiverListener  {

    internal var util = Utilities() //Network and other related utilities
    private var alarmUtil = AlarmSchedulers()

    override fun onCreate(){
        super.onCreate()
        Log.i(TAG, "JobScheduler created")
    }


    override fun onStartJob(params: JobParameters): Boolean {
        //Utilities.scheduleJob(applicationContext) // reschedule the job

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {

                // -Snip-
            })
        } else {
        }

        val activeNetwork = connectivityManager.activeNetworkInfo
        Log.d("RadioControl-Job", "Active: $activeNetwork")

        val context = applicationContext
        val sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val disabledPref = context.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)

        val h = HashSet(listOf("")) //Set default set for SSID check
        val selections = prefs.getStringSet("ssid", h) //Gets stringset, if empty sets default
        val networkAlert = prefs.getBoolean("isNetworkAlive", false)
        prefs.getBoolean("isBatteryOn", true)

        //Log.i("RadioControl-Job,"Battery Optimized");
        //Check if user wants the app on
        if (sp.getInt("isActive", 0) == 0) {
            Log.d("RadioControl-Job", "RadioControl has been disabled-job")
            if (networkAlert) {
                pingTask(context)
            }
            //Adds wifi signal lost log for nonrooters
            if (!Utilities.isConnectedWifi(context)) {
                Log.d("RadioControl-Job", "WiFi signal LOST")
                writeLog("WiFi Signal lost", context)
            }
        }
        if (sp.getInt("isActive", 0) == 1) {
            //Check if we just lost WiFi signal
            if (!Utilities.isConnectedWifi(context) && activeNetwork == null) {
                Log.d("RadioControl-Job", "WiFi signal LOST")
                writeLog("WiFi Signal lost", context)
                if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                    Log.d("RadioControl-Job", "1")
                    //Checks that user is not in call
                    if (!util.isCallActive(context)) {
                        //Runs the alternate root command
                        if (prefs.getBoolean("altRootCommand", true)) {
                            if (Utilities.getCellStatus(context) == 1) {
                                val output = Shell.su("service call phone 27").exec().out
                                Utilities.writeLog("root accessed: $output", applicationContext)
                                Log.d("RadioControl-Job", "Cell Radio has been turned on")
                                writeLog("Cell radio has been turned on", context)
                            }
                        } else {
                            if (prefs.getBoolean("altBTCommand", false)) {
                                val output = Shell.su("settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false").exec().out
                                Utilities.writeLog("root accessed: $output", context)
                                //RootAccess.runCommands(airOffCmd3)
                                Log.d("RadioControl-Job", "Airplane mode has been turned off(with bt cmd)")
                                writeLog("Airplane mode has been turned off", context)
                            } else {
                                val output = Shell.su("settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\"").exec().out
                                Utilities.writeLog("root accessed: $output", context)
                                //RootAccess.runCommands(airOffCmd2)
                                Log.d("RadioControl-Job", "Airplane mode has been turned off")
                                writeLog("Airplane mode has been turned off", context)
                            }

                        }
                    } else if (util.isCallActive(context)) {
                        while (util.isCallActive(context)) {
                            waitFor(1000)//Wait for call to end
                            Log.d("RadioControl-Job", "waiting for call to end")
                        }
                        //Utilities.scheduleJob(context)
                    }//Checks that user is currently in call and pauses execution till the call ends
                } else {
                    Log.d("RadioControl-Job", "2")
                }
            } else if (Utilities.isConnectedWifi(context) && !Utilities.isAirplaneMode(context)) {
                //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
                //Check the list of disabled networks
                if (!disabledPref.contains(Utilities.getCurrentSsid(context))) {
                    Log.d("RadioControl-Job", "The current SSID was not found in the disabled list")
                    //Checks that user is not in call
                    if (!util.isCallActive(context)) {
                        //Checks if the user doesn't want network alerts
                        if (!networkAlert) {
                            //Runs the alternate root command
                            if (prefs.getBoolean("altRootCommand", false)) {

                                if (Utilities.getCellStatus(context) == 0) {
                                    val output = Shell.su("service call phone 27").exec().out
                                    Utilities.writeLog("root accessed: $output", applicationContext)
                                    Log.d("RadioControl-Job", "Cell Radio has been turned off")
                                    writeLog("Cell radio has been turned off", context)
                                } else if (Utilities.getCellStatus(context) == 1) {
                                    Log.d("RadioControl-Job", "Cell Radio is already off")
                                }

                            } else {
                                val output = Shell.su("settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true").exec().out
                                Utilities.writeLog("root accessed: $output", context)
                                //RootAccess.runCommands(airCmd)
                                Log.d("RadioControl-Job", "Airplane mode has been turned on")
                                writeLog("Airplane mode has been turned on", context)
                            }

                        } else {
                            pingTask(context)
                        }//The user does want network alert notifications

                    } else if (util.isCallActive(context)) {
                        while (util.isCallActive(context)) {
                            waitFor(1000)//Wait for call to end
                            Log.d("RadioControl-Job", "waiting for call to end")
                        }
                    }//Checks that user is currently in call and pauses execution till the call ends
                } else if (selections!!.contains(Utilities.getCurrentSsid(context))) {
                    Log.d("RadioControl-Job", "The current SSID was blocked from list $selections")
                    writeLog("The current SSID was blocked from list $selections", context)
                }//Pauses because WiFi network is in the list of disabled SSIDs
            } else if (activeNetwork.detailedState.equals("z")){
                Log.d("Rad","Test: $activeNetwork.detailedState" )
            }

        }

        return true
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
                Log.d("RadioControl-Job", "There was an error saving the log: $e")
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
                Log.d("RadioControl-Job", "Reachable?: $reachable")

                val sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)

                val alertPriority = prefs.getBoolean("networkPriority", false)//Setting for network notifier
                val alertSounds = prefs.getBoolean("networkSound", false)
                val alertVibrate = prefs.getBoolean("networkVibrate", false)


                if (sp.getInt("isActive", 0) == 0) {
                    //If the connection can't reach Google
                    if (!reachable) {
                        Utilities.sendNote(context, context.getString(com.nikhilparanjape.radiocontrol.R.string.not_connected_alert), alertVibrate, alertSounds, alertPriority)
                        writeLog("Not connected to the internet", context)
                    }
                } else if (sp.getInt("isActive", 0) == 1) {
                    //If the connection can't reach Google
                    if (!reachable) {
                        Utilities.sendNote(context, context.getString(com.nikhilparanjape.radiocontrol.R.string.not_connected_alert), alertVibrate, alertSounds, alertPriority)
                        writeLog("Not connected to the internet", context)
                    } else {
                        //Runs the alternate root command
                        if (prefs.getBoolean("altRootCommand", false)) {
                            val output = Shell.su("service call phone 27").exec().out
                            Utilities.writeLog("root accessed: $output", context)
                            alarmUtil.scheduleRootAlarm(context)
                            Log.d("RadioControl-Job", "Cell Radio has been turned off")
                            writeLog("Cell radio has been turned off", context)
                        } else if (!prefs.getBoolean("altRootCommand", false)) {
                            val output = Shell.su("settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true").exec().out
                            Utilities.writeLog("root accessed: $output", context)
                            //RootAccess.runCommands(airCmd)
                            Log.d("RadioControl-Job", "Airplane mode has been turned on")
                            writeLog("Airplane mode has been turned on", context)
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

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    companion object {

        private const val TAG = "SyncService"
        private const val PRIVATE_PREF = "prefs"
    }

}
