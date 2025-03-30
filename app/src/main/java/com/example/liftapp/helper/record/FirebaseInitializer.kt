package com.example.liftapp.helper.record

import com.google.firebase.database.FirebaseDatabase

object FirebaseInitializer {
    fun initialize() {
        val database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true) // Enable offline mode before using the database
    }
}