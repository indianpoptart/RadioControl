package com.nikhilparanjape.radiocontrol;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Nikhil on 12/25/2015.
 */
public class RootAccess{
    public static void runCommands(String[] commands){
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
    public static boolean getRoot(){
        Process p;
        try {
            p = Runtime.getRuntime().exec("su"); //Request SU
            DataOutputStream os = new DataOutputStream(p.getOutputStream()); //Used for terminal
            os.writeBytes("exit\n"); //Quits the terminal session
            os.flush(); //Ends datastream
            Log.d("Root", "Commands Completed");
            return true;
        } catch (IOException e) {
            Log.d("Root", "There was an error with root");
            return false;
        }
    }
}
