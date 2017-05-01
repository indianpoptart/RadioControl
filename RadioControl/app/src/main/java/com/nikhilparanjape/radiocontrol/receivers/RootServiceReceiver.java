package com.nikhilparanjape.radiocontrol.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.CellRadioService;

/**
 * Created by admin on 9/24/2016.
 */

public class RootServiceReceiver extends WakefulBroadcastReceiver{
    public static final int REQUEST_CODE = 35718;

    public RootServiceReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, CellRadioService.class);
        Utilities util = new Utilities();

        context.stopService(i);
        Log.d("RadioControl","CellService Stopped");
        util.cancelRootAlarm(context);
        Log.d("RadioControl","RootClock cancelled");
    }
}
