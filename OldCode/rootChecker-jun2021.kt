//Check for root first
if (!rootCheck()) {
    Log.d("RadioControl-Main","NotRooted")
    //toggle.isEnabled = false
    toggle.isChecked = false //Uncheck toggle
    statusText.setText(R.string.noRoot) //Sets the status text to no root

    statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_no_root)) //Sets text to deactivated (RED) color

    //Drawer icon
    carrierIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_error_outline).apply {
        colorInt = Color.RED
    }
    carrierName = "Not Rooted"

}
else{
    Log.d("RadioControl-Main","RootedIGuess")
    toggle.isChecked = true
    //Preference handling
    editor.putInt(getString(R.string.preference_app_active), 1)
    //UI Handling
    statusText.setText(R.string.showEnabled) //Sets the status text to enabled
    statusText.setTextColor(ContextCompat.getColor(applicationContext, R.color.status_activated))

    carrierIcon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_check_circle).apply {
        colorInt = Color.GREEN
    }
    carrierName = "Rooted"

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