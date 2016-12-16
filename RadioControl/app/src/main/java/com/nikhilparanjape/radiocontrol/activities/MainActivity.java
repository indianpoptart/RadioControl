package com.nikhilparanjape.radiocontrol.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nikhilparanjape.radiocontrol.BuildConfig;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.NightModeReceiver;
import com.nikhilparanjape.radiocontrol.receivers.TimedAlarmReceiver;
import com.nikhilparanjape.radiocontrol.rootUtils.PingWrapper;
import com.nikhilparanjape.radiocontrol.rootUtils.RootAccess;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;
import com.nikhilparanjape.radiocontrol.services.CellRadioService;
import com.nikhilparanjape.radiocontrol.services.ScheduledAirplaneService;
import com.nikhilparanjape.radiocontrol.util.IabHelper;
import com.nikhilparanjape.radiocontrol.util.IabResult;
import com.nikhilparanjape.radiocontrol.util.Inventory;
import com.nikhilparanjape.radiocontrol.util.Purchase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import it.gmariotti.changelibs.library.Util;
import it.gmariotti.changelibs.library.view.ChangeLogRecyclerView;

import static android.app.AlarmManager.*;

/**
 * Created by Nikhil Paranjape on 11/3/2015.
 */

public class MainActivity extends AppCompatActivity {
    private static final String PRIVATE_PREF = "prefs";
    private static final String VERSION_KEY = "version_number";

    Drawable icon;
    Drawable carrierIcon;
    Drawable wifiIcon;
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
    //static final String ITEM_TEST_PURCHASE = "com.nikhilparanjape.radiocontrol.donate.test_purchase2";


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
        clayout = (CoordinatorLayout) findViewById(R.id.clayout);
        final ProgressBar dialog = (ProgressBar) findViewById(R.id.pingProgressBar);
        final ActionBar actionBar = getSupportActionBar();

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, TutorialActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
                String intervalTime = getPrefs.getString("interval_prefs","10");
                boolean airplaneService = getPrefs.getBoolean("isAirplaneService", false);

                if(!intervalTime.equals("0") && airplaneService){
                    Intent i= new Intent(getApplicationContext(), ScheduledAirplaneService.class);
                    getBaseContext().startService(i);
                    Log.d("RadioControl", "Service launched");
                }
            }
        });

        // Start the thread
        t.start();
        //Hides the progress dialog
        dialog.setVisibility(View.GONE);

        //Sets the actionbar with hamburger
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_menu).color(Color.WHITE).sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP).paddingDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxnZmUx4gqEFCsMW+/uPXIzJSaaoP4J/2RVaxYT9Be0jfga0qdGF+Vq56mzQ/LYEZgLvFelGdWwXJ5Izq5Wl/cEW8cExhQ/WDuJvYVaemuU+JnHP1zIZ2H28NtzrDH0hb59k9R8owSx7NPNITshuC4MPwwOQDgDaYk02Hgi4woSzbDtyrvwW1A1FWpftb78i8Pphr7bT14MjpNyNznk4BohLMncEVK22O1N08xrVrR66kcTgYs+EZnkRKk2uPZclsPq4KVKG8LbLcxmDdslDBnhQkSPe3ntAC8DxGhVdgJJDwulcepxWoCby1GcMZTUAC1OKCZlvGRGSwyfIqbqF2JQIDAQAB";

        //initializes iab helper
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result)
            {
                if (!result.isSuccess()) {
                    Log.d("RadioControl", "In-app Billing setup failed: " + result);
                } else {
                    Log.d("RadioControl", "In-app Billing is set up OK");
                }
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
        final TextView statusText = (TextView)findViewById(R.id.statusText);
        final TextView linkText = (TextView)findViewById(R.id.linkSpeed);
        final TextView connectionStatusText = (TextView) findViewById(R.id.pingStatus);
        Switch toggle = (Switch) findViewById(R.id.enableSwitch);

        //Checks for root
        rootInit();

        //checks if ads should be enabled
        if(!pref.getBoolean("disableAds",false)){
            if(!pref.getBoolean("isDonated",false)){
                runOnUiThread(new Runnable() {
                    public void run() {
                        AdView mAdView = (AdView) findViewById(R.id.adView);
                        AdRequest adRequest = new AdRequest.Builder().build();
                        mAdView.loadAd(adRequest);
                    }
                });
            }
        }

        //LinkSpeed Button
        Button linkSpeedButton = (Button) findViewById(R.id.linkSpeedButton);

        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isDeveloper",false)){
            linkSpeedButton.setVisibility(View.GONE);
            linkText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            linkSpeedButton.setVisibility(View.VISIBLE);
            linkText.setVisibility(View.VISIBLE);
        }

        linkSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showWifiInfoDialog();
                int linkspeed = Utilities.linkSpeed(getApplicationContext());
                int GHz = Utilities.frequency(getApplicationContext());
                NetworkInfo test = Utilities.getNetworkInfo(getApplicationContext());
                NetworkInfo.State b = Utilities.getMobileState(getApplicationContext());
                Network[] c = Utilities.getMobState(getApplicationContext());
                String d = Utilities.getNetworkType(getApplicationContext());
                ServiceState state = new ServiceState();
                Log.d("RadioControl","Test: " + test);
                Log.d("RadioControl","Test2: " + b);
                Log.d("RadioControl","Test3: " + Arrays.toString(c));
                Log.d("RadioControl","Test4: " + d);
                Log.d("RadioControl","Test5: " + util.getCellStrength());
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
            }

        });

        //Connection Test button (Dev Feature)
        final Button conn = (Button) findViewById(R.id.pingTestButton);
        Button serviceTest = (Button) findViewById(R.id.airplane_service_test);
        Button nightCancel = (Button) findViewById(R.id.night_mode_cancel);
        Button radioOffButton = (Button) findViewById(R.id.cellRadioOff);
        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.GONE);
            serviceTest.setVisibility(View.GONE);
            nightCancel.setVisibility(View.GONE);
            connectionStatusText.setVisibility(View.GONE);
            radioOffButton.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.VISIBLE);
            serviceTest.setVisibility(View.VISIBLE);
            nightCancel.setVisibility(View.VISIBLE);
            connectionStatusText.setVisibility(View.VISIBLE);
            radioOffButton.setVisibility(View.VISIBLE);
        }

        conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionStatusText.setText(R.string.ping);
                connectionStatusText.setTextColor(getResources().getColor(R.color.material_grey_50));
                dialog.setVisibility(View.VISIBLE);
                new AsyncBackgroundTask(getApplicationContext()).execute("");
            }

        });
        SharedPreferences.Editor editor2 = sharedPref.edit();
        serviceTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(getApplicationContext(), ScheduledAirplaneService.class);
                getBaseContext().startService(i);
                util.scheduleAlarm(getApplicationContext());
                Log.d("RadioControl", "Service started");
            }

        });
        nightCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NightModeReceiver.class);
                final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), NightModeReceiver.REQUEST_CODE,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pIntent);
            }

        });
        radioOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};
                //RootAccess.runCommands(cellOffCmd);
                Intent cellIntent = new Intent(getApplicationContext(), CellRadioService.class);
                startService(cellIntent);
                util.scheduleRootAlarm(getApplicationContext());
            }

        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
        //params.setMargins(0, 85, 16, 85); //substitute parameters for left, top, right, bottom
        //fab.setLayoutParams(params);

        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_network_check_white_48dp));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setVisibility(View.VISIBLE);
                new AsyncBackgroundTask(getApplicationContext()).execute("");

            }
        });

        drawerCreate(); //Initalizes Drawer

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    editor.putInt("isActive",0);
                    statusText.setText("is Disabled");
                    statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                    editor.apply();

                } else {
                    editor.putInt("isActive",1);
                    statusText.setText("is Enabled");
                    statusText.setTextColor(getResources().getColor(R.color.status_activated));
                    editor.apply();
                }
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
            Log.d("RadioControl","Unable to get version name");
        }

        if (currentVersionNumber > savedVersionNumber) {
            showUpdated();
            editor.putInt(VERSION_KEY, currentVersionNumber);
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
    }


    //Method to create the Navigation Drawer
    public void drawerCreate(){
        // Get System TELEPHONY service reference
        TelephonyManager tManager = (TelephonyManager) getBaseContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        // Get carrier name (Network Operator Name)
        String carrierName = tManager.getSimOperatorName();
        Log.d("RadioControl",carrierName);

        //Drawable lg = getResources().getDrawable(R.mipmap.lg);
        if(getDeviceName().contains("Nexus 6P")){
            icon = getResources().getDrawable(R.mipmap.huawei);
        }
        else if(getDeviceName().contains("Motorola")){
            icon = getResources().getDrawable(R.mipmap.moto2);
        }
        else if(getDeviceName().contains("Pixel")){
            icon = getResources().getDrawable(R.mipmap.google);
        }
        else if(getDeviceName().contains("LG")){
            icon = getResources().getDrawable(R.mipmap.lg);
        }
        else if(getDeviceName().contains("Samsung")){
            icon = getResources().getDrawable(R.mipmap.samsung);
        }
        else if(getDeviceName().contains("OnePlus")){
            icon = getResources().getDrawable(R.mipmap.oneplus);
        }
        else{
            icon = getResources().getDrawable(R.mipmap.ic_launcher);
        }

        // Carrier icon
        if(carrierName.contains("Fi Network") || carrierName.contains("Project Fi")){
            carrierIcon = getResources().getDrawable(R.mipmap.fi_logo);
        }
        else if(carrierName.contains("AT&T") || carrierName.contains("att")){
            carrierIcon = getResources().getDrawable(R.mipmap.att_logo);
        }
        else if(carrierName.contains("Republic") || carrierName.contains("republic")){
            carrierIcon = getResources().getDrawable(R.mipmap.republic_logo);
        }
        else if(carrierName.contains("sprint") || carrierName.contains("Sprint")){
            carrierIcon = getResources().getDrawable(R.mipmap.sprint_logo);
        }
        else if(carrierName.contains("T-Mobile")){
            carrierIcon = getResources().getDrawable(R.mipmap.tmobile_logo);
        }
        else if(carrierName.contains("Verizon")){
            carrierIcon = getResources().getDrawable(R.mipmap.verizon_logo);
        }
        else if(carrierName.contains("Android") || carrierName.equals("")){
            carrierIcon = getResources().getDrawable(R.mipmap.android_logo);
            carrierName = "No Carrier";
        }
        else if(carrierName.contains("Vodafone")){
            carrierIcon = getResources().getDrawable(R.mipmap.vodafone_logo);
        }
        else{
            carrierIcon = getResources().getDrawable(R.mipmap.android_logo);
        }

        //WiFi Icon
        String ssid = util.getCurrentSsid(getApplicationContext());
        if(!ssid.equals("Not Connected")){
            wifiIcon = getResources().getDrawable(R.mipmap.wifi_on);
        }
        else{
            wifiIcon = getResources().getDrawable(R.mipmap.wifi_off);
        }
        //Creates navigation drawer header
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.mipmap.header)
                .addProfiles(
                        new ProfileDrawerItem().withName(getDeviceName()).withEmail("v" + versionName).withIcon(icon),
                        new ProfileDrawerItem().withName(getString(R.string.netOperator)).withEmail(carrierName).withIcon(carrierIcon),
                        new ProfileDrawerItem().withName(getString(R.string.wifiNetwork)).withEmail(ssid).withIcon(wifiIcon)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        //Creates navigation drawer items
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName(R.string.home).withIcon(GoogleMaterial.Icon.gmd_wifi);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName(R.string.settings).withIcon(GoogleMaterial.Icon.gmd_settings);
        SecondaryDrawerItem item3 = new SecondaryDrawerItem().withName(R.string.about).withIcon(GoogleMaterial.Icon.gmd_info);
        SecondaryDrawerItem item4 = new SecondaryDrawerItem().withName(R.string.donate).withIcon(GoogleMaterial.Icon.gmd_attach_money);
        SecondaryDrawerItem item5 = new SecondaryDrawerItem().withName(R.string.sendFeedback).withIcon(GoogleMaterial.Icon.gmd_send);
        SecondaryDrawerItem item6 = new SecondaryDrawerItem().withName(R.string.stats).withIcon(GoogleMaterial.Icon.gmd_timeline);

        //Create navigation drawer
        result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(false)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        item6,
                        item3,
                        new DividerDrawerItem(),
                        item4,
                        item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
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
                                Snackbar.make(clayout, "No log file found", Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }
                        else if (position == 5) {
                            startAboutActivity();
                            Log.d("drawer", "Started about activity");
                        } else if (position == 7) {
                            //Donation
                            Log.d("RadioControl", "Donation button pressed");
                            if(util.isConnected(getApplicationContext())){
                                showDonateDialog();
                            }
                            else{
                                showErrorDialog();
                            }
                        } else if (position == 8) {
                            //Feedback Button, send as email
                            Log.d("RadioControl", "Feedback");
                            sendFeedback();
                        }
                        return false;
                    }
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
    //whats new dialog
    private void showWhatsNewDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        ChangeLogRecyclerView chgList= (ChangeLogRecyclerView) inflater.inflate(R.layout.dialog_whats_new, null);
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog

        builder.setView(chgList).setTitle(R.string.whatsNew)//sets title
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    public void showLatencyDialog(){
        new MaterialDialog.Builder(this)
                .title("Testing")
                .content("Please wait...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .show();
    }
    public void showUpdated() {
        new MaterialDialog.Builder(this)
                .title("RadioControl has been updated")
                .theme(Theme.LIGHT)
                .positiveText("GOT IT")
                .negativeText("WHAT'S NEW")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        String chk = which.name();
                        Log.d("RadioControl", "Updated: " + chk);
                        if(chk.equals("POSITIVE")){
                            dialog.dismiss();
                        }
                        else if(chk.equals("NEGATIVE")){
                            startChangelogActivity();
                        }
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
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v("RadioControl", "Donation Cancelled");
                        dialog.dismiss();
                    }

                });

        final AlertDialog alert = builder.create();
        alert.show();

        //Sets the purchase options
        Button oneButton = (Button) view.findViewById(R.id.oneDollar);
        oneButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alert.cancel(); //Closes the donate dialog
                buyItem(0); //Opens billing for set item
            }
        });
        Button threeButton = (Button) view.findViewById(R.id.threeDollar);
        threeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alert.cancel(); //Closes the donate dialog
                buyItem(1); //Opens billing for set item
            }
        });
        Button fiveButton = (Button) view.findViewById(R.id.fiveDollar);
        fiveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alert.cancel(); //Closes the donate dialog
                buyItem(2); //Opens billing for set item
            }
        });
        Button tenButton = (Button) view.findViewById(R.id.tenDollar);
        tenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alert.cancel(); //Closes the donate dialog
                buyItem(3); //Opens billing for set item
            }
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
    public void boolPrefEditor(String key, boolean value){
        SharedPreferences pref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key,value);
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

        drawerCreate();
        final SharedPreferences sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(!pref.getBoolean("disableAds",false)){
            if(!pref.getBoolean("isDonated",false)){
                runOnUiThread(new Runnable() {
                    public void run() {
                        AdView mAdView = (AdView) findViewById(R.id.adView);
                        AdRequest adRequest = new AdRequest.Builder().build();
                        AdRequest adRequestTest = new AdRequest.Builder()
                                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                                .build();
                        mAdView.loadAd(adRequest);
                    }
                });
            }
        }
        //Connection Test button
        Button conn = (Button) findViewById(R.id.pingTestButton);
        //Ping text
        TextView connectionStatusText = (TextView) findViewById(R.id.pingStatus);

        //Check if the easter egg is NOT activated
        if(!sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.GONE);
            connectionStatusText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            conn.setVisibility(View.VISIBLE);
            connectionStatusText.setVisibility(View.VISIBLE);
        }

        //LinkSpeed Button
        Button btn3 = (Button) findViewById(R.id.linkSpeedButton);
        final TextView linkText = (TextView)findViewById(R.id.linkSpeed);
        //LinkSpeed button and text
        if(!sharedPref.getBoolean("isDeveloper",false)){
            btn3.setVisibility(View.GONE);
            linkText.setVisibility(View.GONE);
        }
        else if(sharedPref.getBoolean("isDeveloper",false)){
            btn3.setVisibility(View.VISIBLE);
            linkText.setVisibility(View.VISIBLE);
        }
        TextView statusText = (TextView)findViewById(R.id.statusText);
        Switch toggle = (Switch) findViewById(R.id.enableSwitch);

        if(!rootInit()){
            toggle.setClickable(false);
            statusText.setText(R.string.noRoot);
            statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
        }

        if(sharedPref.getInt("isActive",1) == 1){
            if(!rootInit()){
                toggle.setClickable(false);
                statusText.setText(R.string.noRoot);
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
            }
            else{
                statusText.setText(R.string.rEnabled);
                statusText.setTextColor(getResources().getColor(R.color.status_activated));
                toggle.setChecked(true);
            }

        }
        else if(sharedPref.getInt("isActive",0) == 0){
            if(!rootInit()){
                toggle.setClickable(false);
                statusText.setText(R.string.noRoot);
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
            }
            else{
                statusText.setText(R.string.rDisabled);
                statusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                toggle.setChecked(false);
            }

        }
    }
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {
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

        }
    };
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

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
        }
        //Check if $2.99
        else if(bill == 1){
            mHelper.launchPurchaseFlow(this, ITEM_THREE_DOLLAR, 002,
                    mPurchaseFinishedListener, "supportThreeDollar");
        }
        //Check if $4.99
        else if(bill == 2){
            mHelper.launchPurchaseFlow(this, ITEM_FIVE_DOLLAR, 003,
                    mPurchaseFinishedListener, "supportFiveDollar");
        }
        //Check if $9.99
        else if(bill == 3){
            mHelper.launchPurchaseFlow(this, ITEM_TEN_DOLLAR, 004,
                    mPurchaseFinishedListener, "supportTenDollar");

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
                Log.d("RadioControl", "There was an error saving the log: " + e);
            }
        }
    }

    private class AsyncBackgroundTask extends AsyncTask<String, Void, PingWrapper> {
        Context context;
        private ProgressBar dialog;

        public AsyncBackgroundTask(MainActivity activity) {
            dialog = (ProgressBar) findViewById(R.id.pingProgressBar);
            dialog.setIndeterminate(true);
        }

        public AsyncBackgroundTask(Context context) {
            this.context = context;
        }
        @Override
        protected PingWrapper doInBackground(String... params) {
            Runtime runtime = Runtime.getRuntime();
            PingWrapper w = new PingWrapper();
            StringBuffer echo = new StringBuffer();
            String s = "";
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 4 8.8.8.8");
                int exitValue = ipProcess.waitFor();
                Log.d("RadioControl", "Latency Test returned " + exitValue);
                if(exitValue == 0){
                    InputStreamReader reader = new InputStreamReader(ipProcess.getInputStream());
                    BufferedReader buf = new BufferedReader(reader);
                    String line = "";
                    while((line = buf.readLine()) != null){
                        echo.append(line + "\n");
                    }
                    s = util.getPingStats(echo.toString());
                }

                if(exitValue == 0){
                    w.exitCode = true;
                }
                w.status = s;

            }
            catch (IOException e){ e.printStackTrace(); }
            catch (InterruptedException e) { e.printStackTrace(); }
            return w;
        }
        @Override
        protected void onPostExecute(PingWrapper w) {
            dialog = (ProgressBar)findViewById(R.id.pingProgressBar);
            dialog.setVisibility(View.GONE);
            final TextView connectionStatusText = (TextView) findViewById(R.id.pingStatus);
            Log.d("RadioControl","Status: " + w.status);
            double status;
            boolean isDouble = true;
            String pStatus;
            try{
                Double.parseDouble(w.status);
            } catch(Exception e){
                isDouble = false;
                Log.d("RadioControl", "Not a double");
            }
            try{
                pStatus = w.status;

                if(isDouble){
                    status = Double.parseDouble(w.status);
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
                Snackbar.make(findViewById(android.R.id.content), "An error has occurred", Snackbar.LENGTH_LONG).show();
            }

            boolean result = w.exitCode;

            if(result){
                if(Utilities.isConnectedWifi(getApplicationContext())){
                    connectionStatusText.setText(R.string.connectedWifi);
                    connectionStatusText.setTextColor(getResources().getColor(R.color.status_activated));
                    writeLog(getString(R.string.connectedWifi),context);
                }
                else if(Utilities.isConnectedMobile(getApplicationContext())){
                    if(Utilities.isConnectedFast(getApplicationContext())){
                        connectionStatusText.setText(R.string.connectedFCell);
                        connectionStatusText.setTextColor(getResources().getColor(R.color.status_activated));
                        writeLog(getString(R.string.connectedFCell),context);
                    }
                    else if(!Utilities.isConnectedFast(getApplicationContext())){
                        connectionStatusText.setText(R.string.connectedSCell);
                        connectionStatusText.setTextColor(getResources().getColor(R.color.status_activated));
                        writeLog(getString(R.string.connectedSCell),context);
                    }

                }

            }
            else{
                if(Utilities.isAirplaneMode(getApplicationContext()) && !Utilities.isConnected(getApplicationContext())){
                    connectionStatusText.setText(R.string.airplaneOn);
                    connectionStatusText.setTextColor(getResources().getColor(R.color.status_deactivated));
                }
                else{
                    connectionStatusText.setText(R.string.connectionUnable);
                    connectionStatusText.setTextColor(getResources().getColor(R.color.status_deactivated));
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
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}
