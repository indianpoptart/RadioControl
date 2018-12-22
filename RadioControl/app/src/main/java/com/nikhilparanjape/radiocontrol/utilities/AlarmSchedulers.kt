package com.nikhilparanjape.radiocontrol.utilities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.nikhilparanjape.radiocontrol.receivers.*
import java.util.*

/**
 * Created by Nikhil on 10/12/2018.
 *
 * A class with a bunch of AlarmScheduler Utilities for RadioControl
 */

class AlarmSchedulers{
    fun scheduleGeneralAlarm (c: Context, schedule: Boolean, hour: Int, minute: Int, intentApp: String, pIntentApp: String){
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR, hour)
        // Construct an intent that will execute the AlarmReceiver based on which class you want
        val intent = when {
            intentApp.contains("Wakeup") -> Intent(c, WakeupReceiver::class.java)
            intentApp.contains("TimedAlarm") -> Intent(c, TimedAlarmReceiver::class.java)
            intentApp.contains("RootService") -> Intent(c, RootServiceReceiver::class.java)
            intentApp.contains("NightMode") -> Intent(c, NightModeReceiver::class.java)
            else -> Intent(c,TimedAlarmReceiver::class.java)
        }
        val pIntent = when {
            pIntentApp.contains("Wakeup") -> PendingIntent.getBroadcast(c, WakeupReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pIntentApp.contains("TimedAlarm") -> PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pIntentApp.contains("RootService") -> PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pIntentApp.contains("NightMode") -> PendingIntent.getBroadcast(c, NightModeReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            else -> PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
    fun cancelAlarm(c: Context, intentApp: String, pIntentApp: String){
        val intent = when {
            intentApp.contains("Wakeup") -> Intent(c, WakeupReceiver::class.java)
            intentApp.contains("TimedAlarm") -> Intent(c, TimedAlarmReceiver::class.java)
            intentApp.contains("RootService") -> Intent(c, RootServiceReceiver::class.java)
            intentApp.contains("NightMode") -> Intent(c, NightModeReceiver::class.java)
            else -> Intent(c,TimedAlarmReceiver::class.java)
        }
        val pIntent = when {
            pIntentApp.contains("Wakeup") -> PendingIntent.getBroadcast(c, WakeupReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pIntentApp.contains("TimedAlarm") -> PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pIntentApp.contains("RootService") -> PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            pIntentApp.contains("NightMode") -> PendingIntent.getBroadcast(c, NightModeReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            else -> PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
        Log.d("RadioControl", "$intentApp cancelled")
    }
    fun scheduleWakeupAlarm(c: Context, hour: Int) {
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR, hour)
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, WakeupReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, ActionReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)

    }

    fun scheduleNightWakeupAlarm(c: Context, hourofDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay)
        cal.add(Calendar.MINUTE, minute)
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, TimedAlarmReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)
    }

    // Setup a recurring alarm every 15,30,60 minutes
    fun scheduleAlarm(c: Context) {

        val preferences = PreferenceManager.getDefaultSharedPreferences(c)
        val intervalTimeString = preferences.getString("interval_prefs", "10")
        val intervalTime = Integer.parseInt(intervalTimeString)
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.MINUTE, intervalTime)
        Log.d("RadioControl", "Interval: " + intervalTime + cal.time)

        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, TimedAlarmReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val interval = cal.timeInMillis
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        Log.d("RadioControl", "Time: " + (cal.timeInMillis - firstMillis))
        //alarm.setInexactRepeating(RTC, firstMillis,
        //cal.getTimeInMillis(), pIntent);

        alarm.setInexactRepeating(AlarmManager.RTC, firstMillis + interval, interval, pIntent)

    }

    fun scheduleRootAlarm(c: Context) {
        val cal = Calendar.getInstance()
        // start 10 seconds
        cal.add(Calendar.SECOND, 5)

        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, RootServiceReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every X seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)
        Log.d("RadioControl", "RootClock enabled for " + cal.time)

    }

    fun cancelRootAlarm(c: Context) {
        val intent = Intent(c, RootServiceReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
        Log.d("RadioControl", "RootClock cancelled")
    }

    fun cancelAlarm(c: Context) {
        val intent = Intent(c, TimedAlarmReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
    }

    fun cancelNightAlarm(c: Context, hourofDay: Int, minute: Int) {
        val cal = Calendar.getInstance()
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay)
        cal.add(Calendar.MINUTE, minute)
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(c, NightModeReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getBroadcast(c, NightModeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every 5 seconds
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                cal.timeInMillis, pIntent)
    }

    fun cancelWakeupAlarm(c: Context) {
        val intent = Intent(c, WakeupReceiver::class.java)
        val pIntent = PendingIntent.getBroadcast(c, WakeupReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarm.cancel(pIntent)
    }
}