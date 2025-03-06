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

        val record = StrengthRecord(repetitions, dumbbellWeight, oneRepMax, strengthLevel, duration, unit, date, time)

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
                            val currentStrengthLevelValue = strengthLevelHierarchy[record.strengthLevel] ?: 1
                            if (currentStrengthLevelValue > highestStrengthLevelValue) {
                                highestStrengthLevelValue = currentStrengthLevelValue
                                highestStrengthLevel = record.strengthLevel
                            }
                        }
                    }

                    // Return collected stats
                    onSuccess(totalRepetitions, totalDuration, highestStrengthLevel, numberOfRecords, bestRep, highestOneRepMax, weightUnit)
                } else {
                    // No records found
                    onSuccess(0, 0L, "Unknown", 0, 0, 0.0, 0)
                }
            }
            .addOnFailureListener { e ->
                onFailure(Exception("Failed to fetch records: ${e.message}"))
            }
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

}
