package com.nikhilparanjape.radiocontrol.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log

import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities


/**
 * Created by admin on 9/23/2016.
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

    override fun onHandleIntent(intent: Intent?) {
        if (!mRunning) {
            mRunning = true
            Log.d("RadioControl", "CellService Toggled")
            val cellOffCmd = arrayOf("service call phone 27", "service call phone 14 s16")
            RootAccess.runCommands(cellOffCmd)
            Log.d("RadioControl", "CellService Killed")
            this.stopSelf()
        }
    }


}