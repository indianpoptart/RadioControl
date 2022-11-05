package com.nikhilparanjape.radiocontrol.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.aboutlibraries.Libs
import com.nikhilparanjape.radiocontrol.BuildConfig
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.activities.ChangeLogActivity
import com.nikhilparanjape.radiocontrol.activities.TutorialActivity
import com.nikhilparanjape.radiocontrol.utilities.Utilities

/**
 * Created by Nikhil on 4/5/2016.
 *
 * @author Nikhil Paranjape
 *
 * This class renders the about.xml layout into a fragment inside AboutActivity
 * It also allows for programmable control of the preferences in about.xml
 */

class AboutFragment : PreferenceFragmentCompat() {
    private var z = 0
    private var versionName = BuildConfig.VERSION_NAME

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about)
        val versionPref = findPreference<Preference>("version")

        val cs = versionName
        versionPref?.summary = "v$cs"


        // This will disable any preferences that require internet access to load
        if (!Utilities.isConnected(requireContext())) {
            preferenceScreen.findPreference<Preference>("help")?.isEnabled = false
            preferenceScreen.findPreference<Preference>("source")?.isEnabled = false
            preferenceScreen.findPreference<Preference>("support")?.isEnabled = false
        }
        //SimpleChromeCustomTabs.initialize(requireContext()) //Initialize SimpleChromeCustomTabs process for loading webpages
        // Wow I already migrated away from this nice, not sure exactly when, but maybe you do?: 20XX-XX-XX
    }
    override fun onPreferenceTreeClick(preference: Preference): Boolean {

        return when (preference.key) {
            getString(R.string.key_preference_about_version) -> {
                val getPrefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext()) //Initializes prefs.xml
                val editor = getPrefs.edit()//Initializes xml editor
                z++
                Log.d(TAG, (7 - z).toString() + " steps away from easter egg")
                //Toast.makeText(getActivity(), (7 - z) + " steps away from easter egg", Toast.LENGTH_SHORT).show();
                if (z >= 7) {
                    if (!getPrefs.getBoolean("isDeveloper", false)) {
                        Toast.makeText(activity, R.string.dev_activated, Toast.LENGTH_LONG).show()
                        z = 0
                        Log.d(TAG, "Developer features activated")


                        editor.putBoolean("isDeveloper", true) //Puts the boolean into prefs.xml
                        editor.apply() //Ends writing to prefs file
                    } else if (getPrefs.getBoolean("isDeveloper", false)) {
                        Toast.makeText(activity, R.string.dev_deactivated, Toast.LENGTH_LONG).show()
                        z = 0
                        Log.d(TAG, requireContext().getString(R.string.dev_deactivated))

                        editor.putBoolean("isDeveloper", false) //Puts the boolean into prefs.xml
                        editor.apply() //Ends writing to prefs file
                    }
                }
                false
            }
            getString(R.string.key_pref_about_changelog) -> {
                changelog(requireContext())
                false
            }
            getString(R.string.key_preference_about_tutorial) -> {
                tutorial(requireContext())
                false
            }
            getString(R.string.key_preference_about_source) -> {
                displayLicensesAlertDialog(requireContext())
                false
            }
            getString(R.string.key_preference_about_support) -> {
                displaySupportWebsite(requireContext())
                false
            }
            getString(R.string.key_preference_about_aboutlib) -> {
                /*LibsBuilder()
                        .withLibraries("crouton", "actionbarsherlock", "showcaseview", "android_job")
                        .withAutoDetect(true)
                        .withLicenseShown(true)
                        .withVersionShown(true)
                        .withActivityTitle("Open Source Libraries")
                        .withListener(libsListener)
                        .withLibTaskCallback(libTaskCallback)
                        .withUiListener(libsUIListener)
                        .start(requireContext())*/
                val libs = Libs.Builder()
                    .build()
                val libraries = libs.libraries // retrieve all libraries defined in the metadata
                val licenses = libs.licenses // retrieve all licenses defined in the metadata
                for (lib in libraries) {
                    Log.i("AboutLibraries", "${lib.name}")
                }
                false
            }

            else -> {
                super.onPreferenceTreeClick(preference)
            }
        }
    }


    private fun displayLicensesAlertDialog(c: Context) {
        if (Utilities.isConnected(c)) {
            val url = "https://nikhilp.org/radiocontrol/opensource"
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(4342338)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        } else {
            Snackbar.make(requireView(), "No internet connection found", Snackbar.LENGTH_LONG)
                    .show()
        }

    }

    private fun displaySupportWebsite(c: Context) {
        if (Utilities.isConnected(c)) {
            val url = "https://nikhilp.org/support"
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(4816556)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        } else {
            Snackbar.make(requireView(), "No internet connection found", Snackbar.LENGTH_LONG)
                    .show()
        }

    }

    //whats new dialog
    private fun changelog(c: Context) {
        val i = Intent(c, ChangeLogActivity::class.java)
        startActivity(i)
    }

    //whats new dialog
    private fun tutorial(c: Context) {
        val i = Intent(c, TutorialActivity::class.java)
        startActivity(i)
    }

    // libTaskCallback is deprecated after v8. Which is fine, it only did logging. Migrated away on 2022-11-01
    /*private var libTaskCallback: LibTaskCallback = object : LibTaskCallback {
        override fun onLibTaskStarted() {
            Log.d(TAG, "About libraries started")
        }

        override fun onLibTaskFinished(itemAdapter: ItemAdapter<*>) {
            Log.d(TAG, "About libraries finished")
        }
    }*/

    companion object {
        private const val TAG = "RadioControl-About"
    }

}