package com.nikhilparanjape.radiocontrol.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.appyvet.rangebar.RangeBar;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.receivers.ActionReceiver;
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities;

public class DoNotDisturbActivity extends AppCompatActivity {

    int hours;
    private static final String PRIVATE_PREF = "prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_do_not_disturb);

        final ActionBar actionBar = getSupportActionBar();
        final Utilities util = new Utilities();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = pref.edit();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP).paddingDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_do_not_disturb);
        }
        final ImageView status = (ImageView) findViewById(R.id.dnd_status);
        final TextView hourStatus = (TextView) findViewById(R.id.hour_text);
        final Button cancelButton = (Button) findViewById(R.id.cancel_button);

        if(pref.getBoolean("isNoDisturbEnabled",false)){
            status.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_do_not_disturb_on_white_48px));
            hourStatus.setText("set for " + pref.getInt("dndHours", 0) + " hour(s)");
            cancelButton.setVisibility(View.VISIBLE);
        }
        else{
            hourStatus.setText("not set");
            status.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_do_not_disturb_off_white_48px));
            cancelButton.setVisibility(View.GONE);
        }

        RangeBar rangebar = (RangeBar) findViewById(R.id.rangebar);

        rangebar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex,
                                              int rightPinIndex,
                                              String leftPinValue, String rightPinValue) {
                //Log.d("RadioControl", "DND SET TO " + rightPinValue + " Hours");
                hours = rightPinIndex;
            }
        });

        //Initialize set button
        Button set_button = (Button) findViewById(R.id.set_button);

        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hours > 0){
                    editor.putBoolean("isNoDisturbEnabled", true);
                    editor.putInt("dndHours", hours);
                    editor.apply();
                    hourStatus.setText("set for " + hours + " hour(s)");
                    Log.d("RadioControl", "I've set do not disturb on for " + hours + " hours");
                    startStandbyMode();
                    util.cancelAlarm(getApplicationContext()); // Cancels the recurring alarm that starts airplane service
                    util.scheduleWakeupAlarm(getApplicationContext(),hours);
                    Toast.makeText(DoNotDisturbActivity.this, "Do not disturb set for "  + hours + " hours", Toast.LENGTH_LONG).show();
                    //onBackPressed();
                }
                else{
                    editor.putBoolean("isNoDisturbEnabled", false);
                    editor.apply();
                    hourStatus.setText(R.string.not_set);
                }


            }

        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putBoolean("isNoDisturbEnabled", false);
                editor.apply();
                hourStatus.setText(R.string.not_set);
                status.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_do_not_disturb_off_white_48px));
                cancelButton.setVisibility(View.GONE);
                util.cancelWakeupAlarm(getApplicationContext());
                Toast.makeText(DoNotDisturbActivity.this, "Do not disturb cancelled", Toast.LENGTH_LONG).show();

            }

        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
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

}
