package com.TI23B1.inventoryapp

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.google.gson.annotations.SerializedName



class InventoryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.database.setPersistenceEnabled(true)

        val database = Firebase.database
        val myRef = database.getReference("message")
        myRef.setValue("Hello, World!")

    }
}