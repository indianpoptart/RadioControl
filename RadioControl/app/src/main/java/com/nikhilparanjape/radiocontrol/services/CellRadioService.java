package com.nikhilparanjape.radiocontrol.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;

/**
 * Created by admin on 9/23/2016.
 */

public class CellRadioService extends Service {
    private boolean mRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRunning) {
            mRunning = true;
            Log.d("RadioControl","CellServiceOn");
            String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};
            RootAccess.runCommands(cellOffCmd);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}