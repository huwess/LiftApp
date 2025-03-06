package com.example.liftapp.helper.record

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StrengthRecordHelper {
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

        // Get current date and time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = dateFormat.format(Date())
        val time = timeFormat.format(Date())

        // Generate a unique record ID
        val recordsRef = database.getReference("users/$userId/records")
        val newRecordRef = recordsRef.push() // Generates a unique key

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
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun fetchHomeData(
        onSuccess: (
            totalRepetitions: Int,
            totalDuration: Long,
            highestStrengthLevel: String,
            numberOfRecords: Int,
            bestRep: Int, // Reps performed with the highest 1-rep max
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

        recordsRef.get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    var totalRepetitions = 0
                    var totalDuration = 0L
                    var highestStrengthLevelValue = 0
                    var highestStrengthLevel = "Untrained"
                    var numberOfRecords = 0
                    var bestRep = 0
                    var highestOneRepMax = 0.0
                    var weightUnit = 0

                    // Iterate through all records
                    for (recordSnapshot in snapshot.children) {
                        val record = recordSnapshot.getValue(StrengthRecord::class.java)
                        if (record != null) {
                            totalRepetitions += record.repetitions
                            totalDuration += record.duration
                            numberOfRecords++

                            // Track highest 1-rep max and bestRep
                            if (record.oneRepMax > highestOneRepMax) {
                                highestOneRepMax = record.oneRepMax
                                weightUnit = record.unit
                                bestRep = record.repetitions
                            }

                            // Determine highest strength level
                            val currentStrengthLevelValue =
                                strengthLevelHierarchy[record.strengthLevel] ?: 1
                            if (currentStrengthLevelValue > highestStrengthLevelValue) {
                                highestStrengthLevelValue = currentStrengthLevelValue
                                highestStrengthLevel = record.strengthLevel
                            }
                        }
                    }

                    // Return collected stats
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
                    // No records found
                    onSuccess(0, 0L, "Unknown", 0, 0, 0.0, 0)
                }
            }
            .addOnFailureListener { e ->
                onFailure(Exception("Failed to fetch records: ${e.message}"))
            }
    }

    fun fetchWeeklyStrengthData(
        onSuccess: (List<Pair<String, String>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        val recordsRef = database.getReference("users/$userId/records")

        recordsRef.get().addOnSuccessListener { snapshot ->
            Log.d("StrengthRecordHelper", "Snapshot exists: ${snapshot.exists()}, Children count: ${snapshot.childrenCount}")

            val weeklyData = mutableMapOf<String, String>()
            val currentMonth = getCurrentMonth() // Get the current month in "YYYY-MM"

            snapshot.children.forEach { recordSnapshot ->
                val dateKey = recordSnapshot.child("date").getValue(String::class.java) ?: return@forEach
                Log.d("StrengthRecordHelper", "Record Date: $dateKey")

                // Ensure the record belongs to the current month
                if (!dateKey.startsWith(currentMonth)) return@forEach

                val strengthLevelStr = recordSnapshot.child("strengthLevel").getValue(String::class.java) ?: "Unknown"
                Log.d("StrengthRecordHelper", "Strength Level: $strengthLevelStr")

                val strengthLevelValue = strengthLevelHierarchy[strengthLevelStr] ?: 0
                val weekNumber = getWeekNumber(dateKey)

                // Compare and store the highest strength level per week
                val currentMax = strengthLevelHierarchy[weeklyData[weekNumber]] ?: 0
                if (strengthLevelValue > currentMax) {
                    weeklyData[weekNumber] = strengthLevelStr
                }
            }

            // Convert map to sorted list
            val sortedWeeklyData = weeklyData.toList().sortedBy { it.first }
            Log.d("StrengthRecordHelper", "Final weekly data: $sortedWeeklyData")
            onSuccess(sortedWeeklyData)

        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }


    fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Month is 0-based in Calendar
        return String.format("%04d-%02d", year, month)
    }



    // Helper function to get week number from a date string
    fun getWeekNumber(dateStr: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(dateStr) ?: return "Unknown"

        val calendar = Calendar.getInstance().apply {
            time = date
        }

        // Get the first Sunday of the month
        val firstDayCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, 1)

            // Move to the first Sunday of the month
            while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val firstSunday = firstDayCalendar.timeInMillis
        val daysSinceFirstSunday = ((date.time - firstSunday) / (1000 * 60 * 60 * 24)).toInt()
        val weekNumber = (daysSinceFirstSunday / 7) + 1

        return "Week $weekNumber"
    }





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
                    for (recordSnapshot in snapshot.children) {
                        val record = recordSnapshot.getValue(StrengthRecord::class.java)
                        record?.let { recordsList.add(it) }
                    }
                }

                onSuccess(recordsList)
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(Exception("Failed to fetch records: ${error.message}"))
            }
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

    // Fetch records for a specific day (in UTC)
    fun getRecordsForDay(year: Int, month: Int, day: Int, onSuccess: (List<StrengthRecord>) -> Unit) {
        // Convert the year, month, and day to a string in the format "yyyy-MM-dd"
        val dayString = String.format("%04d-%02d-%02d", year, month + 1, day) // e.g., "2025-03-06"

        // Log the converted day string
        Log.d("FirebaseDebug", "Converted day string: $dayString")

        val recordsList = mutableListOf<StrengthRecord>()
        Log.d("FirebaseDebug", "Fetching records for day $dayString for user ID: ${auth.currentUser?.uid}")

        // Fetch records for the specific day using fetchRecordsByDate
        fetchRecordsByDate(dayString, { records ->
            records.forEach { record ->
                // Log the record's date and check if it matches the dayString (e.g., "2025-03-06")
                Log.d("FirebaseDebug", "Record date: ${record.date}")
                if (record.date == dayString) {
                    Log.d("FirebaseDebug", "Record date matches the day string: ${record.date}")
                    recordsList.add(record)
                }
            }
            onSuccess(recordsList)
        }, { error ->
            Log.e("FirebaseDebug", "Error fetching records for day: ${error.message}")
        })
    }


}
