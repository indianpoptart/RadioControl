package com.nikhilparanjape.radiocontrol;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

/**
 * Created by Nikhil on 4/5/2016.
 */
public class AboutFragment extends PreferenceFragment {
    String versionName = BuildConfig.VERSION_NAME;
    private static final String PRIVATE_PREF = "prefs";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about);

        final Context c = getActivity();
        Preference versionPref = findPreference("version");
        CharSequence cs = versionName;
        versionPref.setSummary("v" + cs);
        versionPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            int z = 0;
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences sp = c.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE); //Initializes prefs.xml
                SharedPreferences.Editor editor = sp.edit();//Initializes xml editor
                z++;
                Log.d("RadioControl",(7-z)+ " steps away from easter egg");
                if(z >= 7){
                    if (!sp.getBoolean("isDeveloper",false)) {
                        Toast.makeText(getActivity(), R.string.dev_activated, Toast.LENGTH_LONG).show();
                        z=0;
                        Log.d("RadioControl","Developer features activated");


                        editor.putBoolean("isDeveloper", true); //Puts the boolean into prefs.xml
                        editor.apply(); //Ends writing to prefs file
                    }
                    else if(sp.getBoolean("isDeveloper",false)){
                        Toast.makeText(getActivity(), R.string.dev_deactivated, Toast.LENGTH_LONG).show();
                        z=0;
                        Log.d("RadioControl",c.getString(R.string.dev_deactivated));



                        editor.putBoolean("isDeveloper", false); //Puts the boolean into prefs.xml
                        editor.apply(); //Ends writing to prefs file
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
        LayoutInflater inflater = LayoutInflater.from(getActivity());//Creates layout inflator for dialog
        View view = inflater.inflate(R.layout.dialog_whatsnew, null);//Initializes the view for whats new dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());//creates alertdialog

        builder.setView(view).setTitle(R.string.changelog)//sets title
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

}
