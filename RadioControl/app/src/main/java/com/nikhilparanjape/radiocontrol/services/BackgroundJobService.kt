package com.nikhilparanjape.radiocontrol.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.text.format.DateFormat
import android.util.Log
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.getCellStatus
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.getCurrentSsid
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.isAirplaneMode
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.isCallActive
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.isConnected
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.isConnectedMobile
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.isConnectedWifi
import com.nikhilparanjape.radiocontrol.utilities.Utilities.Companion.setMobileNetworkFromLollipop
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.IOException
import java.net.InetAddress

/**
 * This service starts the BackgroundAirplaneService as a foreground service if on Android Oreo or higher.
 *
 *
 * @author Nikhil Paranjape
 */
class BackgroundJobService : JobService(), ConnectivityReceiver.ConnectivityReceiverListener {

    //internal var util = Utilities() //Network and other related utilities
    private var alarmUtil = AlarmSchedulers()

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "BackgroundJobScheduler created")
    }

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "Job started")
        //Utilities.scheduleJob(applicationContext) // reschedule the job

        //val sp = applicationContext.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val disabledPref = applicationContext.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)

        //TODO Transfer to external algorithm class
        //val h = HashSet(listOf("")) //Set default empty set for SSID check
        val selections = prefs.getStringSet("ssid", HashSet(listOf(""))) //Gets stringset, if empty sets default
        val networkAlert = prefs.getBoolean("isNetworkAlive", false)

        //Check if user wants the app on
        if (prefs.getInt("isActive", 0) == 0) {
            Log.d(TAG, "RadioControl has been disabled")
            //If the user wants to be alerted when the network is not internet reachable
            if (networkAlert) {
                pingTask()
                jobFinished(params, false)
            }
            //Adds wifi signal lost log for devices that aren't rooted
            if (!isConnectedWifi(applicationContext)) {
                Log.d(TAG, "WiFi signal LOST")
                writeLog("WiFi Signal lost", applicationContext)
                jobFinished(params, false)
            }
        } else if (prefs.getInt("isActive", 0) == 1) {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {})
            val activeNetwork = connectivityManager.activeNetworkInfo //This is used to check if the mobile network is currently off/disabled

            Log.d(TAG, "Connected?: $activeNetwork")

            Log.d(TAG, "Main Program Begin")

            //Check if there is no WiFi connection && the Cell network is still not active
            if (!isConnectedWifi(applicationContext) && activeNetwork == null) {
                Log.d(TAG, "WiFi signal LOST")
                writeLog("WiFi Signal lost", applicationContext)

                // Ensures that Airplane mode is on, or that the cell radio is off
                if (isAirplaneMode(applicationContext) || !isConnectedMobile(applicationContext)) {

                    //Continue iff the device is not in an active call
                    if (!isCallActive(applicationContext)) {

                        //Runs the alt cellular mode, otherwise, run the standard airplane mode
                        if (prefs.getBoolean("altRootCommand", false)) {
                            if (getCellStatus(applicationContext) == 1) {
                                val output = Shell.su("service call phone 27").exec().out
                                writeLog("root accessed: $output", applicationContext)
                                Log.d(TAG, "Cell Radio has been turned on")
                                writeLog("Cell radio has been turned on", applicationContext)
                                jobFinished(params, false)
                            }
                        } else {
                            val output = Shell.su("settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false").exec().out
                            writeLog("root accessed: $output", applicationContext)
                            //RootAccess.runCommands(airOffCmd3)
                            Log.d(TAG, "Airplane mode has been turned off")
                            writeLog("Airplane mode has been turned off", applicationContext)
                            jobFinished(params, false)
                        }
                    }
                    //Waits for the active call to finish before initiating
                    else if (isCallActive(applicationContext)) {
                        while (isCallActive(applicationContext)) {
                            waitFor(1000)//Wait every second for call to end
                            Log.d(TAG, "waiting for call to end")
                        }
                    }
                }
            //Here we check if the device just connected to a wifi network, as well as if airplane mode and/or the cell radio are off and/or connected respectively
            } else if (isConnectedWifi(applicationContext) && !isAirplaneMode(applicationContext)) {
                Log.d(TAG, "WiFi signal got")

                //Checks if the currently connected SSID is in the list of disabled networks
                if (!disabledPref.contains(getCurrentSsid(applicationContext))) {
                    Log.d(TAG, "The current SSID was not found in the disabled list")

                    //Checks that user is not in call
                    if (!isCallActive(applicationContext)) {

                        //Checks if the user doesn't want network alerts
                        if (!networkAlert) {

                            //Runs the cellular mode, otherwise, run default airplane mode
                            if (prefs.getBoolean("altRootCommand", false)) {

                                when {
                                    getCellStatus(applicationContext) == 0 -> {
                                        val output = Shell.su("service call phone 27").exec().out
                                        writeLog("root accessed: $output", applicationContext)
                                        Log.d(TAG, "Cell Radio has been turned off")
                                        writeLog("Cell radio has been turned off", applicationContext)
                                        jobFinished(params, false)
                                    }
                                    getCellStatus(applicationContext) == 1 -> {
                                        Log.d(TAG, "Cell Radio is already off")
                                        jobFinished(params, false)
                                    }
                                    getCellStatus(applicationContext) == 2 -> {
                                        Log.e(TAG, "Location can't be accessed, try alt method")
                                        setMobileNetworkFromLollipop(applicationContext)
                                    }
                                }

                            } else {
                                val output = Shell.su("settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true").exec().out
                                writeLog("root accessed: $output", applicationContext)
                                //RootAccess.runCommands(airCmd)
                                Log.d(TAG, "Airplane mode has been turned on")
                                writeLog("Airplane mode has been turned on", applicationContext)
                                jobFinished(params, false)
                            }
                        } else {
                            pingTask()
                            jobFinished(params, false)
                        }//The user does want network alert notifications

                    } else if (isCallActive(applicationContext)) {
                        Log.d(TAG, "Still on the phone")
                        jobFinished(params, true)
                        /*while (isCallActive(applicationContext)) {
                            waitFor(1000)//Wait for call to end
                            Log.d(_root_ide_package_.com.nikhilparanjape.radiocontrol.services.BackgroundJobService.Companion.TAG, "waiting for call to end")
                        }*/
                    }//Checks that user is currently in call and pauses execution till the call ends
                } else if (selections!!.contains(getCurrentSsid(applicationContext))) {
                    Log.d(TAG, "The current SSID was blocked from list $selections")
                    writeLog("The current SSID was blocked from list $selections", applicationContext)
                    jobFinished(params, false)
                }//Pauses because WiFi network is in the list of disabled SSIDs
            }
            //Handle any other event not covered above, usually something is not right, or we lack some permission
            else {
                if (activeNetwork == null) {
                    Log.d(TAG, "Yeah, we connected")
                } else {
                    //So activeNetwork has to have some value, lets see what that is
                    Log.e(TAG, "EGADS: $activeNetwork")
                }
                jobFinished(params, false)
            }

        } else {
            Log.e(TAG, "Something's wrong, I can feel it")
            jobFinished(params, false)
        }
        Log.i(TAG, "Job completed")

        return true
    }

    private fun writeLog(data: String, c: Context) {
        val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(c)
        if (preferences.getBoolean("enableLogs", false)) {
            try {
                val h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString()
                val log = File(c.filesDir, "radiocontrol.log")
                if (!log.exists()) {
                    log.createNewFile() // Create a log file if it does not exist
                }
                val logPath = "radiocontrol.log"
                val string = "\n$h: $data"

                val fos = c.openFileOutput(logPath, Context.MODE_APPEND)
                fos.write(string.toByteArray()) // Writes the current string to a bytearray into the path radiocontrol.log
                fos.close()
            } catch (e: IOException) {
                Log.d(TAG, "There was an error saving the log: $e")
            }
        }
    }

    private fun pingTask() {
        try {
            //Wait for network to be connected fully
            while (!isConnected(applicationContext)) {
                Thread.sleep(1000)
            }
            val address = InetAddress.getByName("1.1.1.1")
            val reachable = address.isReachable(4000)
            Log.d(TAG, "Reachable?: $reachable")

            //val sp = applicationContext.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)

            val alertPriority = prefs.getBoolean("networkPriority", false)//Setting for network notifier
            val alertSounds = prefs.getBoolean("networkSound", false)
            val alertVibrate = prefs.getBoolean("networkVibrate", false)


            if (prefs.getInt("isActive", 0) == 0) {
                //If the connection can't reach Google
                if (!reachable) {
                    Utilities.sendNote(applicationContext, applicationContext.getString(com.nikhilparanjape.radiocontrol.R.string.not_connected_alert), alertVibrate, alertSounds, alertPriority)
                    writeLog("Not connected to the internet", applicationContext)
                }
            } else if (prefs.getInt("isActive", 0) == 1) {
                //If the connection can't reach Google
                if (!reachable) {
                    Utilities.sendNote(applicationContext, applicationContext.getString(com.nikhilparanjape.radiocontrol.R.string.not_connected_alert), alertVibrate, alertSounds, alertPriority)
                    writeLog("Not connected to the internet", applicationContext)
                } else {
                    //Runs the cellular mode
                    if (prefs.getBoolean("altRootCommand", false)) {
                        val output = Shell.su("service call phone 27").exec().out
                        Utilities.writeLog("root accessed: $output", applicationContext)
                        alarmUtil.scheduleRootAlarm(applicationContext)
                        Log.d(TAG, "Cell Radio has been turned off")
                        writeLog("Cell radio has been turned off", applicationContext)
                    } else if (!prefs.getBoolean("altRootCommand", false)) {
                        val output = Shell.su("settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true").exec().out
                        Utilities.writeLog("root accessed: $output", applicationContext)
                        //RootAccess.runCommands(airCmd)
                        Log.d(TAG, "Airplane mode has been turned on")
                        writeLog("Airplane mode has been turned on", applicationContext)
                    }
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
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
        private const val TAG = "RadioControl-JobSrv"
        /*private const val PRIVATE_PREF = "prefs"*/


    }

}
