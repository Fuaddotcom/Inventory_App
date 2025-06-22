package com.TI23B1.inventoryapp

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import com.TI23B1.inventoryapp.utils.AppPreferences

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        AppPreferences.init(applicationContext)
    }
}