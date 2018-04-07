package com.nikhilparanjape.radiocontrol.receivers;

/**
 * Created by admin on 4/23/2017.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService;

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, BackgroundAirplaneService.class));
        } else {
            context.startService(new Intent(context, BackgroundAirplaneService.class));
        }
    }
}