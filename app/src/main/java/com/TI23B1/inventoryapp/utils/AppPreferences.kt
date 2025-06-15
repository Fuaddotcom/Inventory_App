package com.TI23B1.inventoryapp.utils

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "MyAppPreferences"
    private const val KEY_HAS_SEEN_GREETING = "has_seen_greeting"

    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var hasSeenGreeting: Boolean
        get() = preferences.getBoolean(KEY_HAS_SEEN_GREETING, false) // Default to false
        set(value) = preferences.edit().putBoolean(KEY_HAS_SEEN_GREETING, value).apply()
}