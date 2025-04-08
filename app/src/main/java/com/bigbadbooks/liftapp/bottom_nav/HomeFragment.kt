package com.bigbadbooks.liftapp.bottom_nav

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bigbadbooks.liftapp.MainActivity
import com.bigbadbooks.liftapp.R
import com.bigbadbooks.liftapp.calendar.CalendarFragment
import com.bigbadbooks.liftapp.calendar.CurrentWeekData
import com.bigbadbooks.liftapp.calendar.WeekCalendarAdapter
import com.bigbadbooks.liftapp.databinding.FragmentHomeBinding
import com.bigbadbooks.liftapp.helper.calculator.Calculator
import com.bigbadbooks.liftapp.helper.record.StrengthRecordHelper
import com.bigbadbooks.liftapp.helper.users.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment() {

    private val strengthLevelHierarchy = mapOf(
        "Elite" to 5,
        "Advanced" to 4,
        "Intermediate" to 3,
        "Novice" to 2,
        "Untrained" to 1,
        "Unknown" to 0
    )

    private lateinit var firebaseAuth: FirebaseAuth
    private var _fragmentHomeBinding: FragmentHomeBinding? = null
    private val fragmentHomeBinding get() = _fragmentHomeBinding!!
    private lateinit var strengthRecordHelper: StrengthRecordHelper
    private lateinit var calculator: Calculator

    private lateinit var lineChart: LineChart
    private lateinit var xValues: List<String>
    private val userViewModel: UserViewModel by activityViewModels()
    private var isDataLoaded = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        return fragmentHomeBinding.root

    }

    companion object {
        private const val TAG = "Home"
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        strengthRecordHelper = StrengthRecordHelper()
        calculator = Calculator()
        fetchData()

        setupChart()

        // Fetch and update chart with real data
        strengthRecordHelper.fetchWeeklyStrengthData(
            onSuccess = { weeklyData ->
                updateChart(weeklyData)
            },
            onFailure = { e ->
                Log.e("HomeFragment", "Failed to fetch weekly strength data: ${e.message}")
            }
        )


//        val description =  Description()
//        description.text = "Strength Level"
//        description.setPosition(160f, 20f)
//        fragmentHomeBinding.chart.description = description
//        fragmentHomeBinding.chart.axisRight.setDrawLabels(false)

//        val xValues = arrayOf( "Untrained", "Novice", "Intermediate", "Advance", "Elite")
//        var xAxis = XAxis()
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
//        xAxis.setAxisMinimum(0f)
//        xAxis.setAxisMaximum(30f)
//        xAxis.axisLineWidth = 1f
//        xAxis.axisLineColor = Color.BLACK
//        xAxis.setLabelCount(30)
//        xAxis.setGranularity(1f)
//        xAxis.setAvoidFirstLastClipping(true)
//
//
//        var yAxis = YAxis()
////        yAxis.setAxisMinimum(0f)
////        yAxis.setAxisMaximum(100f)
////        yAxis.setAxisLineWidth(2f)
////        yAxis.setAxisLineColor(Color.BLACK)
////        yAxis.setLabelCount(10)
//        yAxis = fragmentHomeBinding.chart.axisLeft
//        yAxis.valueFormatter = IndexAxisValueFormatter(xValues)
//        yAxis.setLabelCount(6)
//        yAxis.setGranularity(1f)








        // Get current week data
        val currentWeek = CurrentWeekData()
        val daysInWeek = currentWeek.daysInWeek
        Log.d("WEEK_DATA", "Days count: ${daysInWeek.size}, First day: ${daysInWeek.firstOrNull()}")
        Log.d("RecyclerView", "Days in week: ${daysInWeek.size}")

        fragmentHomeBinding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 7)
            adapter = WeekCalendarAdapter(daysInWeek)
            setHasFixedSize(true)
        }
        fragmentHomeBinding.allRecords.setOnClickListener {
            val calendarFragment = CalendarFragment()
            requireActivity().supportFragmentManager.beginTransaction().replace(R.id.frame_layout, calendarFragment)
                .addToBackStack(null).commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentHomeBinding = null
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun fetchData() {
        strengthRecordHelper.fetchHomeData(
            onSuccess = { totalRepetitions, totalDuration, highestStrengthLevel, numberOfRecords, highestRepsInSingleRecord, highestOneRepMax, weightUnit ->
                if(totalRepetitions != 0) {
                    // Convert duration to minutes and seconds
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(totalDuration)
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration) % 60

                    Log.d("UNITTesting", "UNIT IS: ${userViewModel.unit.value}")

                    fragmentHomeBinding.liftCount.text = totalRepetitions.toString()
                    fragmentHomeBinding.totalMinutes.text = String.format("%d.%d", minutes, seconds)
                    fragmentHomeBinding.currentLevel.text = highestStrengthLevel
                    fragmentHomeBinding.totalRecords.text = String.format("%02d", numberOfRecords)
                    fragmentHomeBinding.bestRepetition.text = String.format("%02d", highestRepsInSingleRecord)
                    fragmentHomeBinding.top1rm.text = convertByUnit(highestOneRepMax, weightUnit)

                    val w90 = calculator.getPercentageWeight(highestOneRepMax, 0.9)
                    val w80 = calculator.getPercentageWeight(highestOneRepMax, 0.8)
                    val w60 = calculator.getPercentageWeight(highestOneRepMax, 0.6)
                    val w50 = calculator.getPercentageWeight(highestOneRepMax, 0.5)

                    //Weight base on percentage
                    fragmentHomeBinding.weight90.text = convertByUnit(w90, weightUnit)
                    fragmentHomeBinding.weight80.text = convertByUnit(w80, weightUnit)
                    fragmentHomeBinding.weight60.text = convertByUnit(w60, weightUnit)
                    fragmentHomeBinding.weight50.text = convertByUnit(w50, weightUnit)

                    fragmentHomeBinding.rep90.text = calculator.calculateRepetitions(highestOneRepMax, w90, highestRepsInSingleRecord).toString()
                    fragmentHomeBinding.rep80.text = calculator.calculateRepetitions(highestOneRepMax, w80, highestRepsInSingleRecord).toString()
                    fragmentHomeBinding.rep60.text = calculator.calculateRepetitions(highestOneRepMax, w60, highestRepsInSingleRecord).toString()
                    fragmentHomeBinding.rep50.text = calculator.calculateRepetitions(highestOneRepMax, w50, highestRepsInSingleRecord).toString()
                }

                // Once data is loaded, signal MainActivity if not already done
                if (!isDataLoaded) {
                    isDataLoaded = true
                    (activity as? MainActivity)?.onHomeDataLoaded()
                }

            },
            onFailure = { e ->
                Log.e("RecordsSummary", "Error fetching records: ${e.message}")
                fragmentHomeBinding.liftCount.text = "null"
                fragmentHomeBinding.totalMinutes.text = "null"

                // Even if there’s an error, signal to remove the splash screen
                if (!isDataLoaded) {
                    isDataLoaded = true
                    (activity as? MainActivity)?.onHomeDataLoaded()
                }
            }
        )
    }


    @SuppressLint("DefaultLocale")
    private fun convertByUnit(value: Double, recordUnit: Int): String {
        if(userViewModel.unit.value == 0) {
            if(recordUnit == 1) {
                return String.format("%.1fkg", calculator.lbToKg(value))
            } else {
                return String.format("%.1fkg", value)
            }
        } else {
            if(recordUnit == 0) {
                return String.format("%.1flb", calculator.kgToLb(value))
            } else {
                return String.format("%.1flb", value)
            }
        }
    }



    //GRAPH
    private fun setupChart() {
//        val description = Description()
//        description.text = "Weekly Strength Progress"
//        description.setPosition(270f, 20f)
        fragmentHomeBinding.chart.description.isEnabled = false
        fragmentHomeBinding.chart.description.textSize = 8f
        fragmentHomeBinding.chart.axisRight.setDrawLabels(false)
        fragmentHomeBinding.chart.axisLeft.setDrawGridLines(false)
        fragmentHomeBinding.chart.setScaleEnabled(false)
        fragmentHomeBinding.chart.setPinchZoom(false)
        fragmentHomeBinding.chart.extraTopOffset = 20f
        fragmentHomeBinding.chart.xAxis.setDrawGridLines(false)

        // Set up the X-Axis (Weeks)
        val xAxis = fragmentHomeBinding.chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("", "Week 1", "Week 2", "Week 3", "Week 4"))  // Label weeks

        // Set up the Y-Axis (Strength Levels, excluding "Unknown")
        val strengthLevels = arrayOf("Unknown","Untrained", "Novice", "Intermediate", "Advanced", "Elite")
        val yAxis = fragmentHomeBinding.chart.axisLeft
        yAxis.axisMinimum = 0f  // Start at "Untrained" (1)
        yAxis.axisMaximum = 5f  // Up to "Elite" (5)
        yAxis.granularity = 1f
        yAxis.setLabelCount(strengthLevels.size, true)
        yAxis.valueFormatter = IndexAxisValueFormatter(strengthLevels) // Label strength levels

        fragmentHomeBinding.chart.invalidate()
    }

    private fun updateChart(weeklyData: List<Pair<String, String>>) {
        if (weeklyData.isEmpty()) {
            Log.e("HomeFragment", "weeklyData is empty, no data to display.")
            return
        }

        val entries = ArrayList<Entry>()

        weeklyData.forEach { (week, strengthLevel) ->
            val weekNumber = week.replace("Week ", "").toFloatOrNull()
            val strengthValue = strengthLevelHierarchy[strengthLevel]

            Log.d("HomeFragment", "Processing: Week = $week, Strength Level = $strengthLevel")

            if (weekNumber != null && strengthValue != null && strengthValue > 0) {
                entries.add(Entry(weekNumber, strengthValue.toFloat()))
            } else {
                Log.e("HomeFragment", "Skipping invalid entry: week=$weekNumber, strength=$strengthValue")
            }
        }

        if (entries.isEmpty()) {
            Log.e("HomeFragment", "No valid entries to display on the chart.")
            return
        }

        val dataSet = LineDataSet(entries, "Weekly Strength Level")
        dataSet.color = Color.BLUE
        dataSet.valueTextSize = 12f
        dataSet.setCircleColor(Color.RED)
        dataSet.setDrawCircleHole(false)

        val lineData = LineData(dataSet)
        if (fragmentHomeBinding.chart.data != null) {
            fragmentHomeBinding.chart.data.clearValues()
        }
        fragmentHomeBinding.chart.data = lineData
        fragmentHomeBinding.chart.invalidate()
    }






}