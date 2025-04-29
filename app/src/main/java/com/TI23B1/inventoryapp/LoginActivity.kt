package com.TI23B1.inventoryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private val TAG = "GoogleSignIn"
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if the user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, proceed to the next activity
            navigateToMainActivity()
            finish() // Important: Close the LoginActivity so the user can't go back
            return // Exit onCreate to prevent further login attempts
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Request ID token
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                Log.e(TAG, "Google sign in failed with result code: ${result.resultCode}")
                Toast.makeText(this, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<View>(R.id.sign_in_button).setOnClickListener {
            signIn()
        }

        // We no longer need to check for the last signed-in Google account here
        // because Firebase Auth state is the source of truth.
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "handleSignInResult:success " + account?.idToken)

            // Send Google ID token to Firebase
            account?.idToken?.let {
                firebaseAuthWithGoogle(it)
            } ?: run {
                Log.e(TAG, "Google ID token is null.")
                updateUI(null)
            }
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "firebaseAuthWithGoogle:success")
                    val user = auth.currentUser
                    updateUI(user) // Update UI with FirebaseUser object
                    // Proceed to your main activity
                    Toast.makeText(this, "Firebase Authentication successful!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                    finish() // Important: Close the LoginActivity
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "firebaseAuthWithGoogle:failure", task.exception)
                    Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Log.d(TAG, "Firebase User UID: ${user.uid}, Email: ${user.email}")
            // You can update your UI to reflect the logged-in state if needed here.
            // However, since we are immediately navigating, this might not be necessary.
        } else {
            // Update your UI to show the user is not logged in
            Toast.makeText(this, "Not signed in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java) // Replace MainActivity with your actual next activity
        startActivity(intent)
    }
}