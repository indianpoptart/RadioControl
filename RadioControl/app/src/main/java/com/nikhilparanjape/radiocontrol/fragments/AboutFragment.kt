package com.nikhilparanjape.radiocontrol.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.aboutlibraries.LibTaskCallback
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.LibsConfiguration
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.nikhilparanjape.radiocontrol.BuildConfig
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.activities.ChangeLogActivity
import com.nikhilparanjape.radiocontrol.activities.TutorialActivity
import com.nikhilparanjape.radiocontrol.utilities.Utilities
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import org.jetbrains.anko.doAsync

/**
 * Created by Nikhil on 4/5/2016.
 */

class AboutFragment : PreferenceFragmentCompat() {
    private var z = 0
    private var versionName = BuildConfig.VERSION_NAME

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.about)
        val versionPref = findPreference<Preference>("version")


        val cs = versionName
        versionPref?.summary = "v$cs"

        if (!Utilities.isConnected(requireContext())) {
            preferenceScreen.findPreference<Preference>("help")?.isEnabled = false
            preferenceScreen.findPreference<Preference>("source")?.isEnabled = false
            preferenceScreen.findPreference<Preference>("support")?.isEnabled = false
        }
        doAsync {
            SimpleChromeCustomTabs.initialize(requireContext())
        }
    }
    override fun onPreferenceTreeClick(preference: Preference): Boolean {

        return when (preference.key) {
            getString(R.string.key_preference_about_version) -> {
                val sp = requireContext().getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE) //Initializes prefs.xml
                val editor = sp.edit()//Initializes xml editor
                z++
                Log.d("RadioControl-About", (7 - z).toString() + " steps away from easter egg")
                //Toast.makeText(getActivity(), (7 - z) + " steps away from easter egg", Toast.LENGTH_SHORT).show();
                if (z >= 7) {
                    if (!sp.getBoolean("isDeveloper", false)) {
                        Toast.makeText(activity, R.string.dev_activated, Toast.LENGTH_LONG).show()
                        z = 0
                        Log.d("RadioControl-About", "Developer features activated")


                        editor.putBoolean("isDeveloper", true) //Puts the boolean into prefs.xml
                        editor.apply() //Ends writing to prefs file
                    } else if (sp.getBoolean("isDeveloper", false)) {
                        Toast.makeText(activity, R.string.dev_deactivated, Toast.LENGTH_LONG).show()
                        z = 0
                        Log.d("RadioControl-About", requireContext().getString(R.string.dev_deactivated))


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
                LibsBuilder()
                        .withLibraries("crouton", "actionbarsherlock", "showcaseview", "android_job")
                        .withAutoDetect(true)
                        .withLicenseShown(true)
                        .withVersionShown(true)
                        .withActivityTitle("Open Source Libraries")
                        .withListener(libsListener)
                        .withLibTaskCallback(libTaskCallback)
                        .withUiListener(libsUIListener)
                        .start(requireContext())
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

    private var libTaskCallback: LibTaskCallback = object : LibTaskCallback {
        override fun onLibTaskStarted() {
            Log.e("AboutLibraries", "started")
        }

        override fun onLibTaskFinished(itemAdapter: ItemAdapter<*>) {
            Log.e("AboutLibraries", "finished")
        }
    }

    private var libsUIListener: LibsConfiguration.LibsUIListener = object : LibsConfiguration.LibsUIListener {
        override fun preOnCreateView(view: View): View {
            return view
        }

        override fun postOnCreateView(view: View): View {
            return view
        }
    }

    private var libsListener: LibsConfiguration.LibsListener = object : LibsConfiguration.LibsListener {
        override fun onIconClicked(v: View) {
            Toast.makeText(v.context, "We are able to track this now ;)", Toast.LENGTH_LONG).show()
        }

        override fun onLibraryAuthorClicked(v: View, library: Library): Boolean {
            return false
        }

        override fun onLibraryContentClicked(v: View, library: Library): Boolean {
            return false
        }

        override fun onLibraryBottomClicked(v: View, library: Library): Boolean {
            return false
        }

        override fun onExtraClicked(v: View, specialButton: Libs.SpecialButton): Boolean {
            return false
        }

        override fun onIconLongClicked(v: View): Boolean {
            return false
        }

        override fun onLibraryAuthorLongClicked(v: View, library: Library): Boolean {
            return false
        }

        override fun onLibraryContentLongClicked(v: View, library: Library): Boolean {
            return false
        }

        override fun onLibraryBottomLongClicked(v: View, library: Library): Boolean {
            return false
        }
    }

    companion object {
        private const val PRIVATE_PREF = "prefs"
    }


}
