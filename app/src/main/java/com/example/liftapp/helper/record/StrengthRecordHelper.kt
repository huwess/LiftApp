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

}
