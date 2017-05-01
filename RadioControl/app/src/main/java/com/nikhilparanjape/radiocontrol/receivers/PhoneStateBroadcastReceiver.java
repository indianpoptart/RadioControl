package com.nikhilparanjape.radiocontrol.receivers;

/**
 * Created by admin on 3/4/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.listeners.CustomPhoneStateListener;

public class PhoneStateBroadcastReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(new CustomPhoneStateListener(context), PhoneStateListener.LISTEN_CALL_STATE);
        Log.d("RadioControl","PhoneCall ENGAGED");

    }

}