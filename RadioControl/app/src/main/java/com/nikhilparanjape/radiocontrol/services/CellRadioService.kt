package com.nikhilparanjape.radiocontrol.services

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.topjohnwu.superuser.Shell

/**
 * Created by admin on 09/23/2016.
 *
 * @author Nikhil Paranjape
 *
 * @description This class handles toggling the cell radio using a shell command.
 */
class CellRadioService : IntentService("CellRadioService") {
    private var mRunning: Boolean = false

    override fun onCreate() {
        // The service is being created
        super.onCreate()
        mRunning = false
    }
    override fun onBind(intent: Intent): IBinder? {
        // A client is binding to the service with bindService()
        return null
    }
    override fun onHandleIntent(intent: Intent?) {
        if (!mRunning) {
            mRunning = true
            Log.d("RadioControl-cell", "CellService Toggled")
            //val cellOffCmd = arrayOf("service call phone 27") *not needed as it can run on libsu shell
            // Run commands and get output immediately
            val output = Shell.su("service call phone 27").exec().out
            Utilities.writeLog("root accessed: $output", applicationContext)
            Log.d("RadioControl-cell", "CellService Killed")
            this.stopSelf()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        mRunning = false
    }
}