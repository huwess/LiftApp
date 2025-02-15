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
    fun saveUserData(name: String, age: Int, weight: Int, gender: String, unit: Int,   callback: (Boolean, String?) -> Unit) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("UserProfileHelper", "Saving data for user: $userId")


            val user = User(name, age, weight, gender, unit)
            usersRef.child(userId).setValue(user)
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


//    // Get user data from database
//    fun getUserData(callback: (Boolean, Map<String, Any>?, String?) -> Unit) {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            usersRef.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        val userData = snapshot.value as? Map<String, Any>
//                        name = userData?.get("name") as? String ?: ""
//                        age = userData?.get("age") as? Int ?: 0
//                        unit = userData?.get("unit") as? Int ?: 0
//                        gender = userData?.get("gender") as? String ?: ""
//                        weight = userData?.get("weight") as? Int ?: 0
//                        callback(true, userData, null)
//                    } else {
//                        callback(false, null, "User data not found")
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    callback(false, null, error.message)
//                }
//            })
//        } else {
//            callback(false, null, "User not logged in")
//        }
//    }
//
//    // Update user data in database
//    fun updateUserData(name: String? = null, age: Int? = null, unit: Int? = null, gender: String? = null, weight: Int? = null, callback: (Boolean, String?) -> Unit) {
//        val currentUser = auth.currentUser
//        if (currentUser != null) {
//            val updates = mutableMapOf<String, Any>()
//            name?.let { updates["name"] = it }
//            age?.let { updates["age"] = it }
//            unit?.let { updates["unit"] = it }
//            gender?.let { updates["gender"] = it }
//            weight?.let { updates["weight"] = it }
//
//            if (updates.isNotEmpty()) {
//                usersRef.child(currentUser.uid).updateChildren(updates)
//                    .addOnSuccessListener { callback(true, null) }
//                    .addOnFailureListener { e -> callback(false, e.message) }
//            } else {
//                callback(false, "No data to update")
//            }
//        } else {
//            callback(false, "User not logged in")
//        }
//    }
}

