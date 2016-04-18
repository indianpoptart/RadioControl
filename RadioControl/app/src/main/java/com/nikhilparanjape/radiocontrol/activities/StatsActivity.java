package com.nikhilparanjape.radiocontrol.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nikhilparanjape.radiocontrol.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;


public class StatsActivity extends AppCompatActivity {
    int wifiSigLost = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        final ActionBar actionBar = getSupportActionBar();


        actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP).paddingDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Statistics");

        LineChart chart = (LineChart) findViewById(R.id.chart);
        XAxis xAxis = chart.getXAxis();

        YAxis leftAxis = chart.getAxisLeft();

        chart.setEnabled(true);

        xAxis.setEnabled(true);
        leftAxis.setEnabled(true);

        xAxis.setDrawLabels(true);

        LimitLine ll = new LimitLine(140f, "WiFi Signal Lost");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(4f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);
        // .. and more styling options

        xAxis.addLimitLine(ll);
        getWifiLost();
        Log.d("RadioControl","Lost Signal " + wifiSigLost + " times");
        TextView wifiLostText = (TextView) findViewById(R.id.wifiLostText);
        if(wifiSigLost == 1){
            wifiLostText.setText("1 time");
        }
        else{
            wifiLostText.setText(wifiSigLost + " times");
        }


    }

    public void getWifiLost(){
        File log = new File(getApplicationContext().getFilesDir(), "radiocontrol.log");

        FileInputStream is;
        BufferedReader reader;
        int count=0;
        try {
            if (log.exists()) {
                is = new FileInputStream(log);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while (line != null) {
                    line = reader.readLine();
                    if(countMatches(line,"lost") == 1){
                        wifiSigLost++;
                    }
                    Log.d("RadioControl","LINE: " + line + " contains " + wifiSigLost);
                }

            }
            else{
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show();
                wifiSigLost = 0;
            }
        } catch(IOException e){
            Log.d("RadioControl", "Error: " + e);
            Snackbar.make(findViewById(android.R.id.content), "Error: " + e, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public int countMatches(String str, String sub) {
        try{
            if (str.contains(sub)) {
                return 1;

            } else {
                return 0;
            }
        } catch(NullPointerException e){

        }
        return 0;

    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
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
