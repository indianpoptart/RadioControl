package com.nikhilparanjape.radiocontrol.activities

import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.nikhilparanjape.radiocontrol.R

/**
 * Created by Nikhil on Fill
 *
 * @author
 * An activity that lists all saved WiFi Networks
 */

class NetworkListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_list)
        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(IconicsDrawable(this)
                                                .icon(GoogleMaterial.Icon.gmd_arrow_back)
                                                .color(IconicsColor.colorInt(Color.WHITE))
                                                .size(IconicsSize.TOOLBAR_ICON_SIZE)
                                                .padding(IconicsSize.TOOLBAR_ICON_PADDING))
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.title_activity_network)
        }
        val netList = findViewById<ListView>(R.id.network_list)

        listWifiNetworks(netList)
    }

    private fun listWifiNetworks(lv: ListView) {
        val wm = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val networks = wm.configuredNetworks
        val prefs = getSharedPreferences("disabled-networks", Context.MODE_PRIVATE)

        val la = object : ListAdapter {
            override fun areAllItemsEnabled(): Boolean {
                return false
            }

            override fun isEnabled(position: Int): Boolean {
                return false
            }

            override fun registerDataSetObserver(observer: DataSetObserver) {

            }

            override fun unregisterDataSetObserver(observer: DataSetObserver) {

            }

            override fun getCount(): Int {
                return networks.size
            }

            override fun getItem(position: Int): Any {
                return networks[position]
            }

            override fun getItemId(position: Int): Long {
                return (getItem(position) as WifiConfiguration).networkId.toLong()
            }

            override fun hasStableIds(): Boolean {
                return false
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
                var convertViewGet = convertView
                if (convertViewGet == null) {
                    val vi = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

                    val net = getItem(position) as WifiConfiguration
                    convertViewGet = vi.inflate(R.layout.wifi_network, parent, false)
                    val name = convertViewGet.findViewById<TextView>(R.id.net_name)
                    val ssid = net.SSID.substring(1, net.SSID.length - 1)
                    name.text = ssid
                    val on = convertViewGet.findViewById<SwitchCompat>(R.id.network_on)
                    val isEnabled = prefs.getBoolean(net.SSID.substring(1, net.SSID.length - 1), false)
                    on.isChecked = isEnabled
                    on.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            prefs.edit().putBoolean(ssid, true).apply()
                        } else {
                            prefs.edit().remove(ssid).apply()
                        }
                    }
                }

                return convertViewGet
            }

            override fun getItemViewType(position: Int): Int {
                return 1
            }

            override fun getViewTypeCount(): Int {
                return 1
            }

            override fun isEmpty(): Boolean {
                return false
            }
        }
        lv.adapter = la
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
}
