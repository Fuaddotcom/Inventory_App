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

class LoginActivity : AppCompatActivity() {

    private val TAG = "GoogleSignIn"
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check for existing Google Sign In account, if the user is already signed in
        // silently sign-in and get the account.
        val account = GoogleSignIn.getLastSignedInAccount(this)
        account?.let {
            // User is already signed in, proceed to the next activity
            updateUI(it)
        }

        // Set up ActivityResultLauncher for the sign-in intent
        signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                // Sign-in failed, update UI appropriately
                Toast.makeText(this, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set the OnClickListener for the Google Sign-In button
        findViewById<View>(R.id.sign_in_button).setOnClickListener {
            signIn()
        }
    }
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            updateUI(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            updateUI(null)
        }
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            val displayName = account.displayName
            val email = account.email
            val photoUrl = account.photoUrl?.toString() ?: ""

            Log.d(TAG, "Display Name: $displayName")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Photo URL: $photoUrl")

            // Proceed to your main activity or perform registration/login with your backend
            Toast.makeText(this, "Sign-in successful!", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, MainActivity::class.java))
            // finish()
        } else {
            // User is not signed in, update UI to show sign-in button
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
        }
    }
}