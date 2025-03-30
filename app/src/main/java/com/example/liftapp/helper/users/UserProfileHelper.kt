package com.example.liftapp.helper.users
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserProfileHelper {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(
        "https://lift-app-id-default-rtdb.asia-southeast1.firebasedatabase.app/"
    )
    private val usersRef: DatabaseReference = database.getReference("users")


    // Save user data to database
    fun saveUserData(
        name: String,
        age: Int,
        weight: Double,
        gender: String,
        unit: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("UserProfileHelper", "Saving data for user: $userId")


            val user = User(name, age, weight, gender, unit)
            usersRef.child(userId).updateChildren(user.toMap())
                .addOnSuccessListener {
                    Log.d("UserProfileHelper", "Data successfully saved to Firebase")
                    callback(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e("UserProfileHelper", "Failed to save data: ${e.message}")
                    callback(false, e.message)
                }
        } else {
            Log.e("UserProfileHelper", "User not logged in")
            callback(false, "User not logged in")
        }
    }

    fun fetchUserData(callback: (User?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("UserProfileHelper", "User not authenticated")
            callback(null)
            return
        }

        val userId = currentUser.uid
        Log.d("UserProfileHelper", "Fetching data for userId: $userId")
        usersRef.child(userId).keepSynced(true) // Cache user data offline
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        Log.d("UserProfileHelper", "User data retrieved: $user")
                        callback(user)
                    } else {
                        Log.e("UserProfileHelper", "User data is null after deserialization")
                        callback(null)
                    }
                } else {
                    Log.e("UserProfileHelper", "No data found for userId: $userId")
                    callback(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserProfileHelper", "Database error: ${error.message}")
                callback(null)
            }
        })
    }


    fun updateUserData(
        userId: String,
        updates: Map<String, Any>,
        callback: (Boolean, String?) -> Unit
    ) {
        usersRef.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun updateUserWeightAndUnit(
        userId: String,
        weight: Double,
        unit: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        val updates = mapOf(
            "weight" to weight,
            "unit" to unit
        )

        usersRef.child(userId).updateChildren(updates)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    // Check if user exists in the database
    fun checkUserExists(userId: String, callback: (Boolean) -> Unit) {
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User exists in the database
                    callback(true)
                } else {
                    // User does not exist
                    callback(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserProfileHelper", "Database error: ${error.message}")
                callback(false)
            }
        })
    }


}
