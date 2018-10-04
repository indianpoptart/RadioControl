package com.nikhilparanjape.radiocontrol.activities

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.text.Html
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.android.vending.billing.IInAppBillingService
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.PurchaseEvent
import com.crashlytics.android.answers.RatingEvent
import com.crashlytics.android.core.CrashlyticsCore
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.nikhilparanjape.radiocontrol.BuildConfig
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver
import com.nikhilparanjape.radiocontrol.receivers.NightModeReceiver
import com.nikhilparanjape.radiocontrol.receivers.WifiReceiver
import com.nikhilparanjape.radiocontrol.rootUtils.PingWrapper
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities
import com.nikhilparanjape.radiocontrol.services.BackgroundAirplaneService
import com.nikhilparanjape.radiocontrol.services.CellRadioService
import com.nikhilparanjape.radiocontrol.services.PersistenceService
import com.nikhilparanjape.radiocontrol.services.TestJobService
import com.nikhilparanjape.radiocontrol.util.IabHelper

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.Currency

import io.fabric.sdk.android.Fabric
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


/**
 * Created by Nikhil Paranjape on 11/3/2015.
 */

abstract class TestAct : AppCompatActivity() {

    internal abstract var icon: Drawable
    internal abstract var carrierIcon: Drawable
    internal abstract var result: Drawer
    internal var versionName = BuildConfig.VERSION_NAME
    internal var util = Utilities()
    internal var mService: IInAppBillingService? = null
    internal abstract var clayout: CoordinatorLayout
    private var mServiceComponent: ComponentName? = null


    internal var mServiceConn: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }

        override fun onServiceConnected(name: ComponentName,
                                        service: IBinder) {
            mService = IInAppBillingService.Stub.asInterface(service)
        }
    }
    private var mHelper: IabHelper? = null
    private var mPurchaseFinishedListener = { result, purchase ->
        if (result.isFailure) {
            if (result.toString().contains("Purchase signature verification failed")) {
                consumeItem()
                Toast.makeText(this@TestAct, R.string.donationThanks, Toast.LENGTH_LONG).show()
                Log.d("RadioControl", "In-app purchase succeeded, however verification failed")
                val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = pref.edit()
                editor.putBoolean("isDonated", true)
                editor.apply()
            } else if (result.toString().contains("User cancelled")) {
                //Toast.makeText(TestAct.this, R.string.donationCancel, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(android.R.id.content), R.string.donationCancel, Snackbar.LENGTH_LONG)
                        .show()
                Log.d("RadioControl", "Purchase Cancelled")
            } else if (result.toString().contains("Item Already Owned")) {
                Toast.makeText(this@TestAct, R.string.donationExists, Toast.LENGTH_LONG).show()
                Log.d("RadioControl", "Donation already purchased")
            } else {
                Toast.makeText(this@TestAct, getString(R.string.donationFailed) + result, Toast.LENGTH_LONG).show()
                Log.d("RadioControl", "In-app purchase failed: " + result + "Purchase: " + purchase)
            }

        } else if (result.isSuccess) {
            Toast.makeText(this@TestAct, R.string.donationThanks, Toast.LENGTH_LONG).show()
            Log.d("RadioControl", "In-app purchase succeeded")
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = pref.edit()
            editor.putBoolean("isDonated", true)
            editor.apply()
            //consumeItem();

        }

    }
    internal var mConsumeFinishedListener = { purchase, result ->

        if (result.isSuccess) {
            Toast.makeText(this@TestAct, R.string.donationThanks, Toast.LENGTH_LONG).show()
            Log.d("RadioControl", "In-app purchase succeeded")
            val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = pref.edit()
            editor.putBoolean("isDonated", true)
            editor.apply()

        } else if (result.isFailure) {
            //Toast.makeText(TestAct.this, "Thanks for the thought, but the purchase failed", Toast.LENGTH_LONG).show();
            Log.d("RadioControl", "In-app purchase failed")
        }
    }

    internal var mReceivedInventoryListener: IabHelper.QueryInventoryFinishedListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        if (result.isFailure) {
            Log.d("RadioControl", "QueryInventoryFinished Error")
        } else {
            mHelper!!.consumeAsync(inventory.getPurchase(ITEM_SKU),
                    mConsumeFinishedListener)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clayout = findViewById(R.id.clayout)
        val dialog = findViewById<ProgressBar>(R.id.pingProgressBar)
        mServiceComponent = ComponentName(this, TestJobService::class.java)

        // Handle Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar

        //Sets the actionbar with hamburger
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_menu).color(Color.WHITE).sizeDp(IconicsDrawable.TOOLBAR_ICON_SIZE).paddingDp(IconicsDrawable.TOOLBAR_ICON_PADDING))
            actionBar.setDisplayHomeAsUpEnabled(true)

        }

        //test code
        val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxnZmUx4gqEFCsMW+/uPXIzJSaaoP4J/2RVaxYT9Be0jfga0qdGF+Vq56mzQ/LYEZgLvFelGdWwXJ5Izq5Wl/cEW8cExhQ/WDuJvYVaemuU+JnHP1zIZ2H28NtzrDH0hb59k9R8owSx7NPNITshuC4MPwwOQDgDaYk02Hgi4woSzbDtyrvwW1A1FWpftb78i8Pphr7bT14MjpNyNznk4BohLMncEVK22O1N08xrVrR66kcTgYs+EZnkRKk2uPZclsPq4KVKG8LbLcxmDdslDBnhQkSPe3ntAC8DxGhVdgJJDwulcepxWoCby1GcMZTUAC1OKCZlvGRGSwyfIqbqF2JQIDAQAB"

        //initializes iab helper
        mHelper = IabHelper(this, base64EncodedPublicKey)

        mHelper!!.startSetup { result ->
            if (!result.isSuccess) {
                Log.d("RadioControl", "In-app Billing setup failed: $result")
            } else {
                Log.d("RadioControl", "In-app Billing is set up OK")
            }
        }

        //Checks for IAB support
        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.setPackage("com.android.vending")
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE)

        //Async thread to do a preference checks
        doAsync{
            //  Initialize SharedPreferences
            val getPrefs = PreferenceManager
                    .getDefaultSharedPreferences(baseContext)

            //  Create a new boolean and preference and set it to true if it's not already there
            val isFirstStart = getPrefs.getBoolean("firstStart", true)
            //Gets the current android build version on device
            val currentapiVersion = Build.VERSION.SDK_INT

            //  If the activity has never started before...
            if (isFirstStart) {
                //  Make a new preferences editor
                val e = getPrefs.edit()

                if (currentapiVersion >= 24) {
                    e.putBoolean("workmode", true)
                }

                //  Launch app intro
                val i = Intent(this@TestAct, TutorialActivity::class.java)
                startActivity(i)

                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("firstStart", false)

                //  Apply changes
                e.apply()
            }
            val intervalTime = getPrefs.getString("interval_prefs", "10")
            val airplaneService = getPrefs.getBoolean("isAirplaneService", false)

            //Begin background service
            if (intervalTime != "0" && airplaneService) {
                val i = Intent(applicationContext, BackgroundAirplaneService::class.java)
                baseContext.startService(i)
                Log.d("RadioControl", "back Service launched")
            }
            if (getPrefs.getBoolean("workMode", true)) {
                val i = Intent(applicationContext, PersistenceService::class.java)
                if (Build.VERSION.SDK_INT >= 26) {
                    baseContext.startForegroundService(i)
                } else {
                    baseContext.startService(i)
                }
                Log.d("RadioControl", "persist Service launched")
            }

            if (!getPrefs.getBoolean("workMode", true)) {
                registerForBroadcasts(applicationContext)
            }

            //Hides the progress dialog
            dialog.visibility = View.GONE


            init()//initializes the whats new dialog

            //Checks for root
            rootInit()

            //EndAsync

        }
        val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPref.edit()
        val statusText = findViewById<TextView>(R.id.statusText)
        val linkText = findViewById<TextView>(R.id.linkSpeed)
        val connectionStatusText = findViewById<TextView>(R.id.pingStatus)
        val toggle = findViewById<Switch>(R.id.enableSwitch)

        if (pref.getBoolean("allowFabric", false)) {
            Fabric.with(this, Crashlytics())
        } else {
            Fabric.with(this, Crashlytics.Builder()
                    .core(CrashlyticsCore.Builder()
                            .disabled(true).build()).build())
        }

        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)

        //LinkSpeed Button
        val linkSpeedButton = findViewById<Button>(R.id.linkSpeedButton)

        //Check if the easter egg is NOT activated
        if (!sharedPref.getBoolean("isDeveloper", false)) {
            linkSpeedButton.visibility = View.GONE
            linkText.visibility = View.GONE
        } else if (sharedPref.getBoolean("isDeveloper", false)) {
            linkSpeedButton.visibility = View.VISIBLE
            linkText.visibility = View.VISIBLE
        }

        linkSpeedButton.setOnClickListener { v ->
            //showWifiInfoDialog();

            val linkspeed = Utilities.linkSpeed(applicationContext)
            val GHz = Utilities.frequency(applicationContext)
            Log.i("RadioControl", "Test1: " + Utilities.getCellStatus(applicationContext))
            if (linkspeed == -1) {
                linkText.setText(R.string.cellNetwork)
            } else {
                if (GHz == 2) {
                    linkText.text = "Link speed: " + linkspeed + "Mbps @ 2.4 GHz"

                } else if (GHz == 5) {
                    linkText.text = "Link speed: " + linkspeed + "Mbps @ 5 GHz"

                }

            }
        }

        //Connection Test button (Dev Feature)
        val conn = findViewById<Button>(R.id.pingTestButton)
        val serviceTest = findViewById<Button>(R.id.airplane_service_test)
        val nightCancel = findViewById<Button>(R.id.night_mode_cancel)
        val radioOffButton = findViewById<Button>(R.id.cellRadioOff)
        val forceCrashButton = findViewById<Button>(R.id.forceCrashButton)
        //Check if the easter egg is NOT activated
        if (!sharedPref.getBoolean("isDeveloper", false)) {
            conn.visibility = View.GONE
            serviceTest.visibility = View.GONE
            nightCancel.visibility = View.GONE
            connectionStatusText.visibility = View.GONE
            radioOffButton.visibility = View.GONE
            forceCrashButton.visibility = View.GONE
        } else if (sharedPref.getBoolean("isDeveloper", false)) {
            conn.visibility = View.VISIBLE
            serviceTest.visibility = View.VISIBLE
            nightCancel.visibility = View.VISIBLE
            connectionStatusText.visibility = View.VISIBLE
            radioOffButton.visibility = View.VISIBLE
            forceCrashButton.visibility = View.VISIBLE
        }

        conn.setOnClickListener { v ->
            connectionStatusText.setText(R.string.ping)
            connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.material_grey_50))
            dialog.visibility = View.VISIBLE
            AsyncBackgroundTask(applicationContext).execute("")
        }
        serviceTest.setOnClickListener { v ->
            val i = Intent(applicationContext, BackgroundAirplaneService::class.java)
            baseContext.startService(i)
            util.scheduleAlarm(applicationContext)
            Log.d("RadioControl", "Service started")
        }
        nightCancel.setOnClickListener { v ->
            val intent = Intent(applicationContext, NightModeReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(applicationContext, NightModeReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarm = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm?.cancel(pIntent)
        }
        radioOffButton.setOnClickListener { v ->
            //String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};
            //RootAccess.runCommands(cellOffCmd);
            val cellIntent = Intent(applicationContext, CellRadioService::class.java)
            startService(cellIntent)
            util.scheduleRootAlarm(applicationContext)
        }

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)fab.getLayoutParams();
        //params.setMargins(0, 85, 16, 85); //substitute parameters for left, top, right, bottom
        //fab.setLayoutParams(params);

        fab.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_network_check_white_48dp))
        fab.setOnClickListener { view ->
            dialog.visibility = View.VISIBLE
            AsyncBackgroundTask(applicationContext).execute("")
        }

        drawerCreate() //Initalizes Drawer

        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                editor.putInt("isActive", 0)
                statusText.setText(R.string.showDisabled)
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                editor.apply()

            } else {
                editor.putInt("isActive", 1)
                statusText.setText(R.string.showEnabled)
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                editor.apply()
                val i = Intent(applicationContext, BackgroundAirplaneService::class.java)
                applicationContext.startService(i)
            }
        }

    }

    //Initialize method for the Whats new dialog
    private fun init() {
        val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        var currentVersionNumber = 0

        val savedVersionNumber = sharedPref.getInt(VERSION_KEY, 0)

        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            currentVersionNumber = pi.versionCode
        } catch (e: Exception) {
            Log.e("RadioControl", "Unable to get version number")
        }

        if (currentVersionNumber > savedVersionNumber) {
            showUpdated()
            editor.putInt(VERSION_KEY, currentVersionNumber)
            editor.apply()
        }
        if (android.os.Build.VERSION.SDK_INT >= 24 && !sharedPref.getBoolean("workMode", false)) {
            editor.putBoolean("workMode", true)
            editor.apply()
        }
    }

    //Start a new activity for sending a feedback email
    private fun sendFeedback() {
        val emailIntent = Intent(android.content.Intent.ACTION_SEND)
        emailIntent.type = "text/html"
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mail_feedback_email)))
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject))
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message))
        startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_feedback)))
        writeLog("Feedback sent", applicationContext)
        Answers.getInstance().logRating(RatingEvent()
                .putRating(4)
                .putContentName("RadioControl Feedback")
                .putContentType("Feedback")
                .putContentId("feedback-001"))
    }

    fun registerForBroadcasts(context: Context) {
        val component = ComponentName(context, WifiReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    //Method to create the Navigation Drawer
    fun drawerCreate() {

        var carrierName = "Not Rooted"
        val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)


        //Drawable lg = getResources().getDrawable(R.mipmap.lg);
        if (deviceName.contains("Nexus 6P")) {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.huawei)!!
        } else if (deviceName.contains("Motorola")) {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.moto2)!!
        } else if (deviceName.contains("Pixel")) {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.google)!!
        } else if (deviceName.contains("LG")) {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.lg)!!
        } else if (deviceName.contains("Samsung")) {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.samsung)!!
        } else if (deviceName.contains("OnePlus")) {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.oneplus)!!
        } else {
            icon = AppCompatResources.getDrawable(applicationContext, R.mipmap.ic_launcher)!!
        }

        // root icon
        if (rootInit()) {
            carrierIcon = IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_check_circle)
                    .color(Color.GREEN)
            carrierName = "Rooted"
        } else {
            carrierIcon = IconicsDrawable(this)
                    .icon(GoogleMaterial.Icon.gmd_error_outline)
                    .color(Color.RED)
        }
        var headerIcon = ContextCompat.getDrawable(applicationContext, R.mipmap.header)

        if (sharedPref.getBoolean("isDeveloper", false)) {
            headerIcon = ContextCompat.getDrawable(applicationContext, R.mipmap.header2)
        }

        //Creates navigation drawer header
        val headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(headerIcon)
                .addProfiles(
                        ProfileDrawerItem().withName(deviceName).withEmail("v$versionName").withIcon(icon),
                        ProfileDrawerItem().withName("Root Status").withEmail(carrierName).withIcon(carrierIcon)
                )
                .withOnAccountHeaderListener { view, profile, currentProfile -> false }
                .build()
        //Creates navigation drawer items
        val item1 = PrimaryDrawerItem().withIdentifier(1).withName(R.string.home).withIcon(GoogleMaterial.Icon.gmd_wifi)
        val item2 = SecondaryDrawerItem().withIdentifier(2).withName(R.string.settings).withIcon(GoogleMaterial.Icon.gmd_settings)
        val item3 = SecondaryDrawerItem().withIdentifier(3).withName(R.string.about).withIcon(GoogleMaterial.Icon.gmd_info)
        val item4 = SecondaryDrawerItem().withIdentifier(4).withName(R.string.donate).withIcon(GoogleMaterial.Icon.gmd_attach_money)
        val item5 = SecondaryDrawerItem().withIdentifier(5).withName(R.string.sendFeedback).withIcon(GoogleMaterial.Icon.gmd_send)
        val item6 = SecondaryDrawerItem().withIdentifier(6).withName(R.string.stats).withIcon(GoogleMaterial.Icon.gmd_timeline)
        val item7 = SecondaryDrawerItem().withIdentifier(7).withName(R.string.standby_drawer_name).withIcon(GoogleMaterial.Icon.gmd_pause_circle_outline)
        val item8 = SecondaryDrawerItem().withIdentifier(8).withName(R.string.drawer_string_troubleshooting).withIcon(GoogleMaterial.Icon.gmd_help)

        //Create navigation drawer
        result = DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        item1,
                        DividerDrawerItem(),
                        item2,
                        item6,
                        item3,
                        DividerDrawerItem(),
                        item8,
                        item4,
                        item5,
                        item7
                )
                .withOnDrawerItemClickListener { view, position, drawerItem ->
                    Log.d("RadioControl", "The drawer is at position $position")
                    //About button
                    if (position == 3) {
                        startSettingsActivity()

                        Log.d("drawer", "Started settings activity")
                    } else if (position == 4) {
                        val log = File(applicationContext.filesDir, "radiocontrol.log")
                        if (log.exists() && log.canRead()) {
                            Log.d("RadioControl", "Log Exists")
                            startStatsActivity()
                        } else {
                            result.setSelection(item1)
                            Snackbar.make(clayout, "No log file found", Snackbar.LENGTH_LONG)
                                    .show()
                        }
                    } else if (position == 5) {
                        startAboutActivity()
                        Log.d("drawer", "Started about activity")
                    } else if (position == 7) {
                        result.setSelection(item1)
                        Snackbar.make(clayout, "Coming in v5.1!", Snackbar.LENGTH_LONG)
                                .show()
                        startTroubleActivity()
                    } else if (position == 8) {
                        //Donation
                        result.setSelection(item1)
                        Log.d("RadioControl", "Donation button pressed")
                        if (Utilities.isConnected(applicationContext)) {
                            showDonateDialog()
                        } else {
                            showErrorDialog()
                        }
                    } else if (position == 9) {
                        result.setSelection(item1)
                        Log.d("RadioControl", "Feedback")
                        sendFeedback()
                    } else if (position == 10) {
                        result.setSelection(item1)
                        Log.d("RadioControl", "Standby Mode Engaged")
                        startStandbyMode()
                    }
                    false
                }
                .build()
        result.setSelection(item1)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (result.isDrawerOpen) {
                    result.closeDrawer()
                } else {
                    result.openDrawer()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showToast(message: String) {
        val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        if (message.equals("true", ignoreCase = true)) {
            editor.putBoolean("isStandbyDialog", true)
            editor.apply()
        } else {
            editor.putBoolean("isStandbyDialog", false)
            editor.apply()
        }

    }

    fun startStandbyMode() {
        val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        if (!sharedPref.getBoolean("isStandbyDialog", false)) {
            MaterialDialog.Builder(this)
                    .iconRes(R.mipmap.ic_launcher)
                    .limitIconToDefaultSize()
                    .title(Html.fromHtml(getString(R.string.permissionSample, getString(R.string.app_name))))
                    .positiveText("Ok")
                    .backgroundColorRes(R.color.material_drawer_dark_background)
                    .onAny { dialog, which -> showToast("" + dialog.isPromptCheckBoxChecked) }
                    .checkBoxPromptRes(R.string.dont_ask_again, false, null)
                    .show()
        }

        editor.putInt("isActive", 0)
        editor.apply()
        val intentAction = Intent(applicationContext, ActionReceiver::class.java)
        Log.d("RadioControl", "Value Changed")
        Toast.makeText(applicationContext, "Standby Mode enabled",
                Toast.LENGTH_LONG).show()

        val pIntentlogin = PendingIntent.getBroadcast(applicationContext, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT)
        val note = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                .setContentTitle("Standby Mode")
                .setContentText("RadioControl services have been paused")
                //Using this action button I would like to call logTest
                .addAction(R.drawable.ic_done, "Turn OFF standby mode", pIntentlogin)
                .setPriority(-2)
                .setOngoing(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.notify(10110, note.build())
    }

    //starts troubleshooting activity
    private fun startTroubleActivity() {
        val intent = Intent(this, TroubleshootingActivity::class.java)
        startActivity(intent)
    }

    //starts about activity
    private fun startAboutActivity() {
        val intent = Intent(this, AboutActivity::class.java)
        startActivity(intent)
    }

    //starts settings activity
    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    //starts settings activity
    private fun startStatsActivity() {
        val intent = Intent(this, StatsActivity::class.java)
        startActivity(intent)
    }

    private fun startChangelogActivity() {
        val intent = Intent(this, ChangeLogActivity::class.java)
        startActivity(intent)
    }

    private fun showUpdated() {
        MaterialDialog.Builder(this)
                .title("RadioControl has been updated")
                .theme(Theme.DARK)
                .positiveText("GOT IT")
                .negativeText("WHAT'S NEW")
                .onAny { dialog, which ->
                    val chk = which.name
                    Log.d("RadioControl", "Updated: $chk")
                    if (chk == "POSITIVE") {
                        dialog.dismiss()
                    } else if (chk == "NEGATIVE") {
                        startChangelogActivity()
                    }
                }
                .show()
    }

    //donate dialog
    private fun showDonateDialog() {
        val inflater = LayoutInflater.from(this)//Creates layout inflator for dialog
        val view = inflater.inflate(R.layout.dialog_donate, null)//Initializes the view for donate dialog
        val builder = AlertDialog.Builder(this)//creates alertdialog


        builder.setView(view).setTitle(R.string.donate)//sets title
                .setPositiveButton(R.string.cancel) { dialog, which ->
                    Log.v("RadioControl", "Donation Cancelled")
                    dialog.dismiss()
                }

        val alert = builder.create()
        alert.show()

        //Sets the purchase options
        val oneButton = view.findViewById<Button>(R.id.oneDollar)
        oneButton.setOnClickListener { v ->
            alert.cancel() //Closes the donate dialog
            buyItem(0) //Opens billing for set item
        }
        val threeButton = view.findViewById<Button>(R.id.threeDollar)
        threeButton.setOnClickListener { v ->
            alert.cancel() //Closes the donate dialog
            buyItem(1) //Opens billing for set item
        }
        val fiveButton = view.findViewById<Button>(R.id.fiveDollar)
        fiveButton.setOnClickListener { v ->
            alert.cancel() //Closes the donate dialog
            buyItem(2) //Opens billing for set item
        }
        val tenButton = view.findViewById<Button>(R.id.tenDollar)
        tenButton.setOnClickListener { v ->
            alert.cancel() //Closes the donate dialog
            buyItem(3) //Opens billing for set item
        }


    }

    //Internet Error dialog
    private fun showErrorDialog() {
        val inflater = LayoutInflater.from(this)//Creates layout inflator for dialog
        val view = inflater.inflate(R.layout.dialog_no_internet, null)//Initializes the view for error dialog
        val builder = AlertDialog.Builder(this)//creates alertdialog
        val title = TextView(this)
        // You Can Customise your Title here
        title.setText(R.string.noInternet)
        title.setBackgroundColor(Color.DKGRAY)
        title.setPadding(10, 10, 10, 10)
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)
        title.textSize = 20f

        builder.setCustomTitle(title)
        builder.setView(view)
                .setPositiveButton("OK") { dialog, which -> dialog.dismiss() }
        builder.create().show()
    }

    private fun rootInit(): Boolean {
        try {
            val p = Runtime.getRuntime().exec("su")
            return true
        } catch (e: IOException) {
            return false
        }

    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        doAsync {
            if (pref.getBoolean("workMode", true)) {

            } else {
                registerForBroadcasts(applicationContext)
            }
        }
        drawerCreate()


        if (pref.getBoolean("allowFabric", true)) {
            Fabric.with(this, Crashlytics())
        } else {
            Fabric.with(this, Crashlytics.Builder()
                    .core(CrashlyticsCore.Builder()
                            .disabled(true).build()).build())
        }

        //Connection Test button (Dev Feature)
        val conn = findViewById<Button>(R.id.pingTestButton)
        val serviceTest = findViewById<Button>(R.id.airplane_service_test)
        val nightCancel = findViewById<Button>(R.id.night_mode_cancel)
        val radioOffButton = findViewById<Button>(R.id.cellRadioOff)
        val forceCrashButton = findViewById<Button>(R.id.forceCrashButton)
        val connectionStatusText = findViewById<TextView>(R.id.pingStatus)
        //LinkSpeed Button
        val btn3 = findViewById<Button>(R.id.linkSpeedButton)
        val linkText = findViewById<TextView>(R.id.linkSpeed)
        val statusText = findViewById<TextView>(R.id.statusText)
        val toggle = findViewById<Switch>(R.id.enableSwitch)

        //Check if the easter egg is NOT activated
        if (!sharedPref.getBoolean("isDeveloper", false)) {
            conn.visibility = View.GONE
            serviceTest.visibility = View.GONE
            nightCancel.visibility = View.GONE
            connectionStatusText.visibility = View.GONE
            radioOffButton.visibility = View.GONE
            forceCrashButton.visibility = View.GONE
            btn3.visibility = View.GONE
            linkText.visibility = View.GONE
        } else if (sharedPref.getBoolean("isDeveloper", false)) {
            conn.visibility = View.VISIBLE
            serviceTest.visibility = View.VISIBLE
            nightCancel.visibility = View.VISIBLE
            connectionStatusText.visibility = View.VISIBLE
            radioOffButton.visibility = View.VISIBLE
            forceCrashButton.visibility = View.VISIBLE
            btn3.visibility = View.VISIBLE
            linkText.visibility = View.VISIBLE
        }

        if (!rootInit()) {
            toggle.isClickable = false
            statusText.setText(R.string.noRoot)
            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
        }

        if (sharedPref.getInt("isActive", 1) == 1) {
            if (!rootInit()) {
                toggle.isClickable = false
                statusText.setText(R.string.noRoot)
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
            } else {
                statusText.setText(R.string.rEnabled)
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                toggle.isChecked = true
            }

        } else if (sharedPref.getInt("isActive", 0) == 0) {
            if (!rootInit()) {
                toggle.isClickable = false
                statusText.setText(R.string.noRoot)
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
            } else {
                statusText.setText(R.string.rDisabled)
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                toggle.isChecked = false
            }

        }
    }

    private fun consumeItem() {
        mHelper!!.queryInventoryAsync(mReceivedInventoryListener)
    }

    private fun buyItem(bill: Int) {
        //Check if $0.99
        if (bill == 0) {
            mHelper!!.launchPurchaseFlow(this, ITEM_ONE_DOLLAR, 1,
                    mPurchaseFinishedListener, "supportOneDollar")
            Answers.getInstance().logPurchase(PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(0.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation1")
                    .putItemType("Donation")
                    .putItemId("sku-001")
                    .putSuccess(true))
        } else if (bill == 1) {
            mHelper!!.launchPurchaseFlow(this, ITEM_THREE_DOLLAR, 2,
                    mPurchaseFinishedListener, "supportThreeDollar")
            Answers.getInstance().logPurchase(PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(2.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation3")
                    .putItemType("Donation")
                    .putItemId("sku-002")
                    .putSuccess(true))
        } else if (bill == 2) {
            mHelper!!.launchPurchaseFlow(this, ITEM_FIVE_DOLLAR, 3,
                    mPurchaseFinishedListener, "supportFiveDollar")
            Answers.getInstance().logPurchase(PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(4.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation5")
                    .putItemType("Donation")
                    .putItemId("sku-003")
                    .putSuccess(true))
        } else if (bill == 3) {
            mHelper!!.launchPurchaseFlow(this, ITEM_TEN_DOLLAR, 4,
                    mPurchaseFinishedListener, "supportTenDollar")
            Answers.getInstance().logPurchase(PurchaseEvent()
                    .putItemPrice(BigDecimal.valueOf(9.99))
                    .putCurrency(Currency.getInstance("USD"))
                    .putItemName("Donation10")
                    .putItemType("Donation")
                    .putItemId("sku-004")
                    .putSuccess(true))

        }//Check if $9.99
        //Check if $4.99
        //Check if $2.99


    }

    fun writeLog(data: String, c: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(c)
        if (preferences.getBoolean("enableLogs", false)) {
            try {
                val h = DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()).toString()
                val log = File(c.filesDir, "radiocontrol.log")
                if (!log.exists()) {
                    log.createNewFile()
                }
                val logPath = "radiocontrol.log"
                val string = "\n$h: $data"

                val fos = c.openFileOutput(logPath, Context.MODE_APPEND)
                fos.write(string.toByteArray())
                fos.close()
            } catch (e: IOException) {
                Log.e("RadioControl", "Error saving log")
            }

        }
    }
    fun pingCheck(){
        doAsync {
            val runtime = Runtime.getRuntime()
            val echo = StringBuilder()
            var s = ""

            try {
                val ipProcess = runtime.exec("/system/bin/ping -c 4 8.8.8.8")
                val exitValue = ipProcess.waitFor()
                Log.d("RadioControl", "Latency Test returned $exitValue")
                if (exitValue == 0) {
                    val reader = InputStreamReader(ipProcess.inputStream)
                    val buf = BufferedReader(reader)
                    var line: String
                    while ((line.equals(buf.readLine())) != null) {
                        echo.append(line).append("\n")
                    }
                    s = Utilities.getPingStats(echo.toString())
                }

                if (exitValue == 0) {

                }
                uiThread {
                    try {
                        pStatus = w.status

                        if (isDouble) {
                            status = java.lang.Double.parseDouble(w.status)
                            if (status <= 50) {
                                Snackbar.make(clayout, "Excellent Latency: $status ms", Snackbar.LENGTH_LONG).show()
                            } else if (status >= 51 && status <= 100) {
                                Snackbar.make(clayout, "Average Latency: $status ms", Snackbar.LENGTH_LONG).show()
                            } else if (status >= 101 && status <= 200) {
                                Snackbar.make(clayout, "Poor Latency: $status ms", Snackbar.LENGTH_LONG).show()
                            } else if (status >= 201) {
                                Snackbar.make(clayout, "Poor Latency. VOIP and online gaming may suffer: $status ms", Snackbar.LENGTH_LONG).show()
                            }
                        } else {
                            //Check for packet loss stuff
                            if (pStatus!!.contains("100% packet loss")) {
                                Snackbar.make(clayout, "100% packet loss detected", Snackbar.LENGTH_LONG).show()
                            } else if (pStatus.contains("25% packet loss")) {
                                Snackbar.make(clayout, "25% packet loss detected", Snackbar.LENGTH_LONG).show()
                            } else if (pStatus.contains("50% packet loss")) {
                                Snackbar.make(clayout, "50% packet loss detected", Snackbar.LENGTH_LONG).show()
                            } else if (pStatus.contains("75% packet loss")) {
                                Snackbar.make(clayout, "75% packet loss detected", Snackbar.LENGTH_LONG).show()
                            } else if (pStatus.contains("unknown host")) {
                                Snackbar.make(clayout, "Unknown host", Snackbar.LENGTH_LONG).show()
                            }
                        }

                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                        Snackbar.make(findViewById(android.R.id.content), "An error has occurred", Snackbar.LENGTH_LONG).show()
                    }

                    val result = w.exitCode

                    if (result) {
                        if (Utilities.isConnectedWifi(applicationContext)) {
                            connectionStatusText.setText(R.string.connectedWifi)
                            connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                            writeLog(getString(R.string.connectedWifi), context)
                        } else if (Utilities.isConnectedMobile(applicationContext)) {
                            if (Utilities.isConnectedFast(applicationContext)) {
                                connectionStatusText.setText(R.string.connectedFCell)
                                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                                writeLog(getString(R.string.connectedFCell), context)
                            } else if (!Utilities.isConnectedFast(applicationContext)) {
                                connectionStatusText.setText(R.string.connectedSCell)
                                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                                writeLog(getString(R.string.connectedSCell), context)
                            }

                        }

                    } else {
                        if (Utilities.isAirplaneMode(applicationContext) && !Utilities.isConnected(applicationContext)) {
                            connectionStatusText.setText(R.string.airplaneOn)
                            connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                        } else {
                            connectionStatusText.setText(R.string.connectionUnable)
                            connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                            writeLog(getString(R.string.connectionUnable), context)
                        }

                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private inner class AsyncBackgroundTask internal constructor(internal var context: Context) : AsyncTask<String, Void, PingWrapper>() {
        private var dialog: ProgressBar? = null
        override fun doInBackground(vararg params: String): PingWrapper {
            val runtime = Runtime.getRuntime()
            val w = PingWrapper()
            val echo = StringBuilder()
            var s = ""
            try {
                val ipProcess = runtime.exec("/system/bin/ping -c 4 8.8.8.8")
                val exitValue = ipProcess.waitFor()
                Log.d("RadioControl", "Latency Test returned $exitValue")
                if (exitValue == 0) {
                    val reader = InputStreamReader(ipProcess.inputStream)
                    val buf = BufferedReader(reader)
                    var line: String
                    while ((line = buf.readLine()) != null) {
                        echo.append(line).append("\n")
                    }
                    s = Utilities.getPingStats(echo.toString())
                }

                if (exitValue == 0) {
                    w.exitCode = true
                }
                w.status = s

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return w
        }

        override fun onPostExecute(w: PingWrapper) {
            dialog = findViewById(R.id.pingProgressBar)
            dialog!!.visibility = View.GONE
            val connectionStatusText = findViewById<TextView>(R.id.pingStatus)
            Log.d("RadioControl", "Status: " + w.status!!)
            val status: Double
            var isDouble = true
            val pStatus: String?
            try {
                java.lang.Double.parseDouble(w.status)
            } catch (e: Exception) {
                isDouble = false
                Log.d("RadioControl", "Not a double: $e")
                Snackbar.make(clayout, "NumberFormatException " + w.status!!, Snackbar.LENGTH_LONG).show()
                Crashlytics.logException(e)
            }

            try {
                pStatus = w.status

                if (isDouble) {
                    status = java.lang.Double.parseDouble(w.status)
                    if (status <= 50) {
                        Snackbar.make(clayout, "Excellent Latency: $status ms", Snackbar.LENGTH_LONG).show()
                    } else if (status >= 51 && status <= 100) {
                        Snackbar.make(clayout, "Average Latency: $status ms", Snackbar.LENGTH_LONG).show()
                    } else if (status >= 101 && status <= 200) {
                        Snackbar.make(clayout, "Poor Latency: $status ms", Snackbar.LENGTH_LONG).show()
                    } else if (status >= 201) {
                        Snackbar.make(clayout, "Poor Latency. VOIP and online gaming may suffer: $status ms", Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    //Check for packet loss stuff
                    if (pStatus!!.contains("100% packet loss")) {
                        Snackbar.make(clayout, "100% packet loss detected", Snackbar.LENGTH_LONG).show()
                    } else if (pStatus.contains("25% packet loss")) {
                        Snackbar.make(clayout, "25% packet loss detected", Snackbar.LENGTH_LONG).show()
                    } else if (pStatus.contains("50% packet loss")) {
                        Snackbar.make(clayout, "50% packet loss detected", Snackbar.LENGTH_LONG).show()
                    } else if (pStatus.contains("75% packet loss")) {
                        Snackbar.make(clayout, "75% packet loss detected", Snackbar.LENGTH_LONG).show()
                    } else if (pStatus.contains("unknown host")) {
                        Snackbar.make(clayout, "Unknown host", Snackbar.LENGTH_LONG).show()
                    }
                }

            } catch (e: Exception) {
                Crashlytics.logException(e)
                Snackbar.make(findViewById(android.R.id.content), "An error has occurred", Snackbar.LENGTH_LONG).show()
            }

            val result = w.exitCode

            if (result) {
                if (Utilities.isConnectedWifi(applicationContext)) {
                    connectionStatusText.setText(R.string.connectedWifi)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                    writeLog(getString(R.string.connectedWifi), context)
                } else if (Utilities.isConnectedMobile(applicationContext)) {
                    if (Utilities.isConnectedFast(applicationContext)) {
                        connectionStatusText.setText(R.string.connectedFCell)
                        connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                        writeLog(getString(R.string.connectedFCell), context)
                    } else if (!Utilities.isConnectedFast(applicationContext)) {
                        connectionStatusText.setText(R.string.connectedSCell)
                        connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                        writeLog(getString(R.string.connectedSCell), context)
                    }

                }

            } else {
                if (Utilities.isAirplaneMode(applicationContext) && !Utilities.isConnected(applicationContext)) {
                    connectionStatusText.setText(R.string.airplaneOn)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                } else {
                    connectionStatusText.setText(R.string.connectionUnable)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                    writeLog(getString(R.string.connectionUnable), context)
                }

            }
        }
    }

    fun forceCrash(view: View) {
        throw RuntimeException("This is a test crash")
    }

    override fun onStart() {
        super.onStart()
        // Start service and provide it a way to communicate with this class.
        val startServiceIntent = Intent(this, TestJobService::class.java)
        startService(startServiceIntent)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mService != null) {
            unbindService(mServiceConn)
        }
        if (mHelper != null) mHelper!!.dispose()
        mHelper = null

    }

    override fun onStop() {
        // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
        // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
        // to stopService() won't prevent scheduled jobs to be processed. However, failing
        // to call stopService() would keep it alive indefinitely.
        stopService(Intent(this, TestJobService::class.java))
        super.onStop()
    }

    companion object {
        private val PRIVATE_PREF = "prefs"
        private val VERSION_KEY = "version_number"
        internal val ITEM_SKU = "com.nikhilparanjape.radiocontrol.test_donate1"
        internal val ITEM_ONE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.one"
        internal val ITEM_THREE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.three"
        internal val ITEM_FIVE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.five"
        internal val ITEM_TEN_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.ten"
        //Grab device make and model for drawer
        val deviceName: String
            get() {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                return if (model.startsWith(manufacturer)) {
                    capitalize(model)
                } else {
                    capitalize(manufacturer) + " " + model
                }
            }


        //Capitalizes names for devices. Used by getDeviceName()
        private fun capitalize(s: String?): String {
            if (s == null || s.length == 0) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first) + s.substring(1)
            }
        }
    }
}
