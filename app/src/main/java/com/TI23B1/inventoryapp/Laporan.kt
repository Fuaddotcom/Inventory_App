package com.TI23B1.inventoryapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

data class StockMovement(
    val namaBarang: String = "",
    val namaSupplier: String = "",
    val tanggal: String = "",
    val jenisPergerakan: String = "", // "Masuk" or "Keluar"
    val quantity: Int = 0
) {
    constructor() : this("", "", "","", 0)
}

data class ItemSummary(val namaBarang: String, val namaSupplier: String, var totalQuantity: Int)

class Laporan : AppCompatActivity() {

    private lateinit var databaseBarangMasuk: DatabaseReference
    private lateinit var databaseBarangKeluar: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StockReportAdapter // You'll need to create this adapter
    private val stockReportItems = mutableListOf<Any>() // Can hold different data types
    private val allStockMovements = mutableListOf<StockMovement>() // Initialize this
    private val currentStockMap = mutableMapOf<String, ItemSummary>() // Initialize this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.recyclerViewLaporan)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = StockReportAdapter(stockReportItems)
        recyclerView.adapter = adapter

        databaseBarangMasuk = FirebaseDatabase.getInstance().getReference("barang_masuk")
        databaseBarangKeluar = FirebaseDatabase.getInstance().getReference("barang_keluar")

        fetchAndProcessData()
    }

    private fun fetchAndProcessData() {
        val barangMasukListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val masukList = mutableListOf<StockMovement>()
                for (childSnapshot in snapshot.children) {
                    val cargo = CargoIn.fromSnapshot(childSnapshot)
                    cargo?.let {
                        masukList.add(StockMovement(it.namaBarang, it.namaSupplier, it.tanggalMasuk, "Masuk", it.quantity))
                    }
                }
                allStockMovements.addAll(masukList)
                updateReportData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to read Barang Masuk data.", error.toException())
            }
        }
        databaseBarangMasuk.addValueEventListener(barangMasukListener)

        val barangKeluarListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val keluarList = mutableListOf<StockMovement>()
                for (childSnapshot in snapshot.children) {
                    val cargo = CargoIn.fromSnapshot(childSnapshot)
                    cargo?.let {
                        keluarList.add(StockMovement(it.namaBarang, it.namaSupplier,` it.tanggalMasuk, "Keluar", it.quantity))
                    }
                }
                allStockMovements.addAll(keluarList)
                updateReportData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Failed to read Barang Keluar data.", error.toException())
            }
        }
        databaseBarangKeluar.addValueEventListener(barangKeluarListener)
    }

    private fun updateReportData() {
        currentStockMap.clear()
        for (movement in allStockMovements) {
            if (movement.jenisPergerakan == "Masuk") {
                currentStockMap.getOrPut(movement.namaBarang) { ItemSummary(movement.namaBarang, "", 0) }.totalQuantity += movement.quantity
            } else if (movement.jenisPergerakan == "Keluar") {
                currentStockMap.getOrPut(movement.namaBarang) { ItemSummary(movement.namaBarang, "", 0) }.totalQuantity -= movement.quantity
            }
        }

        stockReportItems.clear()
        stockReportItems.add("Current Stock")
        stockReportItems.addAll(currentStockMap.values.toList())
        stockReportItems.add("Stock Movement History")
        stockReportItems.addAll(allStockMovements.sortedByDescending { it.tanggal })

        adapter.notifyDataSetChanged()
    }
}