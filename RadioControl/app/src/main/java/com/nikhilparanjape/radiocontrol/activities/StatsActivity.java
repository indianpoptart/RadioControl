package com.nikhilparanjape.radiocontrol.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.BounceEase;
import com.db.chart.view.animation.easing.CircEase;
import com.db.chart.view.animation.easing.CubicEase;
import com.db.chart.view.animation.easing.ElasticEase;
import com.db.chart.view.animation.easing.ExpoEase;
import com.db.chart.view.animation.easing.LinearEase;
import com.db.chart.view.animation.easing.QuadEase;
import com.db.chart.view.animation.easing.QuartEase;
import com.db.chart.view.animation.easing.QuintEase;
import com.db.chart.view.animation.easing.SineEase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.crash.FirebaseCrash;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.nikhilparanjape.radiocontrol.R;
import com.nikhilparanjape.radiocontrol.rootUtils.Fab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import me.grantland.widget.AutofitHelper;


public class StatsActivity extends AppCompatActivity {
    int wifiSigLost = 0;
    //Sets float for wifiLost
    float janWifiLost = 0;float febWifiLost = 0;float marWifiLost = 0;float aprWifiLost = 0;float mayWifiLost = 0;float junWifiLost = 0;float julWifiLost = 0;float augWifiLost = 0;float sepWifiLost = 0;float octWifiLost = 0;float novWifiLost = 0;float decWifiLost = 0;

    int airplaneOn = 0;
    //Sets float for airplane on
    float janAirOn = 0;float febAirOn = 0;float marAirOn = 0;float aprAirOn = 0;float mayAirOn = 0;float junAirOn = 0;float julAirOn = 0;float augAirOn = 0;float sepAirOn = 0;float octAirOn = 0;float novAirOn = 0;float decAirOn = 0;

    LineChartView chart;
    LineChartView chart1;
    MaterialSheetFab materialSheetFab;

    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = prefs.edit();
        if(!prefs.contains("gridlines")){
            editor.putInt("gridlines",0);
            editor.apply();
        }
        if(!prefs.contains("easing")){
            editor.putInt("easing",4);
            editor.apply();
        }


        final ActionBar actionBar = getSupportActionBar();
        //Sets coordlayout
        final CoordinatorLayout clayout = (CoordinatorLayout) findViewById(R.id.clayout);

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_DP).paddingDp(IconicsDrawable.ANDROID_ACTIONBAR_ICON_SIZE_PADDING_DP));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Statistics");
        }

        Fab fab = (Fab) findViewById(R.id.fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.white);
        int fabColor = getResources().getColor(R.color.accent);

        // Initialize material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay,
                sheetColor, fabColor);

        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                // Called when the material sheet's "show" animation starts.
            }

            @Override
            public void onSheetShown() {
                // Called when the material sheet's "show" animation ends.
            }

            @Override
            public void onHideSheet() {
                // Called when the material sheet's "hide" animation starts.
            }

            public void onSheetHidden() {
                // Called when the material sheet's "hide" animation ends.
            }
        });

        TextView btn=(TextView) findViewById(R.id.fab_sheet_item_grid);
        assert btn !=null;
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                materialSheetFab.hideSheet();
                showGridlineDialog();
            }
        });

        TextView btn1 = (TextView) findViewById(R.id.fab_sheet_item_easing);
        AutofitHelper.create(btn1);
        assert btn1 != null;
        btn1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                materialSheetFab.hideSheet();
                showLongList();
            }
        });

        wifiLostGraph();

        airplaneOnGraph();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        //client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
    public void showColorChooserPrimary() {
    }
    public void showLongList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = prefs.edit();

        new MaterialDialog.Builder(this)
                .title("Easing Animation")
                .theme(Theme.LIGHT)
                .items(R.array.easing)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d("RadioControl","Easing: " + which);
                        if(which == 0){
                            editor.putInt("easing", 0);
                        } else if(which == 1){
                            editor.putInt("easing", 1);
                        } else if(which == 2){
                            editor.putInt("easing", 2);
                        } else if(which == 3){
                            editor.putInt("easing", 3);
                        } else if(which == 4){
                            editor.putInt("easing", 4);
                        } else if(which == 5){
                            editor.putInt("easing", 5);
                        } else if(which == 6){
                            editor.putInt("easing", 6);
                        } else if(which == 7){
                            editor.putInt("easing", 7);
                        } else if(which == 8){
                            editor.putInt("easing", 8);
                        } else if(which == 9){
                            editor.putInt("easing", 9);
                        }
                        editor.apply();
                        recreate();
                    }
                })
                .positiveText(android.R.string.cancel)
                .show();
    }
    public void showGridlineDialog(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = prefs.edit();

        new MaterialDialog.Builder(this)
                .title("Gridlines")
                .theme(Theme.LIGHT)
                .items(R.array.preference_values)
                .itemsCallbackSingleChoice(2, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.d("RadioControl","You chose " + which);
                        float[] n = {0f,0f};
                        if(which == 0){
                            //Full Grids
                            editor.putInt("gridlines",0);
                        } else if(which == 1){
                            //Horizontal Grids
                            editor.putInt("gridlines",1);
                        } else if(which == 2){
                            //Vertical Grids
                            editor.putInt("gridlines",2);
                        } else if(which == 3){
                            //No Grids
                            editor.putInt("gridlines",3);
                        }
                        editor.apply();
                        recreate();
                        return true; // allow selection
                    }
                })
                .positiveText("Choose")
                .show();
    }
    public void wifiLostGraph() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Initiate chart
        chart = (LineChartView) findViewById(R.id.linechart);
        //Initiate dataset for chart
        LineSet dataset = new LineSet();

        Animation anim = new Animation();
        anim.setDuration(1000);
        if(prefs.getInt("easing",4) == 0){
            anim.setEasing(new BounceEase());
        } else if(prefs.getInt("easing",4) == 1){
            anim.setEasing(new CircEase());
        } else if(prefs.getInt("easing",4) == 2){
            anim.setEasing(new CubicEase());
        } else if(prefs.getInt("easing",4) == 3){
            anim.setEasing(new ElasticEase());
        } else if(prefs.getInt("easing",4) == 4){
            anim.setEasing(new ExpoEase());
        } else if(prefs.getInt("easing",4) == 5){
            anim.setEasing(new LinearEase());
        } else if(prefs.getInt("easing",4) == 6){
            anim.setEasing(new QuadEase());
        } else if(prefs.getInt("easing",4) == 7){
            anim.setEasing(new QuartEase());
        } else if(prefs.getInt("easing",4) == 8){
            anim.setEasing(new QuintEase());
        } else if(prefs.getInt("easing",4) == 9){
            anim.setEasing(new SineEase());
        }

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

        //Sets dataset points
        dataset.addPoint("Jan", janWifiLost);dataset.addPoint("Feb", febWifiLost);dataset.addPoint("Mar", marWifiLost);dataset.addPoint("Apr", aprWifiLost);dataset.addPoint("May", mayWifiLost);dataset.addPoint("Jun", junWifiLost);dataset.addPoint("Jul", julWifiLost);dataset.addPoint("Aug", augWifiLost);dataset.addPoint("Sep", sepWifiLost);dataset.addPoint("Oct", octWifiLost);dataset.addPoint("Nov", novWifiLost);dataset.addPoint("Dec", decWifiLost);

        if (janWifiLost == 0 && febWifiLost == 0 && marWifiLost == 0 && aprWifiLost == 0 && mayWifiLost == 0 && junWifiLost == 0 && julWifiLost == 0 && augWifiLost == 0 &&
                sepWifiLost == 0 && octWifiLost == 0 && novWifiLost == 0 && decWifiLost == 0) {
            Log.d("RadioControl", "No log data");
        } else {
            float largest = Collections.max(Arrays.asList(janWifiLost, febWifiLost, marWifiLost, aprWifiLost, mayWifiLost, junWifiLost, julWifiLost, augWifiLost, sepWifiLost, octWifiLost, novWifiLost, decWifiLost));
            int max = (int) largest;

            chart.setAxisBorderValues(0, 0, max);
        }

        Log.d("RadioControl", "Lost Signal " + wifiSigLost + " times");

        Paint p = new Paint();
        p.setColor(Color.BLACK );
        if (prefs.getInt("gridlines",0) == 0){
            chart.setGrid(ChartView.GridType.FULL, p);
        } else if(prefs.getInt("gridlines",0) == 1){
            chart.setGrid(ChartView.GridType.HORIZONTAL, p);
        } else if(prefs.getInt("gridlines",0) == 2){
            chart.setGrid(ChartView.GridType.VERTICAL, p);
        } else if(prefs.getInt("gridlines",0) == 3){
            chart.setGrid(ChartView.GridType.NONE, p);
        }

        chart.show(anim);
    }

    //Airplane on graph
    public void airplaneOnGraph() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Initiate chart
        chart1 = (LineChartView) findViewById(R.id.linechart_airplane_on);
        //Initiate dataset for chart
        LineSet dataset = new LineSet();

        Animation anim = new Animation();
        anim.setDuration(1000);
        if(prefs.getInt("easing",4) == 0){
            anim.setEasing(new BounceEase());
        } else if(prefs.getInt("easing",4) == 1){
            anim.setEasing(new CircEase());
        } else if(prefs.getInt("easing",4) == 2){
            anim.setEasing(new CubicEase());
        } else if(prefs.getInt("easing",4) == 3){
            anim.setEasing(new ElasticEase());
        } else if(prefs.getInt("easing",4) == 4){
            anim.setEasing(new ExpoEase());
        } else if(prefs.getInt("easing",4) == 5){
            anim.setEasing(new LinearEase());
        } else if(prefs.getInt("easing",4) == 6){
            anim.setEasing(new QuadEase());
        } else if(prefs.getInt("easing",4) == 7){
            anim.setEasing(new QuartEase());
        } else if(prefs.getInt("easing",4) == 8){
            anim.setEasing(new QuintEase());
        } else if(prefs.getInt("easing",4) == 9){
            anim.setEasing(new SineEase());
        }

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

        //Sets dataset points
        dataset.addPoint("Jan", janAirOn);dataset.addPoint("Feb", febAirOn);dataset.addPoint("Mar", marAirOn);dataset.addPoint("Apr", aprAirOn);dataset.addPoint("May", mayAirOn);dataset.addPoint("Jun", junAirOn);dataset.addPoint("Jul", julAirOn);dataset.addPoint("Aug", augAirOn);dataset.addPoint("Sep", sepAirOn);dataset.addPoint("Oct", octAirOn);dataset.addPoint("Nov", novAirOn);dataset.addPoint("Dec", decAirOn);

        if (janAirOn == 0 && febAirOn == 0 && marAirOn == 0 && aprAirOn == 0 && mayAirOn == 0 && junAirOn == 0 && julAirOn == 0 && augAirOn == 0 &&
                sepAirOn == 0 && octAirOn == 0 && novAirOn == 0 && decAirOn == 0) {
            Log.d("RadioControl", "No log data");
        } else {
            float largest = Collections.max(Arrays.asList(janAirOn, febAirOn, marAirOn, aprAirOn, mayAirOn, junAirOn, julAirOn, augAirOn, sepAirOn, octAirOn, novAirOn, decAirOn));
            int max = (int) largest;
            chart1.setAxisBorderValues(0, 0, max);
        }


        Log.d("RadioControl", "Lost Signal " + airplaneOn + " times");

        Paint p = new Paint();
        p.setColor(Color.BLACK );
        if (prefs.getInt("gridlines",0) == 0){
            chart1.setGrid(ChartView.GridType.FULL, p);
        } else if(prefs.getInt("gridlines",0) == 1){
            chart1.setGrid(ChartView.GridType.HORIZONTAL, p);
        } else if(prefs.getInt("gridlines",0) == 2){
            chart1.setGrid(ChartView.GridType.VERTICAL, p);
        } else if(prefs.getInt("gridlines",0) == 3){
            chart1.setGrid(ChartView.GridType.NONE, p);
        }
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
                    //Log.d("RadioControl", "LINE: " + line + " contains " + wifiSigLost);
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show();
            }
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, "RadioControl", "Unable to get version name");
            FirebaseCrash.report(e);
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
                    //Log.d("RadioControl", "LINE: " + line + " contains " + airplaneOn);
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

    // Backwards compatible recreate().
    @Override
    public void recreate()
    {
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            super.recreate();
        }
        else
        {
            startActivity(getIntent());
            finish();
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
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
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
