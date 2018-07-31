package com.nikhilparanjape.radiocontrol.activities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import com.afollestad.materialdialogs.MaterialDialog
import com.appyvet.rangebar.RangeBar
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities

class DoNotDisturbActivity : AppCompatActivity() {

    internal var hours: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_do_not_disturb)

        val actionBar = supportActionBar
        val util = Utilities()
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = pref.edit()

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.TOOLBAR_ICON_SIZE).paddingDp(IconicsDrawable.TOOLBAR_ICON_PADDING))
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.title_do_not_disturb)
        }
        val status = findViewById<ImageView>(R.id.dnd_status)
        val hourStatus = findViewById<TextView>(R.id.hour_text)
        val cancelButton = findViewById<Button>(R.id.cancel_button)

        if (pref.getBoolean("isNoDisturbEnabled", false)) {
            status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_do_not_disturb_on_white_48px))
            hourStatus.text = "set for " + pref.getInt("dndHours", 0) + " hour(s)"
            cancelButton.visibility = View.VISIBLE
        } else {
            hourStatus.text = "not set"
            status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_do_not_disturb_off_white_48px))
            cancelButton.visibility = View.GONE
        }

        val rangebar = findViewById<RangeBar>(R.id.rangebar)

        rangebar.setOnRangeBarChangeListener { _, _, rightPinIndex, _, _ -> hours = rightPinIndex }

        //Initialize set button
        val set_button = findViewById<Button>(R.id.set_button)

        set_button.setOnClickListener { _ ->
            if (hours > 0) {
                editor.putBoolean("isNoDisturbEnabled", true)
                editor.putInt("dndHours", hours)
                editor.apply()
                hourStatus.text = "set for $hours hour(s)"
                Log.d("RadioControl", "I've set do not disturb on for $hours hours")
                startStandbyMode()
                util.cancelAlarm(applicationContext) // Cancels the recurring alarm that starts airplane service
                util.scheduleWakeupAlarm(applicationContext, hours)
                Toast.makeText(this@DoNotDisturbActivity, "Do not disturb set for $hours hours", Toast.LENGTH_LONG).show()
                //onBackPressed();
            } else {
                editor.putBoolean("isNoDisturbEnabled", false)
                editor.apply()
                hourStatus.setText(R.string.not_set)
            }


        }

        cancelButton.setOnClickListener { _ ->
            editor.putBoolean("isNoDisturbEnabled", false)
            editor.apply()
            hourStatus.setText(R.string.not_set)
            status.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_do_not_disturb_off_white_48px))
            cancelButton.visibility = View.GONE
            util.cancelWakeupAlarm(applicationContext)
            Toast.makeText(this@DoNotDisturbActivity, "Do not disturb cancelled", Toast.LENGTH_LONG).show()

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
                    .onAny { dialog, _ -> showToast("" + dialog.isPromptCheckBoxChecked) }
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
        notificationManager.notify(10110, note.build())
    }

    companion object {
        private val PRIVATE_PREF = "prefs"
    }

}
