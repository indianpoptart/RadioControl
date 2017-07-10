package com.nikhilparanjape.radiocontrol.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.nikhilparanjape.radiocontrol.services.PersistenceService;

/**
 * Created by admin on 7/9/2017.
 */

public class PersistenceAlarmReceiver extends WakefulBroadcastReceiver{
    public static final int REQUEST_CODE = 25912;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, PersistenceService.class);
        startWakefulService(context, service);
    }
}
