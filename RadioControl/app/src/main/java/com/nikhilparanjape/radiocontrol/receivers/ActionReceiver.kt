package com.nikhilparanjape.radiocontrol.receivers

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

/**
 * Created by nikhilparanjape on 7/5/17.
 *
 * @author Nikhil Paranjape
 *
 *
 */

class ActionReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission") //Code will only run on devices running < Android 12
    override fun onReceive(context: Context, intent: Intent) {

        //Toast.makeText(context,"received",Toast.LENGTH_SHORT).show();

        val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val editor = getPrefs.edit()

        editor.putInt("isActive", 1)
        editor.apply()
        Toast.makeText(context, "Standby Mode disabled",
                Toast.LENGTH_LONG).show()
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            //This is used to close the notification tray
            val it = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
            context.sendBroadcast(it)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(10110)
    }

    companion object {
        /*private const val PRIVATE_PREF = "prefs"*/
        const val REQUEST_CODE = 21383
    }


}