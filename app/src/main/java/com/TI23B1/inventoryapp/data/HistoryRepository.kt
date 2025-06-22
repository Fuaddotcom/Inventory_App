package com.TI23B1.inventoryapp.data // Adjust your package name

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.TI23B1.inventoryapp.models.CargoIn
import com.TI23B1.inventoryapp.models.CargoOut
import com.TI23B1.inventoryapp.models.RecentItem

class HistoryRepository {

    private val database = FirebaseDatabase.getInstance()
    private val barangMasukRef = database.getReference("barang_masuk")
    private val barangKeluarRef = database.getReference("barang_keluar")

    // --- Callbacks for fetching data ---
    interface RecentItemsFetchListener {
        fun onRecentItemsFetched(items: List<RecentItem>)
        fun onError(errorMessage: String)
    }

    interface CountUpdateListener {
        fun onCountUpdated(count: Int)
        fun onError(errorMessage: String)
    }

    // --- Method to fetch all recent items (both Masuk and Keluar) ---
    fun fetchRecentItems(listener: RecentItemsFetchListener) {
        val allRecentItems = mutableListOf<RecentItem>()
        var pendingFetches = 2 // We need to wait for both barang_masuk and barang_keluar to finish

        val TAG = "HistoryRepo"

        // Fetch barang_masuk
        barangMasukRef.orderByChild("tanggalMasuk").limitToLast(5).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val cargoIn = dataSnapshot.getValue(CargoIn::class.java)
                    if (cargoIn != null) {
                        allRecentItems.add(RecentItem.fromCargoIn(cargoIn))
                    }
                }
                pendingFetches--
                checkIfAllFetchesComplete(pendingFetches, allRecentItems, listener, TAG)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch barang_masuk: ${error.message}", error.toException())
                pendingFetches--
                // Even on error, we need to check if other fetch is complete
                checkIfAllFetchesComplete(pendingFetches, allRecentItems, listener, TAG, "Error fetching barang masuk: ${error.message}")
            }
        })

        // Fetch barang_keluar
        barangKeluarRef.orderByChild("tanggalKeluar").limitToLast(5).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val cargoOut = dataSnapshot.getValue(CargoOut::class.java)
                    if (cargoOut != null) {
                        allRecentItems.add(RecentItem.fromCargoOut(cargoOut))
                    }
                }
                pendingFetches--
                checkIfAllFetchesComplete(pendingFetches, allRecentItems, listener, TAG)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to fetch barang_keluar: ${error.message}", error.toException())
                pendingFetches--
                // Even on error, we need to check if other fetch is complete
                checkIfAllFetchesComplete(pendingFetches, allRecentItems, listener, TAG, "Error fetching barang keluar: ${error.message}")
            }
        })
    }

    // Helper to combine results from two async calls
    private fun checkIfAllFetchesComplete(
        pendingFetches: Int,
        allRecentItems: MutableList<RecentItem>,
        listener: RecentItemsFetchListener,
        tag: String,
        errorMessage: String? = null
    ) {
        if (pendingFetches == 0) {
            if (errorMessage != null) {
                listener.onError(errorMessage)
            } else {
                val sortedList = allRecentItems.sortedByDescending { it.timestamp }
                listener.onRecentItemsFetched(sortedList)
                Log.d(tag, "All recent items fetched and sorted. Count: ${sortedList.size}")
            }
        }
    }

    // --- Methods to listen for dashboard counts (real-time) ---
    fun listenToBarangMasukCount(listener: CountUpdateListener): ValueEventListener {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener.onCountUpdated(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError("Gagal Memuat Jumlah Barang Masuk: ${error.message}")
                Log.e("HistoryRepo", "Error listening to barang_masuk count: ${error.message}", error.toException())
            }
        }
        barangMasukRef.addValueEventListener(valueEventListener)
        return valueEventListener // Return listener to be removed later
    }

    fun listenToBarangKeluarCount(listener: CountUpdateListener): ValueEventListener {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener.onCountUpdated(snapshot.childrenCount.toInt())
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onError("Gagal Memuat Jumlah Barang Keluar: ${error.message}")
                Log.e("HistoryRepo", "Error listening to barang_keluar count: ${error.message}", error.toException())
            }
        }
        barangKeluarRef.addValueEventListener(valueEventListener)
        return valueEventListener // Return listener to be removed later
    }

    // --- Method to remove listeners (IMPORTANT for real-time updates) ---
    fun removeCountListener(listener: ValueEventListener) {
        barangMasukRef.removeEventListener(listener) // You might need separate remove methods for each ref
        barangKeluarRef.removeEventListener(listener) // If same listener object is passed to both
        Log.d("HistoryRepo", "Count listener removed.")
    }

    // A better way to remove specific listeners if multiple are attached
    fun removeBarangMasukCountListener(listener: ValueEventListener) {
        barangMasukRef.removeEventListener(listener)
        Log.d("HistoryRepo", "Barang Masuk count listener removed.")
    }

    fun removeBarangKeluarCountListener(listener: ValueEventListener) {
        barangKeluarRef.removeEventListener(listener)
        Log.d("HistoryRepo", "Barang Keluar count listener removed.")
    }
}