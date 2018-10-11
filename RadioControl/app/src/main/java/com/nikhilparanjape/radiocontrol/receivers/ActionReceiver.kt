package com.nikhilparanjape.radiocontrol.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * Created by nikhilparanjape on 7/5/17.
 */

class ActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        val sharedPref = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        editor.putInt("isActive", 1)
        editor.apply()
        Toast.makeText(context, "Standby Mode disabled",
                Toast.LENGTH_LONG).show()
        //This is used to close the notification tray
        val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        context.sendBroadcast(it)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(10110)
    }

    companion object {
        private const val PRIVATE_PREF = "prefs"
        const val REQUEST_CODE = 21383
    }


}