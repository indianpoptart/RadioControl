package com.nikhilparanjape.radiocontrol.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.format.DateFormat;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by admin on 8/20/2016.
 */
public class BackgroundAirplaneService extends IntentService
{

    private static final String PRIVATE_PREF = "prefs";

    //Root commands which disable cell only
    String[] airCmd = {"su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
    //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
    String[] airOffCmd2 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};
    String[] airOffCmd3 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};
    Utilities util = new Utilities(); //Network and other related utilities

    public BackgroundAirplaneService() {
        super("BackgroundAirplaneService");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        createBackgroundNotification("RadioControl","Background Process Running...");
        Log.d("RadioControl-background","Notified");

    }

    public void createBackgroundNotification(String title, String message)
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "Background")
                .setSmallIcon(R.drawable.ic_refresh_white_48dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel("radiocontrol-background", "Background Channel", importance);
            channel.setDescription("For Intelligent mode");
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) this
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(0, mBuilder.build());

        }
        else{
            mBuilder.notify();
        }
        startForeground(0, mBuilder.build());



    }

    protected void onHandleIntent(Intent intent) {
        Log.d("RadioControl", "Get action: " + intent.getAction());
        // Start Alarm Task
        Log.d("RadioControl", "Background Service running");
        Context context = getApplicationContext();
        SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences disabledPref = context.getSharedPreferences("disabled-networks", Context.MODE_PRIVATE);

        Set<String> h = new HashSet<>(Collections.singletonList("")); //Set default set for SSID check
        Set<String> selections = prefs.getStringSet("ssid", h); //Gets stringset, if empty sets default
        boolean networkAlert= prefs.getBoolean("isNetworkAlive",false);
        boolean batteryOptimize = prefs.getBoolean("isBatteryOn",true);

        //Log.i("RadioControl","Battery Optimized");
        //Check if user wants the app on
        if(sp.getInt("isActive",0) == 1){
            //Check if we just lost WiFi signal
            if(!Utilities.isConnectedWifi(context)){
                Log.d("RadioControl","WiFi signal LOST");
                writeLog("WiFi Signal lost",context);
                if(Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                    //Checks that user is not in call
                    if(!util.isCallActive(context)) {
                        //Runs the alternate root command
                        if (prefs.getBoolean("altRootCommand", false)) {
                            if (Utilities.getCellStatus(context) == 1) {
                                Intent cellIntent = new Intent(context, CellRadioService.class);
                                context.startService(cellIntent);
                                Log.d("RadioControl", "Cell Radio has been turned on");
                                writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context), context);
                            }
                        } else {
                            if (prefs.getBoolean("altBTCommand", false)) {
                                RootAccess.INSTANCE.runCommands(airOffCmd3);
                                Log.d("RadioControl", "Airplane mode has been turned off(with bt cmd)");
                                writeLog("Airplane mode has been turned off", context);
                            } else {
                                RootAccess.INSTANCE.runCommands(airOffCmd2);
                                Log.d("RadioControl", "Airplane mode has been turned off");
                                writeLog("Airplane mode has been turned off", context);
                            }

                        }
                    }
                    //Checks that user is currently in call and pauses execution till the call ends
                    else if(util.isCallActive(context)){
                        while(util.isCallActive(context)){
                            waitFor(1000);//Wait for call to end
                        }
                    }
                }
            }

            //If network is connected and airplane mode is off
            if (Utilities.isConnectedWifi(context) && !Utilities.isAirplaneMode(context)) {
                //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
                //Check the list of disabled networks
                if(!disabledPref.contains(Utilities.getCurrentSsid(context))){
                    Log.d("RadioControl",Utilities.getCurrentSsid(context) + " was not found in the disabled list");
                    //Checks that user is not in call
                    if(!util.isCallActive(context)){
                        //Checks if the user doesn't want network alerts
                        if(!networkAlert){
                            //Runs the alternate root command
                            if(prefs.getBoolean("altRootCommand", false)){

                                if(Utilities.getCellStatus(context) == 0){
                                    Intent cellIntent = new Intent(context, CellRadioService.class);
                                    context.startService(cellIntent);
                                    Log.d("RadioControl", "Cell Radio has been turned off");
                                    writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context),context);
                                }
                                else if(Utilities.getCellStatus(context) == 1){
                                    Log.d("RadioControl", "Cell Radio is already off");
                                }

                            }
                            else{
                                RootAccess.INSTANCE.runCommands(airCmd);
                                Log.d("RadioControl", "Airplane mode has been turned on");
                                writeLog("Airplane mode has been turned on, SSID: " + Utilities.getCurrentSsid(context),context);
                            }

                        }
                        //The user does want network alert notifications
                        else {
                            pingTask(context);
                        }

                    }
                    //Checks that user is currently in call and pauses execution till the call ends
                    else if(util.isCallActive(context)){
                        while(util.isCallActive(context)){
                            waitFor(1000);//Wait for call to end
                        }
                    }
                }
                //Pauses because WiFi network is in the list of disabled SSIDs
                else if(selections.contains(Utilities.getCurrentSsid(context))){
                    Log.d("RadioControl",Utilities.getCurrentSsid(context) + " was blocked from list " + selections);
                    writeLog(Utilities.getCurrentSsid(context) + " was blocked from list " + selections,context);
                }
            }

        }
        if(sp.getInt("isActive",0) == 0){
            Log.d("RadioControl","RadioControl has been disabled");
            if(networkAlert){
                pingTask(context);
            }
            //Adds wifi signal lost log for nonrooters
            if(!Utilities.isConnectedWifi(context)) {
                Log.d("RadioControl", "WiFi signal LOST");
                writeLog("WiFi Signal lost", context);
            }
        }

    }
    public void writeLog(String data, Context c){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        if(preferences.getBoolean("enableLogs", false)){
            try{
                String h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString();
                File log = new File(c.getFilesDir(), "radiocontrol.log");
                if(!log.exists()) {
                    log.createNewFile();
                }
                String logPath = "radiocontrol.log";
                String string = "\n" + h + ": " + data;

                FileOutputStream fos = c.openFileOutput(logPath, Context.MODE_APPEND);
                fos.write(string.getBytes());
                fos.close();
            } catch(IOException e){
                Log.d("RadioControl", "There was an error saving the log: " + e);
            }
        }
    }
    public void pingTask(Context context){
        Runtime runtime = Runtime.getRuntime();
        try {
            //Wait for network to be connected fully
            while(!Utilities.isConnected(context)){
                Thread.sleep(1000);
            }
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");//Send 1 packet to Cloudflare and check if it came back
            int exitValue = ipProcess.waitFor();
            Log.d("RadioControl", "Ping test returned " + exitValue);

            SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            boolean alertPriority = prefs.getBoolean("networkPriority", false);//Setting for network notifier
            boolean alertSounds = prefs.getBoolean("networkSound",false);
            boolean alertVibrate = prefs.getBoolean("networkVibrate",false);


            if(sp.getInt("isActive",0) == 0){
                //If the connection can't reach Google
                if(exitValue != 0){
                    Utilities.sendNote(context, context.getString(R.string.not_connected_alert),alertVibrate,alertSounds,alertPriority);
                    writeLog("Not connected to the internet",context);
                }
            }
            else if(sp.getInt("isActive",0) == 1){
                //If the connection can't reach Google
                if(exitValue != 0){
                    Utilities.sendNote(context, context.getString(R.string.not_connected_alert),alertVibrate,alertSounds,alertPriority);
                    writeLog("Not connected to the internet",context);
                }
                else{
                    //Runs the alternate root command
                    if(prefs.getBoolean("altRootCommand", false)){
                        Intent cellIntent = new Intent(context, CellRadioService.class);
                        context.startService(cellIntent);
                        util.scheduleRootAlarm(context);
                        Log.d("RadioControl", "Cell Radio has been turned off");
                        writeLog("Cell radio has been turned off, SSID: " + Utilities.getCurrentSsid(context),context);
                    }
                    else if(!prefs.getBoolean("altRootCommand", false)){
                        RootAccess.INSTANCE.runCommands(airCmd);
                        Log.d("RadioControl", "Airplane mode has been turned on");
                        writeLog("Airplane mode has been turned on, SSID: " + Utilities.getCurrentSsid(context),context);
                    }
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waitFor(long timer){
        try {
            Thread.sleep(timer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDestroy()
    {
        super.onDestroy();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean airplaneService = preferences.getBoolean("isAirplaneService", false);
        if(airplaneService){
            util.scheduleAlarm(getApplicationContext());
        }

        Log.d("RadioControl", "Background Service destroyed");
    }

}