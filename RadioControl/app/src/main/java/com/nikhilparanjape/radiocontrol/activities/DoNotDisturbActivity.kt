package com.nikhilparanjape.radiocontrol.activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.appyvet.rangebar.RangeBar
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver
import com.nikhilparanjape.radiocontrol.utilities.AlarmSchedulers

class DoNotDisturbActivity : AppCompatActivity() {

    private var hours: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_do_not_disturb)

        val actionBar = supportActionBar
        val alarmUtil = AlarmSchedulers()
        val pref = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = pref.edit()

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).apply {
                colorInt = Color.WHITE
                sizeDp = 24
                paddingDp = 1
            })
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.title_do_not_disturb)
        }
        val status = findViewById<ImageView>(R.id.dnd_status)
        val hourStatus = findViewById<TextView>(R.id.hour_text)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        if (pref.getBoolean("isNoDisturbEnabled", false)) {
            status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_do_not_disturb_on_white_48px))
            (getString(R.string.text_status_set_for) + pref.getInt("dndHours", 0) + getString(R.string.text_hour_s)).also { hourStatus.text = it }
            cancelButton.visibility = View.VISIBLE
        } else {
            hourStatus.text = getString(R.string.text_not_set)
            status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_do_not_disturb_off_white_48px))
            cancelButton.visibility = View.GONE
        }

        val rangeBar = findViewById<RangeBar>(R.id.rangebar)

        rangeBar.setOnRangeBarChangeListener { _, _, rightPinIndex, _, _ -> hours = rightPinIndex }

        //Initialize set button
        val setButton = findViewById<Button>(R.id.set_button)

        setButton.setOnClickListener {
            if (hours > 0) {
                editor.putBoolean("isNoDisturbEnabled", true)
                editor.putInt("dndHours", hours)
                editor.apply()
                hourStatus.text = getString(R.string.text_set_for_hours, hours)
                Log.d("RadioControl", "I've set do not disturb on for $hours hours")
                startStandbyMode()
                alarmUtil.cancelAlarm(applicationContext) // Cancels the recurring alarm that starts airplane service
                alarmUtil.scheduleWakeupAlarm(applicationContext, hours)
                Toast.makeText(this, "Do not disturb set for $hours hours", Toast.LENGTH_LONG).show()
                //onBackPressed();
            } else {
                editor.putBoolean("isNoDisturbEnabled", false)
                editor.apply()
                hourStatus.setText(R.string.not_set)
            }
        }
        cancelButton.setOnClickListener {
            editor.putBoolean("isNoDisturbEnabled", false)
            editor.apply()
            hourStatus.setText(R.string.not_set)
            status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_do_not_disturb_off_white_48px))
            cancelButton.visibility = View.GONE
            alarmUtil.cancelWakeupAlarm(applicationContext)
            Toast.makeText(this, "Do not disturb cancelled", Toast.LENGTH_LONG).show()

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startStandbyMode() {
        val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = getPrefs.edit()

        if (!getPrefs.getBoolean("isStandbyDialog", false)) {
            MaterialDialog(this)
                    .title(R.string.permissionSample, "RadioControl")
                    .icon(R.mipmap.ic_launcher)
                    .positiveButton(R.string.text_ok)
                    .show()
        }

        editor.putInt("isActive", 0)
        editor.apply()
        val intentAction = Intent(applicationContext, ActionReceiver::class.java)
        Log.d("RadioControl", "Value Changed")
        Toast.makeText(applicationContext, "Standby Mode enabled",
                Toast.LENGTH_LONG).show()

        val pIntentLogin = PendingIntent.getBroadcast(applicationContext, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT)
        val note = NotificationCompat.Builder(applicationContext,"DND")
                .setSmallIcon(R.drawable.ic_warning_black_48dp)
                .setContentTitle("Standby Mode")
                .setContentText("RadioControl services have been paused")
                //Using this action button I would like to call logTest
                .addAction(R.drawable.ic_appintro_done_white, "Turn OFF standby mode", pIntentLogin)
                .setPriority(-2)
                .setOngoing(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(10110, note.build())
    }

    companion object {
        private const val PRIVATE_PREF = "prefs"
    }
}
