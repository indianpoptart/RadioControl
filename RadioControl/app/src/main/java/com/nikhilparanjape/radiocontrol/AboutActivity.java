package com.nikhilparanjape.radiocontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
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

public class AboutActivity extends PreferenceActivity {
    Drawable icon;
    String versionName = BuildConfig.VERSION_NAME;
    private static final String PRIVATE_PREF = "prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_about);

        addPreferencesFromResource(R.xml.about);

        final Preference pref = findPreference("version");
        CharSequence cs = versionName;
        pref.setSummary(cs);
        //TextView t = (TextView)findViewById(R.id.verNum);
        //t.setText(versionName);

        //drawerCreate();
        Preference versionPref = (Preference) findPreference("version");
        versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            int z = 0;
            public boolean onPreferenceClick(Preference preference) {
                z++;
                if(z >= 7){
                    Toast.makeText(AboutActivity.this, "You found an easter egg", Toast.LENGTH_LONG).show();
                    z=0;
                    Log.d("RadioControl","Easter egg activated");
                    SharedPreferences sp = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE); //Initializes prefs.xml
                    SharedPreferences.Editor editor = sp.edit();//Initializes xml editor

                    editor.putBoolean("isEasterEgg", true); //Puts the boolean into prefs.xml
                    editor.commit(); //Ends writing to prefs file
                }
                return false;
            }
        });

        Preference myPref = (Preference) findPreference("changelog");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                changelog();
                return false;
            }
        });


    }

    //whats new dialog
    private void changelog() {
        LayoutInflater inflater = LayoutInflater.from(this);//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);//creates alertdialog

        builder.setView(view).setTitle("Changelog")//sets title
                .setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    //starts about activity
    public void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);

        startActivity(intent);
    }
    //starts about activity
    public void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
