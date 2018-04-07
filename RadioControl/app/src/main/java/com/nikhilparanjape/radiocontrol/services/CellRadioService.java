package com.nikhilparanjape.radiocontrol.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;


/**
 * Created by admin on 9/23/2016.
 */

public class CellRadioService extends IntentService {
    private boolean mRunning;

    public CellRadioService() {
        super("CellRadioService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = false;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!mRunning) {
            mRunning = true;
            Log.d("RadioControl","CellService Toggled");
            String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};
            RootAccess.runCommands(cellOffCmd);
            Log.d("RadioControl","CellService Killed");
            this.stopSelf();
        }
    }


}