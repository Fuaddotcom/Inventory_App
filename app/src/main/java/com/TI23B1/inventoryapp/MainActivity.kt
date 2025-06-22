package com.TI23B1.inventoryapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
// import com.cloudinary.android.MediaManager // REMOVED: Cloudinary import
// import java.util.HashMap // REMOVED: No longer needed for Cloudinary config
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.TI23B1.inventoryapp.data.CargoInRepository
import com.TI23B1.inventoryapp.data.CargoOutRepository
import com.TI23B1.inventoryapp.data.InventoryRepository
import com.TI23B1.inventoryapp.data.InventoryStockRepository
import com.TI23B1.inventoryapp.dialogs.AddInventoryDialog
import com.TI23B1.inventoryapp.fragments.HomeFragment
import com.TI23B1.inventoryapp.models.CargoIn
import com.TI23B1.inventoryapp.models.CargoOut
import com.TI23B1.inventoryapp.models.InventoryItem // Needed for InventoryRepository
import com.TI23B1.inventoryapp.utils.BarcodeUtils
import com.TI23B1.inventoryapp.utils.PrintUtils
import com.TI23B1.inventoryapp.utils.StickerPrintData


class MainActivity : AppCompatActivity(), AddInventoryDialog.OnSaveListener {


    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var userControl: UserControl

    private lateinit var cargoInRepository: CargoInRepository
    private lateinit var cargoOutRepository: CargoOutRepository
    private lateinit var inventoryStockRepository: InventoryStockRepository
    private lateinit var inventoryRepository: InventoryRepository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userControl = UserControl()

        cargoInRepository = CargoInRepository()
        cargoOutRepository = CargoOutRepository()
        inventoryStockRepository = InventoryStockRepository()
        inventoryRepository = InventoryRepository()

        // REMOVED: initCloudinary() // No Cloudinary init in mobile app

        setupViews()
        setupViewPager()
        setupBottomNavigation()
        setupFAB()

        if (savedInstanceState == null) {
            viewPager.currentItem = 0
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    // REMOVED: private fun initCloudinary() { ... } // No Cloudinary init

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
            val currentFragmentTag = "f" + viewPager.currentItem
            val currentFragment = supportFragmentManager.findFragmentByTag(currentFragmentTag)

            when (currentFragment) {
                is HomeFragment -> {
                    showAddInventoryDialog()
                }
                else -> { /* Do nothing or show a message if FAB is clicked on other tabs */ }
            }
        }
    }

    private fun showAddInventoryDialog() { // This might be the method you call to open the dialog
        val dialog = AddInventoryDialog.newInstance(
            this.inventoryRepository,
            this.inventoryStockRepository
        )
        // Set the listener if your MainActivity implements it
        // dialog.setOnSaveListener(this) // if MainActivity implements OnSaveListener
        dialog.show(supportFragmentManager, "AddInventoryDialog")
    }


    // --- OnSaveListener Implementations (existing) ---
    override fun onSaveCargoIn(cargoIn: CargoIn) {
        cargoInRepository.addCargoIn(cargoIn) { success, message ->
            if (success) {
                Toast.makeText(this, "Incoming Cargo: ${cargoIn.namaBarang} saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error saving incoming cargo: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onSaveCargoOut(cargoOut: CargoOut) {
        cargoOutRepository.addCargoOut(cargoOut) { success, message ->
            if (success) {
                inventoryStockRepository.updateStock(
                    name = cargoOut.namaBarang,
                    quantityChange = -cargoOut.quantity,
                    isIncoming = false
                ) { stockUpdateSuccess, stockUpdateMessage ->
                    if (stockUpdateSuccess) {
                        Toast.makeText(this, "Outgoing Cargo: ${cargoOut.namaBarang} saved! Stock updated.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Outgoing Cargo: ${cargoOut.namaBarang} saved! BUT stock update failed: $stockUpdateMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Error saving outgoing cargo: $message", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCargoOutSavedAndReadyForPrint(cargoOut: CargoOut) {
        val qrCodeContent = "${cargoOut.nomorSuratJalan}|${cargoOut.namaBarang}|${cargoOut.quantity} ${cargoOut.unit}|${cargoOut.tujuanPengiriman}"
        val qrBitmap = BarcodeUtils.generateQrCodeBitmap(qrCodeContent)

        if (qrBitmap != null) {
            val stickerData = StickerPrintData(
                itemName = cargoOut.namaBarang,
                itemCode = cargoOut.nomorSuratJalan,
                quantity = "${cargoOut.quantity}(${cargoOut.unit})",
                destination = cargoOut.tujuanPengiriman,
                qrCodeBitmap = qrBitmap
            )
            val pdfDocument = PrintUtils.createStickerPdf(this, stickerData)
            PrintUtils.printPdfDocument(this, pdfDocument, "Sticker: ${cargoOut.namaBarang}")
        } else {
            Toast.makeText(this, "Failed to generate QR code for printing.", Toast.LENGTH_LONG).show()
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