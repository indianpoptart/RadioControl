package com.nikhilparanjape.radiocontrol.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

    }
    @Nullable
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i("RadioControl","PERSISTENCE Started");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        unregisterReceiver(mybroadcast);

        this.registerReceiver(receiver,filter);

        return flags;
    }
    @Override
    public IBinder onBind(Intent intent) {


        return null;
    }
    private final BroadcastReceiver receiver = new WakefulBroadcastReceiver() {

        private final String PRIVATE_PREF = "prefs";
        private boolean firstConnect = true;

        //Root commands which disable cell only
        String[] airCmd = {"su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
        String[] airOffCmd2 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};
        String[] airOffCmd3 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};


        Utilities util = new Utilities(); //Network and other related utilities


        public void onReceive(Context context, Intent intent) {
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            Log.d("RadioControl", "Get actionS: " + intent.getAction());
            NotificationCompat.Builder note = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_cached_white_36dp)
                    .setContentTitle("Persistent Service")
                    .setContentText("Running")
                    .setPriority(-2)
                    .setOngoing(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(10110, note.build());

            NotificationCompat.Builder sleep = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.drawable.ic_cached_white_36dp)
                    .setContentTitle("Persistent Service")
                    .setContentText("Sleeping")
                    .setPriority(-2)
                    .setOngoing(true);


            SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences disabledPref = context.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE);

            Set<String> h = new HashSet<>(Collections.singletonList("")); //Set default set for SSID check
            Set<String> selections = prefs.getStringSet("ssid", h); //Gets stringset, if empty sets default
            boolean networkAlert = prefs.getBoolean("isNetworkAlive", false);
            boolean batteryOptimize = prefs.getBoolean("isBatteryOn", true);

            Log.i("RadioControl", "WifiReceiver Triggered");
            //Check if user wants the app on
            if (sp.getInt("isActive", 0) == 1) {
                if (batteryOptimize) {
                    Log.d("RadioControl", "Battery Optimization ON");
                    Intent i = new Intent(context, BackgroundAirplaneService.class);
                    notificationManager.notify(10110, sleep.build());
                    context.startService(i);

                } else {
                    Log.d("RadioControl", "Battery Optimization OFF");
                    //Check if we just lost WiFi signal
                    if (!Utilities.isConnectedWifi(context)) {
                        Log.d("RadioControl", "WiFi signal LOST");
                        writeLog("WiFi Signal lost", context);
                        if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                            //Checks that user is not in call
                            if (!util.isCallActive(context)) {
                                //Runs the alternate root command
                                if (prefs.getBoolean("altRootCommand", false)) {
                                    if (Utilities.getCellStatus(context) == 1) {
                                        Intent cellIntent = new Intent(context, CellRadioService.class);
                                        context.startService(cellIntent);
                                        Log.d("RadioControl", "Cell Radio has been turned on");
                                        writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context), context);
                                        notificationManager.notify(10110, sleep.build());
                                    }
                                } else {
                                    if (prefs.getBoolean("altBTCommand", false)) {
                                        RootAccess.runCommands(airOffCmd3);
                                        Log.d("RadioControl", "Airplane mode has been turned off(with bt cmd)");
                                        writeLog("Airplane mode has been turned off", context);
                                        notificationManager.notify(10110, sleep.build());
                                    } else {
                                        RootAccess.runCommands(airOffCmd2);
                                        Log.d("RadioControl", "Airplane mode has been turned off");
                                        writeLog("Airplane mode has been turned off", context);
                                        notificationManager.notify(10110, sleep.build());
                                    }

                                }
                            }
                        }
                    }
                }

                //If network is connected and airplane mode is off
                if (Utilities.isConnectedWifi(context) && !Utilities.isAirplaneMode(context)) {
                    //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
                    //Check the list of disabled networks
                    if (!disabledPref.contains(Utilities.getCurrentSsid(context))) {
                        Log.d("RadioControl", Utilities.getCurrentSsid(context) + " was not found in the disabled list");
                        //Checks that user is not in call
                        if (!util.isCallActive(context)) {
                            //Checks if the user doesn't want network alerts
                            if (!networkAlert) {
                                //Runs the alternate root command
                                if (prefs.getBoolean("altRootCommand", false)) {

                                    if (Utilities.getCellStatus(context) == 0) {
                                        Intent cellIntent = new Intent(context, CellRadioService.class);
                                        context.startService(cellIntent);
                                        Log.d("RadioControl", "Cell Radio has been turned off");
                                        writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context), context);
                                        notificationManager.notify(10110, sleep.build());
                                    } else if (Utilities.getCellStatus(context) == 1) {
                                        Log.d("RadioControl", "Cell Radio is already off");
                                        notificationManager.notify(10110, sleep.build());
                                    }

                                } else {
                                    RootAccess.runCommands(airCmd);
                                    Log.d("RadioControl", "Airplane mode has been turned on");
                                    writeLog("Airplane mode has been turned on, SSID: " + Utilities.getCurrentSsid(context), context);
                                    notificationManager.notify(10110, sleep.build());
                                }

                            }
                            //The user does want network alert notifications
                            else {
                                //new com.nikhilparanjape.radiocontrol.receivers.WifiReceiver.AsyncPingTask(context).execute("");
                            }

                        }
                        //Checks that user is currently in call and pauses execution till the call ends
                        else if (util.isCallActive(context)) {
                            while (util.isCallActive(context)) {
                                waitFor(1000);//Wait for call to end
                            }
                        }
                    }
                    //Pauses because WiFi network is in the list of disabled SSIDs
                    else if (selections.contains(Utilities.getCurrentSsid(context))) {
                        Log.d("RadioControl", Utilities.getCurrentSsid(context) + " was blocked from list " + selections);
                        writeLog(Utilities.getCurrentSsid(context) + " was blocked from list " + selections, context);
                        notificationManager.notify(10110, sleep.build());
                    }
                }

            }
            if (sp.getInt("isActive", 0) == 0) {
                Log.d("RadioControl", "RadioControl has been disabled");
                if (networkAlert) {
                    //new com.nikhilparanjape.radiocontrol.receivers.WifiReceiver.AsyncPingTask(context).execute("");
                    notificationManager.notify(10110, sleep.build());
                }
                //Adds wifi signal lost log for nonrooters
                if (!Utilities.isConnectedWifi(context)) {
                    Log.d("RadioControl", "WiFi signal LOST");
                    writeLog("WiFi Signal lost", context);
                    notificationManager.notify(10110, sleep.build());
                }
            }


        }

        public void writeLog(String data, Context c) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            if (preferences.getBoolean("enableLogs", false)) {
                try {
                    String h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString();
                    File log = new File(c.getFilesDir(), "radiocontrol.log");
                    if (!log.exists()) {
                        log.createNewFile();
                    }
                    String logPath = "radiocontrol.log";
                    String string = "\n" + h + ": " + data;

                    FileOutputStream fos = c.openFileOutput(logPath, Context.MODE_APPEND);
                    fos.write(string.getBytes());
                    fos.close();
                } catch (IOException e) {
                    FirebaseCrash.logcat(Log.ERROR, "RadioControl", "Error with log");
                    FirebaseCrash.report(e);
                }
            }
        }


        public void waitFor(long timer) {
            try {
                Thread.sleep(timer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    };
}
