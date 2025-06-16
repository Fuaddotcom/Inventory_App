// com/TI23B1/inventoryapp/MainActivity.kt
package com.TI23B1.inventoryapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.TI23B1.inventoryapp.dialogs.AddInventoryDialog
import com.TI23B1.inventoryapp.fragments.HistoryFragment
import com.TI23B1.inventoryapp.fragments.HomeFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
// import com.google.firebase.database.FirebaseDatabase // No longer needed directly in MainActivity for persistence

class MainActivity : AppCompatActivity(), AddInventoryDialog.OnSaveListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var userControl: UserControl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // REMOVE THIS LINE: FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        userControl = UserControl() // Initialize UserControl

        setupViews()
        setupViewPager()
        setupBottomNavigation()
        setupFAB()

        if (savedInstanceState == null) {
            viewPager.currentItem = 0
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun setupViews() {
        viewPager = findViewById(R.id.view_pager)
        bottomNavigation = findViewById(R.id.bottom_navigation)
        fabAdd = findViewById(R.id.fab_add)
    }

    private fun setupViewPager() {
        val pagerAdapter = MainViewPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNavigation.selectedItemId = bottomNavigation.menu.getItem(position).itemId

                when (position) {
                    0 -> showFAB(true)
                    else -> showFAB(false)
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_history -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_inventory -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_profile -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFAB() {
        fabAdd.setOnClickListener {
            val currentFragment = (viewPager.adapter as MainViewPagerAdapter).createFragment(viewPager.currentItem)
            when (currentFragment) {
                is HomeFragment -> {
                    showAddInventoryDialog()
                }
                else -> { /* ... */ }
            }
        }
    }

    private fun showAddInventoryDialog() {
        val dialog = AddInventoryDialog.newInstance()
        dialog.setOnSaveListener(this)
        dialog.show(supportFragmentManager, "AddInventoryDialog")
    }

    override fun onSaveCargoIn(cargoIn: CargoIn) {
        // You'll need FirebaseDatabase.getInstance() here to save data, which is fine
        // as setPersistenceEnabled is now called much earlier in MyApplication's onCreate.
        com.google.firebase.database.FirebaseDatabase.getInstance().reference
            .child("barang_masuk")
            .push()
            .setValue(cargoIn)

        val currentFragment = (viewPager.adapter as MainViewPagerAdapter).createFragment(viewPager.currentItem)
        if (currentFragment is HomeFragment) {
            Toast.makeText(this, "Incoming Cargo: ${cargoIn.namaBarang} saved!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveCargoOut(cargoOut: CargoOut) {
        com.google.firebase.database.FirebaseDatabase.getInstance().reference
            .child("barang_keluar")
            .push()
            .setValue(cargoOut)

        val currentFragment = (viewPager.adapter as MainViewPagerAdapter).createFragment(viewPager.currentItem)
        if (currentFragment is HomeFragment) {
            Toast.makeText(this, "Outgoing Cargo: ${cargoOut.namaBarang} saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFAB(show: Boolean) {
        if (show) {
            fabAdd.show()
        } else {
            fabAdd.hide()
        }
    }
}