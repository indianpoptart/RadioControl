package com.nikhilparanjape.radiocontrol.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by nikhilparanjape on 7/5/17.
 */

public class ActionReceiver extends BroadcastReceiver {
    private static final String PRIVATE_PREF = "prefs";
    public static final int REQUEST_CODE = 21345;

    @Override
    public void onReceive(Context context, Intent intent) {

        //Toast.makeText(context,"recieved",Toast.LENGTH_SHORT).show();

        final SharedPreferences sharedPref = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("isActive",1);
        editor.apply();
        Toast.makeText(context, "Standby Mode disabled",
                Toast.LENGTH_LONG).show();
        //This is used to close the notification tray
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcast(it);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(10110);
    }


}