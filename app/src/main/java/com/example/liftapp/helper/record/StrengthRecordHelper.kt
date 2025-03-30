package com.example.liftapp.helper.record

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class StrengthRecordHelper {
    // Get the instance with your Firebase URL
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://lift-app-id-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val strengthLevelHierarchy = mapOf(
        "Elite" to 5,
        "Advanced" to 4,
        "Intermediate" to 3,
        "Novice" to 2,
        "Untrained" to 1,
        "Unknown" to 0
    )

    /**
     * Saves a strength record for the current user.
     */
    fun saveStrengthRecord(
        repetitions: Int,
        dumbbellWeight: Double,
        oneRepMax: Double,
        strengthLevel: String,
        duration: Long,
        unit: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }
        // Get current date and time in desired formats
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = dateFormat.format(Date())
        val time = timeFormat.format(Date())

        // Generate a unique key for this record
        val recordsRef = database.getReference("users/$userId/records")
        recordsRef.keepSynced(true) // Request that this node is always kept in sync (and cached)
        val newRecordRef = recordsRef.push()

        val record = StrengthRecord(
            repetitions,
            dumbbellWeight,
            oneRepMax,
            strengthLevel,
            duration,
            unit,
            date,
            time
        )

        newRecordRef.setValue(record)
            .addOnSuccessListener {
                Log.d("StrengthRecordHelper", "Record saved successfully")
                onSuccess()
            }
            .addOnFailureListener {
                Log.e("StrengthRecordHelper", "Failed to save record: ${it.message}")
                onFailure(it)
            }
    }

    /**
     * Fetches home data by aggregating records.
     * Returns total repetitions, duration, best strength level, record count,
     * best rep count, highest 1RM, and the weight unit.
     */
    fun fetchHomeData(
        onSuccess: (
            totalRepetitions: Int,
            totalDuration: Long,
            highestStrengthLevel: String,
            numberOfRecords: Int,
            bestRep: Int,
            highestOneRepMax: Double,
            weightUnit: Int
        ) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        val recordsRef = database.getReference("users/$userId/records")
        recordsRef.keepSynced(true)

        recordsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalRepetitions = 0
                var totalDuration = 0L
                var highestStrengthLevelValue = 0
                var highestStrengthLevel = "Untrained"
                var numberOfRecords = 0
                var bestRep = 0
                var highestOneRepMax = 0.0
                var weightUnit = 0

                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    snapshot.children.forEach { recordSnapshot ->
                        val record = recordSnapshot.getValue(StrengthRecord::class.java)
                        if (record != null) {
                            totalRepetitions += record.repetitions
                            totalDuration += record.duration
                            numberOfRecords++
                            if (record.oneRepMax > highestOneRepMax) {
                                highestOneRepMax = record.oneRepMax
                                weightUnit = record.unit
                                bestRep = record.repetitions
                            }
                            val currentStrengthLevelValue = strengthLevelHierarchy[record.strengthLevel] ?: 1
                            if (currentStrengthLevelValue > highestStrengthLevelValue) {
                                highestStrengthLevelValue = currentStrengthLevelValue
                                highestStrengthLevel = record.strengthLevel
                            }
                        }
                    }
                    Log.d("StrengthRecordHelper", "Aggregated home data: totalReps=$totalRepetitions")
                    onSuccess(
                        totalRepetitions,
                        totalDuration,
                        highestStrengthLevel,
                        numberOfRecords,
                        bestRep,
                        highestOneRepMax,
                        weightUnit
                    )
                } else {
                    Log.d("StrengthRecordHelper", "No records found; returning defaults.")
                    onSuccess(0, 0L, "Unknown", 0, 0, 0.0, 0)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("StrengthRecordHelper", "Error fetching home data: ${error.message}")
                // Return default values to avoid blocking UI (e.g., splash screen)
                onSuccess(0, 0L, "Unknown", 0, 0, 0.0, 0)
            }
        })
    }

    /**
     * Fetches weekly strength data.
     * Returns a sorted list of pairs, where each pair is (week, highest strength level).
     */
    fun fetchWeeklyStrengthData(
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        val recordsRef = database.getReference("users/$userId/records")
        // Using .get() here; note that this returns a Task snapshot (which should work offline if cached)
        recordsRef.get().addOnSuccessListener { snapshot ->
            Log.d("StrengthRecordHelper", "Snapshot exists: ${snapshot.exists()}, children: ${snapshot.childrenCount}")
            val weeklyData = mutableMapOf<String, String>()
            val currentMonth = getCurrentMonth()

            snapshot.children.forEach { recordSnapshot ->
                val dateKey = recordSnapshot.child("date").getValue(String::class.java) ?: return@forEach
                if (!dateKey.startsWith(currentMonth)) return@forEach

                val strengthLevelStr = recordSnapshot.child("strengthLevel").getValue(String::class.java) ?: "Unknown"
                val strengthLevelValue = strengthLevelHierarchy[strengthLevelStr] ?: 0
                val weekNumber = getWeekNumber(dateKey)

                // Store the highest strength level per week
                val currentMax = strengthLevelHierarchy[weeklyData[weekNumber]] ?: 0
                if (strengthLevelValue > currentMax) {
                    weeklyData[weekNumber] = strengthLevelStr
                }
            }

            val sortedWeeklyData = weeklyData.toList().sortedBy { it.first }
            Log.d("StrengthRecordHelper", "Final weekly data: $sortedWeeklyData")
            onSuccess(sortedWeeklyData)
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }

    /**
     * Returns the current month as a string in "YYYY-MM" format.
     */
    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based
        return String.format("%04d-%02d", year, month)
    }

    /**
     * Calculates the week number for a given date string ("yyyy-MM-dd").
     * Returns a string in the format "Week X".
     */
    fun getWeekNumber(dateStr: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateStr) ?: return "Unknown"
        val calendar = Calendar.getInstance().apply { time = date }
        // Find the first Sunday of the month
        val firstDayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        val firstSunday = firstDayCalendar.timeInMillis
        val daysSinceFirstSunday = ((date.time - firstSunday) / (1000 * 60 * 60 * 24)).toInt()
        val weekNumber = (daysSinceFirstSunday / 7) + 1
        return "Week $weekNumber"
    }

    /**
     * Fetches records for a specific date (formatted as "yyyy-MM-dd").
     */
    fun fetchRecordsByDate(
        selectedDate: String,
        onSuccess: (List<StrengthRecord>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }
        val recordsRef = database.getReference("users/$userId/records")
        val query = recordsRef.orderByChild("date").equalTo(selectedDate)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val recordsList = mutableListOf<StrengthRecord>()
                if (snapshot.exists()) {
                    snapshot.children.forEach { recordSnapshot ->
                        recordSnapshot.getValue(StrengthRecord::class.java)?.let { recordsList.add(it) }
                    }
                }
                onSuccess(recordsList)
            }
            override fun onCancelled(error: DatabaseError) {
                onFailure(Exception("Failed to fetch records: ${error.message}"))
            }
        })
    }

    /**
     * Fetches records for a specific day (in UTC) given the year, month, and day.
     */
    fun getRecordsForDay(year: Int, month: Int, day: Int, onSuccess: (List<StrengthRecord>) -> Unit) {
        val dayString = String.format("%04d-%02d-%02d", year, month + 1, day)
        Log.d("FirebaseDebug", "Converted day string: $dayString")
        val recordsList = mutableListOf<StrengthRecord>()
        Log.d("FirebaseDebug", "Fetching records for day $dayString for user ID: ${auth.currentUser?.uid}")
        fetchRecordsByDate(dayString, { records ->
            records.forEach { record ->
                Log.d("FirebaseDebug", "Record date: ${record.date}")
                if (record.date == dayString) {
                    Log.d("FirebaseDebug", "Record date matches: ${record.date}")
                    recordsList.add(record)
                }
            }
            onSuccess(recordsList)
        }, { error ->
            Log.e("FirebaseDebug", "Error fetching records for day: ${error.message}")
        })
    }

    fun getRecordsForMonth(year: Int, month: Int, day: Int, onSuccess: (List<StrengthRecord>) -> Unit) {
        // Convert the year, month, and day to a string in the format "yyyy-MM-dd"
//        val dayString = String.format("%04d-%02d-%02d", year, month + 1, day) // e.g., "2025-03-06"
//
//        Log.d("FirebaseDebug", "Converted day string: $dayString")
//
//        val recordsList = mutableListOf<StrengthRecord>()
//        Log.d("FirebaseDebug", "Fetching records for day $dayString for user ID: ${auth.currentUser?.uid}")
//
//        // Fetch records for the specific day using fetchRecordsByDate
//        fetchRecordsByDate(dayString, { records ->
//            records.forEach { record ->
//                // Log the record's date
//                Log.d("FirebaseDebug", "Record date: ${record.date}")
//
//                // Extract the month part of the record's date (e.g., "2025-03-07" -> "03")
//                val recordMonth = record.date.substring(5, 7)  // Extract MM from yyyy-MM-dd
//
//                Log.d("FirebaseDebug", "Extracted record month: $recordMonth")
//
//                // Compare the month part of the record's date (MM) with the requested month (MM)
//                if (recordMonth == String.format("%02d", month + 1)) {
//                    Log.d("FirebaseDebug", "Record month matches the requested month: ${record.date}")
//                    recordsList.add(record)
//                }
//            }
//
//            if (recordsList.isEmpty()) {
//                Log.d("FirebaseDebug", "No records found for this month")
//            }
//
//            onSuccess(recordsList)
//        }, { error ->
//            Log.e("FirebaseDebug", "Error fetching records for month: ${error.message}")
//        })
    }
}
