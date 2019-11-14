package com.nikhilparanjape.radiocontrol.receivers

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.nikhilparanjape.radiocontrol.services.BackgroundJobService


@Suppress("DEPRECATION")
/**
 * Created by Nikhil Paranjape on 11/8/2015.
 *
 * This uses some deprecated functions and may stop working :(
 *
 * This file is only here for backwards compatibility
 *
 * @author Nikhil Paranjape
 */

//This file is kept as a fallback
class ConnectivityReceiver : WakefulBroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        Log.d("RadioControl-CR", "Get action-ConRec: " + intent.action!!)

        val componentName = ComponentName(context, BackgroundJobService::class.java)
        val jobInfo = JobInfo.Builder(12, componentName)
                .setRequiresCharging(false)
                .setOverrideDeadline(4000)
                .setPersisted(true)
                .build()

        val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler?
        val resultCode = jobScheduler!!.schedule(jobInfo)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("RadioControl-CR", "Job scheduled!")
        } else {
            Log.d("RadioControl-CR", "Job not scheduled")
        }

    }
    interface ConnectivityReceiverListener
}
