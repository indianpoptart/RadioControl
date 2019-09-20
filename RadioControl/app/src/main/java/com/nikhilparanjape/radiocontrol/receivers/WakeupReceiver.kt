package com.nikhilparanjape.radiocontrol.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager

import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers

class WakeupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmUtil = AlarmSchedulers()
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
