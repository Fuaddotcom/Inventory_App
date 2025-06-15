package com.TI23B1.inventoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.TI23B1.inventoryapp.utils.AppPreferences // Import your AppPreferences

class Greeting : AppCompatActivity() {

    private lateinit var userControl: UserControl // Assuming UserControl handles actual user session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AppPreferences if not already done (though MyApplication handles this)
        // AppPreferences.init(this) // Not strictly needed here if MyApplication handles it

        userControl = UserControl() // Initialize your UserControl here

        // 1. Check if user is already signed in
        val currentUser = userControl.getCurrentUser()
        if (currentUser != null) {
            // User is signed in, bypass greeting and go to MainActivity
            navigateToMainActivity()
            finish()
            return
        }

        // 2. User is NOT signed in. Check if they have seen the greeting before.
        if (AppPreferences.hasSeenGreeting) {
            // User has seen the greeting before, so go directly to LoginActivity
            navigateToLoginActivity()
            finish()
            return
        }

        // 3. This is a true first-time launch where user is not logged in AND has not seen greeting.
        // Show the greeting screen.
        enableEdgeToEdge()
        setContentView(R.layout.activity_greeting) // This will load your greeting layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnAyoMulai = findViewById<Button>(R.id.buttonAyoMulai)
        btnAyoMulai.setOnClickListener {
            // User clicks "Ayo Mulai" from the greeting screen
            AppPreferences.hasSeenGreeting = true // Mark that greeting has been seen
            navigateToLoginActivity()
            finish()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}