package com.nikhilparanjape.radiocontrol;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Set;


/**
 * Created by Nikhil Paranjape on 11/8/2015.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String PRIVATE_PREF = "prefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);

        //Root commands which disable cell only
        String[] airCmd = {"su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
        String[] airOffCmd2 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};

        SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Utilities util = new Utilities(); //Network utils

        Set<String> selections = prefs.getStringSet("ssid", null);
        boolean alertPriority = prefs.getBoolean("networkPriority", false);//Setting for network notifier
        boolean alertSounds = prefs.getBoolean("networkSound",false);
        boolean alertVibrate = prefs.getBoolean("networkVibrate",false);
        boolean networkAlert= prefs.getBoolean("isNetworkAlive",false);


        //Check if user wants the app on
        if(sp.getInt("isActive",0) == 1){
            //if network is connected and airplane mode is off
            if (util.isConnected(context) && !util.isAirplaneMode(context)) {
                //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
                //Check for wifi connection
                if(util.isConnectedWifi(context)) {
                    //Check the list of disabled networks
                    if(!selections.contains(getCurrentSsid(context))){
                        Log.d("RadioControl",getCurrentSsid(context) + " was not found the following strings " + selections);
                        //Checks that user is not in call
                        if(!isCallActive(context)){
                            //Checks if the user wants network alerts
                            if(networkAlert){
                                //If the connection can reach Google
                                if(util.isOnline()){
                                    RootAccess.runCommands(airCmd);
                                    Log.d("RadioControl", "WiFi is on, Airplane mode is on");
                                }
                                //If the network is not alive
                                else{
                                    sendNote(context, "You are not connected to the internet",alertVibrate,alertSounds,alertPriority);
                                }
                            }
                            //The user does not want network alert notifications
                            else{
                                RootAccess.runCommands(airCmd);
                                Log.d("RadioControl", "WiFi is on, Airplane mode is on");
                            }

                        }
                        //Checks that user is currently in call and pauses execution till the call ends
                        else if(isCallActive(context)){
                            while(isCallActive(context)){
                                waitFor(1000);//Wait for call to end
                            }
                        }
                    }
                    //Pauses because WiFi network is in the list of disabled SSIDs
                    else if(selections.contains(getCurrentSsid(context))){
                        Log.d("RadioControl",getCurrentSsid(context) + " was blocked from list " + selections);
                    }
                }
            }
            //Check if we just lost WiFi signal
            if(util.isConnected(context) == false){
                Log.d("RadioControl","WIFI SIGNAL LOST");
                if(util.isAirplaneMode(context)){
                    RootAccess.runCommands(airOffCmd2);
                    Log.d("RadioControl","Wifi signal lost, airplane mode has been turned on");
                }
                else{
                    Log.d("RadioControl","Wifi is on");
                }
            }
        }
        else if(sp.getInt("isActive",0) == 0){
            Log.d("RadioControl","RadioControl has been disabled");
            if(networkAlert){
                //If the connection can reach Google
                if(!util.isOnline()){
                    sendNote(context, "You are not connected to the internet",alertVibrate,alertSounds,alertPriority);
                }
                //If the network is not alive
                else{

                }
            }
        }
    }
    public void waitFor(long timer){
        try {
            Thread.sleep(timer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //Checks for current ssid
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                ssid = connectionInfo.getSSID();
            ssid = ssid.substring(1, ssid.length()-1);
        }
        return ssid;
    }
    //Code that checks WiFi link speed
    public static int linkSpeed(Context c){
        WifiManager wifiManager = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
        Log.d("RadioControl", "Link speed = " + linkSpeed + "Mbps");
        return linkSpeed;
    }
    //Network Alert Notification
    public static void sendNote(Context context, String mes, boolean vibrate, boolean sound, boolean heads){
        int priority;
        if(!heads){
            priority = 0;
        }
        else if(heads){
            priority = 1;
        }
        else{
            priority = 1;
        }

        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), 0);
        //Resources r = getResources();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                .setContentTitle("RadioControl Network Alert")
                .setContentIntent(pi)
                .setContentText(mes)
                .setPriority(priority)
                .setAutoCancel(true)
                .build();

        if(vibrate){
            notification.defaults|= Notification.DEFAULT_VIBRATE;
        }
        if(sound){
            notification.defaults|= Notification.DEFAULT_SOUND;
        }


        notificationManager.notify(1, notification);

    }

    //Check if user is currently in call
    public boolean isCallActive(Context context){ AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode()== AudioManager.MODE_IN_CALL){
            return true;
        }
        else{
            return false;
        }
    }
};