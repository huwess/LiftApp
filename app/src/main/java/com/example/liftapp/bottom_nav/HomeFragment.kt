package com.example.liftapp.bottom_nav

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.liftapp.R
import com.example.liftapp.calendar.CalendarFragment
import com.example.liftapp.calendar.CurrentWeekData
import com.example.liftapp.calendar.WeekCalendarAdapter
import com.example.liftapp.databinding.FragmentHomeBinding
import com.example.liftapp.helper.calculator.Calculator
import com.example.liftapp.helper.record.StrengthRecordHelper
import com.google.firebase.auth.FirebaseAuth
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var _fragmentHomeBinding: FragmentHomeBinding? = null
    private val fragmentHomeBinding get() = _fragmentHomeBinding!!
    private lateinit var strengthRecordHelper: StrengthRecordHelper
    private lateinit var calculator: Calculator

    private lateinit var lineChart: LineChart
    private lateinit var xValues: List<String>


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        strengthRecordHelper = StrengthRecordHelper()
        calculator = Calculator()
        fetchData()
        val description =  Description()
        description.text = "Strength Level"
        description.setPosition(160f, 20f)
        fragmentHomeBinding.chart.description = description
        fragmentHomeBinding.chart.axisRight.setDrawLabels(false)

        val xValues = arrayOf( "Untrained", "Novice", "Intermediate", "Advance", "Elite")
        var xAxis = XAxis()
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM)
        xAxis.setAxisMinimum(0f)
        xAxis.setAxisMaximum(30f)
        xAxis.axisLineWidth = 1f
        xAxis.axisLineColor = Color.BLACK
        xAxis.setLabelCount(30)
        xAxis.setGranularity(1f)
        xAxis.setAvoidFirstLastClipping(true)


        var yAxis = YAxis()
//        yAxis.setAxisMinimum(0f)
//        yAxis.setAxisMaximum(100f)
//        yAxis.setAxisLineWidth(2f)
//        yAxis.setAxisLineColor(Color.BLACK)
//        yAxis.setLabelCount(10)
        yAxis = fragmentHomeBinding.chart.axisLeft
        yAxis.valueFormatter = IndexAxisValueFormatter(xValues)
        yAxis.setLabelCount(6)
        yAxis.setGranularity(1f)

        val entries1 = ArrayList<Entry>()
        entries1.add(Entry(0F, 1f))
        entries1.add(Entry(1F, 2f))
        entries1.add(Entry(2F, 3f))
        entries1.add(Entry(3F, 4f))
        entries1.add(Entry(4F, 5f))
        entries1.add(Entry(5F, 5f))
        entries1.add(Entry(6F, 5f))
        entries1.add(Entry(7F, 5f))
        entries1.add(Entry(8F, 5f))
        entries1.add(Entry(9F, 5f))
        entries1.add(Entry(10F, 5f))
        entries1.add(Entry(11F, 5f))
        entries1.add(Entry(12F, 5f))
        entries1.add(Entry(13F, 5f))
        entries1.add(Entry(14F, 5f))
        entries1.add(Entry(15F, 5f))
        entries1.add(Entry(16F, 5f))
        entries1.add(Entry(17F, 5f))
        entries1.add(Entry(18F, 5f))
        entries1.add(Entry(18F, 5f))
        entries1.add(Entry(19F, 5f))
        entries1.add(Entry(20F, 5f))
        entries1.add(Entry(21F, 5f))
        entries1.add(Entry(22F, 5f))
        entries1.add(Entry(23F, 5f))
        entries1.add(Entry(24F, 5f))
        entries1.add(Entry(25F, 5f))
        entries1.add(Entry(26F, 5f))
        entries1.add(Entry(27F, 5f))
        entries1.add(Entry(28F, 5f))
        entries1.add(Entry(29F, 5f))
        entries1.add(Entry(30F, 5f))





        val dataSet1 = LineDataSet(entries1, "Progress")
        dataSet1.setColor(Color.BLUE);

        val lineData = LineData(dataSet1)
        fragmentHomeBinding.chart.setData(lineData)
        fragmentHomeBinding.chart.invalidate()

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
        strengthRecordHelper.fetchAllRecords(
            onSuccess = { totalRepetitions, totalDuration, highestStrengthLevel, numberOfRecords, highestRepsInSingleRecord, highestOneRepMax ->
                // Convert duration to minutes and seconds
                val minutes = TimeUnit.MILLISECONDS.toMinutes(totalDuration)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration) % 60

                fragmentHomeBinding.liftCount.text = totalRepetitions.toString()
                fragmentHomeBinding.totalMinutes.text = String.format("%d.%d", minutes, seconds)
                fragmentHomeBinding.currentLevel.text = highestStrengthLevel
                fragmentHomeBinding.totalRecords.text = String.format("%02d", numberOfRecords)
                fragmentHomeBinding.bestRepetition.text = String.format("%02d", highestRepsInSingleRecord)
                fragmentHomeBinding.top1rm.text = String.format("%.2fkg", highestOneRepMax)

                val w90 = calculator.getPercentageWeight(highestOneRepMax, 0.9)
                val w80 = calculator.getPercentageWeight(highestOneRepMax, 0.8)
                val w60 = calculator.getPercentageWeight(highestOneRepMax, 0.6)
                val w50 = calculator.getPercentageWeight(highestOneRepMax, 0.5)

                //Weight base on percentage
                fragmentHomeBinding.weight90.text = String.format("%.1fkg", w90)
                fragmentHomeBinding.weight80.text = String.format("%.1fkg", w80)
                fragmentHomeBinding.weight60.text = String.format("%.1fkg", w60)
                fragmentHomeBinding.weight50.text = String.format("%.1fkg", w50)

                fragmentHomeBinding.rep90.text = calculator.calculateRepetitions(highestOneRepMax, w90, highestRepsInSingleRecord).toString()
                fragmentHomeBinding.rep80.text = calculator.calculateRepetitions(highestOneRepMax, w80, highestRepsInSingleRecord).toString()
                fragmentHomeBinding.rep60.text = calculator.calculateRepetitions(highestOneRepMax, w60, highestRepsInSingleRecord).toString()
                fragmentHomeBinding.rep50.text = calculator.calculateRepetitions(highestOneRepMax, w50, highestRepsInSingleRecord).toString()



                // Display the results
                Log.d("RecordsSummary", "Total Repetitions: $totalRepetitions")
                Log.d("RecordsSummary", "Total Duration: $minutes minutes $seconds seconds")
            },
            onFailure = { e ->
                Log.e("RecordsSummary", "Error fetching records: ${e.message}")
                fragmentHomeBinding.liftCount.text = "null"
                fragmentHomeBinding.totalMinutes.text = "null"
            }
        )
    }




}