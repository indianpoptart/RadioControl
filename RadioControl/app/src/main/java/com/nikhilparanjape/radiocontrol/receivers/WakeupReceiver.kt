package com.nikhilparanjape.radiocontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers

/**
 * Created by Nikhil Paranjape on 12/16/2015.
 *
 * @author Nikhil Paranjape
 *
 *
 */

class WakeupReceiver : BroadcastReceiver() {
    private var alarmUtil = AlarmSchedulers()

    override fun onReceive(context: Context, intent: Intent) {
        alarmUtil.scheduleAlarm(context)

        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val editor = pref.edit()
        editor.putBoolean("isNoDisturbEnabled", false)
        editor.apply()
    }
    companion object {

        const val REQUEST_CODE = 12345
    }
}
