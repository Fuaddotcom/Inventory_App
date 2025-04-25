package com.TI23B1.inventoryapp

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


data class User(val name: String? = null, val email: String? = null) {
    constructor() : this(null, null)
}

class UserControl {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
    private val TAG = "UserControl"

    fun writeNewUser(userId: String, name: String, email: String){
        val user = User(name, email)
        database.child("users").child(userId).setValue(user)
    }

    fun readUserData(userId: String, onUserRetrieved: (User?) -> Unit) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val user = dataSnapshot.getValue(User::class.java)
                onUserRetrieved(user)
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read user.", error.toException())
                onUserRetrieved(null)
            }
        })
    }
}