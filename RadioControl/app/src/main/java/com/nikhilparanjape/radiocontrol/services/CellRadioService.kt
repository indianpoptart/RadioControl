package com.nikhilparanjape.radiocontrol.services

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.core.app.JobIntentService
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.topjohnwu.superuser.Shell

/**
 * Created by admin on 09/23/2016.
 *
 * @author Nikhil Paranjape
 *
 * @description This class handles toggling the cell radio using a shell command.
 */
class CellRadioService: JobIntentService() {
    /**
     * Unique job ID for this service.
     */
    private val jobID = 1002
    private var mRunning: Boolean = false // This ensures the service doesn't run parallel

    /**
     * Convenience method for enqueuing work in to this service.
     */
    fun enqueueWork(context: Context?, work: Intent?) {
        if (work != null) {
            mRunning = false
            enqueueWork(applicationContext, CellRadioService::class.java, jobID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        Log.i(TAG, "Executing work: $intent")
        if (!mRunning) {
            mRunning = true
            Log.d(TAG, "CellService Started")
            //val cellOffCmd = arrayOf("service call phone 27") *not needed as it can run on libsu shell
            // Run commands and get output immediately
            val output = Shell.su("service call phone 27").exec().out
            Utilities.writeLog("root accessed: $output", applicationContext)
            Log.d(TAG, "CellService Killed")
            this.stopSelf()
        }
        Log.i(TAG, "Completed service @ " + SystemClock.elapsedRealtime())
    }

    override fun onDestroy() {
        super.onDestroy()
        mRunning = false
    }
    companion object {

        private const val TAG = "RadioControl-CRJS"
        /*private const val PRIVATE_PREF = "prefs"*/

    }
}