package com.nikhilparanjape.radiocontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Nikhil Paranjape on 12/16/2015.
 */

public class AboutActivity extends PreferenceActivity {
    String versionName = BuildConfig.VERSION_NAME;
    private static final String PRIVATE_PREF = "prefs";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.about);

        Preference versionPref = findPreference("version");
        CharSequence cs = versionName;
        versionPref.setSummary("v" + cs);
        versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            int z = 0;
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sp = getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE); //Initializes prefs.xml
                SharedPreferences.Editor editor = sp.edit();//Initializes xml editor
                z++;
                Log.d("RadioControl",(7-z)+ " steps away from easter egg");
                if(z >= 7){
                    if (!sp.getBoolean("isDeveloper",false)) {
                        Toast.makeText(AboutActivity.this, "Enabled developer features", Toast.LENGTH_LONG).show();
                        z=0;
                        Log.d("RadioControl","Developer features activated");


                        editor.putBoolean("isDeveloper", true); //Puts the boolean into prefs.xml
                        editor.commit(); //Ends writing to prefs file
                    }
                    else if(sp.getBoolean("isDeveloper",false)){
                        Toast.makeText(AboutActivity.this, "Disabled developer features", Toast.LENGTH_LONG).show();
                        z=0;
                        Log.d("RadioControl","Developer features deactivated");


                        editor.putBoolean("isDeveloper", false); //Puts the boolean into prefs.xml
                        editor.commit(); //Ends writing to prefs file
                    }

                }
                return false;
            }
        });

        Preference myPref = findPreference("changelog");
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

}
