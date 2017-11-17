package com.nikhilparanjape.radiocontrol.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver;

/**
 * Created by admin on 7/9/2017.
 */

public class PersistenceService extends Service {
    private final BroadcastReceiver mybroadcast = new WifiReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("RadioControl","PERSISTENCE Created");
    }
    @Nullable
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i("RadioControl","PERSISTENCE Started");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        this.registerReceiver(mybroadcast,filter);


        return flags;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("RadioControl","PERSISTENCE Bound");

        return null;
    }

}
