package com.nikhilparanjape.radiocontrol.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Created by admin on 10/12/2018.
 *
 * @author Nikhil Paranjape
 *
 * @description This class is a placeholder for the DnD service that disables RadioControl from doing its thing
 */
class DoNotDisturbService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }
}
