package com.nikhilparanjape.radiocontrol.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver;
import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
