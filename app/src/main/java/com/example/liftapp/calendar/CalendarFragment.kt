package com.example.liftapp.calendar

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.liftapp.databinding.FragmentCalendarBinding
import com.example.liftapp.helper.record.StrengthRecord
import java.util.Calendar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.liftapp.R
import com.example.liftapp.helper.record.StrengthRecordHelper

class CalendarFragment : Fragment() {

    private var _fragmentCalendarBinding: FragmentCalendarBinding? = null
    private val binding get() = _fragmentCalendarBinding!!

    private lateinit var strengthRecordHelper: StrengthRecordHelper
    private lateinit var recordsAdapter: RecordsAdapter
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val TAG = "CalendarFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCalendarBinding = FragmentCalendarBinding.inflate(inflater, container, false)
        strengthRecordHelper = StrengthRecordHelper()
        Log.d(TAG, "onCreateView: View created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Setting up views")

        // Set up the CalendarView to show the current date
        binding.calendarView.date = System.currentTimeMillis()  // Set to current date by default
        Log.d(TAG, "onViewCreated: CalendarView date set to current time")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recordsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)  // Use LinearLayoutManager for vertical list
        recordsAdapter = RecordsAdapter(emptyList())  // Initially empty list
        recyclerView.adapter = recordsAdapter

        val currentMonth = getCurrentMonth()
        val currentYear = getCurrentYear()
        val currentDay = getCurrentDay()

        Log.d(TAG, "onViewCreated: RecyclerView initialized")

        // Fetch records for the current day (before any date is selected)
        fetchRecordsForSelectedDay(currentYear, currentMonth, currentDay)

        // Listen for date selection (this is the correct place to listen for clicks on specific days)
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // Log the date being clicked
            Log.d(TAG, "onViewCreated: CalendarView date clicked - Year: $year, Month: ${month + 1}, Day: $dayOfMonth")

            // When a date is selected, we fetch the records for that day
            fetchRecordsForSelectedDay(year, month, dayOfMonth)
        }

        // Initially load records for the current month
        loadRecordsForCurrentMonth()
    }

    private fun loadRecordsForCurrentMonth() {
        // Get the current month records for the logged-in user
        val currentMonth = getCurrentMonth()
        val currentYear = getCurrentYear()
        val currentDay = getCurrentDay()

        Log.d(TAG, "loadRecordsForCurrentMonth: Fetching records for Year: $currentYear, Month: $currentMonth")

        strengthRecordHelper.getRecordsForMonth(currentYear, currentMonth, currentDay) { records ->
            Log.d(TAG, "loadRecordsForCurrentMonth: Records fetched - Size: ${records.size}")
            displayRecords(records)
        }
    }

    private fun fetchRecordsForSelectedDay(year: Int, month: Int, dayOfMonth: Int) {
        // Fetch records for the specific day selected
        Log.d(TAG, "fetchRecordsForSelectedDay: Fetching records for Year: $year, Month: $month, Day: $dayOfMonth")

        strengthRecordHelper.getRecordsForDay(year, month, dayOfMonth) { records ->
            Log.d(TAG, "fetchRecordsForSelectedDay: Records fetched for selected day - Size: ${records.size}")
            displayRecords(records)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun displayRecords(records: List<StrengthRecord>) {
        Log.d(TAG, "displayRecords: Updating RecyclerView with records - Size: ${records.size}")
        // Update the RecyclerView's adapter with the new list of records
        recordsAdapter = RecordsAdapter(records)
        recyclerView.adapter = recordsAdapter  // Set the updated adapter

        // Optionally, call notifyDataSetChanged() if you need to explicitly refresh the adapter
        recordsAdapter.notifyDataSetChanged()
    }

    private fun getCurrentMonth(): Int {
        // Logic to get current month (0-based, January is 0)
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH)
    }

    private fun getCurrentDay(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun getCurrentYear(): Int {
        // Logic to get current year
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentCalendarBinding = null
    }
}
