package com.nikhilparanjape.radiocontrol.activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.View.inflate
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.pm.PackageInfoCompat.getLongVersionCode
import com.afollestad.materialdialogs.MaterialDialog
import com.github.stephenvinouze.core.managers.KinAppManager
import com.github.stephenvinouze.core.models.KinAppProductType
import com.github.stephenvinouze.core.models.KinAppPurchase
import com.github.stephenvinouze.core.models.KinAppPurchaseResult
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.*
import com.mikepenz.materialdrawer.util.addItems
import com.mikepenz.materialdrawer.widget.AccountHeaderView
import com.nikhilparanjape.radiocontrol.BuildConfig
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.databinding.ActivityMainBinding
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver
import com.nikhilparanjape.radiocontrol.receivers.ConnectivityReceiver
import com.nikhilparanjape.radiocontrol.services.BackgroundJobService
import com.nikhilparanjape.radiocontrol.services.CellRadioService
import com.nikhilparanjape.radiocontrol.services.PersistenceService
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.IOException
import java.net.InetAddress
import kotlin.coroutines.CoroutineContext
import kotlin.system.measureTimeMillis
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.lifecycle.lifecycleScope
import com.mikepenz.materialdrawer.iconics.iconicsIcon
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import kotlinx.coroutines.*

/**
 * Created by Nikhil Paranjape on 11/3/2015.
 *
 * Converted to Kotlin on 10/06/2018.
 */

class MainActivity : AppCompatActivity(), KinAppManager.KinAppListener, CoroutineScope {
    //All the constant values and contexts required
    companion object {
        /*private const val PRIVATE_PREF = "prefs"*/ //Used for shared prefs
        private const val VERSION_KEY = "version_number"
        //Donation related keys (wow I misspelled the shit out of my own name for these variables, which I can't change since they are hardcoded into google play, nice going)
        internal const val ITEM_ONE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.one"
        internal const val ITEM_THREE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.three"
        internal const val ITEM_FIVE_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.five"
        internal const val ITEM_TEN_DOLLAR = "com.nikihlparanjape.radiocontrol.donate.ten"
        private var alarmUtil = AlarmSchedulers() //Allows methods to extend the AlarmScheduler Utility

        //private var gUtility = GraphicsUtility() //Implements all graphics and the like

        /**Variables for Drawer items**/
        private lateinit var deviceIcon: Drawable
        private lateinit var carrierIcon: Drawable
        private lateinit var isRootedIcon: Drawable
        private lateinit var notRootedIcon: Drawable
        private lateinit var carrierName: String
        private var versionName = BuildConfig.VERSION_NAME //Takes the apps current version name and makes it a variable
        /**Variables for Drawer items**/

        //private var util = Utilities() //Allows methods to extend any general utilities

        private lateinit var clayout: CoordinatorLayout
        private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
        private lateinit var slider: MaterialDrawerSliderView
        private var mServiceComponent: ComponentName? = null
        private lateinit var binding: ActivityMainBinding
        private var isBillingReady = false

        //JobID for jobscheduler(BackgroundJobService)
        private const val jobID = 0x01
        // Job variable that is tied to the Activity lifecycle Coroutines
        lateinit var job: Job
        var isRooted: Boolean = false // Assume not rooted

        //Grab device make and model for drawer
        val getDeviceName: String
            get() {
                val manufacturer = Build.MANUFACTURER
                val model = Build.MODEL
                return if (model.startsWith(manufacturer)) {
                    capitalizeText(model)
                } else {
                    capitalizeText(manufacturer) + " " + model
                }
            }

        //A function that capitalizes names for strings. Used directly by getDeviceName()
        private fun capitalizeText(s: String?): String {
            //Return nothing if string 's' is empty or null
            if (s == null || s.isEmpty()) {
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
    /** BEGIN Billing setup **/
    //Public key for donation
    private val base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxnZmUx4gqEFCsMW+/uPXIzJSaaoP4J/2RVaxYT9Be0jfga0qdGF+Vq56mzQ/LYEZgLvFelGdWwXJ5Izq5Wl/cEW8cExhQ/WDuJvYVaemuU+JnHP1zIZ2H28NtzrDH0hb59k9R8owSx7NPNITshuC4MPwwOQDgDaYk02Hgi4woSzbDtyrvwW1A1FWpftb78i8Pphr7bT14MjpNyNznk4BohLMncEVK22O1N08xrVrR66kcTgYs+EZnkRKk2uPZclsPq4KVKG8LbLcxmDdslDBnhQkSPe3ntAC8DxGhVdgJJDwulcepxWoCby1GcMZTUAC1OKCZlvGRGSwyfIqbqF2JQIDAQAB"
    private val billingManager = KinAppManager(this, base64EncodedPublicKey)
    /** END Billing setup **/

    // Overridden from CoroutineScope,
    // Main context that is combined with the context of the Job as well
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    // IO context that is combined with the context of the Job as well
    val ioContext: CoroutineContext
        get() = Dispatchers.IO + job


    /** This is the main activity **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    /**BEGIN bindings**/
        //Bind activity to other components
        binding = ActivityMainBinding.inflate(layoutInflater)
        billingManager.bind(this)

        //Sets root view, toolbar, and the slider with the main binding
        val view = binding.root
        val toolbar = binding.toolbar
        val slider = binding.slider
    /**END bindings**/

    /** BEGIN Core init**/
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy) // Sets thread policy to all threads THIS IS DANGEROUS
        mServiceComponent = ComponentName(this, BackgroundJobService::class.java)

        //Adds filter for app to receive Connectivity_Action
        val filter = IntentFilter()
        @Suppress("DEPRECATION") // This is used for legacy support
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //  Assume this is the first time the user has opened the app...
        val carrierName = "Root Pending" //For drawer's root status and assume not rooted
        //  Pref values
        //  Initialize SharedPreferences
        val mySharedPref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        /*val sharedPref = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE)*/ /** Migrated away from custom prefs.xml file **/
        val editor = mySharedPref.edit()

        editor.putInt(getString(R.string.preference_app_active), 0) // Sets the main ON/OFF for the app
        //  Create a new boolean and preference and set it to true if it's not already there
        val isFirstStart = mySharedPref.getBoolean(getString(R.string.preference_first_start), true)
    /** END Core init**/

    /** BEGIN View related init **/
        setContentView(view)
        //Sets the secondary view features
        clayout = findViewById(R.id.clayout)
        Iconics.init(this) // Main icon set initialization

        val mainProgressBar = findViewById<ProgressBar>(R.id.pingProgressBar) //A progress bar that shows up when the app is hard at work

        // Handle Toolbar
        //val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar) // Sets the actionBar to the ActivityBinding toolbar
        val actionBar = supportActionBar
        val latencyFAB = findViewById<FloatingActionButton>(R.id.fab) //Main action button for ping testing (Should be in the bottom right of the main activity)

        //  TextViews
        val statusText = findViewById<TextView>(R.id.statusText)
        val linkText = findViewById<TextView>(R.id.linkSpeed)
        val connectionStatusText = findViewById<TextView>(R.id.pingStatus)

        //Sets the actionbar with hamburger icon, colors, and padding
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_menu).apply {
                colorInt = Color.WHITE
                sizeDp = 24
                paddingDp = 1
            })
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBarDrawerToggle = ActionBarDrawerToggle(this, view, toolbar, com.mikepenz.materialdrawer.R.string.material_drawer_open, com.mikepenz.materialdrawer.R.string.material_drawer_close)
        }

        //Creates the latency checker FAB button
        latencyFAB.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_network_check_white_48dp))

    /**  BEGIN button initialization **/
        //  UI Switches and Buttons
        val linkSpeedButton = findViewById<Button>(R.id.linkSpeedButton)
        val toggle = findViewById<SwitchMaterial>(R.id.enableSwitch)
        //UI Switches and Buttons

        //  Dev buttons
        val conn = findViewById<Button>(R.id.pingTestButton)
        val serviceTest = findViewById<Button>(R.id.airplane_service_test)
        val nightCancel = findViewById<Button>(R.id.night_mode_cancel)
        val radioOffButton = findViewById<Button>(R.id.cellRadioOff)
        val forceCrashButton = findViewById<Button>(R.id.forceCrashButton)
    /**  END buttons initialization **/

        /* First start init  */
        programVersionUpdateInit(isFirstStart) //initializes the whats new dialog; Set first start parameters

        //Sets a default Profile2 Drawer icon
        carrierIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_help).apply {
            colorInt = Color.YELLOW
        }
        notRootedIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_error_outline).apply {
            colorInt = Color.RED
        }
        isRootedIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_check_circle).apply {
            colorInt = Color.GREEN
        }

        /** Archived Code #2 **/

        //Checks if workmode(Intelligent Mode) is enabled and starts the Persistence Service, otherwise it registers the legacy broadcast receivers
        if (mySharedPref.getBoolean(getString(R.string.preference_work_mode), false)) {
            val i = Intent(applicationContext, PersistenceService::class.java)

            if (Build.VERSION.SDK_INT >= 26) { //   Check that we are running on Android Nougat+
                applicationContext.startForegroundService(i)
            } else {
                applicationContext.startService(i) //Start legacy service mode
            }
            Log.d("RadioControl-Main", "persistence service launched")
        } else {
            registerForBroadcasts(applicationContext)
        }
        //Checks if background optimization is enabled and schedules a job
        if (mySharedPref.getBoolean(getString(R.string.key_preference_settings_battery_opimization), false)) {
            scheduleJob()
        }
        //Hides the progress dialog
        mainProgressBar.visibility = View.GONE

    /** Begin initializing drawer **/

        //Sets
        deviceIcon = when {
            getDeviceName.contains("Nexus") -> AppCompatResources.getDrawable(applicationContext, R.mipmap.ic_nexus_logo)!!
            getDeviceName.contains("Pixel") -> AppCompatResources.getDrawable(applicationContext, R.drawable.ic_google__g__logo)!!
            getDeviceName.contains("Huawei") -> AppCompatResources.getDrawable(applicationContext, R.drawable.ic_huawei_logo)!!
            getDeviceName.contains("LG") -> AppCompatResources.getDrawable(applicationContext, R.drawable.ic_lg_logo_white)!!
            getDeviceName.contains("Motorola") -> AppCompatResources.getDrawable(applicationContext, R.mipmap.moto2)!!
            getDeviceName.contains("OnePlus") -> AppCompatResources.getDrawable(applicationContext, R.mipmap.oneplus)!!
            getDeviceName.contains("Samsung") -> AppCompatResources.getDrawable(applicationContext, R.mipmap.samsung)!!

            else -> AppCompatResources.getDrawable(applicationContext, R.mipmap.ic_launcher)!!
        }

        val profile1 = ProfileDrawerItem().apply { nameText = getDeviceName; descriptionText = "v$versionName"; iconDrawable = deviceIcon }
        val profile2 = ProfileDrawerItem().apply { nameText = getString(R.string.profile_root_status); descriptionText = carrierName; iconDrawable = carrierIcon }

        //Creates navigation drawer header
        val headerView = AccountHeaderView(this).apply {
            attachToSliderView(slider)
            addProfiles(
                    profile1,
                    profile2
            )
            withSavedInstance(savedInstanceState)
        }

        //Creates navigation drawer items
        val item1 = PrimaryDrawerItem().apply { identifier = 1; nameRes = R.string.home; iconicsIcon = GoogleMaterial.Icon.gmd_wifi }
        val item2 = SecondaryDrawerItem().apply { identifier = 2; nameRes = R.string.settings; iconicsIcon = GoogleMaterial.Icon.gmd_settings; isSelectable = false }
        val item3 = SecondaryDrawerItem().apply { identifier = 3; nameRes = R.string.about; iconicsIcon = GoogleMaterial.Icon.gmd_info; isSelectable = false }
        val item4 = SecondaryDrawerItem().apply { identifier = 4; nameRes = R.string.donate; iconicsIcon = GoogleMaterial.Icon.gmd_attach_money; isSelectable = false }
        val item5 = SecondaryDrawerItem().apply { identifier = 5; nameRes = R.string.sendFeedback; iconicsIcon = GoogleMaterial.Icon.gmd_send; isSelectable = false }
        val item6 = SecondaryDrawerItem().apply { identifier = 6; nameRes = R.string.stats; iconicsIcon = GoogleMaterial.Icon.gmd_timeline; isSelectable = false }
        val item7 = SecondaryDrawerItem().apply { identifier = 7; nameRes = R.string.standby_drawer_name; iconicsIcon = GoogleMaterial.Icon.gmd_pause_circle_outline; isSelectable = false }
        val item8 = SecondaryDrawerItem().apply { identifier = 7; nameRes = R.string.drawer_string_troubleshooting; iconicsIcon = GoogleMaterial.Icon.gmd_help; isSelectable = false }
        slider.apply {
            addItems(
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
            onDrawerItemClickListener = { v, _, position ->
                Log.d("RadioControl-Main", "The drawer is at position $position")
                if (position == 3) {
                    startSettingsActivity()

                    Log.d("drawer", "Started settings activity")

                } else if (position == 4) {
                    val log = File(applicationContext.filesDir, "radiocontrol.log")
                    if (log.exists() && log.canRead()) {
                        Log.d("RadioControl-Main", "Log Exists")
                        startStatsActivity()
                    } else {
                        Snackbar.make(clayout, "No log file found", Snackbar.LENGTH_LONG)
                                .show()
                    }
                } else if (position == 5) {
                    startAboutActivity()
                    Log.d("drawer", "Started about activity")
                } else if (position == 7) {
                    Snackbar.make(clayout, "Coming in v6.1!", Snackbar.LENGTH_LONG)
                            .show()
                    //startTroubleActivity()
                } else if (position == 8) {
                    //Donation
                    Log.d("RadioControl-Main", "Donation button pressed")
                    if (Utilities.isConnected(applicationContext) && isBillingReady) {
                        showDonateDialog()
                    } else {
                        showErrorDialog()
                    }
                } else if (position == 9) {
                    Log.d("RadioControl-Main", "Feedback")
                    sendFeedback()
                } else if (position == 10) {
                    Log.d("RadioControl-Main", "Standby Mode Engaged")
                    startStandbyMode()
                }
                false
            }
            setSavedInstance(savedInstanceState)
        }
        if (savedInstanceState == null) {
            // set the selection to the item with the identifier 11
            slider.setSelection(21, false)

            //set the active profile
            headerView.activeProfile = profile1
        }
        //Create navigation drawer
        slider.setSelection(1)
    /** END initializing drawer **/

    /** BEGIN Dev mode init handling **/
        //Check if the easter egg is NOT activated
        if (!mySharedPref.getBoolean(getString(R.string.preference_is_developer), false)) {
            conn.visibility = View.GONE
            serviceTest.visibility = View.GONE
            nightCancel.visibility = View.GONE
            connectionStatusText.visibility = View.GONE
            radioOffButton.visibility = View.GONE
            forceCrashButton.visibility = View.GONE
            linkSpeedButton.visibility = View.GONE
            linkText.visibility = View.GONE
        } else if (mySharedPref.getBoolean(getString(R.string.preference_is_developer), false)) {
            conn.visibility = View.VISIBLE
            serviceTest.visibility = View.VISIBLE
            nightCancel.visibility = View.VISIBLE
            connectionStatusText.visibility = View.VISIBLE
            radioOffButton.visibility = View.VISIBLE
            forceCrashButton.visibility = View.VISIBLE
            linkSpeedButton.visibility = View.VISIBLE
            linkText.visibility = View.VISIBLE
        }
    /** END Dev mode init handling **/

    /** BEGIN Payment Item fetch **/
        /*launch() {
            val productList: ArrayList<String> = ArrayList()
            productList.add(ITEM_ONE_DOLLAR)
            productList.add(ITEM_THREE_DOLLAR)
            productList.add(ITEM_FIVE_DOLLAR)
            productList.add(ITEM_TEN_DOLLAR)
            billingManager.fetchProductsAsync(productList, KinAppProductType.INAPP).await()
        }*/
    /** END Payment Item fetch **/

    /** BEGIN Button Click Listeners **/

        /** Main RadioControl toggle switch listener **/
        toggle.setOnCheckedChangeListener { _, isChecked ->
            Log.d("RadioControl-Main","Click Clock")
            mainProgressBar.visibility = View.VISIBLE

            if (!isChecked) {
                Log.d("RadioControl-Main", "Not Checked")
                //Preference handling
                editor.putInt(getString(R.string.preference_app_active), 0)
                editor.apply()
                //UI Handling
                statusText.setText(R.string.showDisabled) //Sets status text to disabled
                statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                mainProgressBar.visibility = View.GONE

            } else {
                // Starts a deferred task to check for root and then returns true or false
                val deferred = lifecycleScope.async(Dispatchers.IO) {
                    return@async when {
                        Shell.rootAccess() -> {
                            isRooted = true
                            writeLog("root accessed: ", applicationContext)
                            true
                        }
                        else -> false
                    }
                }
                lifecycleScope.launch(Dispatchers.Main) {
                    Log.d("RadioControl-Main","Lifecycled")
                    delay(100)
                    if (deferred.isActive) {
                        try{
                            val result = deferred.await()
                            Log.d("RadioControl-TryCycle","Result is: $result")

                        } finally {
                            val result = deferred.await()
                            Log.d("RadioControl-FinalCycle","Result is: $result")
                            uiSetToggle(result,editor,mySharedPref)
                            mainProgressBar.visibility = View.GONE
                        }

                    } else {
                        val result = deferred.await()
                        Log.d("RadioControl-LifeCycle","Result is: $result")
                        uiSetToggle(result,editor,mySharedPref)
                        mainProgressBar.visibility = View.GONE
                    }
                }
            }
            //dialog.visibility = View.GONE //Make sure the dialog is gone
            editor.apply()
        }
        /** END RadioControl toggle switch listener **/

        toggle.setOnLongClickListener {
            val bgj = Intent(applicationContext, BackgroundJobService::class.java)

            //Preference handling
            editor.putInt(getString(R.string.preference_app_active), 1)
            //UI Handling
            statusText.setText(R.string.showEnabledDebug)
            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated_debug))
            applicationContext.startService(bgj)
            alarmUtil.scheduleAlarm(applicationContext)
            Toast.makeText(applicationContext, "The impossible was just attempted",
                Toast.LENGTH_LONG).show()
            editor.apply()
            true
        }
        // Runs the latency check on click of the FAB
        latencyFAB.setOnClickListener {
            mainProgressBar.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= 10000){
                Snackbar.make(clayout, "Android 12 support is still in progress", Snackbar.LENGTH_LONG)
                    .show()
                mainProgressBar.visibility = View.GONE
            } else{
                lifecycleScope.launch {
                    pingCheck()
                }
            }
        }
    /** END Button Click Listeners **/

    /** BEGIN DEV Button Click Listeners **/
        conn.setOnClickListener {
            connectionStatusText.setText(R.string.ping)
            connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.material_grey_50))
            mainProgressBar.visibility = View.VISIBLE
            Log.d("RadioControl-Ping","Build SDK Version: " + Build.VERSION.SDK_INT)
            lifecycleScope.async {
                pingCheck()
            }
        }
        serviceTest.setOnClickListener {
            val i = Intent(applicationContext, BackgroundJobService::class.java)
            applicationContext.startService(i)
            alarmUtil.scheduleAlarm(applicationContext)
            Log.d("RadioControl-Main", "Service started")
        }
        nightCancel.setOnClickListener {
            /*val intent = Intent(applicationContext, NightModeReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(applicationContext, NightModeReceiver.REQUEST_CODE,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarm = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(pIntent)*/
            val app = "NightMode"
            alarmUtil.cancelAlarm(applicationContext, app, app)
        }
        radioOffButton.setOnClickListener {
            //String[] cellOffCmd = {"service call phone 27","service call phone 14 s16"};
            //Utilities.setMobileNetworkFromLollipop(applicationContext)
            //RootAccess.runCommands(cellOffCmd);

            val cellIntent = Intent(applicationContext, CellRadioService::class.java)
            startService(cellIntent)
            alarmUtil.scheduleRootAlarm(applicationContext)
        }

        //DEV View | Listener for the link speed button
        linkSpeedButton.setOnClickListener {
            //showWifiInfoDialog();
            val activeNetwork = connectivityManager.activeNetworkInfo
            val cellStat = Utilities.getCellStatus(applicationContext)
            Log.d("RadioControl-Job", "Active: $activeNetwork") //Shows more info when debugging
            Log.d("RadioControl-Main", "Cell: $cellStat")
            val linkSpeed = Utilities.linkSpeed(applicationContext)
            val gHz = Utilities.frequency(applicationContext)
            if (linkSpeed == -1) {
                linkText.setText(R.string.cellNetwork)
            } else {
                if (gHz == 2) {
                    linkText.text = getString(R.string.link_speed_24, linkSpeed)

                } else if (gHz == 5) {
                    linkText.text = getString(R.string.link_speed_5, linkSpeed)

                }
            }
        }

    /** END DEV Button Click Listeners **/

    }

    //function for setting main toggle related UI elements as well as
    private fun uiSetToggle(result: Boolean, editor: SharedPreferences.Editor, getPrefs: SharedPreferences){
        val bgj = Intent(applicationContext, BackgroundJobService::class.java)
        val statusText = findViewById<TextView>(R.id.statusText)
        val toggle = findViewById<SwitchMaterial>(R.id.enableSwitch)

        if(!result){
            Log.d("RadioControl-Main","NotRooted")
            //toggle.isEnabled = false
            toggle.isChecked = false //Uncheck toggle
            editor.putInt(getString(R.string.preference_app_active), 0)
            editor.apply()
            statusText.setText(R.string.noRoot) //Sets the status text to no root

            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_no_root)) //Sets text to deactivated (RED) color

            //Drawer icon
            carrierIcon = notRootedIcon
            carrierName = "Not Rooted"
        } else if (result) {
            Log.d("RadioControl-Main","RootedIGuess")
            toggle.isChecked = true
            //Preference handling
            editor.putInt(getString(R.string.preference_app_active), 1)
            editor.apply()
            //UI Handling
            statusText.setText(R.string.showEnabled) //Sets the status text to enabled
            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))

            carrierIcon = isRootedIcon
            carrierName = "Rooted"

            // TODO Move background init to a different method if possible
            //Service initialization
            applicationContext.startService(bgj)
            //Alarm scheduling
            alarmUtil.scheduleAlarm(applicationContext)

            //Checks if workmode is enabled and starts the Persistence Service, otherwise it registers the broadcast receivers
            if (getPrefs.getBoolean(getString(R.string.preference_work_mode), true)) {
                val i = Intent(applicationContext, PersistenceService::class.java)
                if (Build.VERSION.SDK_INT >= 26) {
                    applicationContext.startForegroundService(i)
                } else {
                    applicationContext.startService(i)
                }
                Log.d("RadioControl-Main", "persist Service launched")
            } else {
                registerForBroadcasts(applicationContext)
            }
            //Checks if background optimization is enabled and schedules a job
            if (getPrefs.getBoolean(getString(R.string.key_preference_settings_battery_opimization), false)) {
                scheduleJob()
            }
        }
    }

    //Initialize method for the Whats new dialog as well as the first start protocol
    private fun programVersionUpdateInit(isFirstStart: Boolean) {
        Log.d("RadioControl-Main","CHECKING FOR NEW VERSION")
        lifecycleScope.launch {
            val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = getPrefs.edit()
            var currentVersionNumber: Int

            try {
                val pi = packageManager.getPackageInfo(packageName, 0)
                val longVersionCode = getLongVersionCode(pi)
                currentVersionNumber = longVersionCode.toInt()
            } catch (e: PackageManager.NameNotFoundException) {
                currentVersionNumber = -1
                Log.e("RadioControl-Main", "Unable to get version number")
            }
            when {
                isFirstStart -> {
                    Log.d("RadioControl-Main","IT'S YOUR FIRST TIME HERE")
                    editor.putInt(VERSION_KEY, currentVersionNumber)
                    editor.putInt(getString(R.string.preference_app_active), 0) //Sets app to "off" for default

                    // Edit preference to make it false because we don't want this to run again
                    editor.putBoolean(getString(R.string.preference_first_start), false)

                    //Enables Intelligent Mode if Nougat+
                    if (Build.VERSION.SDK_INT >= 24) { // && !getPrefs.getBoolean(getString(R.string.preference_work_mode), false)
                        editor.putBoolean(getString(R.string.preference_work_mode), true)
                    }

                    //  Launch tutorial/onboarding
                    editor.apply()
                    val i = Intent(applicationContext, TutorialActivity::class.java)
                    startActivity(i)
                }
            }
            val savedVersionNumber = getPrefs.getInt(VERSION_KEY, 1)
            //Sets app version number

            //Checks if app version has changed since last opening
            if (currentVersionNumber > savedVersionNumber) {
                showUpdated(this@MainActivity)
            }
            editor.putInt(VERSION_KEY, currentVersionNumber)
            editor.apply() //Finalize shared pref changes
        }
    }

    private fun scheduleJob() {
        val myJob = JobInfo.Builder(jobID, ComponentName(packageName, BackgroundJobService::class.java.name))
                .setMinimumLatency(1000)
                .setOverrideDeadline(1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setRequiresCharging(false)
                .build()

        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(myJob)
    }

    //Start a new activity for sending a feedback email
    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "text/html"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mail_feedback_email)))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject))
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message))

        startActivity(Intent.createChooser(emailIntent, getString(R.string.title_send_feedback)))
        writeLog("Feedback sent", applicationContext)
    }

    private fun registerForBroadcasts(context: Context) {
        val component = ComponentName(context, ConnectivityReceiver::class.java)
        val pm = context.packageManager
        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        actionBarDrawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        actionBarDrawerToggle.syncState()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /*override fun onSaveInstanceState(_outState: Bundle) {
        var outState = _outState
        //add the values which need to be saved from the drawer to the bundle
        outState = slider.saveInstanceState(outState)

        //add the values which need to be saved from the accountHeader to the bundle
        outState = headerView.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }*/


    fun onBackPressed(binding: ActivityMainBinding) {
        val root = binding.root
        val slider = binding.slider
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (root.isDrawerOpen(slider)) {
            root.closeDrawer(slider)
        } else {
            super.onBackPressed()
        }
    }

    private fun startStandbyMode() {
        val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = getPrefs.edit()

        if (!getPrefs.getBoolean(getString(R.string.preference_standby_dialog), false)) {
            MaterialDialog(this)
                    .icon(R.mipmap.ic_launcher)
                    .title(R.string.permissionSample, "RadioControl")
                    .positiveButton(R.string.text_ok)
                    .show()
        }

        editor.putInt(getString(R.string.preference_app_active), 0)

        val intentAction = Intent(applicationContext, ActionReceiver::class.java)
        Log.d("RadioControl-Main", "Value Changed")
        Toast.makeText(applicationContext, "Standby Mode enabled",
                Toast.LENGTH_LONG).show()

        val pIntentLogin = PendingIntent.getBroadcast(applicationContext, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT)
        val note = NotificationCompat.Builder(applicationContext, "MainActivity")
                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                .setContentTitle(getString(R.string.title_standby_dialog))
                .setContentText(getString(R.string.title_service_paused))
                //Using this action button I would like to call logTest
                .addAction(R.drawable.ic_appintro_done_white, getString(R.string.button_disable_standby), pIntentLogin)
                .setPriority(IMPORTANCE_LOW)
                .setOngoing(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(10110, note.build())
        editor.apply()
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

    //starts settings
    private fun startSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    //starts settings activity
    private fun startStatsActivity() {
        val intent = Intent(this, StatsActivity::class.java)
        startActivity(intent)}

    override fun onBillingReady() {
        isBillingReady = true
    }

    override fun onPurchaseFinished(
        purchaseResult: KinAppPurchaseResult,
        purchase: KinAppPurchase?
    ) {
        // Handle your purchase result here
        when (purchaseResult) {
            KinAppPurchaseResult.SUCCESS -> {
                Toast.makeText(applicationContext, R.string.donationThanks, Toast.LENGTH_LONG).show()
                Log.d("RadioControl-Main", "In-app purchase succeeded")
                val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = getPrefs.edit()
                editor.putBoolean(getString(R.string.preference_is_donated), true)
                editor.apply()

                //billingManager.consumePurchase(purchase!!).await()

            }
            KinAppPurchaseResult.ALREADY_OWNED -> {
                Toast.makeText(applicationContext, R.string.donationExists, Toast.LENGTH_LONG).show()
                Log.d("RadioControl-Main", "Donation already purchased")
            }
            KinAppPurchaseResult.INVALID_PURCHASE -> {
                // Purchase invalid and cannot be processed
            }
            KinAppPurchaseResult.INVALID_SIGNATURE -> {
                Toast.makeText(applicationContext, R.string.donationThanks, Toast.LENGTH_LONG).show()
                Log.d("RadioControl-Main", "In-app purchase succeeded, however verification failed")
                val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                val editor = pref.edit()
                editor.putBoolean(getString(R.string.preference_is_donated), true)
                editor.apply()
            }
            KinAppPurchaseResult.CANCEL -> {
                //Toast.makeText(MainActivity.this, R.string.donationCancel, Toast.LENGTH_LONG).show();
                Snackbar.make(findViewById(android.R.id.content), R.string.donationCancel, Snackbar.LENGTH_LONG)
                    .show()
                Log.d("RadioControl-Main", "Purchase Cancelled")
            }
        }
    }

    private fun showUpdated(c: Context) = MaterialDialog(c)
            .title(R.string.title_whats_new)
            .positiveButton(R.string.text_got_it) { dialog ->
                dialog.dismiss()
            }
            .negativeButton(R.string.text_whats_new) {
                val intent = Intent(this, ChangeLogActivity::class.java)
                startActivity(intent)
            }
            .show()

    //donate dialog
    private fun showDonateDialog() {
        val view = inflate(applicationContext, R.layout.dialog_donate, null)//Initializes the view for donate dialog
        val builder = AlertDialog.Builder(this)//creates alertdialog


        builder.setView(view).setTitle(R.string.donate)//sets title
                .setPositiveButton(R.string.cancel) { dialog, _ ->
                    Log.v("RadioControl-Main", "Donation Cancelled")
                    dialog.dismiss()
                }

        val alert = builder.create()
        alert.show()

        //Sets the purchase options
        val oneButton = view.findViewById<Button>(R.id.oneDollar)
        oneButton.setOnClickListener {
            alert.cancel() //Closes the donate dialog
            billingManager.purchase(this, ITEM_ONE_DOLLAR, KinAppProductType.INAPP)
        }
        val threeButton = view.findViewById<Button>(R.id.threeDollar)
        threeButton.setOnClickListener {
            alert.cancel() //Closes the donate dialog
            billingManager.purchase(this, ITEM_THREE_DOLLAR, KinAppProductType.INAPP)
        }
        val fiveButton = view.findViewById<Button>(R.id.fiveDollar)
        fiveButton.setOnClickListener {
            alert.cancel() //Closes the donate dialog
            billingManager.purchase(this, ITEM_FIVE_DOLLAR, KinAppProductType.INAPP)
        }
        val tenButton = view.findViewById<Button>(R.id.tenDollar)
        tenButton.setOnClickListener {
            alert.cancel() //Closes the donate dialog
            billingManager.purchase(this, ITEM_TEN_DOLLAR, KinAppProductType.INAPP)
        }


    }

    //Internet Error dialog
    private fun showErrorDialog() {
        val view = inflate(applicationContext, R.layout.dialog_no_internet, null)//Initializes the view for error dialog
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
            .setPositiveButton(R.string.text_ok) { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    override fun onResume() {
        super.onResume()
        //  TODO Figure out how to take init code and run without duplication
        val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        //If workmode is false
        if (!pref.getBoolean(getString(R.string.preference_work_mode), true)) {
            registerForBroadcasts(applicationContext)
        }
        //drawerCreate()

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
        val toggle = findViewById<SwitchMaterial>(R.id.enableSwitch)

        //Check if the easter egg is NOT activated
        if (!getPrefs.getBoolean(getString(R.string.preference_is_developer), false)) {
            conn.visibility = View.GONE
            serviceTest.visibility = View.GONE
            nightCancel.visibility = View.GONE
            connectionStatusText.visibility = View.GONE
            radioOffButton.visibility = View.GONE
            forceCrashButton.visibility = View.GONE
            btn3.visibility = View.GONE
            linkText.visibility = View.GONE
        } else if (getPrefs.getBoolean(getString(R.string.preference_is_developer), false)) {
            conn.visibility = View.VISIBLE
            serviceTest.visibility = View.VISIBLE
            nightCancel.visibility = View.VISIBLE
            connectionStatusText.visibility = View.VISIBLE
            radioOffButton.visibility = View.VISIBLE
            forceCrashButton.visibility = View.VISIBLE
            btn3.visibility = View.VISIBLE
            linkText.visibility = View.VISIBLE
        }

        /*if (!Shell.rootAccess()) {
            //toggle.isEnabled = false
            statusText.setText(R.string.noRoot)
            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
        }*/
        if (getPrefs.getInt(getString(R.string.preference_app_active), 0) == 1) {
            statusText.setText(R.string.rEnabled)
            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))

            carrierIcon = isRootedIcon
            carrierName = "Rooted"

            toggle.isChecked = true

        } else if (getPrefs.getInt(getString(R.string.preference_app_active), 0) == 0) {
            statusText.setText(R.string.rDisabled)
            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
            toggle.isChecked = false
        }
    }

    private fun writeLog(data: String, c: Context) {
        if (androidx.preference.PreferenceManager.getDefaultSharedPreferences(c).getBoolean("enableLogs", false)) {
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
                Log.e("RadioControl-Main", "Error saving log")
            }
        }
    }

    private suspend fun pingCheck() {
        val preferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val ip = preferences.getString("prefPingIp", "1.0.0.1")
        val address = InetAddress.getByName(ip)
        var reachable = false

        val timeDifference = measureTimeMillis {
            reachable = address.isReachable(1000)
        }
        Log.d("RadioControl-Main", "Reachable?: $reachable, Time: $timeDifference")

        val latencyProgressBar: ProgressBar = findViewById(R.id.pingProgressBar)
        latencyProgressBar.visibility = View.GONE
        val connectionStatusText = findViewById<TextView>(R.id.pingStatus)
        if (reachable) {
            when {
                timeDifference <= 50 -> Snackbar.make(clayout, "Excellent Latency: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                51.0 <= timeDifference && timeDifference <= 100.0 -> Snackbar.make(clayout, "Average Latency: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                101.0 <= timeDifference && timeDifference <= 200.0 -> Snackbar.make(clayout, "Poor Latency: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                timeDifference >= 201 -> Snackbar.make(clayout, "Very Poor Latency. VOIP and online gaming may suffer: $timeDifference ms", Snackbar.LENGTH_LONG).show()
            }
        }
        //Sadly packet loss testing is gone :(
        /*//Check for packet loss stuff
        when {
            pStatus!!.contains("100% packet loss") -> Snackbar.make(clayout, "100% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("25% packet loss") -> Snackbar.make(clayout, "25% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("50% packet loss") -> Snackbar.make(clayout, "50% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("75% packet loss") -> Snackbar.make(clayout, "75% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("unknown host") -> Snackbar.make(clayout, "Unknown host", Snackbar.LENGTH_LONG).show()
        }*/
        //  TODO:Move to NetworkUtilities class
        if (reachable) {
            if (Utilities.isConnectedWifi(applicationContext)) {
                // TODO Return info back to main activity
                connectionStatusText.setText(R.string.connectedWifi)
                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated)) // This is all debug anyways
                writeLog(getString(R.string.connectedWifi), applicationContext)
                // TODO move UI elements to react to return statements
            } else if (Utilities.isConnectedMobile(applicationContext)) {
                if (Utilities.isConnectedFast(applicationContext)) {
                    connectionStatusText.setText(R.string.connectedFCell)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                    writeLog(getString(R.string.connectedFCell), applicationContext)
                } else if (!Utilities.isConnectedFast(applicationContext)) {
                    connectionStatusText.setText(R.string.connectedSCell)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                    writeLog(getString(R.string.connectedSCell), applicationContext)
                }
            }

        } else {
            if (Utilities.isAirplaneMode(applicationContext) && !Utilities.isConnected(applicationContext)) {
                Snackbar.make(clayout, R.string.airplaneOn, Snackbar.LENGTH_LONG).show()
                connectionStatusText.setText(R.string.airplaneOn)
                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
            } else {
                Snackbar.make(clayout, R.string.connectionUnable, Snackbar.LENGTH_LONG).show()
                connectionStatusText.setText(R.string.connectionUnable)
                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                writeLog(getString(R.string.connectionUnable), applicationContext)
            }
        }
        // END TODO: Move to NetworkUtilities class
    }
    /*
    private suspend fun pingCheckUI() {
        val dialog: ProgressBar = findViewById(R.id.pingProgressBar)
        dialog.visibility = View.GONE
        val connectionStatusText = findViewById<TextView>(R.id.pingStatus)

        when {
            NetworkUtility.reachable(applicationContext) -> {
                when {
                    timeDifference <= 50 -> Snackbar.make(clayout, "Excellent Latency: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                    51.0 <= timeDifference && timeDifference <= 100.0 -> Snackbar.make(clayout, "Average Latency: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                    101.0 <= timeDifference && timeDifference <= 200.0 -> Snackbar.make(clayout, "Poor Latency: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                    timeDifference >= 201 -> Snackbar.make(clayout, "Very Poor Latency. VOIP and online gaming may suffer: $timeDifference ms", Snackbar.LENGTH_LONG).show()
                }
            }
        }
        //Sadly packet loss testing is gone :(
        /*//Check for packet loss stuff
        when {
            pStatus!!.contains("100% packet loss") -> Snackbar.make(clayout, "100% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("25% packet loss") -> Snackbar.make(clayout, "25% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("50% packet loss") -> Snackbar.make(clayout, "50% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("75% packet loss") -> Snackbar.make(clayout, "75% packet loss detected", Snackbar.LENGTH_LONG).show()
            pStatus.contains("unknown host") -> Snackbar.make(clayout, "Unknown host", Snackbar.LENGTH_LONG).show()
        }*/
        //  TODO: Refactor into NetworkUtilities class
        if (reachable) {
            if (Utilities.isConnectedWifi(applicationContext)) {
                // TODO Return info back to main activity
                connectionStatusText.setText(R.string.connectedWifi)
                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                writeLog(getString(R.string.connectedWifi), applicationContext)
                // TODO move UI elements to react to return statements
            } else if (Utilities.isConnectedMobile(applicationContext)) {
                if (Utilities.isConnectedFast(applicationContext)) {
                    connectionStatusText.setText(R.string.connectedFCell)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                    writeLog(getString(R.string.connectedFCell), applicationContext)
                } else if (!Utilities.isConnectedFast(applicationContext)) {
                    connectionStatusText.setText(R.string.connectedSCell)
                    connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))
                    writeLog(getString(R.string.connectedSCell), applicationContext)
                }
            }

        } else {
            if (Utilities.isAirplaneMode(applicationContext) && !Utilities.isConnected(applicationContext)) {
                connectionStatusText.setText(R.string.airplaneOn)
                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
            } else {
                connectionStatusText.setText(R.string.connectionUnable)
                connectionStatusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_deactivated))
                writeLog(getString(R.string.connectionUnable), applicationContext)
            }
        }
        // END TODO: Refactor into NetworkUtilities class
    }*/

    override fun onStart() {
        super.onStart()
        // Start service and provide it a way to communicate with this class.
        val startServiceIntent = Intent(this, BackgroundJobService::class.java)
        startService(startServiceIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingManager.verifyPurchase(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        billingManager.unbind()
    }
}

/** Archived code **/
    /** #1 **/
        /*fun onStop() {
            // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
            // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
            // to stopService() won't prevent scheduled jobs to be processed. However, failing
            // to call stopService() would keep it alive indefinitely.
            //stopService(Intent(this, BackgroundJobService::class.java))
            super.onStop()
        }*/
    /** #1 **/

    /** #2 **/
        //Checks for root, if none, disabled toggle switch
        /*if (!isRooted) {
            statusText.setText(R.string.noRoot)    //Text formatters

            statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_no_root)) //Sets text to deactivated (RED) color
            editor.apply() //Commit new values to sharedprefs

            //Drawer icon
            /*carrierIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_error_outline).apply {
                colorInt = Color.RED /**    Useful now as the toggle is no longer disabled here**/
            }*/
            // DID Show snackbar asking to request root (Maybe only do this when flipping switch?)
        } else { //We have root, do root related initializations
            carrierIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_check_circle).apply {
                colorInt = Color.GREEN
            }
            carrierName = "Rooted"
        }*/
    /** #2 **/
/** Archived code **/