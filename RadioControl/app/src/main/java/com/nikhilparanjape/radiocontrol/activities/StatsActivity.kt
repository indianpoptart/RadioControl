package com.nikhilparanjape.radiocontrol.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.afollestad.aesthetic.Aesthetic
import com.afollestad.aesthetic.AestheticActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.db.chart.model.LineSet
import com.db.chart.view.AxisController
import com.db.chart.view.ChartView
import com.db.chart.view.LineChartView
import com.db.chart.view.animation.Animation
import com.db.chart.view.animation.easing.*
import com.google.android.material.snackbar.Snackbar
import com.gordonwong.materialsheetfab.MaterialSheetFab
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.nikhilparanjape.radiocontrol.R
import com.nikhilparanjape.radiocontrol.utilities.Fab
import me.grantland.widget.AutofitHelper
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class StatsActivity : AestheticActivity() {
    private var wifiSigLost = 0
    //Sets float for wifiLost
    private var janWifiLost = 0f
    private var febWifiLost = 0f
    private var marWifiLost = 0f
    private var aprWifiLost = 0f
    private var mayWifiLost = 0f
    private var junWifiLost = 0f
    private var julWifiLost = 0f
    private var augWifiLost = 0f
    private var sepWifiLost = 0f
    private var octWifiLost = 0f
    private var novWifiLost = 0f
    private var decWifiLost = 0f

    private var airplaneOn = 0
    //Sets float for airplane on
    private var janAirOn = 0f
    private var febAirOn = 0f
    private var marAirOn = 0f
    private var aprAirOn = 0f
    private var mayAirOn = 0f
    private var junAirOn = 0f
    private var julAirOn = 0f
    private var augAirOn = 0f
    private var sepAirOn = 0f
    private var octAirOn = 0f
    private var novAirOn = 0f
    private var decAirOn = 0f

    private var rootOn = 0
    //Sets float for airplane on
    private var janRootOn = 0f
    private var febRootOn = 0f
    private var marRootOn = 0f
    private var aprRootOn = 0f
    private var mayRootOn = 0f
    private var junRootOn = 0f
    private var julRootOn = 0f
    private var augRootOn = 0f
    private var sepRootOn = 0f
    private var octRootOn = 0f
    private var novRootOn = 0f
    private var decRootOn = 0f

    private var bootOn = 0
    //Sets float for boot handles
    private var janBootOn = 0f
    private var febBootOn = 0f
    private var marBootOn = 0f
    private var aprBootOn = 0f
    private var mayBootOn = 0f
    private var junBootOn = 0f
    private var julBootOn = 0f
    private var augBootOn = 0f
    private var sepBootOn = 0f
    private var octBootOn = 0f
    private var novBootOn = 0f
    private var decBootOn = 0f

    private lateinit var chart: LineChartView
    private lateinit var chart1: LineChartView
    private lateinit var chart2: LineChartView
    private lateinit var chart3: LineChartView
    private lateinit var materialSheetFab: MaterialSheetFab<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        // If we haven't set any defaults, do that now
        if (Aesthetic.isFirstTime) {
            Aesthetic.config {
                isDark(true)

                // Causes an Activity recreate, calls setTheme(Int) on it.
                activityTheme(R.style.MaterialDarkThemenoab)
            }
        }
        Aesthetic.config {
            isDark(true)
            colorPrimary(res = R.color.colorPrimaryDark)

            // Causes an Activity recreate, calls setTheme(Int) on it.
            activityTheme(R.style.MaterialDarkThemenoab)
        }
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
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
            supportActionBar?.setHomeAsUpIndicator(IconicsDrawable(this, GoogleMaterial.Icon.gmd_arrow_back).apply {
                colorInt = Color.WHITE
                sizeDp = 24
                paddingDp = 1
            })
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            actionBar.title = "Statistics"

        }

        val fab = findViewById<Fab>(R.id.fab)
        val sheetView = findViewById<View>(R.id.fab_sheet)
        val overlay = findViewById<View>(R.id.overlay)
        val sheetColor = ContextCompat.getColor(applicationContext, R.color.material_drawer_background)
        val fabColor = ContextCompat.getColor(applicationContext, R.color.colorAccent)

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
        btn.setOnClickListener {
            materialSheetFab.hideSheet()
            showGridLineDialog()
        }
        val btn1 = findViewById<TextView>(R.id.fab_sheet_item_easing)
        AutofitHelper.create(btn1)
        btn1.setOnClickListener {
            materialSheetFab.hideSheet()
            showLongList()
        }

        val btn2 = findViewById<TextView>(R.id.fab_sheet_item_duration)
        AutofitHelper.create(btn2)
        btn2.setOnClickListener {
            materialSheetFab.hideSheet()
            showDurationDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        doAsync {
            try{
                wifiLostGraph()

                airplaneOnGraph()

                rootAccessGraph()

                bootHandleGraph()
            }catch (e: Exception){
                Log.d("RadioControl", "Error: $e")
            }
        }
    }

    private fun showLongList() {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()

        MaterialDialog(this)
                .title(R.string.title_easing_animation)
                .listItemsSingleChoice(R.array.easing){ _, index, _ ->
                    Log.d("RadioControl", "Easing: $index")
                    when (index) {
                        0 -> editor.putInt("easing", 0)
                        1 -> editor.putInt("easing", 1)
                        2 -> editor.putInt("easing", 2)
                        3 -> editor.putInt("easing", 3)
                        4 -> editor.putInt("easing", 4)
                        5 -> editor.putInt("easing", 5)
                        6 -> editor.putInt("easing", 6)
                        7 -> editor.putInt("easing", 7)
                        8 -> editor.putInt("easing", 8)
                        9 -> editor.putInt("easing", 9)
                    }
                    editor.apply()
                    recreate()
                }
                .positiveButton(R.string.button_text_choose)
                .show()
    }

    private fun showDurationDialog() {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()
        var index = prefs.getInt("duration", 2)
        index -= 1

        MaterialDialog(this)
                .title(R.string.title_animation_duration)
                .listItemsSingleChoice(R.array.animation_duration_values){ _, index, _ ->
                    Log.d("RadioControl", "You chose $index")
                    floatArrayOf(0f, 0f)
                    when (index) {
                        0 -> //1 second delay
                            editor.putInt("duration", 1)
                        1 -> //2 second delay
                            editor.putInt("duration", 2)
                        2 -> //3 Second delay
                            editor.putInt("duration", 3)
                        3 -> //4 second delay
                            editor.putInt("duration", 4)
                        4 -> //5 second delay
                            editor.putInt("duration", 5)
                    }
                    editor.apply()
                    recreate()
                }
                .positiveButton(R.string.button_text_choose)
                .show()
    }

    private fun showGridLineDialog() {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = prefs.edit()

        MaterialDialog(this)
                .title(R.string.title_gridlines)
                .listItemsSingleChoice(R.array.preference_values){_, index, _ ->
                    Log.d(R.string.app_name.toString(), "You chose $index")
                    floatArrayOf(0f, 0f)
                    when (index) {
                        0 -> //Full Grids
                            editor.putInt(getString(R.string.preference_gridlines), 0)
                        1 -> //Horizontal Grids
                            editor.putInt(getString(R.string.preference_gridlines), 1)
                        2 -> //Vertical Grids
                            editor.putInt(getString(R.string.preference_gridlines), 2)
                        3 -> //No Grids
                            editor.putInt(getString(R.string.preference_gridlines), 3)
                    }
                    editor.apply()
                    recreate()
                }
                .positiveButton(R.string.button_text_choose)
                .show()
    }

    private fun wifiLostGraph() {
        val progress1 = findViewById<ProgressBar>(R.id.progressWifiOff)
        val logStatus = findViewById<TextView>(R.id.title_no_log_data_wifi_off)
        var dataAvailable = false
        progress1.visibility = View.VISIBLE
        logStatus.visibility = View.GONE

        doAsync {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart = findViewById(R.id.linechart)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)

            val anim = Animation()
            anim.setDuration(dura*1000)
            when {
                prefs.getInt("easing", 4) == 0 -> anim.setEasing(BounceEase())
                prefs.getInt("easing", 4) == 1 -> anim.setEasing(CircEase())
                prefs.getInt("easing", 4) == 2 -> anim.setEasing(CubicEase())
                prefs.getInt("easing", 4) == 3 -> anim.setEasing(ElasticEase())
                prefs.getInt("easing", 4) == 4 -> anim.setEasing(ExpoEase())
                prefs.getInt("easing", 4) == 5 -> anim.setEasing(LinearEase())
                prefs.getInt("easing", 4) == 6 -> anim.setEasing(QuadEase())
                prefs.getInt("easing", 4) == 7 -> anim.setEasing(QuartEase())
                prefs.getInt("easing", 4) == 8 -> anim.setEasing(QuintEase())
                prefs.getInt("easing", 4) == 9 -> anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

                    //Set chart defaults

            //Sets dataset points

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
                logStatus.visibility = View.VISIBLE
                progress1.visibility = View.GONE
            } else {
                dataAvailable = true
                val largest = Collections.max(listOf(janWifiLost, febWifiLost, marWifiLost, aprWifiLost, mayWifiLost, junWifiLost, julWifiLost, augWifiLost, sepWifiLost, octWifiLost, novWifiLost, decWifiLost))
                val max = largest.toInt()

                chart.setAxisBorderValues(0, 0, max)
            }

            Log.d("RadioControl", "Lost Signal $wifiSigLost times")

            val p = Paint()
            p.color = Color.BLACK
            when {
                prefs.getInt("gridlines", 0) == 0 -> chart.setGrid(ChartView.GridType.FULL, p)
                prefs.getInt("gridlines", 0) == 1 -> chart.setGrid(ChartView.GridType.HORIZONTAL, p)
                prefs.getInt("gridlines", 0) == 2 -> chart.setGrid(ChartView.GridType.VERTICAL, p)
                prefs.getInt("gridlines", 0) == 3 -> chart.setGrid(ChartView.GridType.NONE, p)
            }

            uiThread {
                if(!dataAvailable){
                    logStatus.visibility = View.VISIBLE
                }
                progress1.visibility = View.GONE
                chart.show(anim)
            }
        }
    }

    //Airplane on graph
    private fun airplaneOnGraph() {
        val progress2 = findViewById<ProgressBar>(R.id.progressAirplaneOn)
        val logStatus = findViewById<TextView>(R.id.title_no_log_data_airplane_on)
        var dataAvailable = false
        progress2.visibility = View.VISIBLE
        logStatus.visibility = View.GONE
        doAsync {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart1 = findViewById(R.id.linechart_airplane_on)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)
            val anim = Animation()
            anim.setDuration(dura*1000)
            when {
                prefs.getInt("easing", 4) == 0 -> anim.setEasing(BounceEase())
                prefs.getInt("easing", 4) == 1 -> anim.setEasing(CircEase())
                prefs.getInt("easing", 4) == 2 -> anim.setEasing(CubicEase())
                prefs.getInt("easing", 4) == 3 -> anim.setEasing(ElasticEase())
                prefs.getInt("easing", 4) == 4 -> anim.setEasing(ExpoEase())
                prefs.getInt("easing", 4) == 5 -> anim.setEasing(LinearEase())
                prefs.getInt("easing", 4) == 6 -> anim.setEasing(QuadEase())
                prefs.getInt("easing", 4) == 7 -> anim.setEasing(QuartEase())
                prefs.getInt("easing", 4) == 8 -> anim.setEasing(QuintEase())
                prefs.getInt("easing", 4) == 9 -> anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

                    //Set chart defaults

            //Sets color of dataset points
            //gets airplane mode stats

            //Sets dataset points


            //Checker of

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
                logStatus.visibility = View.VISIBLE
                progress2.visibility = View.GONE
            } else {
                dataAvailable = true
                val largest = Collections.max(listOf(janAirOn, febAirOn, marAirOn, aprAirOn, mayAirOn, junAirOn, julAirOn, augAirOn, sepAirOn, octAirOn, novAirOn, decAirOn))
                val max = largest.toInt()
                chart1.setAxisBorderValues(0, 0, max)
            }


            Log.d("RadioControl", "Lost Signal $airplaneOn times")

            val p = Paint()
            p.color = Color.BLACK
            when {
                prefs.getInt(getString(R.string.preference_gridlines), 0) == 0 -> chart1.setGrid(ChartView.GridType.FULL, p)
                prefs.getInt(getString(R.string.preference_gridlines), 0) == 1 -> chart1.setGrid(ChartView.GridType.HORIZONTAL, p)
                prefs.getInt(getString(R.string.preference_gridlines), 0) == 2 -> chart1.setGrid(ChartView.GridType.VERTICAL, p)
                prefs.getInt(getString(R.string.preference_gridlines), 0) == 3 -> chart1.setGrid(ChartView.GridType.NONE, p)
            }

            uiThread {
                if(!dataAvailable){
                    logStatus.visibility = View.VISIBLE
                }
                progress2.visibility = View.GONE

                chart1.show(anim)
            }
        }
    }

    private fun rootAccessGraph(){
        val progress3 = findViewById<ProgressBar>(R.id.progressRootAccess)
        val logStatus = findViewById<TextView>(R.id.title_no_log_data_root_access)
        var dataAvailable = false
        progress3.visibility = View.VISIBLE
        logStatus.visibility = View.GONE
        doAsync {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart2 = findViewById(R.id.linechart_root_access)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)

            val anim = Animation()
            anim.setDuration(dura*1000)
            when {
                prefs.getInt("easing", 4) == 0 -> anim.setEasing(BounceEase())
                prefs.getInt("easing", 4) == 1 -> anim.setEasing(CircEase())
                prefs.getInt("easing", 4) == 2 -> anim.setEasing(CubicEase())
                prefs.getInt("easing", 4) == 3 -> anim.setEasing(ElasticEase())
                prefs.getInt("easing", 4) == 4 -> anim.setEasing(ExpoEase())
                prefs.getInt("easing", 4) == 5 -> anim.setEasing(LinearEase())
                prefs.getInt("easing", 4) == 6 -> anim.setEasing(QuadEase())
                prefs.getInt("easing", 4) == 7 -> anim.setEasing(QuartEase())
                prefs.getInt("easing", 4) == 8 -> anim.setEasing(QuintEase())
                prefs.getInt("easing", 4) == 9 -> anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

                    //Set chart defaults

            //Sets color of dataset points
            //gets airplane mode stats

            //Sets dataset points


            //Checker of

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
                logStatus.visibility = View.VISIBLE
                progress3.visibility = View.GONE
            } else {
                dataAvailable = true
                val largest = Collections.max(listOf(janRootOn, febRootOn, marRootOn, aprRootOn, mayRootOn, junRootOn, julRootOn, augRootOn, sepRootOn, octRootOn, novRootOn, decRootOn))
                val max = largest.toInt()
                chart2.setAxisBorderValues(0, 0, max)
            }


            Log.d("RadioControl", "Root Accessed $rootOn times")

            val p = Paint()
            p.color = Color.BLACK
            when {
                prefs.getInt("gridlines", 0) == 0 -> chart2.setGrid(ChartView.GridType.FULL, p)
                prefs.getInt("gridlines", 0) == 1 -> chart2.setGrid(ChartView.GridType.HORIZONTAL, p)
                prefs.getInt("gridlines", 0) == 2 -> chart2.setGrid(ChartView.GridType.VERTICAL, p)
                prefs.getInt("gridlines", 0) == 3 -> chart2.setGrid(ChartView.GridType.NONE, p)
            }


            uiThread {
                if(!dataAvailable){
                    logStatus.visibility = View.VISIBLE
                }
                progress3.visibility = View.GONE

                chart2.show(anim)
            }
        }
    }
    private fun bootHandleGraph(){
        val progress4 = findViewById<ProgressBar>(R.id.progressBootAccess)
        val logStatus = findViewById<TextView>(R.id.title_no_log_data_boot_access)
        var dataAvailable = false
        progress4.visibility = View.VISIBLE
        logStatus.visibility = View.GONE
        doAsync {
            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
            //Initiate chart
            chart3 = findViewById(R.id.linechart_boot_access)
            //Initiate dataset for chart
            val dataset = LineSet()

            val dura = prefs.getInt("duration", 2)

            val anim = Animation()
            anim.setDuration(dura*1000)
            when {
                prefs.getInt("easing", 4) == 0 -> anim.setEasing(BounceEase())
                prefs.getInt("easing", 4) == 1 -> anim.setEasing(CircEase())
                prefs.getInt("easing", 4) == 2 -> anim.setEasing(CubicEase())
                prefs.getInt("easing", 4) == 3 -> anim.setEasing(ElasticEase())
                prefs.getInt("easing", 4) == 4 -> anim.setEasing(ExpoEase())
                prefs.getInt("easing", 4) == 5 -> anim.setEasing(LinearEase())
                prefs.getInt("easing", 4) == 6 -> anim.setEasing(QuadEase())
                prefs.getInt("easing", 4) == 7 -> anim.setEasing(QuartEase())
                prefs.getInt("easing", 4) == 8 -> anim.setEasing(QuintEase())
                prefs.getInt("easing", 4) == 9 -> anim.setEasing(SineEase())
            }

            val ovLap = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            anim.setOverlap(1.0f, ovLap)
            anim.setAlpha(1)

            //Set chart defaults

            //Sets color of dataset points
            //gets airplane mode stats

            //Sets dataset points


            //Checker of

            //Set chart defaults
            chart3.addData(dataset)
            chart3.setAxisColor(Color.BLACK)
            chart3.setLabelsColor(Color.BLACK)
            chart3.setXAxis(true)
            chart3.setYAxis(true)
            chart3.setYLabels(AxisController.LabelPosition.OUTSIDE)

            //Sets color of dataset points
            dataset.setColor(Color.BLACK)
                    .setDotsColor(Color.parseColor("#758cbb"))
                    .setDashed(floatArrayOf(10f, 10f))
                    .beginAt(0)
            //gets airplane mode stats
            getRootAccessTimes()

            //Sets dataset points
            dataset.addPoint("Jan", janBootOn)
            dataset.addPoint("Feb", febBootOn)
            dataset.addPoint("Mar", marBootOn)
            dataset.addPoint("Apr", aprBootOn)
            dataset.addPoint("May", mayBootOn)
            dataset.addPoint("Jun", junBootOn)
            dataset.addPoint("Jul", julBootOn)
            dataset.addPoint("Aug", augBootOn)
            dataset.addPoint("Sep", sepBootOn)
            dataset.addPoint("Oct", octBootOn)
            dataset.addPoint("Nov", novBootOn)
            dataset.addPoint("Dec", decBootOn)


            //Checker of
            if (janBootOn == 0f && febBootOn == 0f && marBootOn == 0f && aprBootOn == 0f && mayBootOn == 0f && junBootOn == 0f && julBootOn == 0f && augBootOn == 0f &&
                    sepBootOn == 0f && octBootOn == 0f && novBootOn == 0f && decBootOn == 0f) {
                Log.d("RadioControl", "No log data")
            } else {
                dataAvailable = true
                val largest = Collections.max(listOf(janBootOn, febBootOn, marBootOn, aprBootOn, mayBootOn, junBootOn, julBootOn, augBootOn, sepBootOn, octBootOn, novBootOn, decBootOn))
                val max = largest.toInt()
                chart3.setAxisBorderValues(0, 0, max)
            }


            Log.d("RadioControl", "Boot Accessed $bootOn times")

            val p = Paint()
            p.color = Color.BLACK
            when {
                prefs.getInt("gridlines", 0) == 0 -> chart3.setGrid(ChartView.GridType.FULL, p)
                prefs.getInt("gridlines", 0) == 1 -> chart3.setGrid(ChartView.GridType.HORIZONTAL, p)
                prefs.getInt("gridlines", 0) == 2 -> chart3.setGrid(ChartView.GridType.VERTICAL, p)
                prefs.getInt("gridlines", 0) == 3 -> chart3.setGrid(ChartView.GridType.NONE, p)
            }


            uiThread {
                if(!dataAvailable){
                    logStatus.visibility = View.VISIBLE
                }
                progress4.visibility = View.GONE

                chart3.show(anim)
            }
        }
    }

    //Required for functionality
    @SuppressLint("SimpleDateFormat")
    private fun getWifiLost() {
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
                        when {
                            line!!.contains("$year-01") -> janWifiLost++
                            line.contains("$year-02") -> febWifiLost++
                            line.contains("$year-03") -> marWifiLost++
                            line.contains("$year-04") -> aprWifiLost++
                            line.contains("$year-05") -> mayWifiLost++
                            line.contains("$year-06") -> junWifiLost++
                            line.contains("$year-07") -> julWifiLost++
                            line.contains("$year-08") -> augWifiLost++
                            line.contains("$year-09") -> sepWifiLost++
                            line.contains("$year-10") -> octWifiLost++
                            line.contains("$year-11") -> novWifiLost++
                            line.contains("$year-12") -> decWifiLost++
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

    //Required for functionality
    @SuppressLint("SimpleDateFormat")
    private fun getAirplaneModeOn() {
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
                        when {
                            line!!.contains("$year-01") -> janAirOn++
                            line.contains("$year-02") -> febAirOn++
                            line.contains("$year-03") -> marAirOn++
                            line.contains("$year-04") -> aprAirOn++
                            line.contains("$year-05") -> mayAirOn++
                            line.contains("$year-06") -> junAirOn++
                            line.contains("$year-07") -> julAirOn++
                            line.contains("$year-08") -> augAirOn++
                            line.contains("$year-09") -> sepAirOn++
                            line.contains("$year-10") -> octAirOn++
                            line.contains("$year-11") -> novAirOn++
                            line.contains("$year-12") -> decAirOn++
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
    //Required for functionality
    @SuppressLint("SimpleDateFormat")
    private fun getRootAccessTimes(){
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
                        when {
                            line!!.contains("$year-01") -> janRootOn++
                            line.contains("$year-02") -> febRootOn++
                            line.contains("$year-03") -> marRootOn++
                            line.contains("$year-04") -> aprRootOn++
                            line.contains("$year-05") -> mayRootOn++
                            line.contains("$year-06") -> junRootOn++
                            line.contains("$year-07") -> julRootOn++
                            line.contains("$year-08") -> augRootOn++
                            line.contains("$year-09") -> sepRootOn++
                            line.contains("$year-10") -> octRootOn++
                            line.contains("$year-11") -> novRootOn++
                            line.contains("$year-12") -> decRootOn++
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
    @SuppressLint("SimpleDateFormat") //Required due to the way the app reads info for statistics
    private fun getBootTimes(){
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
                    if (countMatches(line, "boot") == 1) {
                        when {
                            line!!.contains("$year-01") -> janBootOn++
                            line.contains("$year-02") -> febBootOn++
                            line.contains("$year-03") -> marBootOn++
                            line.contains("$year-04") -> aprBootOn++
                            line.contains("$year-05") -> mayBootOn++
                            line.contains("$year-06") -> junBootOn++
                            line.contains("$year-07") -> julBootOn++
                            line.contains("$year-08") -> augBootOn++
                            line.contains("$year-09") -> sepBootOn++
                            line.contains("$year-10") -> octBootOn++
                            line.contains("$year-11") -> novBootOn++
                            line.contains("$year-12") -> decBootOn++
                        }
                        bootOn++
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

    private fun countMatches(str: String?, sub: String): Int {
        try {
            return if (str!!.contains(sub)) { 1 }
            else { 0 }
        } catch (e: NullPointerException) {
            Log.d("RadioControl", getString(R.string.error_count_return_null))
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
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }


}
