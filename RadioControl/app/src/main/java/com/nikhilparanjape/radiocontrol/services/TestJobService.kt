package com.nikhilparanjape.radiocontrol.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities

class TestJobService : JobService() {

    override fun onCreate(){
        super.onCreate();
        Log.i(TAG, "JobScheduler created");
    }

    override fun onStartJob(params: JobParameters): Boolean {
        val service = Intent(applicationContext, BackgroundAirplaneService::class.java)
        applicationContext.startService(service)
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
