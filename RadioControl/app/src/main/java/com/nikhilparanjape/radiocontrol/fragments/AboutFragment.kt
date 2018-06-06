package com.nikhilparanjape.radiocontrol.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.Snackbar
import android.util.Log
import android.widget.Toast


import com.nikhilparanjape.radiocontrol.BuildConfig
import com.nikhilparanjape.radiocontrol.activities.ChangeLogActivity
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.activities.TutorialActivity
import com.nikhilparanjape.radiocontrol.rootUtils.Utilities
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs

import org.w3c.dom.Document
import org.xml.sax.SAXException

import java.io.IOException
import java.io.InputStream
import java.net.URL

import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Created by Nikhil on 4/5/2016.
 */
class AboutFragment : PreferenceFragment() {
    internal var versionName = BuildConfig.VERSION_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.about)


        val c = activity
        SimpleChromeCustomTabs.initialize(c)

        if (!Utilities.isConnected(c)) {
            preferenceScreen.findPreference("source").isEnabled = false
        }

        val versionPref = findPreference("version")
        val cs = versionName
        versionPref.summary = "v$cs"
        versionPref.onPreferenceClickListener = object : Preference.OnPreferenceClickListener {
            internal var z = 0

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

    companion object {
        private val PRIVATE_PREF = "prefs"
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
