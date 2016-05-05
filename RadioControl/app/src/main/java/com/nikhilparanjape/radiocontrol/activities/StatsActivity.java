package com.nikhilparanjape.radiocontrol.activities;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.ExpoEase;
import com.db.chart.view.animation.style.BaseStyleAnimation;
import com.db.chart.view.animation.style.DashAnimation;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nikhilparanjape.radiocontrol.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;


public class StatsActivity extends AppCompatActivity {
    int wifiSigLost = 0;
    float janWifiLost = 0;
    float febWifiLost = 0;
    float marWifiLost = 0;
    float aprWifiLost = 0;
    float mayWifiLost = 0;
    float junWifiLost = 0;
    float julWifiLost = 0;
    float augWifiLost = 0;
    float sepWifiLost = 0;
    float octWifiLost = 0;
    float novWifiLost = 0;
    float decWifiLost = 0;

    int airplaneOn = 0;
    float janAirOn = 0;
    float febAirOn = 0;
    float marAirOn = 0;
    float aprAirOn = 0;
    float mayAirOn = 0;
    float junAirOn = 0;
    float julAirOn = 0;
    float augAirOn = 0;
    float sepAirOn = 0;
    float octAirOn = 0;
    float novAirOn = 0;
    float decAirOn = 0;

    LineChartView chart;
    LineChartView chart1;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        final ActionBar actionBar = getSupportActionBar();

        final CoordinatorLayout clayout = (CoordinatorLayout) findViewById(R.id.clayout);

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP).paddingDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Statistics");
        }

        final Animation anim = new Animation();
        anim.setDuration(1000);
        anim.setEasing(new ExpoEase());
        int[] ovLap = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        anim.setOverlap(1.0f, ovLap);
        anim.setAlpha(1);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_refresh_white_48dp));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Snackbar.make(clayout, "I don't do anything yet!", Snackbar.LENGTH_LONG)
                        .show();
            }
        });


        wifiLostGraph();

        airplaneOnGraph();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void wifiLostGraph() {
        //Initiate chart
        chart = (LineChartView) findViewById(R.id.linechart);
        //Initiate dataset for chart
        LineSet dataset = new LineSet();

        Animation anim = new Animation();
        anim.setDuration(1000);
        anim.setEasing(new ExpoEase());
        int[] ovLap = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        anim.setOverlap(1.0f, ovLap);
        anim.setAlpha(1);

        //Set chart defaults
        chart.addData(dataset);
        chart.setAxisColor(Color.BLACK);
        chart.setLabelsColor(Color.BLACK);
        chart.setXAxis(true);
        chart.setYAxis(true);
        chart.setYLabels(AxisController.LabelPosition.OUTSIDE);


        dataset.setColor(Color.BLACK)
                .setDotsColor(Color.parseColor("#758cbb"))
                .setDashed(new float[]{10f,10f})
                .beginAt(0);

        getWifiLost();


        dataset.addPoint("Jan", janWifiLost);
        dataset.addPoint("Feb", febWifiLost);
        dataset.addPoint("Mar", marWifiLost);
        dataset.addPoint("Apr", aprWifiLost);
        dataset.addPoint("May", mayWifiLost);
        dataset.addPoint("Jun", junWifiLost);
        dataset.addPoint("Jul", julWifiLost);
        dataset.addPoint("Aug", augWifiLost);
        dataset.addPoint("Sep", sepWifiLost);
        dataset.addPoint("Oct", octWifiLost);
        dataset.addPoint("Nov", novWifiLost);
        dataset.addPoint("Dec", decWifiLost);
        if (janWifiLost == 0 && febWifiLost == 0 && marWifiLost == 0 && aprWifiLost == 0 && mayWifiLost == 0 && junWifiLost == 0 && julWifiLost == 0 && augWifiLost == 0 &&
                sepWifiLost == 0 && octWifiLost == 0 && novWifiLost == 0 && decWifiLost == 0) {
            Log.d("RadioControl", "No log data");
        } else {
            float largest = Collections.max(Arrays.asList(janWifiLost, febWifiLost, marWifiLost, aprWifiLost, mayWifiLost, junWifiLost, julWifiLost, augWifiLost, sepWifiLost, octWifiLost, novWifiLost, decWifiLost));
            int max = (int) largest;

            chart.setAxisBorderValues(0, 0, max);
        }

        Log.d("RadioControl", "Lost Signal " + wifiSigLost + " times");

        chart.show(anim);
    }

    //Airplane on graph
    public void airplaneOnGraph() {
        //Initiate chart
        chart1 = (LineChartView) findViewById(R.id.linechart_airplane_on);
        //Initiate dataset for chart
        LineSet dataset = new LineSet();

        Animation anim = new Animation();
        anim.setDuration(1000);
        anim.setEasing(new ExpoEase());
        int[] ovLap = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        anim.setOverlap(1.0f, ovLap);
        anim.setAlpha(1);

        //Set chart defaults
        chart1.addData(dataset);
        chart1.setAxisColor(Color.BLACK);
        chart1.setLabelsColor(Color.BLACK);
        chart1.setXAxis(true);
        chart1.setYAxis(true);
        chart1.setYLabels(AxisController.LabelPosition.OUTSIDE);


        dataset.setColor(Color.BLACK)
                .setDotsColor(Color.parseColor("#758cbb"))
                .setDashed(new float[]{10f,10f})
                .beginAt(0);

        getAirplaneModeOn();


        dataset.addPoint("Jan", janAirOn);
        dataset.addPoint("Feb", febAirOn);
        dataset.addPoint("Mar", marAirOn);
        dataset.addPoint("Apr", aprAirOn);
        dataset.addPoint("May", mayAirOn);
        dataset.addPoint("Jun", junAirOn);
        dataset.addPoint("Jul", julAirOn);
        dataset.addPoint("Aug", augAirOn);
        dataset.addPoint("Sep", sepAirOn);
        dataset.addPoint("Oct", octAirOn);
        dataset.addPoint("Nov", novAirOn);
        dataset.addPoint("Dec", decAirOn);
        if (janAirOn == 0 && febAirOn == 0 && marAirOn == 0 && aprAirOn == 0 && mayAirOn == 0 && junAirOn == 0 && julAirOn == 0 && augAirOn == 0 &&
                sepAirOn == 0 && octAirOn == 0 && novAirOn == 0 && decAirOn == 0) {
            Log.d("RadioControl", "No log data");
        } else {
            float largest = Collections.max(Arrays.asList(janAirOn, febAirOn, marAirOn, aprAirOn, mayAirOn, junAirOn, julAirOn, augAirOn, sepAirOn, octAirOn, novAirOn, decAirOn));
            int max = (int) largest;
            chart1.setAxisBorderValues(0, 0, max);
        }


        Log.d("RadioControl", "Lost Signal " + airplaneOn + " times");

        chart1.show(anim);
    }

    public void getWifiLost() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String year = formatter.format(new Date());
        File log = new File(getApplicationContext().getFilesDir(), "radiocontrol.log");

        FileInputStream is;
        BufferedReader reader;
        try {
            if (log.exists()) {
                is = new FileInputStream(log);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while (line != null) {
                    line = reader.readLine();
                    if (countMatches(line, "lost") == 1) {
                        if (line.contains(year + "-01")) {
                            janWifiLost++;
                        } else if (line.contains(year + "-02")) {
                            febWifiLost++;
                        } else if (line.contains(year + "-03")) {
                            marWifiLost++;
                        } else if (line.contains(year + "-04")) {
                            aprWifiLost++;
                        } else if (line.contains(year + "-05")) {
                            mayWifiLost++;
                        } else if (line.contains(year + "-06")) {
                            junWifiLost++;
                        } else if (line.contains(year + "-07")) {
                            julWifiLost++;
                        } else if (line.contains(year + "-08")) {
                            augWifiLost++;
                        } else if (line.contains(year + "-09")) {
                            sepWifiLost++;
                        } else if (line.contains(year + "-10")) {
                            octWifiLost++;
                        } else if (line.contains(year + "-11")) {
                            novWifiLost++;
                        } else if (line.contains(year + "-12")) {
                            decWifiLost++;
                        }
                        wifiSigLost++;
                    }
                    Log.d("RadioControl", "LINE: " + line + " contains " + wifiSigLost);
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            Log.d("RadioControl", "Error: " + e);
            Snackbar.make(findViewById(android.R.id.content), "Error: " + e, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public void getAirplaneModeOn() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String year = formatter.format(new Date());
        File log = new File(getApplicationContext().getFilesDir(), "radiocontrol.log");

        FileInputStream is;
        BufferedReader reader;
        try {
            if (log.exists()) {
                is = new FileInputStream(log);
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();

                while (line != null) {
                    line = reader.readLine();
                    if (countMatches(line, "Airplane mode has been turned off") == 1) {
                        if (line.contains(year + "-01")) {
                            janAirOn++;
                        } else if (line.contains(year + "-02")) {
                            febAirOn++;
                        } else if (line.contains(year + "-03")) {
                            marAirOn++;
                        } else if (line.contains(year + "-04")) {
                            aprAirOn++;
                        } else if (line.contains(year + "-05")) {
                            mayAirOn++;
                        } else if (line.contains(year + "-06")) {
                            junAirOn++;
                        } else if (line.contains(year + "-07")) {
                            julAirOn++;
                        } else if (line.contains(year + "-08")) {
                            augAirOn++;
                        } else if (line.contains(year + "-09")) {
                            sepAirOn++;
                        } else if (line.contains(year + "-10")) {
                            octAirOn++;
                        } else if (line.contains(year + "-11")) {
                            novAirOn++;
                        } else if (line.contains(year + "-12")) {
                            decAirOn++;
                        }
                        airplaneOn++;
                    }
                    Log.d("RadioControl", "LINE: " + line + " contains " + airplaneOn);
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            Log.d("RadioControl", "Error: " + e);
            Snackbar.make(findViewById(android.R.id.content), "Error: " + e, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    public int countMatches(String str, String sub) {
        try {
            if (str.contains(sub)) {
                return 1;

            } else {
                return 0;
            }
        } catch (NullPointerException e) {
            Log.d("RadioControl", "Counting returned null");
        }
        return 0;

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
