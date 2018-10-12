package com.nikhilparanjape.radiocontrol.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.Utilities

/**
 * This service starts the BackgroundAirplaneService as a foreground service if on Android Oreo or higher.
 *
 *
 *
 * @author Nikhil Paranjape
 */
class TestJobService : JobService() {

    override fun onCreate(){
        super.onCreate();
        Log.i(TAG, "JobScheduler created");
    }


    override fun onStartJob(params: JobParameters): Boolean {
        val service = Intent(applicationContext, BackgroundAirplaneService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("RadioControl","Trying background service")
            applicationContext.startForegroundService(service)
        } else {
            applicationContext.startService(service)
        }

        Utilities.scheduleJob(applicationContext) // reschedule the job
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    companion object {

        private const val TAG = "SyncService"
    }
}
