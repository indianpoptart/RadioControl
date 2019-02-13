package com.nikhilparanjape.radiocontrol.receivers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import android.net.ConnectivityManager
import com.nikhilparanjape.radiocontrol.services.BackgroundJobService
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import android.app.job.JobInfo
import android.content.ComponentName
import android.app.job.JobScheduler
import android.content.Context.JOB_SCHEDULER_SERVICE
import androidx.core.content.ContextCompat.getSystemService





@Suppress("DEPRECATION")
/**
 * Created by Nikhil Paranjape on 11/8/2015.
 *
 * This file will get deprecated soon :( Sad, as it's the backbone of this app
 *
 * No longer the "backbone" testing if it still is
 *
 * @author Nikhil Paranjape
 */

//This file is kept as a fallback
class ConnectivityReceiver : WakefulBroadcastReceiver() {

    private val mConnRecListener = ConnectivityReceiver.Companion

    override fun onReceive(context: Context, intent: Intent) {
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        Log.d("RadioControl", "Get action-ConRec: " + intent.action!!)

        val componentName = ComponentName(context, BackgroundJobService::class.java)
        val jobInfo = JobInfo.Builder(12, componentName)
                .setRequiresCharging(false)
                .setOverrideDeadline(0)
                .build()

        val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler?
        val resultCode = jobScheduler!!.schedule(jobInfo)
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("RadioControl-CR", "Job scheduled!")
        } else {
            Log.d("RadioControl-CR", "Job not scheduled")
        }

    }
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }
    interface ConnectivityReceiverListener

    companion object {
        fun onNetworkConnectionChanged(isConnected: Boolean) {

        }
    }
}
