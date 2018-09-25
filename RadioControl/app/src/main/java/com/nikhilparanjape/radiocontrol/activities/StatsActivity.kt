package com.nikhilparanjape.radiocontrol.activities

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.db.chart.model.LineSet
import com.db.chart.view.AxisController
import com.db.chart.view.ChartView
import com.db.chart.view.LineChartView
import com.db.chart.view.animation.Animation
import com.db.chart.view.animation.easing.BounceEase
import com.db.chart.view.animation.easing.CircEase
import com.db.chart.view.animation.easing.CubicEase
import com.db.chart.view.animation.easing.ElasticEase
import com.db.chart.view.animation.easing.ExpoEase
import com.db.chart.view.animation.easing.LinearEase
import com.db.chart.view.animation.easing.QuadEase
import com.db.chart.view.animation.easing.QuartEase
import com.db.chart.view.animation.easing.QuintEase
import com.db.chart.view.animation.easing.SineEase
import com.gordonwong.materialsheetfab.MaterialSheetFab
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.rootUtils.Fab

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Collections
import java.util.Date

import me.grantland.widget.AutofitHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class StatsActivity : AppCompatActivity() {
    internal var wifiSigLost = 0
    //Sets float for wifiLost
    internal var janWifiLost = 0f
    internal var febWifiLost = 0f
    internal var marWifiLost = 0f
    internal var aprWifiLost = 0f
    internal var mayWifiLost = 0f
    internal var junWifiLost = 0f
    internal var julWifiLost = 0f
    internal var augWifiLost = 0f
    internal var sepWifiLost = 0f
    internal var octWifiLost = 0f
    internal var novWifiLost = 0f
    internal var decWifiLost = 0f

    internal var airplaneOn = 0
    //Sets float for airplane on
    internal var janAirOn = 0f
    internal var febAirOn = 0f
    internal var marAirOn = 0f
    internal var aprAirOn = 0f
    internal var mayAirOn = 0f
    internal var junAirOn = 0f
    internal var julAirOn = 0f
    internal var augAirOn = 0f
    internal var sepAirOn = 0f
    internal var octAirOn = 0f
    internal var novAirOn = 0f
    internal var decAirOn = 0f

    internal var rootOn = 0
    //Sets float for airplane on
    internal var janRootOn = 0f
    internal var febRootOn = 0f
    internal var marRootOn = 0f
    internal var aprRootOn = 0f
    internal var mayRootOn = 0f
    internal var junRootOn = 0f
    internal var julRootOn = 0f
    internal var augRootOn = 0f
    internal var sepRootOn = 0f
    internal var octRootOn = 0f
    internal var novRootOn = 0f
    internal var decRootOn = 0f

    lateinit var chart: LineChartView
    lateinit var chart1: LineChartView
    lateinit var chart2: LineChartView
    lateinit var materialSheetFab: MaterialSheetFab<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        if (!prefs.contains("gridlines")) {
            editor.putInt("gridlines", 0)
            editor.apply()
        }
        if (!prefs.contains("easing")) {
            editor.putInt("easing", 4)
            editor.apply()
        }

        val actionBar = findViewById<Toolbar>(R.id.toolbar)
        //Sets coordlayout
        findViewById<CoordinatorLayout>(R.id.clayout)

        if (actionBar != null) {
            //actionBar.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).color(Color.WHITE).sizeDp(IconicsDrawable.TOOLBAR_ICON_SIZE).paddingDp(IconicsDrawable.TOOLBAR_ICON_PADDING))
            //actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Statistics"
        }

        val fab = findViewById<Fab>(R.id.fab)
        val sheetView = findViewById<View>(R.id.fab_sheet)
        val overlay = findViewById<View>(R.id.overlay)
        val sheetColor = ContextCompat.getColor(applicationContext, R.color.white)
        val fabColor = ContextCompat.getColor(applicationContext, R.color.accent)

        // Initialize material sheet FAB
        materialSheetFab = MaterialSheetFab(fab, sheetView, overlay,
                sheetColor, fabColor)

        materialSheetFab.setEventListener(object : MaterialSheetFabEventListener() {
            override fun onShowSheet() {
                // Called when the material sheet's "show" animation starts.
            }

            override fun onSheetShown() {
                // Called when the material sheet's "show" animation ends.
            }

            override fun onHideSheet() {
                // Called when the material sheet's "hide" animation starts.
            }

            override fun onSheetHidden() {
                // Called when the material sheet's "hide" animation ends.
            }
        })


        val btn = findViewById<TextView>(R.id.fab_sheet_item_grid)!!
        btn.setOnClickListener { _ ->
            materialSheetFab.hideSheet()
            showGridlineDialog()
        }

        val btn1 = findViewById<TextView>(R.id.fab_sheet_item_easing)
        AutofitHelper.create(btn1)
        btn1.setOnClickListener { _ ->
            materialSheetFab.hideSheet()
            showLongList()
        }

        val btn2 = findViewById<TextView>(R.id.fab_sheet_item_duration)
        AutofitHelper.create(btn2)
        btn2.setOnClickListener { _ ->
            materialSheetFab.hideSheet()
            showDurationDialog()

        }

    }

    override fun onResume() {
        super.onResume()

        //chart.notifyDataUpdate()
        //chart1.notifyDataUpdate()
        //chart2.notifyDataUpdate()

        doAsync {
            try{
                wifiLostGraph()

                airplaneOnGraph()

                rootAccessGraph()
            }catch (e: Exception){
                Log.d("RadioControl", "Error: $e");
            }

        }


    }

    fun showLongList() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()

        MaterialDialog.Builder(this)
                .title("Easing Animation")
                .theme(Theme.DARK)
                .items(R.array.easing)
                .itemsCallback { _, _, which, _ ->
                    Log.d("RadioControl", "Easing: $which")
                    if (which == 0) {
                        editor.putInt("easing", 0)
                    } else if (which == 1) {
                        editor.putInt("easing", 1)
                    } else if (which == 2) {
                        editor.putInt("easing", 2)
                    } else if (which == 3) {
                        editor.putInt("easing", 3)
                    } else if (which == 4) {
                        editor.putInt("easing", 4)
                    } else if (which == 5) {
                        editor.putInt("easing", 5)
                    } else if (which == 6) {
                        editor.putInt("easing", 6)
                    } else if (which == 7) {
                        editor.putInt("easing", 7)
                    } else if (which == 8) {
                        editor.putInt("easing", 8)
                    } else if (which == 9) {
                        editor.putInt("easing", 9)
                    }
                    editor.apply()
                    recreate()
                }
                .positiveText(android.R.string.cancel)
                .show()
    }

    fun showDurationDialog() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        var index = prefs.getInt("duration", 2)
        index -= 1

        MaterialDialog.Builder(this)
                .title("Animation Duration")
                .theme(Theme.DARK)
                .items(R.array.animation_duration_values)
                .itemsCallbackSingleChoice(index) { _, _, which, _ ->
                    Log.d("RadioControl", "You chose $which")
                    floatArrayOf(0f, 0f)
                    if (which == 0) {
                        //1 second delay
                        editor.putInt("duration", 1)
                    } else if (which == 1) {
                        //2 second delay
                        editor.putInt("duration", 2)
                    } else if (which == 2) {
                        //3 Second delay
                        editor.putInt("duration", 3)
                    } else if (which == 3) {
                        //4 second delay
                        editor.putInt("duration", 4)
                    }
                    else if (which == 4) {
                        //5 second delay
                        editor.putInt("duration", 5)
                    }
                    editor.apply()
                    recreate()
                    true // allow selection
                }
                .positiveText("Choose")
                .show()
    }

    fun showGridlineDialog() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        val index = prefs.getInt("gridlines", 2)

        MaterialDialog.Builder(this)
                .title("Gridlines")
                .theme(Theme.DARK)
                .items(R.array.preference_values)
                .itemsCallbackSingleChoice(index) { _, _, which, _ ->
                    Log.d("RadioControl", "You chose $which")
                    floatArrayOf(0f, 0f)
                    if (which == 0) {
                        //Full Grids
                        editor.putInt("gridlines", 0)
                    } else if (which == 1) {
                        //Horizontal Grids
                        editor.putInt("gridlines", 1)
                    } else if (which == 2) {
                        //Vertical Grids
                        editor.putInt("gridlines", 2)
                    } else if (which == 3) {
                        //No Grids
                        editor.putInt("gridlines", 3)
                    }
                    editor.apply()
                    recreate()
                    true // allow selection
                }
                .positiveText("Choose")
                .show()
    }

    fun wifiLostGraph() {
        val progress1 = findViewById<ProgressBar>(R.id.progressWifiOff)
        progress1.visibility = View.VISIBLE

        doAsync {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart = findViewById(R.id.linechart)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)

            val anim = Animation()
            anim.setDuration(dura*1000)
            if (prefs.getInt("easing", 4) == 0) {
                anim.setEasing(BounceEase())
            } else if (prefs.getInt("easing", 4) == 1) {
                anim.setEasing(CircEase())
            } else if (prefs.getInt("easing", 4) == 2) {
                anim.setEasing(CubicEase())
            } else if (prefs.getInt("easing", 4) == 3) {
                anim.setEasing(ElasticEase())
            } else if (prefs.getInt("easing", 4) == 4) {
                anim.setEasing(ExpoEase())
            } else if (prefs.getInt("easing", 4) == 5) {
                anim.setEasing(LinearEase())
            } else if (prefs.getInt("easing", 4) == 6) {
                anim.setEasing(QuadEase())
            } else if (prefs.getInt("easing", 4) == 7) {
                anim.setEasing(QuartEase())
            } else if (prefs.getInt("easing", 4) == 8) {
                anim.setEasing(QuintEase())
            } else if (prefs.getInt("easing", 4) == 9) {
                anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

            //Set chart defaults
            chart.addData(dataset)
            chart.setAxisColor(Color.BLACK)
            chart.setLabelsColor(Color.BLACK)
            chart.setXAxis(true)
            chart.setYAxis(true)
            chart.setYLabels(AxisController.LabelPosition.OUTSIDE)


            dataset.setColor(Color.BLACK)
                    .setDotsColor(Color.parseColor("#758cbb"))
                    .setDashed(floatArrayOf(10f, 10f))
                    .beginAt(0)

            getWifiLost()

            //Sets dataset points
            dataset.addPoint("Jan", janWifiLost)
            dataset.addPoint("Feb", febWifiLost)
            dataset.addPoint("Mar", marWifiLost)
            dataset.addPoint("Apr", aprWifiLost)
            dataset.addPoint("May", mayWifiLost)
            dataset.addPoint("Jun", junWifiLost)
            dataset.addPoint("Jul", julWifiLost)
            dataset.addPoint("Aug", augWifiLost)
            dataset.addPoint("Sep", sepWifiLost)
            dataset.addPoint("Oct", octWifiLost)
            dataset.addPoint("Nov", novWifiLost)
            dataset.addPoint("Dec", decWifiLost)

            if (janWifiLost == 0f && febWifiLost == 0f && marWifiLost == 0f && aprWifiLost == 0f && mayWifiLost == 0f && junWifiLost == 0f && julWifiLost == 0f && augWifiLost == 0f &&
                    sepWifiLost == 0f && octWifiLost == 0f && novWifiLost == 0f && decWifiLost == 0f) {
                Log.d("RadioControl", "No log data")
            } else {
                val largest = Collections.max(Arrays.asList(janWifiLost, febWifiLost, marWifiLost, aprWifiLost, mayWifiLost, junWifiLost, julWifiLost, augWifiLost, sepWifiLost, octWifiLost, novWifiLost, decWifiLost))
                val max = largest.toInt()

                chart.setAxisBorderValues(0, 0, max)
            }

            Log.d("RadioControl", "Lost Signal $wifiSigLost times")

            val p = Paint()
            p.color = Color.BLACK
            if (prefs.getInt("gridlines", 0) == 0) {
                chart.setGrid(ChartView.GridType.FULL, p)
            } else if (prefs.getInt("gridlines", 0) == 1) {
                chart.setGrid(ChartView.GridType.HORIZONTAL, p)
            } else if (prefs.getInt("gridlines", 0) == 2) {
                chart.setGrid(ChartView.GridType.VERTICAL, p)
            } else if (prefs.getInt("gridlines", 0) == 3) {
                chart.setGrid(ChartView.GridType.NONE, p)
            }

            uiThread {
                progress1.visibility = View.GONE
                chart.show(anim)
            }
        }
    }

    //Airplane on graph
    fun airplaneOnGraph() {
        val progress2 = findViewById<ProgressBar>(R.id.progressAirplaneOn)
        progress2.visibility = View.VISIBLE
        doAsync {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart1 = findViewById(R.id.linechart_airplane_on)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)
            val anim = Animation()
            anim.setDuration(dura*1000)
            if (prefs.getInt("easing", 4) == 0) {
                anim.setEasing(BounceEase())
            } else if (prefs.getInt("easing", 4) == 1) {
                anim.setEasing(CircEase())
            } else if (prefs.getInt("easing", 4) == 2) {
                anim.setEasing(CubicEase())
            } else if (prefs.getInt("easing", 4) == 3) {
                anim.setEasing(ElasticEase())
            } else if (prefs.getInt("easing", 4) == 4) {
                anim.setEasing(ExpoEase())
            } else if (prefs.getInt("easing", 4) == 5) {
                anim.setEasing(LinearEase())
            } else if (prefs.getInt("easing", 4) == 6) {
                anim.setEasing(QuadEase())
            } else if (prefs.getInt("easing", 4) == 7) {
                anim.setEasing(QuartEase())
            } else if (prefs.getInt("easing", 4) == 8) {
                anim.setEasing(QuintEase())
            } else if (prefs.getInt("easing", 4) == 9) {
                anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

            //Set chart defaults
            chart1.addData(dataset)
            chart1.setAxisColor(Color.BLACK)
            chart1.setLabelsColor(Color.BLACK)
            chart1.setXAxis(true)
            chart1.setYAxis(true)
            chart1.setYLabels(AxisController.LabelPosition.OUTSIDE)

            //Sets color of dataset points
            dataset.setColor(Color.BLACK)
                    .setDotsColor(Color.parseColor("#758cbb"))
                    .setDashed(floatArrayOf(10f, 10f))
                    .beginAt(0)
            //gets airplane mode stats
            getAirplaneModeOn()

            //Sets dataset points
            dataset.addPoint("Jan", janAirOn)
            dataset.addPoint("Feb", febAirOn)
            dataset.addPoint("Mar", marAirOn)
            dataset.addPoint("Apr", aprAirOn)
            dataset.addPoint("May", mayAirOn)
            dataset.addPoint("Jun", junAirOn)
            dataset.addPoint("Jul", julAirOn)
            dataset.addPoint("Aug", augAirOn)
            dataset.addPoint("Sep", sepAirOn)
            dataset.addPoint("Oct", octAirOn)
            dataset.addPoint("Nov", novAirOn)
            dataset.addPoint("Dec", decAirOn)


            //Checker of
            if (janAirOn == 0f && febAirOn == 0f && marAirOn == 0f && aprAirOn == 0f && mayAirOn == 0f && junAirOn == 0f && julAirOn == 0f && augAirOn == 0f &&
                    sepAirOn == 0f && octAirOn == 0f && novAirOn == 0f && decAirOn == 0f) {
                Log.d("RadioControl", "No log data")
            } else {
                val largest = Collections.max(Arrays.asList(janAirOn, febAirOn, marAirOn, aprAirOn, mayAirOn, junAirOn, julAirOn, augAirOn, sepAirOn, octAirOn, novAirOn, decAirOn))
                val max = largest.toInt()
                chart1.setAxisBorderValues(0, 0, max)
            }


            Log.d("RadioControl", "Lost Signal $airplaneOn times")

            val p = Paint()
            p.color = Color.BLACK
            if (prefs.getInt("gridlines", 0) == 0) {
                chart1.setGrid(ChartView.GridType.FULL, p)
            } else if (prefs.getInt("gridlines", 0) == 1) {
                chart1.setGrid(ChartView.GridType.HORIZONTAL, p)
            } else if (prefs.getInt("gridlines", 0) == 2) {
                chart1.setGrid(ChartView.GridType.VERTICAL, p)
            } else if (prefs.getInt("gridlines", 0) == 3) {
                chart1.setGrid(ChartView.GridType.NONE, p)
            }

            uiThread {
                progress2.visibility = View.GONE

                chart1.show(anim)
            }
        }
    }

    fun rootAccessGraph(){
        val progress3 = findViewById<ProgressBar>(R.id.progressRootAccess)
        progress3.visibility = View.VISIBLE
        doAsync {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart2 = findViewById(R.id.linechart_root_access)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)

            val anim = Animation()
            anim.setDuration(dura*1000)
            if (prefs.getInt("easing", 4) == 0) {
                anim.setEasing(BounceEase())
            } else if (prefs.getInt("easing", 4) == 1) {
                anim.setEasing(CircEase())
            } else if (prefs.getInt("easing", 4) == 2) {
                anim.setEasing(CubicEase())
            } else if (prefs.getInt("easing", 4) == 3) {
                anim.setEasing(ElasticEase())
            } else if (prefs.getInt("easing", 4) == 4) {
                anim.setEasing(ExpoEase())
            } else if (prefs.getInt("easing", 4) == 5) {
                anim.setEasing(LinearEase())
            } else if (prefs.getInt("easing", 4) == 6) {
                anim.setEasing(QuadEase())
            } else if (prefs.getInt("easing", 4) == 7) {
                anim.setEasing(QuartEase())
            } else if (prefs.getInt("easing", 4) == 8) {
                anim.setEasing(QuintEase())
            } else if (prefs.getInt("easing", 4) == 9) {
                anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

            //Set chart defaults
            chart2.addData(dataset)
            chart2.setAxisColor(Color.BLACK)
            chart2.setLabelsColor(Color.BLACK)
            chart2.setXAxis(true)
            chart2.setYAxis(true)
            chart2.setYLabels(AxisController.LabelPosition.OUTSIDE)

            //Sets color of dataset points
            dataset.setColor(Color.BLACK)
                    .setDotsColor(Color.parseColor("#758cbb"))
                    .setDashed(floatArrayOf(10f, 10f))
                    .beginAt(0)
            //gets airplane mode stats
            getRootAccessTimes()

            //Sets dataset points
            dataset.addPoint("Jan", janRootOn)
            dataset.addPoint("Feb", febRootOn)
            dataset.addPoint("Mar", marRootOn)
            dataset.addPoint("Apr", aprRootOn)
            dataset.addPoint("May", mayRootOn)
            dataset.addPoint("Jun", junRootOn)
            dataset.addPoint("Jul", julRootOn)
            dataset.addPoint("Aug", augRootOn)
            dataset.addPoint("Sep", sepRootOn)
            dataset.addPoint("Oct", octRootOn)
            dataset.addPoint("Nov", novRootOn)
            dataset.addPoint("Dec", decRootOn)


            //Checker of
            if (janRootOn == 0f && febRootOn == 0f && marRootOn == 0f && aprRootOn == 0f && mayRootOn == 0f && junRootOn == 0f && julRootOn == 0f && augRootOn == 0f &&
                    sepRootOn == 0f && octRootOn == 0f && novRootOn == 0f && decRootOn == 0f) {
                Log.d("RadioControl", "No log data")
            } else {
                val largest = Collections.max(Arrays.asList(janRootOn, febRootOn, marRootOn, aprRootOn, mayRootOn, junRootOn, julRootOn, augRootOn, sepRootOn, octRootOn, novRootOn, decRootOn))
                val max = largest.toInt()
                chart2.setAxisBorderValues(0, 0, max)
            }


            Log.d("RadioControl", "Root Accessed $rootOn times")

            val p = Paint()
            p.color = Color.BLACK
            if (prefs.getInt("gridlines", 0) == 0) {
                chart2.setGrid(ChartView.GridType.FULL, p)
            } else if (prefs.getInt("gridlines", 0) == 1) {
                chart2.setGrid(ChartView.GridType.HORIZONTAL, p)
            } else if (prefs.getInt("gridlines", 0) == 2) {
                chart2.setGrid(ChartView.GridType.VERTICAL, p)
            } else if (prefs.getInt("gridlines", 0) == 3) {
                chart2.setGrid(ChartView.GridType.NONE, p)
            }


            uiThread {
                progress3.visibility = View.GONE

                chart2.show(anim)
            }
        }
    }

    fun getWifiLost() {
        val formatter = SimpleDateFormat("yyyy")
        val year = formatter.format(Date())
        val log = File(applicationContext.filesDir, "radiocontrol.log")

        val `is`: FileInputStream
        val reader: BufferedReader
        try {
            if (log.exists()) {
                `is` = FileInputStream(log)
                reader = BufferedReader(InputStreamReader(`is`))
                var line: String? = reader.readLine()

                while (line != null) {
                    line = reader.readLine()
                    if (countMatches(line, "lost") == 1) {
                        if (line!!.contains("$year-01")) {
                            janWifiLost++
                        } else if (line.contains("$year-02")) {
                            febWifiLost++
                        } else if (line.contains("$year-03")) {
                            marWifiLost++
                        } else if (line.contains("$year-04")) {
                            aprWifiLost++
                        } else if (line.contains("$year-05")) {
                            mayWifiLost++
                        } else if (line.contains("$year-06")) {
                            junWifiLost++
                        } else if (line.contains("$year-07")) {
                            julWifiLost++
                        } else if (line.contains("$year-08")) {
                            augWifiLost++
                        } else if (line.contains("$year-09")) {
                            sepWifiLost++
                        } else if (line.contains("$year-10")) {
                            octWifiLost++
                        } else if (line.contains("$year-11")) {
                            novWifiLost++
                        } else if (line.contains("$year-12")) {
                            decWifiLost++
                        }
                        wifiSigLost++
                    }
                    //Log.d("RadioControl", "LINE: " + line + " contains " + wifiSigLost);
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show()
            }
        } catch (e: IOException) {
            Snackbar.make(findViewById(android.R.id.content), "Error: $e", Snackbar.LENGTH_LONG)
                    .show()
        }

    }

    fun getAirplaneModeOn() {
        val formatter = SimpleDateFormat("yyyy")
        val year = formatter.format(Date())
        val log = File(applicationContext.filesDir, "radiocontrol.log")

        val `is`: FileInputStream
        val reader: BufferedReader
        try {
            if (log.exists()) {
                `is` = FileInputStream(log)
                reader = BufferedReader(InputStreamReader(`is`))
                var line: String? = reader.readLine()

                while (line != null) {
                    line = reader.readLine()
                    if (countMatches(line, "Airplane mode has been turned off") == 1 || countMatches(line, "Cell radio has been turned") == 1) {
                        if (line!!.contains("$year-01")) {
                            janAirOn++
                        } else if (line.contains("$year-02")) {
                            febAirOn++
                        } else if (line.contains("$year-03")) {
                            marAirOn++
                        } else if (line.contains("$year-04")) {
                            aprAirOn++
                        } else if (line.contains("$year-05")) {
                            mayAirOn++
                        } else if (line.contains("$year-06")) {
                            junAirOn++
                        } else if (line.contains("$year-07")) {
                            julAirOn++
                        } else if (line.contains("$year-08")) {
                            augAirOn++
                        } else if (line.contains("$year-09")) {
                            sepAirOn++
                        } else if (line.contains("$year-10")) {
                            octAirOn++
                        } else if (line.contains("$year-11")) {
                            novAirOn++
                        } else if (line.contains("$year-12")) {
                            decAirOn++
                        }
                        airplaneOn++
                    }
                    //Log.d("RadioControl", "LINE: " + line + " contains " + airplaneOn);
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show()
            }
        } catch (e: IOException) {
            Log.d("RadioControl", "Error: $e")
            Snackbar.make(findViewById(android.R.id.content), "Error: $e", Snackbar.LENGTH_LONG)
                    .show()
        }

    }
    fun getRootAccessTimes(){
        val formatter = SimpleDateFormat("yyyy")
        val year = formatter.format(Date())
        val log = File(applicationContext.filesDir, "radiocontrol.log")

        val `is`: FileInputStream
        val reader: BufferedReader
        try {
            if (log.exists()) {
                `is` = FileInputStream(log)
                reader = BufferedReader(InputStreamReader(`is`))
                var line: String? = reader.readLine()

                while (line != null) {
                    line = reader.readLine()
                    if (countMatches(line, "root") == 1) {
                        if (line!!.contains("$year-01")) {
                            janRootOn++
                        } else if (line.contains("$year-02")) {
                            febRootOn++
                        } else if (line.contains("$year-03")) {
                            marRootOn++
                        } else if (line.contains("$year-04")) {
                            aprRootOn++
                        } else if (line.contains("$year-05")) {
                            mayRootOn++
                        } else if (line.contains("$year-06")) {
                            junRootOn++
                        } else if (line.contains("$year-07")) {
                            julRootOn++
                        } else if (line.contains("$year-08")) {
                            augRootOn++
                        } else if (line.contains("$year-09")) {
                            sepRootOn++
                        } else if (line.contains("$year-10")) {
                            octRootOn++
                        } else if (line.contains("$year-11")) {
                            novRootOn++
                        } else if (line.contains("$year-12")) {
                            decRootOn++
                        }
                        rootOn++
                    }
                    //Log.d("RadioControl", "LINE: " + line + " contains " + wifiSigLost);
                }

            } else {
                Snackbar.make(findViewById(android.R.id.content), "No log file found", Snackbar.LENGTH_LONG)
                        .show()
            }
        } catch (e: IOException) {
            Snackbar.make(findViewById(android.R.id.content), "Error: $e", Snackbar.LENGTH_LONG)
                    .show()
        }

    }

    // Backwards compatible recreate().
    override fun recreate() {
        startActivity(intent)
        finish()
    }

    fun countMatches(str: String?, sub: String): Int {
        try {
            return if (str!!.contains(sub)) {
                1

            } else {
                0
            }
        } catch (e: NullPointerException) {
            Log.d("RadioControl", "Counting returned null")
        }

        return 0

    }

    override fun onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet()
        } else {
            super.onBackPressed()
        }
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
