package com.nikhilparanjape.radiocontrol.rootUtils;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Nikhil on 12/25/2015.
 */
public class RootAccess{
    public static void runCommands(String[] commands){
        Process p;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : commands) {
                os.writeBytes(tmpCmd + "\n"); //Sends commands to the terminal
            }
            os.writeBytes("exit\n");
            os.flush();
        }
        catch (IOException e){
            e.printStackTrace();

        }
    }
}
