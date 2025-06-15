package com.TI23B1.inventoryapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.TI23B1.inventoryapp.dialogs.AddInventoryDialog // Make sure this import is correct
import com.TI23B1.inventoryapp.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), AddInventoryDialog.OnSaveListener { // <--- ADD THIS PART

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var userControl: UserControl // Assuming UserControl is defined elsewhere

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userControl = UserControl() // Initialize UserControl

        setupViews()
        setupBottomNavigation()
        setupFAB()

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun setupViews() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        fabAdd = findViewById(R.id.fab_add)
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    showFAB(true)
                    true
                }
                R.id.nav_history -> {
                    // Load HistoryFragment or handle history display
                    showFAB(false)
                    true
                }
                R.id.nav_inventory -> {
                    // Load InventoryFragment or handle inventory display
                    showFAB(true)
                    true
                }
                R.id.nav_profile -> {
                    // Load ProfileFragment or handle profile display
                    showFAB(false)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFAB() {
        fabAdd.setOnClickListener {
            // Handle FAB click based on current fragment
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            when (currentFragment) {
                is HomeFragment -> {
                    showAddInventoryDialog()
                }
                // Add more cases for other fragments where the FAB might trigger a different action
                // is AnotherFragment -> {
                //     showAnotherDialog()
                // }
            }
        }
    }

    private fun showAddInventoryDialog() {
        val dialog = AddInventoryDialog.newInstance()
        dialog.setOnSaveListener(this) // This line will now work correctly!
        dialog.show(supportFragmentManager, "AddInventoryDialog")
    }

    private fun loadFragment(fragment: Fragment) {
        // Pass user data to fragment if it's HomeFragment
        if (fragment is HomeFragment) {
            val currentUser = userControl.getCurrentUser() // Ensure UserControl.getCurrentUser() returns a non-null value or handle null
            val bundle = Bundle().apply {
                putString("user_name", currentUser?.displayName ?: "User")
                putString("user_email", currentUser?.email ?: "")
                putString("user_photo_url", currentUser?.photoUrl?.toString() ?: "")
            }
            fragment.arguments = bundle
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showFAB(show: Boolean) {
        if (show) {
            fabAdd.show()
        } else {
            fabAdd.hide()
        }
    }

    // --- INTERFACE IMPLEMENTATIONS ---
    // These methods are now properly overriding the interface methods.
    override fun onSaveCargoIn(cargoIn: CargoIn) {
        // Handle saving CargoIn data
        // You can implement your Firebase/database saving logic here
        // For example:
        // FirebaseDatabase.getInstance().reference
        //     .child("cargo_in")
        //     .child(cargoIn.cargoId)
        //     .setValue(cargoIn.toMap())

        // You might also want to refresh the current fragment to show new data
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is HomeFragment) {
            // Refresh the fragment data if needed
            // currentFragment.refreshData()
            // To show a simple toast for testing:
            // Toast.makeText(this, "Incoming Cargo: ${cargoIn.namaBarang} saved!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveCargoOut(cargoOut: CargoOut) {
        // Handle saving CargoOut data
        // You can implement your Firebase/database saving logic here
        // For example:
        // FirebaseDatabase.getInstance().reference
        //     .child("cargo_out")
        //     .child(cargoOut.cargoId)
        //     .setValue(cargoOut.toMap())

        // You might also want to refresh the current fragment to show new data
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is HomeFragment) {
            // Refresh the fragment data if needed
            // currentFragment.refreshData()
            // To show a simple toast for testing:
            // Toast.makeText(this, "Outgoing Cargo: ${cargoOut.namaBarang} saved!", Toast.LENGTH_SHORT).show()
        }
    }
}