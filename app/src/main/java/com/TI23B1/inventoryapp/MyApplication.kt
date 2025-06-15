package com.TI23B1.inventoryapp

import android.app.Application
import com.TI23B1.inventoryapp.utils.AppPreferences

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        AppPreferences.init(this) // Initialize SharedPreferences
    }
}