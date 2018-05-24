package com.nikhilparanjape.radiocontrol.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.PowerManager;
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
        try{
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "RadioControl-persist");
            if ((wakeLock != null) &&           // we have a WakeLock
                    (!wakeLock.isHeld())) {  // but we don't hold it
                //wakeLock.acquire(); Do not uncomment
            }


        } catch(NullPointerException e){
            Log.d("Radiocontrol-persist","NullPointer");
        }


        Log.i("RadioControl","PERSISTENCE Started");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);


        try{
            this.registerReceiver(mybroadcast,filter);
        } catch(Exception e){
            Log.e("RadioControl-register","Registration Failed");
        }



        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("RadioControl","PERSISTENCE Bound");

        return null;
    }
    public void onDestroy() {

        super.onDestroy();

    }

}
