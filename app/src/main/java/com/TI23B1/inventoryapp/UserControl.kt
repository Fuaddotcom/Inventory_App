package com.TI23B1.inventoryapp

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*

data class User(
    val userId: String = "",
    val username: String = "",
    val namaLengkapUser: String = "",
    val emailUser: String = "",
    val passwordUser: String = "",
    val statusUser: String = ""
)

class UserControl {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference.child("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "UserControl"

    // Check if user is already signed in
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Google Sign-In authentication
    fun firebaseAuthWithGoogle(idToken: String, account: GoogleSignInAccount, onComplete: (FirebaseUser?, Exception?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "firebaseAuthWithGoogle:success")
                    val firebaseUser = auth.currentUser

                    // Add user to Realtime Database if they're new
                    firebaseUser?.let { user ->
                        // Check if user exists in database first
                        readUserData(user.uid) { existingUser ->
                            if (existingUser == null) {
                                // New user - create entry in the database
                                val displayName = account.displayName ?: "User"
                                val username = account.email?.substringBefore("@") ?: "user"

                                writeNewUser(
                                    userId = user.uid,
                                    username = username,
                                    namaLengkapUser = displayName,
                                    emailUser = user.email ?: "",
                                    passwordUser = "", // No password for Google Auth
                                    statusUser = "active"
                                )

                                Log.d(TAG, "Created new user in database: ${user.uid}")
                            } else {
                                Log.d(TAG, "User already exists in database: ${user.uid}")
                            }

                            onComplete(firebaseUser, null)
                        }
                    } ?: run {
                        onComplete(null, Exception("Firebase user is null after successful authentication"))
                    }
                } else {
                    Log.w(TAG, "firebaseAuthWithGoogle:failure", task.exception)
                    onComplete(null, task.exception)
                }
            }
    }

    // Email/Password Registration
    fun registerWithEmailPassword(email: String, password: String, username: String,
                                  fullName: String, onComplete: (FirebaseUser?, Exception?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser

                    firebaseUser?.let { user ->
                        // Create user in database
                        writeNewUser(
                            userId = user.uid,
                            username = username,
                            namaLengkapUser = fullName,
                            emailUser = email,
                            passwordUser = "", // Don't store actual password
                            statusUser = "active"
                        )

                        onComplete(firebaseUser, null)
                    } ?: run {
                        onComplete(null, Exception("Firebase user is null after successful registration"))
                    }
                } else {
                    onComplete(null, task.exception)
                }
            }
    }

    // Email/Password Login
    fun loginWithEmailPassword(email: String, password: String, onComplete: (FirebaseUser?, Exception?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(auth.currentUser, null)
                } else {
                    onComplete(null, task.exception)
                }
            }
    }

    // Sign out user
    fun signOut() {
        auth.signOut()
    }

    // Write user data to database
    fun writeNewUser(
        userId: String,
        username: String,
        namaLengkapUser: String,
        emailUser: String,
        passwordUser: String = "",
        statusUser: String = "active"
    ) {
        val user = User(
            userId = userId,
            username = username,
            namaLengkapUser = namaLengkapUser,
            emailUser = emailUser,
            passwordUser = passwordUser,
            statusUser = statusUser
        )
        database.child(userId).setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "User data saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error writing user data", e)
            }
    }

    // Read user data from database
    fun readUserData(userId: String, onUserRetrieved: (User?) -> Unit) {
        database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(User::class.java)
                onUserRetrieved(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read user.", error.toException())
                onUserRetrieved(null)
            }
        })
    }

    // Update user profile
    fun updateUserProfile(userId: String, updates: Map<String, Any>, onComplete: (Boolean, Exception?) -> Unit) {
        database.child(userId).updateChildren(updates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception)
                }
            }
    }

    // Delete user account
    fun deleteUser(onComplete: (Boolean, Exception?) -> Unit) {
        val user = auth.currentUser

        user?.let { firebaseUser ->
            val userId = firebaseUser.uid

            // First delete from database
            database.child(userId).removeValue()
                .addOnCompleteListener { dbTask ->
                    if (dbTask.isSuccessful) {
                        // Then delete the auth account
                        firebaseUser.delete()
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    onComplete(true, null)
                                } else {
                                    onComplete(false, authTask.exception)
                                }
                            }
                    } else {
                        onComplete(false, dbTask.exception)
                    }
                }
        } ?: run {
            onComplete(false, Exception("No user is currently signed in"))
        }
    }
}