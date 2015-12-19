package com.nikhilparanjape.radiocontrol;

import android.bluetooth.BluetoothAdapter;
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static android.provider.Settings.Global.*;

/**
 * Created by Nikhil Paranjape on 11/8/2015.
 */
public class WifiReceiver extends BroadcastReceiver {
    private static final String PRIVATE_PREF = "radiocontrol-prefs";

    @Override
    public void onReceive(Context context, Intent intent) {
        //Initialize Network Settings
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        //Root commands for airplane mode
        //String[] airplaneCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable"};
        //Root commands if there is an active bluetooth connection
        //String[] bluetoothCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable","service call bluetooth_manager 6"};
        //runs command to disable airplane mode on wifi loss
        //String[] airOffCmd = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};
        //Root commands for airplane mode, without disabling wifi
        String[] airplaneCmd2 = {"su", "settings put global airplane_mode_radios  \"cell,bluetooth,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,wimax' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        //Root commands if there is an active bluetooth connection, without disabling wifi
        String[] bluetoothCmd2 = {"su", "settings put global airplane_mode_radios  \"cell,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,wimax' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
        //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
        String[] airOffCmd2 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};

        SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        //long secondsValue = sp.getLong("seconds_spinner", -1);
        //long timer = secondsValue*1000;
        //Log.d("SpinnerVal", "The seconds were received: " + secondsValue + ", Timer:" + timer);

        //Setup Disabled networks
        String arrayString = sp.getString("disabled_networks", "1");

        NetworkInfo activeNetwork = conMan.getActiveNetworkInfo();

        // Check if the device is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //Check for bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check for airplane mode
        boolean isEnabled = Settings.System.getInt(context.getContentResolver(), AIRPLANE_MODE_ON, 0) == 1;
        MainActivity.airStatus(isEnabled);

        //if connected and airplane mode is off
        if (isConnected && !isEnabled) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            //Check for wifi
            if(isWiFi) {
                //Check the list of disabled networks
                if(!arrayString.contains(getCurrentSsid(context))){
                    Log.d("DISABLED-NETWORK",getCurrentSsid(context) + " was not found in list " + arrayString);
                    //Checks that user is not in call
                    if(!isCallActive(context)){
                        //If the bluetooth connection is on
                        if(bluetoothAdapter.isEnabled() || bluetoothAdapter.isDiscovering()) {
                            //rootAccess(bluetoothCmd,timer);
                            rootAccess(bluetoothCmd2);
                            Log.d("BlueWiFiAirplane", "Wifi-on,airplane-on,bluetooth-on");
                        }
                        //If bluetooth is off, run the standard root request
                        else if(!bluetoothAdapter.isEnabled()){
                            //rootAccess(airplaneCmd,timer);
                            rootAccess(airplaneCmd2);
                            Log.d("WiFiAirplane", "Wifi is on,airplane-on");
                        }
                    }
                    //Checks that user is currently in call and pauses execution till the call ends
                    else if(isCallActive(context)){
                        while(isCallActive(context)){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else if(Arrays.asList(arrayString).contains(getCurrentSsid(context))){
                    Log.d("DISABLED-NETWORK",getCurrentSsid(context) + " was blocked from list " + arrayString);
                }
            }
        }
        //Check if we just lost WiFi signal
        else if(isConnected == false){
            Log.d("WIRELESS","SIGNAL LOST");
            if(isEnabled){
                //rootAccess(airOffCmd,timer);
                rootAccess(airOffCmd2);
                Log.d("Wifi","Wifi signal lost, airplane mode has turned off");
            }
            else{
                Log.d("wifi","Wifi is on");
            }
        }
        else if(!isEnabled){
            Log.d("Airplane","Airplane mode is off");
        }

        else {
            Log.d("Else","something is different");
        }


        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI)
            Log.d("WiFiReceiver", "Have Wifi Connection");
        else
            Log.d("WiFiReceiver", "Don't have Wifi Connection");
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
    //Run the root commands
    public void rootAccess(String[] commands){
        Process p;
        try {
            p = Runtime.getRuntime().exec("su"); //Request SU
            DataOutputStream os = new DataOutputStream(p.getOutputStream()); //Used for terminal
            for (String tmpCmd : commands) {
                os.writeBytes(tmpCmd + "\n"); //Sends commands to the terminal
            }
            os.writeBytes("exit\n"); //Quits the terminal session
            os.flush(); //Ends datastream
            Log.d("Root", "Commands Completed");
        } catch (IOException e) {
            Log.d("Root", "There was an error with root");
        }
    }
};