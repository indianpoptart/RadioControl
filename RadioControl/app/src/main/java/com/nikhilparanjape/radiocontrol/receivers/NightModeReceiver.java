package com.nikhilparanjape.radiocontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;

public class NightModeReceiver extends BroadcastReceiver {
    public NightModeReceiver() {
    }

    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService";

    @Override
    public void onReceive(Context context, Intent intent) {
        Utilities util = new Utilities();
        util.cancelAlarm(context);

        Log.d("RadioControl", "Night Mode started");

    }
}
