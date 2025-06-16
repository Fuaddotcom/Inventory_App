// com/TI23B1/inventoryapp/MyApplication.kt
package com.TI23B1.inventoryapp

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // This is the ONLY place setPersistenceEnabled(true) should be called.
        // It initializes Firebase Database persistence for the entire app.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}