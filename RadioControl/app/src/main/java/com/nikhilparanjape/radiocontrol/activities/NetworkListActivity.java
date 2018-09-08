package com.nikhilparanjape.radiocontrol.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nikhilparanjape.radiocontrol.R;

import java.util.List;

public class NetworkListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_list);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.TOOLBAR_ICON_SIZE).paddingDp(IconicsDrawable.TOOLBAR_ICON_PADDING));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_activity_network);
        }


        ListView List = findViewById(R.id.network_list);
        Thread t = new Thread(() -> {
            listWifiNetworks(List);
        });

        // Start the thread
        t.start();

    }

    private void listWifiNetworks(ListView lv) {
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> networks = wm.getConfiguredNetworks();
        final SharedPreferences prefs = getSharedPreferences("disabled-networks", Context.MODE_PRIVATE);


        ListAdapter la = new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                return false;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return networks.size();
            }

            @Override
            public Object getItem(int position) {
                return networks.get(position);
            }

            @Override
            public long getItemId(int position) {
                return ((WifiConfiguration)getItem(position)).networkId;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {


                    LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    WifiConfiguration net = (WifiConfiguration) getItem(position);
                    convertView = vi.inflate(R.layout.wifi_network, parent, false);
                    TextView Name = convertView.findViewById(R.id.net_name);
                    final String ssid = net.SSID.substring(1, net.SSID.length() - 1);
                    Name.setText(ssid);
                    SwitchCompat on = convertView.findViewById(R.id.network_on);
                    Boolean isEnabled = prefs.getBoolean(net.SSID.substring(1, net.SSID.length() - 1), false);
                    on.setChecked(isEnabled);
                    on.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if(isChecked) {
                            prefs.edit().putBoolean(ssid, true).apply();
                        } else {
                            prefs.edit().remove(ssid).apply();
                        }
                    });


                }



                return convertView;
            }

            @Override
            public int getItemViewType(int position) {
                return 1;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        };
        lv.setAdapter(la);
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
}
