package com.nikhilparanjape.radiocontrol.rootUtils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver;
import com.nikhilparanjape.radiocontrol.receivers.NightModeReceiver;
import com.nikhilparanjape.radiocontrol.receivers.RootServiceReceiver;
import com.nikhilparanjape.radiocontrol.receivers.TimedAlarmReceiver;
import com.nikhilparanjape.radiocontrol.receivers.WakeupReceiver;
import com.nikhilparanjape.radiocontrol.services.TestJobService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import static android.app.AlarmManager.RTC;
import static android.app.AlarmManager.RTC_WAKEUP;


/**
 * Created by Nikhil on 2/3/2016.
 *
 * A custom Utilities class for RadioControl
 */
public class Utilities {

    /**
     * gets network ssid
     * @param context
     * @return
     */
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            ssid = connectionInfo.getSSID();
            ssid = ssid.substring(1, ssid.length()-1);
        }
        else if(!networkInfo.isConnected()){
            ssid = "Not Connected";
        }
        return ssid;
    }

    public static int getCellStatus(Context c){
        int z = 0;
        try {
            TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            List<CellInfo> cellInfoList = tm.getAllCellInfo();
            //This means cell is off
            if (cellInfoList.isEmpty()) {
                z = 1;
            }
            return z;
        } catch(SecurityException e){
            Log.e("RadioControl","Unable to get Location Permission",e);
        } catch(NullPointerException e){
            Log.e("RadioControl","NullPointer ",e);
        }
        return z;
    }

    /**
     * Checks link speed
     * @param c
     * @return
     */
    public static int linkSpeed(Context c){
        WifiManager wifiManager = (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
        Log.d("RadioControl", "Link speed = " + linkSpeed + "Mbps");
        return linkSpeed;
    }

    /**
     * Writes logs
     * @param c
     * @return
     */
    public static void writeLog(String data, Context c){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        if(preferences.getBoolean("enableLogs", false)){
            try{
                String h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString();
                File log = new File(c.getFilesDir(), "radiocontrol.log");
                if(!log.exists()) {
                    log.createNewFile();
                }
                String logPath = "radiocontrol.log";
                String string = "\n" + h + ": " + data;

                FileOutputStream fos = c.openFileOutput(logPath, Context.MODE_APPEND);
                fos.write(string.getBytes());
                fos.close();
            } catch(IOException e){
                Log.e("RadioControl", "Error writing log");
            }
        }
    }

    public void scheduleWakeupAlarm(Context c, int hour) {
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR, hour);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, WakeupReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, ActionReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);

    }
    public void scheduleNightWakeupAlarm(Context c, int hourofDay, int minute) {
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay);
        cal.add(Calendar.MINUTE, minute);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, TimedAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);

    }

    // Schedule the start of the service every 10 - 30 seconds
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, TestJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String intervalTimeString = preferences.getString("interval_prefs","10");
        int intervalTime = Integer.parseInt(intervalTimeString);

        builder.setMinimumLatency(intervalTime * 1000); // wait at least
        builder.setOverrideDeadline(intervalTime * 1000); // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        }
        jobScheduler.schedule(builder.build());
    }

    // Setup a recurring alarm every 15,30,60 minutes
    public void scheduleAlarm(Context c) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        String intervalTimeString = preferences.getString("interval_prefs","10");
        int intervalTime = Integer.parseInt(intervalTimeString);
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.MINUTE, intervalTime);
        Log.d("RadioControl","Interval: " + intervalTime + cal.getTime());

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, TimedAlarmReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        long interval = cal.getTimeInMillis();
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        Log.d("RadioControl","Time: " + (cal.getTimeInMillis()-firstMillis));
        //alarm.setInexactRepeating(RTC, firstMillis,
                //cal.getTimeInMillis(), pIntent);

        alarm.setInexactRepeating(RTC, firstMillis+interval, interval, pIntent);

    }

    public void scheduleRootAlarm(Context c) {
        Calendar cal = Calendar.getInstance();
        // start 10 seconds
        cal.add(Calendar.SECOND, 30);

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, RootServiceReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every X seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        alarm.setInexactRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);
        Log.d("RadioControl","RootClock enabled for " + cal.getTime());

    }
    public void cancelRootAlarm(Context c) {
        Intent intent = new Intent(c, RootServiceReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, RootServiceReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        Log.d("RadioControl","RootClock cancelled");
    }
    public void cancelAlarm(Context c) {
        Intent intent = new Intent(c, TimedAlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, TimedAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    public void cancelNightAlarm(Context c, int hourofDay, int minute) {
        Calendar cal = Calendar.getInstance();
        // start 30 seconds after boot completed
        cal.add(Calendar.HOUR_OF_DAY, hourofDay);
        cal.add(Calendar.MINUTE, minute);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(c, NightModeReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, NightModeReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(RTC_WAKEUP, firstMillis,
                cal.getTimeInMillis(), pIntent);
    }

    public void cancelWakeupAlarm(Context c) {
        Intent intent = new Intent(c, WakeupReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(c, WakeupReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    public static int frequency(Context c){
        WifiManager wifiManager = (WifiManager)c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int freq = wifiManager.getConnectionInfo().getFrequency();
        int GHz = freq/1000;
        if(GHz == 2){
            Log.d("RadioControl", "Frequency = " + freq + "MHz");
            return 2;
        }
        else if(GHz == 5){
            Log.d("RadioControl", "Frequency = " + freq + "MHz");
            return 5;
        }
        else
            return 0;

    }
    public static String getPingStats(String s) {
        try{
            String status;
            if (s.contains("0% packet loss")) {
                int start = s.indexOf("/mdev = ");
                int end = s.indexOf(" ms\n", start);
                s = s.substring(start + 8, end);
                String stats[] = s.split("/");
                return stats[2];
            } else if (s.contains("100% packet loss")) {
                status = "100% packet loss";
                return status;
            } else if (s.contains("% packet loss")) {
                status = "partial packet loss";
                return s;
            } else if (s.contains("unknown host")) {
                status = "unknown host";
                return status;
            } else {
                status = "unknown error in getPingStats";
                return status;
            }
        }catch(StringIndexOutOfBoundsException e){
            return "An error occured";
        }

    }

    /**
     * Makes a network alert
     * @param context
     * @return
     */
    public static void sendNote(Context context, String mes, boolean vibrate, boolean sound, boolean heads){
        createNotificationChannel(context);
        PendingIntent pi = PendingIntent.getActivity(context, 1, new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), 0);
        //Resources r = getResources();
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder builder = (new Notification.Builder(context, "NetworkAlert"))
                    .setContentTitle("Network Alert")
                    .setSmallIcon(R.drawable.ic_signal_cellular_connected_no_internet_2_bar_24px)
                    .setContentIntent(pi)
                    .setContentText("Your WiFi connection is not functioning")
                    .setAutoCancel(true);
            notification = builder.build();
            builder.notify();
        } else {
            NotificationCompat.Builder builder = (new androidx.core.app.NotificationCompat.Builder(context)
                    .setContentTitle("Network Alert")
                    .setSmallIcon(R.drawable.ic_signal_cellular_connected_no_internet_2_bar_24px)
                    .setContentIntent(pi)
                    .setContentText("Your WiFi connection is not functioning")
                    .setPriority(-1)
                    .setAutoCancel(true));
            notification = builder.build();
            builder.notify();
        }

    }
    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Network Alert";
            String description = "Channel for network related alerts";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("networkalert", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }

    /**
     * Enable WiFi without root
     *@param context
     */
    public void enableWifi(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

    }

    /**
     * Disable WiFi without root
     *@param context
     */
    public void disableWifi(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

    }

    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnectedOrConnecting() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }
    public static boolean isWifiOn(Context context){
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Check if there is any connectivity
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return info != null && info.isConnectedOrConnecting();
    }

    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnectedOrConnecting() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Check if there is any active call
     * @param context
     * @return
     */
    public boolean isCallActive(Context context){ AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode()== AudioManager.MODE_IN_CALL){
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnectedOrConnecting() && isConnectionFast(info.getType(), info.getSubtype()));
    }

    public static boolean isAirplaneMode(Context context){
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    public static String getNetworkType(Context context){
        try {
            NetworkInfo info = getNetworkInfo(context);
            int type = info.getType();
            int subType = info.getSubtype();

            if (type == ConnectivityManager.TYPE_WIFI) {
                return "WIFI";
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return "1xRTT"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return "CDMA"; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return "EDGE"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return "EVDO_0"; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return "EVDO_A"; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return "GPRS"; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return "HSDPA"; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return "HSPA"; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return "HSUPA"; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return "UMTS"; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return "EHRPD"; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return "EVDO_B"; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return "HSPAP"; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return "IDEN"; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return "LTE"; // ~ 10+ Mbps
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        return "UNKNOWN";
                }
            } else {
                return "UNKNOWN";
            }
        }
        catch(NullPointerException e){
            return "CELL";
        }

    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType){
        if(type==ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type == ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        }else{
            return false;
        }
    }
}
