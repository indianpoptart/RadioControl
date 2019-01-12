package com.nikhilparanjape.radiocontrol.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
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
import org.w3c.dom.Document
import org.xml.sax.SAXException
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Created by Nikhil on 4/5/2016.
 */

class AboutFragment : PreferenceFragment() {
    private var versionName = BuildConfig.VERSION_NAME


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about)


        val c = activity
        doAsync {
            SimpleChromeCustomTabs.initialize(c)
        }

        if (!Utilities.isConnected(c)) {
            preferenceScreen.findPreference("source").isEnabled = false
        }

        val versionPref = findPreference("version")
        val cs = versionName
        versionPref.summary = "v$cs"
        versionPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
            var z = 0

            override fun onPreferenceClick(preference: Preference): Boolean {
                val sp = c.getSharedPreferences(PRIVATE_PREF, Context.MODE_PRIVATE) //Initializes prefs.xml
                val editor = sp.edit()//Initializes xml editor
                z++
                Log.d("RadioControl", (7 - z).toString() + " steps away from easter egg")
                //Toast.makeText(getActivity(), (7 - z) + " steps away from easter egg", Toast.LENGTH_SHORT).show();
                if (z >= 7) {
                    if (!sp.getBoolean("isDeveloper", false)) {
                        Toast.makeText(activity, R.string.dev_activated, Toast.LENGTH_LONG).show()
                        z = 0
                        Log.d("RadioControl", "Developer features activated")


                        editor.putBoolean("isDeveloper", true) //Puts the boolean into prefs.xml
                        editor.apply() //Ends writing to prefs file
                    } else if (sp.getBoolean("isDeveloper", false)) {
                        Toast.makeText(activity, R.string.dev_deactivated, Toast.LENGTH_LONG).show()
                        z = 0
                        Log.d("RadioControl", c.getString(R.string.dev_deactivated))


                        editor.putBoolean("isDeveloper", false) //Puts the boolean into prefs.xml
                        editor.apply() //Ends writing to prefs file
                    }

                }
                return false
            }

        }

        val myPref = findPreference("changelog")
        myPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            changelog(c)
            false
        }

        val tutorialPref = findPreference("tutorial")
        tutorialPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            tutorial(c)
            false
        }

        val openSource = findPreference("source")
        openSource.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            displayLicensesAlertDialog(c)
            false
        }
        val supportSite = findPreference("support")
        supportSite.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            displaySupportWebsite(c)
            false
        }
        val aboutLib = findPreference("aboutLib")
        aboutLib.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            LibsBuilder()
                    .withLibraries("crouton", "actionbarsherlock", "showcaseview", "android_job")
                    .withAutoDetect(true)
                    .withLicenseShown(true)
                    .withVersionShown(true)
                    .withActivityTitle("Open Source Libraries")
                    .withActivityStyle(Libs.ActivityStyle.DARK)
                    .withListener(libsListener)
                    .withLibTaskCallback(libTaskCallback)
                    .withUiListener(libsUIListener)
                    .start(c)

            false
        }
    }

    private fun displayLicensesAlertDialog(c: Context) {
        if (Utilities.isConnected(c)) {
            val url = "https://nikhilp.org/radiocontrol/opensource"
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(4342338)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, Uri.parse(url))
        } else {
            Snackbar.make(view!!, "No internet connection found", Snackbar.LENGTH_LONG)
                    .show()
        }

    }

    private fun displaySupportWebsite(c: Context) {
        if (Utilities.isConnected(c)) {
            val url = "https://nikhilp.org/support"
            val builder = CustomTabsIntent.Builder()
            builder.setToolbarColor(4816556)

            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, Uri.parse(url))
        } else {
            Snackbar.make(view!!, "No internet connection found", Snackbar.LENGTH_LONG)
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

        override fun onLibTaskFinished(fastItemAdapter: ItemAdapter<*>) {
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
        fun getUpdate() {
            val doc: Document
            try {
                val xmlURL = URL("http://nikhilp.org/radiocontrol/backend/update_check.xml")
                val xml = xmlURL.openStream()
                val dbf = DocumentBuilderFactory.newInstance()
                val db = dbf.newDocumentBuilder()
                doc = db.parse(xml)
                xml.close()
            } catch (e: SAXException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
            }


        }
    }


}
