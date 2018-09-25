package com.nikhilparanjape.radiocontrol.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities
import androidx.core.content.ContextCompat.startForegroundService
import android.os.Build
import androidx.annotation.RequiresApi


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

        private val TAG = "SyncService"
    }
}
