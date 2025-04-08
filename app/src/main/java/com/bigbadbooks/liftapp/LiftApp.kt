package com.bigbadbooks.liftapp

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class LiftApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}