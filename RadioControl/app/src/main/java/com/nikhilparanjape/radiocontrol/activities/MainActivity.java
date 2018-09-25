package com.nikhilparanjape.radiocontrol.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.android.vending.billing.IInAppBillingService;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.PurchaseEvent;
import com.crashlytics.android.answers.RatingEvent;
import com.crashlytics.android.core.CrashlyticsCore;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.nikhilparanjape.radiocontrol.BuildConfig;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver;
import com.nikhilparanjape.radiocontrol.receivers.NightModeReceiver;
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver;
import com.nikhilparanjape.radiocontrol.rootUtils.PingWrapper;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService;
import com.nikhilparanjape.radiocontrol.services.CellRadioService;
import com.nikhilparanjape.radiocontrol.services.PersistenceService;
import com.nikhilparanjape.radiocontrol.services.TestJobService;
import com.nikhilparanjape.radiocontrol.util.IabHelper;
import com.nikhilparanjape.radiocontrol.util.IabResult;
import com.nikhilparanjape.radiocontrol.util.Inventory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Currency;

import io.fabric.sdk.android.Fabric;


/**
 * Created by Nikhil Paranjape on 11/3/2015.
 */

public class MainActivity extends AppCompatActivity {
    private static final String PRIVATE_PREF = "prefs";
    private static final String VERSION_KEY = "version_number";

    Drawable icon;
    Drawable carrierIcon;
    Drawer result;
    String versionName = BuildConfig.VERSION_NAME;
    Utilities util = new Utilities();
    IInAppBillingService mService;
    CoordinatorLayout clayout;
    static final String ITEM_SKU = "com.nikhilparanjape.radiocontrol.test_donate1";
    static final String ITEM_ONE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.one";
    static final String ITEM_THREE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.three";
    static final String ITEM_FIVE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.five";
    static final String ITEM_TEN_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.ten";
    private ComponentName mServiceComponent;


    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };
    IabHelper mHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clayout = findViewById(R.id.clayout);
        final ProgressBar dialog = findViewById(R.id.pingProgressBar);
        mServiceComponent = new ComponentName(this, TestJobService.class);

        // Handle Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        //  Declare a new thread to do a preference check
        Thread t = new Thread(() -> {
            //  Initialize SharedPreferences
            SharedPreferences getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            //  Create a new boolean and preference and set it to true if it's not already there
            boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
            //Gets the current android build version on device
            int currentapiVersion = Build.VERSION.SDK_INT;

            //  If the activity has never started before...
            if (isFirstStart) {
                //  Make a new preferences editor
                SharedPreferences.Editor e = getPrefs.edit();

                if (currentapiVersion >= 24) {
                    e.putBoolean("workmode", true);
                }

                //  Launch app intro
                Intent i = new Intent(MainActivity.this, TutorialActivity.class);
                startActivity(i);

                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("firstStart", false);

                //  Apply changes
                e.apply();
            }
            String intervalTime = getPrefs.getString("interval_prefs","10");
            boolean airplaneService = getPrefs.getBoolean("isAirplaneService", false);

            //Begin background service
            if(!intervalTime.equals("0") && airplaneService){
                Intent i= new Intent(getApplicationContext(), BackgroundAirplaneService.class);
                getBaseContext().startService(i);
                Log.d("RadioControl", "back Service launched");
            }
            if(getPrefs.getBoolean("workMode",true)){
                Intent i= new Intent(getApplicationContext(), PersistenceService.class);
                if(Build.VERSION.SDK_INT>=26) {
                    getBaseContext().startForegroundService(i);
                }else{
                    getBaseContext().startService(i);
                }
                Log.d("RadioControl", "persist Service launched");
            }

            if (!getPrefs.getBoolean("workMode",true)){
                registerForBroadcasts(getApplicationContext());
            }

            //Hides the progress dialog
            dialog.setVisibility(View.GONE);
        });

        // Start the thread
        t.start();

        //Sets the actionbar with hamburger
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_menu).color(Color.WHITE).sizeDp(IconicsDrawable.TOOLBAR_ICON_SIZE).paddingDp(IconicsDrawable.TOOLBAR_ICON_PADDING));
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        //test code
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxnZmUx4gqEFCsMW+/uPXIzJSaaoP4J/2RVaxYT9Be0jfga0qdGF+Vq56mzQ/LYEZgLvFelGdWwXJ5Izq5Wl/cEW8cExhQ/WDuJvYVaemuU+JnHP1zIZ2H28NtzrDH0hb59k9R8owSx7NPNITshuC4MPwwOQDgDaYk02Hgi4woSzbDtyrvwW1A1FWpftb78i8Pphr7bT14MjpNyNznk4BohLMncEVK22O1N08xrVrR66kcTgYs+EZnkRKk2uPZclsPq4KVKG8LbLcxmDdslDBnhQkSPe3ntAC8DxGhVdgJJDwulcepxWoCby1GcMZTUAC1OKCZlvGRGSwyfIqbqF2JQIDAQAB";

        //initializes iab helper
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(result -> {
            if (!result.isSuccess()) {
                Log.d("RadioControl", "In-app Billing setup failed: " + result);
            } else {
                Log.d("RadioControl", "In-app Billing is set up OK");
            }
        });

        //Checks for IAB support
        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        init();//initializes the whats new dialog

        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = sharedPref.edit();
        final TextView statusText = findViewById(R.id.statusText);
        final TextView linkText = findViewById(R.id.linkSpeed);
        final TextView connectionStatusText = findViewById(R.id.pingStatus);
        Switch toggle = findViewById(R.id.enableSwitch);

        //Checks for root
        rootInit();

        if(pref.getBoolean("allowFabric",false)){
            Fabric.with(this, new Crashlytics());
        } else{
            Fabric.with(this, new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder()
                            .disabled(true).build()).build());
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        //LinkSpeed Button
        Button linkSpeedButton = findViewById(R.id.linkSpeedButton);

        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isDeveloper",false)){
            linkSpeedButton.setVisibility(View.GONE);
            linkText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            linkSpeedButton.setVisibility(View.VISIBLE);
            linkText.setVisibility(View.VISIBLE);
        }

        linkSpeedButton.setOnClickListener(v -> {
            //showWifiInfoDialog();

            int linkspeed = Utilities.linkSpeed(getApplicationContext());
            int GHz = Utilities.frequency(getApplicationContext());
            Log.i("RadioControl", "Test1: " + Utilities.getCellStatus(getApplicationContext()));
            if(linkspeed == -1){
                linkText.setText(R.string.cellNetwork);
            }
            else{
                if(GHz == 2){
                    linkText.setText("Link speed: " + linkspeed + "Mbps @ 2.4 GHz");

                }
                else if(GHz == 5){
                    linkText.setText("Link speed: " + linkspeed + "Mbps @ 5 GHz");

                }

            }
        });

        //Connection Test button (Dev Feature)
        final Button conn = findViewById(R.id.pingTestButton);
        Button serviceTest = findViewById(R.id.airplane_service_test);
        Button nightCancel = findViewById(R.id.night_mode_cancel);
        Button radioOffButton = findViewById(R.id.cellRadioOff);
        Button forceCrashButton = findViewById(R.id.forceCrashButton);
        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.GONE);
            serviceTest.setVisibility(View.GONE);
            nightCancel.setVisibility(View.GONE);
            connectionStatusText.setVisibility(View.GONE);
            radioOffButton.setVisibility(View.GONE);
            forceCrashButton.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.VISIBLE);
            serviceTest.setVisibility(View.VISIBLE);
            nightCancel.setVisibility(View.VISIBLE);
            connectionStatusText.setVisibility(View.VISIBLE);
            radioOffButton.setVisibility(View.VISIBLE);
            forceCrashButton.setVisibility(View.VISIBLE);
        }

        conn.setOnClickListener(v -> {
            connectionStatusText.setText(R.string.ping);
            connectionStatusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.material_grey_50));
            dialog.setVisibility(View.VISIBLE);
            new AsyncBackgroundTask(getApplicationContext()).execute("");
        });
        serviceTest.setOnClickListener(v -> {
            Intent i= new Intent(getApplicationContext(), BackgroundAirplaneService.class);
            getBaseContext().startService(i);
            util.scheduleAlarm(getApplicationContext());
            Log.d("RadioControl", "Service started");
        });
        nightCancel.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), NightModeReceiver.class);
            final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), NightModeReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            if (alarm != null) {
                alarm.cancel(pIntent);
            }
        });
        radioOffButton.setOnClickListener(v -> {
            //String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};
            //RootAccess.runCommands(cellOffCmd);
            Intent cellIntent = new Intent(getApplicationContext(), CellRadioService.class);
            startService(cellIntent);
            util.scheduleRootAlarm(getApplicationContext());
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
        //params.setMargins(0, 85, 16, 85); //substitute parameters for left, top, right, bottom
        //fab.setLayoutParams(params);

        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_network_check_white_48dp));
        fab.setOnClickListener(view -> {
            dialog.setVisibility(View.VISIBLE);
            new AsyncBackgroundTask(getApplicationContext()).execute("");
        });

        drawerCreate(); //Initalizes Drawer

        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                editor.putInt("isActive",0);
                statusText.setText(R.string.showDisabled);
                statusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.status_deactivated));
                editor.apply();

            } else {
                editor.putInt("isActive",1);
                statusText.setText(R.string.showEnabled);
                statusText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.status_activated));
                editor.apply();
                Intent i = new Intent(getApplicationContext(), BackgroundAirplaneService.class);
                getApplicationContext().startService(i);
            }
        });

    }

    //Initialize method for the Whats new dialog
    private void init() {
        SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        int currentVersionNumber = 0;

        int savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0);

        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            currentVersionNumber = pi.versionCode;
        } catch (Exception e) {
            Log.e("RadioControl", "Unable to get version number");
        }
        if (currentVersionNumber > savedVersionNumber) {
            showUpdated();
            editor.putInt(VERSION_KEY, currentVersionNumber);
            editor.apply();
        }
        if (android.os.Build.VERSION.SDK_INT >= 24 && !sharedPref.getBoolean("workMode",false)){
            editor.putBoolean("workMode",true);
            editor.apply();
        }
    }

    //Start a new activity for sending a feedback email
    private void sendFeedback() {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{ getString(R.string.mail_feedback_email) });
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message));
        startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_feedback)));
        writeLog("Feedback sent",getApplicationContext());
        Answers.getInstance().logRating(new RatingEvent()
                .putRating(4)
                .putContentName("RadioControl Feedback")
                .putContentType("Feedback")
                .putContentId("feedback-001"));
    }

    public void registerForBroadcasts(Context context) {
        ComponentName component = new ComponentName(context, WifiReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    //Method to create the Navigation Drawer
    public void drawerCreate(){

        String carrierName = "Not Rooted";
        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);


        //Drawable lg = getResources().getDrawable(R.mipmap.lg);
        if(getDeviceName().contains("Nexus 6P")){
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.huawei);
        }
        else if(getDeviceName().contains("Motorola")){
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.moto2);
        }
        else if(getDeviceName().contains("Pixel")){
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.google);
        }
        else if(getDeviceName().contains("LG")){
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.lg);
        }
        else if(getDeviceName().contains("Samsung")){
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.samsung);
        }
        else if(getDeviceName().contains("OnePlus")){
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.oneplus);
        }
        else{
            icon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.ic_launcher);
        }

        // root icon
        if(rootInit()){
            carrierIcon = new IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_check_circle)
                    .color(Color.GREEN);
            carrierName = "Rooted";
        }
        else {
            carrierIcon = new IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_error_outline)
                    .color(Color.RED);
        }
        Drawable headerIcon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.header);

        if(sharedPref.getBoolean("isDeveloper",false)){
            headerIcon = ContextCompat.getDrawable(getApplicationContext(),R.mipmap.header2);
        }


        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(headerIcon)
                .addProfiles(
                        new ProfileDrawerItem().withName(getDeviceName()).withEmail("v" + versionName).withIcon(icon),
                        new ProfileDrawerItem().withName("Root Status").withEmail(carrierName).withIcon(carrierIcon)
                )
                .withOnAccountHeaderListener((view, profile, currentProfile) -> false)
                .build();
        //Creates navigation drawer items
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.home).withIcon(GoogleMaterial.Icon.gmd_wifi);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withIdentifier(2).withName(R.string.settings).withIcon(GoogleMaterial.Icon.gmd_settings);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withIdentifier(3).withName(R.string.about).withIcon(GoogleMaterial.Icon.gmd_info);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withIdentifier(4).withName(R.string.donate).withIcon(GoogleMaterial.Icon.gmd_attach_money);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withIdentifier(5).withName(R.string.sendFeedback).withIcon(GoogleMaterial.Icon.gmd_send);
        SecondaryDrawerItem item6 = new SecondaryDrawerItem().withIdentifier(6).withName(R.string.stats).withIcon(GoogleMaterial.Icon.gmd_timeline);
        SecondaryDrawerItem item7 = new SecondaryDrawerItem().withIdentifier(7).withName(R.string.standby_drawer_name).withIcon(GoogleMaterial.Icon.gmd_pause_circle_outline);
        SecondaryDrawerItem item8 = new SecondaryDrawerItem().withIdentifier(8).withName(R.string.drawer_string_troubleshooting).withIcon(GoogleMaterial.Icon.gmd_help);

        //Create navigation drawer
        result = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item6,
                        item3,
                        new DividerDrawerItem(),
                        item8,
                        item4,
                        item5,
                        item7
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    Log.d("RadioControl", "The drawer is at position " + position);
                    //About button
                    if (position == 3) {
                        startSettingsActivity();

                        Log.d("drawer", "Started settings activity");
                    }else if(position == 4){
                        File log = new File(getApplicationContext().getFilesDir(), "radiocontrol.log");
                        if(log.exists() && log.canRead()) {
                            Log.d("RadioControl", "Log Exists");
                            startStatsActivity();
                        } else{
                            result.setSelection(item1);
                            Snackbar.make(clayout, "No log file found", Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    }
                    else if (position == 5) {
                        startAboutActivity();
                        Log.d("drawer", "Started about activity");
                    } else if (position == 7) {
                        result.setSelection(item1);
                        Snackbar.make(clayout, "Coming in v5.1!", Snackbar.LENGTH_LONG)
                                .show();
                        startTroubleActivity();
                    } else if (position == 8) {
                        //Donation
                        result.setSelection(item1);
                        Log.d("RadioControl", "Donation button pressed");
                        if(Utilities.isConnected(getApplicationContext())){
                            showDonateDialog();
                        }
                        else{
                            showErrorDialog();
                        }
                    } else if (position == 9) {
                        result.setSelection(item1);
                        Log.d("RadioControl", "Feedback");
                        sendFeedback();
                    } else if (position == 10) {
                        result.setSelection(item1);
                        Log.d("RadioControl", "Standby Mode Engaged");
                        startStandbyMode();
                    }
                    return false;
                })
                .build();
        result.setSelection(item1);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(result.isDrawerOpen()){
                    result.closeDrawer();
                }
                else{
                    result.openDrawer();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showToast(String message) {
        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();
        if(message.equalsIgnoreCase("true")){
            editor.putBoolean("isStandbyDialog",true);
            editor.apply();
        }
        else{
            editor.putBoolean("isStandbyDialog",false);
            editor.apply();
        }

    }
    public void startStandbyMode(){
        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        if(!sharedPref.getBoolean("isStandbyDialog",false)){
            new MaterialDialog.Builder(this)
                    .iconRes(R.mipmap.ic_launcher)
                    .limitIconToDefaultSize()
                    .title(Html.fromHtml(getString(R.string.permissionSample, getString(R.string.app_name))))
                    .positiveText("Ok")
                    .backgroundColorRes(R.color.material_drawer_dark_background)
                    .onAny((dialog, which) -> showToast(""+dialog.isPromptCheckBoxChecked()))
                    .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                    .show();
        }

        editor.putInt("isActive",0);
        editor.apply();
        Intent intentAction = new Intent(getApplicationContext(),ActionReceiver.class);
        Log.d("RadioControl","Value Changed");
        Toast.makeText(getApplicationContext(), "Standby Mode enabled",
                Toast.LENGTH_LONG).show();

        PendingIntent pIntentlogin = PendingIntent.getBroadcast(getApplicationContext(),1,intentAction,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder note = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                .setContentTitle("Standby Mode")
                .setContentText("RadioControl services have been paused")
                //Using this action button I would like to call logTest
                .addAction(R.drawable.ic_done,"Turn OFF standby mode", pIntentlogin)
                .setPriority(-2)
                .setOngoing(true);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(10110, note.build());
        }
    }

    //starts troubleshooting activity
    public void startTroubleActivity() {
        Intent intent = new Intent(this, TroubleshootingActivity.class);
        startActivity(intent);
    }
    //starts about activity
    public void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }
    //starts settings activity
    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    //starts settings activity
    public void startStatsActivity() {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }
    public void startChangelogActivity(){
        Intent intent = new Intent(this, ChangeLogActivity.class);
        startActivity(intent);
    }
    public void showUpdated() {
        new MaterialDialog.Builder(this)
                .title("RadioControl has been updated")
                .theme(Theme.DARK)
                .positiveText("GOT IT")
                .negativeText("WHAT'S NEW")
                .onAny((dialog, which) -> {
                    String chk = which.name();
                    Log.d("RadioControl", "Updated: " + chk);
                    if(chk.equals("POSITIVE")){
                        dialog.dismiss();
                    }
                    else if(chk.equals("NEGATIVE")){
                        startChangelogActivity();
                    }
                })
                .show();
    }
    //donate dialog
    private void showDonateDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_donate, null);//Initializes the view for donate dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog


        builder.setView(view).setTitle(R.string.donate)//sets title
                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                    Log.v("RadioControl", "Donation Cancelled");
                    dialog.dismiss();
                });

        final AlertDialog alert = builder.create();
        alert.show();

        //Sets the purchase options
        Button oneButton = view.findViewById(R.id.oneDollar);
        oneButton.setOnClickListener(v -> {
            alert.cancel(); //Closes the donate dialog
            buyItem(0); //Opens billing for set item
        });
        Button threeButton = view.findViewById(R.id.threeDollar);
        threeButton.setOnClickListener(v -> {
        alert.cancel(); //Closes the donate dialog
            buyItem(1); //Opens billing for set item
        });
        Button fiveButton = view.findViewById(R.id.fiveDollar);
        fiveButton.setOnClickListener(v -> {
            alert.cancel(); //Closes the donate dialog
            buyItem(2); //Opens billing for set item
        });
        Button tenButton = view.findViewById(R.id.tenDollar);
        tenButton.setOnClickListener(v -> {
            alert.cancel(); //Closes the donate dialog
            buyItem(3); //Opens billing for set item
        });


    }
    //Internet Error dialog
    private void showErrorDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_no_internet, null);//Initializes the view for error dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog
        TextView title = new TextView(this);
        // You Can Customise your Title here
        title.setText(R.string.noInternet);
        title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);

        builder.setCustomTitle(title);
        builder.setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    //Grab device make and model for drawer
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    //Capitalizes names for devices. Used by getDeviceName()
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    public boolean rootInit(){
        try{
            Process p = Runtime.getRuntime().exec("su");
            return true;
        }catch (IOException e){
            return false;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (pref.getBoolean("workMode",true)) {
            Intent i= new Intent(getApplicationContext(), PersistenceService.class);
            if(Build.VERSION.SDK_INT>=26) {
                //getBaseContext().startForegroundService(i);
            }else{
                //getBaseContext().startService(i);
            }

        }else{
            registerForBroadcasts(getApplicationContext());
        }
        drawerCreate();


        if(pref.getBoolean("allowFabric",true)){
            Fabric.with(this, new Crashlytics());
        }else{
            Fabric.with(this, new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder()
                            .disabled(true).build()).build());
        }
        //Connection Test button (Dev Feature)
        final Button conn = findViewById(R.id.pingTestButton);
        Button serviceTest = findViewById(R.id.airplane_service_test);
        Button nightCancel = findViewById(R.id.night_mode_cancel);
        Button radioOffButton = findViewById(R.id.cellRadioOff);
        Button forceCrashButton = findViewById(R.id.forceCrashButton);
        final TextView connectionStatusText = findViewById(R.id.pingStatus);
        //LinkSpeed Button
        Button btn3 = findViewById(R.id.linkSpeedButton);
        final TextView linkText = findViewById(R.id.linkSpeed);
        TextView statusText = findViewById(R.id.statusText);
        Switch toggle = findViewById(R.id.enableSwitch);

        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.GONE);
            serviceTest.setVisibility(View.GONE);
            nightCancel.setVisibility(View.GONE);
            connectionStatusText.setVisibility(View.GONE);
            radioOffButton.setVisibility(View.GONE);
            forceCrashButton.setVisibility(View.GONE);
            btn3.setVisibility(View.GONE);
            linkText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.VISIBLE);
            serviceTest.setVisibility(View.VISIBLE);
            nightCancel.setVisibility(View.VISIBLE);
            connectionStatusText.setVisibility(View.VISIBLE);
            radioOffButton.setVisibility(View.VISIBLE);
            forceCrashButton.setVisibility(View.VISIBLE);
            btn3.setVisibility(View.VISIBLE);
            linkText.setVisibility(View.VISIBLE);
        }

        if(!rootInit()){
            toggle.setClickable(false);
            statusText.setText(R.string.noRoot);
            statusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_deactivated));
        }

        if(sharedPref.getInt("isActive",1) == 1){
            if(!rootInit()){
                toggle.setClickable(false);
                statusText.setText(R.string.noRoot);
                statusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_deactivated));
            }
            else{
                statusText.setText(R.string.rEnabled);
                statusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_activated));
                toggle.setChecked(true);
            }

        }
        else if(sharedPref.getInt("isActive",0) == 0){
            if(!rootInit()){
                toggle.setClickable(false);
                statusText.setText(R.string.noRoot);
                statusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_deactivated));
            }
            else{
                statusText.setText(R.string.rDisabled);
                statusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_deactivated));
                toggle.setChecked(false);
            }

        }
    }
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = (result, purchase) -> {
                if (result.isFailure()) {
                    if(result.toString().contains("Purchase signature verification failed")){
                        consumeItem();
                        Toast.makeText(MainActivity.this, R.string.donationThanks, Toast.LENGTH_LONG).show();
                        Log.d("RadioControl","In-app purchase succeeded, however verification failed");
                        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putBoolean("isDonated",true);
                        editor.apply();
                    }
                    else if(result.toString().contains("User cancelled")){
                        //Toast.makeText(MainActivity.this, R.string.donationCancel, Toast.LENGTH_LONG).show();
                        Snackbar.make(findViewById(android.R.id.content), R.string.donationCancel, Snackbar.LENGTH_LONG)
                                .show();
                        Log.d("RadioControl","Purchase Cancelled");
                    }
                    else if(result.toString().contains("Item Already Owned")){
                        Toast.makeText(MainActivity.this, R.string.donationExists, Toast.LENGTH_LONG).show();
                        Log.d("RadioControl","Donation already purchased");
                    }
                    else{
                        Toast.makeText(MainActivity.this, getString(R.string.donationFailed) + result, Toast.LENGTH_LONG).show();
                        Log.d("RadioControl","In-app purchase failed: " + result + "Purchase: " + purchase);
                    }

                }
                else if(result.isSuccess()){
                    Toast.makeText(MainActivity.this, R.string.donationThanks, Toast.LENGTH_LONG).show();
                    Log.d("RadioControl","In-app purchase succeeded");
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("isDonated",true);
                    editor.apply();
                    //consumeItem();

                }

            };
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            (purchase, result) -> {

                if (result.isSuccess()) {
                    Toast.makeText(MainActivity.this, R.string.donationThanks, Toast.LENGTH_LONG).show();
                    Log.d("RadioControl","In-app purchase succeeded");
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean("isDonated",true);
                    editor.apply();

                } else if(result.isFailure()){
                    //Toast.makeText(MainActivity.this, "Thanks for the thought, but the purchase failed", Toast.LENGTH_LONG).show();
                    Log.d("RadioControl","In-app purchase failed");
                }
            };

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }
    public void buyItem(int bill){
        //Check if $0.99
        if(bill == 0){
            mHelper.launchPurchaseFlow(this, ITEM_ONE_DOLLAR, 001,
                    mPurchaseFinishedListener, "supportOneDollar");
            Answers.getInstance().logPurchase(new PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(0.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation1")
                    .putItemType("Donation")
                    .putItemId("sku-001")
                    .putSuccess(true));
        }
        //Check if $2.99
        else if(bill == 1){
            mHelper.launchPurchaseFlow(this, ITEM_THREE_DOLLAR, 002,
                    mPurchaseFinishedListener, "supportThreeDollar");
            Answers.getInstance().logPurchase(new PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(2.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation3")
                    .putItemType("Donation")
                    .putItemId("sku-002")
                    .putSuccess(true));
        }
        //Check if $4.99
        else if(bill == 2){
            mHelper.launchPurchaseFlow(this, ITEM_FIVE_DOLLAR, 003,
                    mPurchaseFinishedListener, "supportFiveDollar");
            Answers.getInstance().logPurchase(new PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(4.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation5")
                    .putItemType("Donation")
                    .putItemId("sku-003")
                    .putSuccess(true));
        }
        //Check if $9.99
        else if(bill == 3){
            mHelper.launchPurchaseFlow(this, ITEM_TEN_DOLLAR, 004,
                    mPurchaseFinishedListener, "supportTenDollar");
            Answers.getInstance().logPurchase(new PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(9.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation10")
                    .putItemType("Donation")
                    .putItemId("sku-004")
                    .putSuccess(true));

        }


    }

    public void writeLog(String data, Context c){
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
                Log.e("RadioControl", "Error saving log");
            }
        }
    }

    private class AsyncBackgroundTask extends AsyncTask<String, Void, PingWrapper> {
        Context context;
        private ProgressBar dialog;

        AsyncBackgroundTask(Context context) {
            this.context = context;
        }
        @Override
        protected PingWrapper doInBackground(String... params) {
            Runtime runtime = Runtime.getRuntime();
            PingWrapper w = new PingWrapper();
            StringBuilder echo = new StringBuilder();
            String s = "";
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 4 8.8.8.8");
                int exitValue = ipProcess.waitFor();
                Log.d("RadioControl", "Latency Test returned " + exitValue);
                if(exitValue == 0){
                    InputStreamReader reader = new InputStreamReader(ipProcess.getInputStream());
                    BufferedReader buf = new BufferedReader(reader);
                    String line;
                    while((line = buf.readLine()) != null){
                        echo.append(line).append("\n");
                    }
                    s = Utilities.getPingStats(echo.toString());
                }

                if(exitValue == 0){
                    w.setExitCode(true);
                }
                w.setStatus(s);

            }
            catch (IOException | InterruptedException e){ e.printStackTrace(); }
            return w;
        }
        @Override
        protected void onPostExecute(PingWrapper w) {
            dialog = findViewById(R.id.pingProgressBar);
            dialog.setVisibility(View.GONE);
            final TextView connectionStatusText = findViewById(R.id.pingStatus);
            Log.d("RadioControl","Status: " + w.getStatus());
            double status;
            boolean isDouble = true;
            String pStatus;
            try{
                Double.parseDouble(w.getStatus());
            } catch(Exception e){
                isDouble = false;
                Log.d("RadioControl", "Not a double: " + e);
                Snackbar.make(clayout, "NumberFormatException " + w.getStatus(), Snackbar.LENGTH_LONG).show();
                Crashlytics.logException(e);
            }
            try{
                pStatus = w.getStatus();

                if(isDouble){
                    status = Double.parseDouble(w.getStatus());
                    if(status <= 50){
                        Snackbar.make(clayout, "Excellent Latency: " + status + " ms", Snackbar.LENGTH_LONG).show();
                    }
                    else if(status >= 51 && status <= 100){
                        Snackbar.make(clayout, "Average Latency: " + status + " ms", Snackbar.LENGTH_LONG).show();
                    }
                    else if(status >= 101 && status <= 200){
                        Snackbar.make(clayout, "Poor Latency: " + status + " ms", Snackbar.LENGTH_LONG).show();
                    }
                    else if(status >= 201){
                        Snackbar.make(clayout, "Poor Latency. VOIP and online gaming may suffer: " + status + " ms", Snackbar.LENGTH_LONG).show();
                    }
                }
                else {
                    //Check for packet loss stuff
                    if(pStatus.contains("100% packet loss")){
                        Snackbar.make(clayout, "100% packet loss detected", Snackbar.LENGTH_LONG).show();
                    }
                    else if(pStatus.contains("25% packet loss")){
                        Snackbar.make(clayout, "25% packet loss detected", Snackbar.LENGTH_LONG).show();
                    }
                    else if(pStatus.contains("50% packet loss")){
                        Snackbar.make(clayout, "50% packet loss detected", Snackbar.LENGTH_LONG).show();
                    }
                    else if(pStatus.contains("75% packet loss")){
                        Snackbar.make(clayout, "75% packet loss detected", Snackbar.LENGTH_LONG).show();
                    }
                    else if(pStatus.contains("unknown host")){
                        Snackbar.make(clayout, "Unknown host", Snackbar.LENGTH_LONG).show();
                    }
                }

            } catch(Exception e){
                Crashlytics.logException(e);
                Snackbar.make(findViewById(android.R.id.content), "An error has occurred", Snackbar.LENGTH_LONG).show();
            }

            boolean result = w.getExitCode();

            if(result){
                if(Utilities.isConnectedWifi(getApplicationContext())){
                    connectionStatusText.setText(R.string.connectedWifi);
                    connectionStatusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_activated));
                    writeLog(getString(R.string.connectedWifi),context);
                }
                else if(Utilities.isConnectedMobile(getApplicationContext())){
                    if(Utilities.isConnectedFast(getApplicationContext())){
                        connectionStatusText.setText(R.string.connectedFCell);
                        connectionStatusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_activated));
                        writeLog(getString(R.string.connectedFCell),context);
                    }
                    else if(!Utilities.isConnectedFast(getApplicationContext())){
                        connectionStatusText.setText(R.string.connectedSCell);
                        connectionStatusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_activated));
                        writeLog(getString(R.string.connectedSCell),context);
                    }

                }

            }
            else{
                if(Utilities.isAirplaneMode(getApplicationContext()) && !Utilities.isConnected(getApplicationContext())){
                    connectionStatusText.setText(R.string.airplaneOn);
                    connectionStatusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_deactivated));
                }
                else{
                    connectionStatusText.setText(R.string.connectionUnable);
                    connectionStatusText.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.status_deactivated));
                    writeLog(getString(R.string.connectionUnable),context);
                }

            }
        }
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                Log.d("RadioControl","QueryInventoryFinished Error");
            } else {
                mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU),
                        mConsumeFinishedListener);
            }
        }
    };

    public void forceCrash(View view) {
        throw new RuntimeException("This is a test crash");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Start service and provide it a way to communicate with this class.
        Intent startServiceIntent = new Intent(this, TestJobService.class);
        startService(startServiceIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        if (mHelper != null) mHelper.dispose();
        mHelper = null;

    }

    protected void onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(new Intent(this, TestJobService.class));
        super.onStop();
    }
}
