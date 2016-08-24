package com.nikhilparanjape.radiocontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nikhilparanjape.radiocontrol.services.ScheduledAirplaneService;

public class TimedAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.nikhilparanjape.radiocontrol.services.ScheduledAirplaneService";

    public TimedAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, ScheduledAirplaneService.class);
        context.startService(i);
    }
}
