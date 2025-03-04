package com.example.liftapp.helper.record

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

class StrengthRecordHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://lift-app-id-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val recordsRef: DatabaseReference = database.getReference("records")
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
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onFailure(Exception("User not authenticated"))
            return
        }
        // Log user ID to check if it matches Firebase auth
        Log.d("FirebaseDebug", "User ID: $userId")

        // Try fetching data to test read permission
        recordsRef.child(userId).get().addOnSuccessListener {
            Log.d("FirebaseDebug", "Read permission granted")
        }.addOnFailureListener {
            Log.e("FirebaseDebug", "Read permission denied: ${it.message}")
        }

        // Check if the user has permission
        recordsRef.child(userId).get().addOnSuccessListener {
            // Generate a unique record ID
            val recordId = recordsRef.child(userId).push().key
            if (recordId == null) {
                onFailure(Exception("Failed to generate record ID"))
                return@addOnSuccessListener
            }

            val record = StrengthRecord(repetitions, dumbbellWeight, oneRepMax, strengthLevel, duration, ServerValue.TIMESTAMP)

            recordsRef.child(userId).child(recordId).setValue(record)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailure(it) }
        }.addOnFailureListener {
            onFailure(Exception("Permission issue: ${it.message}"))
        }
    }

    fun fetchAllRecords(
        onSuccess: (
            totalRepetitions: Int,
            totalDuration: Long,
            highestStrengthLevel: String,
            numberOfRecords: Int,
            highestRepsInSingleRecord: Int,
            highestOneRepMax: Double // Add this parameter
        ) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: run {
            onFailure(Exception("User not authenticated"))
            return
        }

        // Fetch all records for the current user
        recordsRef.child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    var totalRepetitions = 0
                    var totalDuration = 0L
                    var highestStrengthLevelValue = 0
                    var highestStrengthLevel = "Untrained" // Default value
                    var numberOfRecords = 0
                    var bestReps = 0
                    var highestOneRepMax = 0.0 // Track highest 1-rep max

                    // Iterate through all records
                    for (recordSnapshot in snapshot.children) {
                        val repetitions = recordSnapshot.child("repetitions").getValue(Int::class.java) ?: 0
                        val duration = recordSnapshot.child("duration").getValue(Long::class.java) ?: 0L
                        val strengthLevel = recordSnapshot.child("strengthLevel").getValue(String::class.java) ?: "Untrained"
                        val oneRepMax = recordSnapshot.child("oneRepMax").getValue(Double::class.java) ?: 0.0

                        // Update totals
                        totalRepetitions += repetitions
                        totalDuration += duration
                        numberOfRecords++

                        // Track highest reps in a single record
//                        if (repetitions > bestReps) {
//                            bestReps = repetitions
//                        }

                        // Track highest 1-rep max
                        if (oneRepMax > highestOneRepMax) {
                            highestOneRepMax = oneRepMax
                            bestReps = repetitions
                        }

                        // Check for highest strength level
                        val currentStrengthLevelValue = strengthLevelHierarchy[strengthLevel] ?: 1
                        if (currentStrengthLevelValue > highestStrengthLevelValue) {
                            highestStrengthLevelValue = currentStrengthLevelValue
                            highestStrengthLevel = strengthLevel
                        }
                    }

                    // Return the results via onSuccess callback
                    onSuccess(totalRepetitions, totalDuration, highestStrengthLevel, numberOfRecords, bestReps, highestOneRepMax)
                } else {
                    // No records found for the user
                    onSuccess(0, 0L, "Untrained", 0, 0, 0.0)
                }
            }
            .addOnFailureListener { e ->
                onFailure(Exception("Failed to fetch records: ${e.message}"))
            }
    }

}
