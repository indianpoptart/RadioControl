package com.nikhilparanjape.radiocontrol.listeners;

/**
 * Created by admin on 3/4/2017.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.CellRadioService;

public class CustomPhoneStateListener extends PhoneStateListener {
    private static final String PRIVATE_PREF = "prefs";
    //Root commands which disable cell only
    String[] airCmd = {"su", "settings put global airplane_mode_radios  \"cell\"", "content update --uri content://settings/global --bind value:s:'cell' --where \"name='airplane_mode_radios'\"", "settings put global airplane_mode_on 1", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true"};
    //runs command to disable airplane mode on wifi loss, while restoring previous airplane settings
    String[] airOffCmd2 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false", "settings put global airplane_mode_radios  \"cell,bluetooth,nfc,wimax\"", "content update --uri content://settings/global --bind value:s:'cell,bluetooth,nfc,wimax' --where \"name='airplane_mode_radios'\""};
    String[] airOffCmd3 = {"su", "settings put global airplane_mode_on 0", "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false"};

    //private static final String TAG = "PhoneStateChanged";
    Context context; //Context to make Toast if required
    public CustomPhoneStateListener(Context context) {
        super();
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        Log.d("RadioControl","PhoneCall State " + state);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                //when Idle i.e no call
                //Toast.makeText(context, "Phone state Idle", Toast.LENGTH_LONG).show();
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //when Off hook i.e in call
                cellChange();
                //Toast.makeText(context, "Phone state Off hook", Toast.LENGTH_LONG).show();
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                //when Ringing
                //Toast.makeText(context, "Phone state Ringing", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }
    public void cellChange(){
        SharedPreferences sp = context.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //Utilities util = new Utilities();

        //Check if user wants the app on
        if(sp.getInt("isActive",0) == 1 && prefs.getBoolean("isPhoneStateCheck",true)) {
            if (Utilities.isAirplaneMode(context) || !Utilities.isConnectedMobile(context)) {
                //Runs the alternate root command
                if (prefs.getBoolean("altRootCommand", false)) {
                    if (Utilities.getCellStatus(context) == 1) {
                        Intent cellIntent = new Intent(context, CellRadioService.class);
                        //util.cancelAlarm(context);
                        context.startService(cellIntent);
                        Log.d("RadioControl", "Cell Radio has been turned on");
                    }
                }
                else {
                    RootAccess.runCommands(airOffCmd2);
                    Log.d("RadioControl", "Airplane mode has been turned off");

                }

            }
        }
    }

}