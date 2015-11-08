package com.nikhilparanjape.radiocontrol;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Nikhil Paranjape on 11/8/2015.
 */
public class WifiReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMan.getActiveNetworkInfo();
        //Root commands for airplane mode
        String[] airplaneCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable"};
        //Root commands if there is an active bluetooth connection
        String[] bluetoothCmd = {"su", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true","svc wifi enable","service call bluetooth_manager 6"};
        //runs command to disable airplane mode on wifi loss
        String[] airOffCmd = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};
        //Initialize network settings
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        // Check if the device is connected to the internet
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        //Check for bluetooth
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check for airplane mode
        boolean isEnabled = Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
        //if connected and airplane mode is off
        if (isConnected && !isEnabled) {
            boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI; //Boolean to check for an active WiFi connection
            //Check for wifi
            if(isWiFi) {
                //If the bluetooth connection is on
                if(bluetoothAdapter.isEnabled() || bluetoothAdapter.isDiscovering()) {
                    rootAccess(bluetoothCmd);

                }
                //If bluetooth is off, run the standard root request
                else if(!bluetoothAdapter.isEnabled()){
                    rootAccess(airplaneCmd);
                }
            }
            //Check if wifi just turned off
        } else if(isConnected == false){

            if(isEnabled){
                rootAccess(airOffCmd);
                Log.d("Wifi","Wifi disconnected, airplane is off");
            }
            else{
                Log.d("wifi","Wifi is on");
            }

            Log.d("Airoff","Airplane mode was disabled");
        }
        else if(!isEnabled){
            Log.d("Airplane","Airplane mode is off");
        }

        else {
            Log.d("Else","something is different");
        }


        if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI)
            Log.d("WifiReceiver", "Have Wifi Connection");
        else
            Log.d("WifiReceiver", "Don't have Wifi Connection");
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
            Thread.sleep(10000);
        } catch (IOException e) {
            Log.d("Root", "There was an error with root");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
};