package com.nikhilparanjape.radiocontrol.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;

import com.nikhilparanjape.radiocontrol.R;

public class OnBootIntentService extends JobIntentService {

    public OnBootIntentService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel(getApplicationContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                new NotificationCompat.Builder(this, "Startup")
                        .setSmallIcon(R.drawable.ic_radiocontrol_main)
                        .setContentTitle("Startup Operations")
                        .setContentText("Running startup operations...")
                        .build();
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Startup";
            String description = "Channel for startup related notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("startup", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}