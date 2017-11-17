package com.nikhilparanjape.radiocontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;

public class WakeupReceiver extends BroadcastReceiver {
    public WakeupReceiver() {
    }

    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Utilities util = new Utilities();
        util.scheduleAlarm(context);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isNoDisturbEnabled", false);
        editor.apply();
    }
}
