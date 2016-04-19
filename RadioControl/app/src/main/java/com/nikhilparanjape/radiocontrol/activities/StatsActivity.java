package com.nikhilparanjape.radiocontrol.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nikhilparanjape.radiocontrol.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class StatsActivity extends AppCompatActivity {
    int wifiSigLost = 0;
    float jan = 0;
    float feb = 0;
    float mar = 0;
    float apr = 0;
    float may = 0;
    float jun = 0;
    float jul = 0;
    float aug = 0;
    float sep = 0;
    float oct = 0;
    float nov = 0;
    float dec = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        final ActionBar actionBar = getSupportActionBar();


        actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP).paddingDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Statistics");

        //Initiate chart
        LineChartView chart = (LineChartView) findViewById(R.id.linechart);
        //Initiate dataset for chart
        LineSet dataset = new LineSet();

        Animation anim = new Animation();
        anim.setDuration(1000);
        anim.setEasing(new BounceEase());

        //Set chart defaults
        chart.addData(dataset);
        chart.setAxisColor(Color.WHITE);
        chart.setLabelsColor(Color.WHITE);
        chart.setXAxis(true);
        chart.setYAxis(true);
        chart.setYLabels(AxisController.LabelPosition.OUTSIDE);


        dataset.setColor(Color.WHITE);

        getWifiLost();


        dataset.addPoint("Jan", jan);
        dataset.addPoint("Feb", feb);
        dataset.addPoint("Mar", mar);
        dataset.addPoint("Apr", apr);
        dataset.addPoint("May", may);
        dataset.addPoint("Jun", jun);
        dataset.addPoint("Jul", jul);
        dataset.addPoint("Aug", aug);
        dataset.addPoint("Sep", sep);
        dataset.addPoint("Oct", oct);
        dataset.addPoint("Nov", nov);
        dataset.addPoint("Dec", dec);

        Log.d("RadioControl","Lost Signal " + wifiSigLost + " times");
        TextView wifiLostText = (TextView) findViewById(R.id.wifiLostText);
        if(wifiSigLost == 1){
            wifiLostText.setText("1 time");
        }
        else{
            wifiLostText.setText(wifiSigLost + " times");
        }

        chart.show(anim);

    }

    public void getWifiLost(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        String year = formatter.format(new Date());
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
                        if(line.contains(year+"-01")){
                            jan++;
                        }
                        else if(line.contains(year+"-02")){
                            feb++;
                        }
                        else if(line.contains(year+"-03")){
                            mar++;
                        }
                        else if(line.contains(year+"-04")){
                            apr++;
                        }
                        else if(line.contains(year+"-05")){
                            may++;
                        }
                        else if(line.contains(year+"-06")){
                            jun++;
                        }
                        else if(line.contains(year+"-07")){
                            jul++;
                        }
                        else if(line.contains(year+"-08")){
                            aug++;
                        }
                        else if(line.contains(year+"-09")){
                            sep++;
                        }
                        else if(line.contains(year+"-10")){
                            oct++;
                        }
                        else if(line.contains(year+"-11")){
                            nov++;
                        }
                        else if(line.contains(year+"-12")){
                            dec++;
                        }
                        wifiSigLost++;
                    }
                    Log.d("RadioControl","LINE: " + line + " contains " + wifiSigLost);
                }

            }
            else{
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show();
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
