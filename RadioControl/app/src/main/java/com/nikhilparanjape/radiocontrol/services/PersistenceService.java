package com.nikhilparanjape.radiocontrol.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver;

/**
 * Created by admin on 7/9/2017.
 */

public class PersistenceService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("RadioControl","Receiver Started");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new WifiReceiver(), intentFilter);
        NotificationCompat.Builder note = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_cached_white_36dp)
                .setContentTitle("Persistent Service")
                .setContentText("Idle")
                .setPriority(-2)
                .setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(10110, note.build());
        return null;
    }

}
