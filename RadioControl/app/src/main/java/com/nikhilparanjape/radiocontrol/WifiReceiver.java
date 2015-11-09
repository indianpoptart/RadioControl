package com.nikhilparanjape.radiocontrol;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.provider.Settings.Global.*;

/**
 * Created by Nikhil Paranjape on 11/8/2015.
 */
public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Initialize Network Settings
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        //Root commands for airplane mode
        String[] airplaneCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable"};
        //Root commands if there is an active bluetooth connection
        String[] bluetoothCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable","service call bluetooth_manager 6"};
        //runs command to disable airplane mode on wifi loss
        String[] airOffCmd = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};


        NetworkInfo activeNetwork = conMan.getActiveNetworkInfo();

        // Check if the device is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //Check for bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check for airplane mode
        boolean isEnabled = Settings.System.getInt(context.getContentResolver(), AIRPLANE_MODE_ON, 0) == 1;

        //if connected and airplane mode is off
        if (isConnected && !isEnabled) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            //Check for wifi
            if(isWiFi) {
                //Check if connection is alive
                if(isURLReachable(context)){
                    //If the bluetooth connection is on
                    if(bluetoothAdapter.isEnabled() || bluetoothAdapter.isDiscovering()) {
                        rootAccessB(bluetoothCmd);
                        Log.d("BlueWiFiAirplane", "Wifi-on,airplane-on,bluetooth-on");

                    }
                    //If bluetooth is off, run the standard root request
                    else if(!bluetoothAdapter.isEnabled()){
                        rootAccess(airplaneCmd);
                        Log.d("WiFiAirplane", "Wifi is on,airplane-on");
                    }
                }
                //the connection is not actually connected to the internet
                else{
                    Log.d("Connection","Connection is not alive");
                    Toast.makeText(context, "WiFi Connection unsuccessful", Toast.LENGTH_LONG).show();
                }

            }
            //Check if we just lost WiFi signal
        }
        else if(isConnected == false){
            Log.d("WIRELESS","SIGNAL LOST");
            if(isEnabled){
                rootAccess(airOffCmd);
                Log.d("Wifi","Wifi signal lost, airplanemode has turned off");
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
            Thread.sleep(10000);
            Log.d("Timer", "10 seconds after commands were completed");
        } catch (IOException e) {
            Log.d("Root", "There was an error with root");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void rootAccessB(String[] commands){
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
            Thread.sleep(15000);
            Log.d("Timer", "10 seconds after commands were completed");
        } catch (IOException e) {
            Log.d("Root", "There was an error with root");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static boolean isURLReachable(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("google.com");   // Change to "http://google.com" for www  test.
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(5 * 1000);          // 10 s.
                urlc.connect();
                if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                    Log.wtf("Connection", "Success !");
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
};