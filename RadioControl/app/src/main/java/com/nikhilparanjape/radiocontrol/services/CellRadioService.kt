package com.nikhilparanjape.radiocontrol.services

import android.app.IntentService
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.nikhilparanjape.radiocontrol.utilities.RootAccess
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.topjohnwu.superuser.Shell

/**
 * Created by admin on 09/23/2016.
 *
 * @author Nikhil Paranjape
 */
class CellRadioService : IntentService("CellRadioService") {
    private var mRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()
        mRunning = false
    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    override fun onHandleIntent(intent: Intent) {
        if (!mRunning) {
            mRunning = true
            Log.d("RadioControl", "CellService Toggled")
            val cellOffCmd = arrayOf("service call phone 27")
            // Run commands and get output immediately
            val output = Shell.su("service call phone 27").exec().out
            Utilities.writeLog("root accessed: $output", applicationContext)
            Log.d("RadioControl", "CellService Killed")
            this.stopSelf()
        }
    }
}