package com.nikhilparanjape.radiocontrol.receivers;

/**
 * Created by admin on 4/23/2017.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService;

public class ServiceStarter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, BackgroundAirplaneService.class);
        context.startService(i);
    }
}