package com.nikhilparanjape.radiocontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import java.util.Arrays;

import static android.provider.Settings.Global.*;

/**
 * Created by Nikhil Paranjape on 11/8/2015.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String PRIVATE_PREF = "prefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Initialize Network Settings
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        //Root commands which disable cell only
        String[] airCmd = {"su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
        String[] airOffCmd2 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};

        SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);

        //Setup Disabled networks
        String arrayString = sp.getString("disabled_networks", "1");

        NetworkInfo activeNetwork = conMan.getActiveNetworkInfo();

        // Check if the device is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //Check for airplane mode
        boolean isEnabled = Settings.Global.getInt(context.getContentResolver(), AIRPLANE_MODE_ON, 0) == 1;

        //if network is connected and airplane mode is off
        if (isConnected && !isEnabled) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            //Check for wifi connection
            if(isWiFi) {
                //Check the list of disabled networks
                if(!arrayString.contains(getCurrentSsid(context))){
                    Log.d("DISABLED-NETWORK",getCurrentSsid(context) + " was not found the following strings " + arrayString);
                    //Checks that user is not in call
                    if(!isCallActive(context)){
                        RootAccess.runCommands(airCmd);
                        Log.d("WiFiAirplane", "Wifi is on,airplane-on");
                    }
                    //Checks that user is currently in call and pauses execution till the call ends
                    else if(isCallActive(context)){
                        while(isCallActive(context)){
                            waitFor(1000);//Wait for call to end
                        }
                    }
                }
                else if(Arrays.asList(arrayString).contains(getCurrentSsid(context))){
                    Log.d("DISABLED-NETWORK",getCurrentSsid(context) + " was blocked from list " + arrayString);
                }
            }
            else{
                Log.d("Unknown","An unknown network was detected"); //Maybe you have a PAN connection?? Who knows
            }
        }
        //Check if we just lost WiFi signal
        if(isConnected == false){
            Log.d("WIRELESS","SIGNAL LOST");
            if(isEnabled){
                RootAccess.runCommands(airOffCmd2);
                Log.d("Wifi","Wifi signal lost, airplane mode has turned off");
            }
            else{
                Log.d("wifi","Wifi is on");
            }
        }
        else {
            Log.d("Else","something is different");
        }

        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI)
            Log.d("WiFiReceiver", "Have Wifi Connection");
        else
            Log.d("WiFiReceiver", "Don't have Wifi Connection");
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
    public int linkSpeed(Context c){
        WifiManager wifiManager = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getRssi();
        Log.d("LinkSpeed","Speed " + linkSpeed);
        return linkSpeed;
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