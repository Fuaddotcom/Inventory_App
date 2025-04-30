package com.TI23B1.inventoryapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.TI23B1.inventoryapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var userControl: UserControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userControl = UserControl()

        // Set up button click listener
        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            registerUser()
        }

        // Add navigation to login
        findViewById<TextView>(R.id.tvLoginLink).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        // Get input values
        val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.etPassword).text.toString().trim()
        val username = findViewById<EditText>(R.id.etUsername).text.toString().trim()
        val fullName = findViewById<EditText>(R.id.etFullName).text.toString().trim()

        // Validate inputs
        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || fullName.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show progress
        // progressBar.visibility = View.VISIBLE


        // Register with UserControl
        userControl.registerWithEmailPassword(email, password, username, fullName) { user, exception ->
            // progressBar.visibility = View.GONE

            if (user != null) {
                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Registration failed: ${exception?.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}