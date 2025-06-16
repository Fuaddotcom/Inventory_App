package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.TI23B1.inventoryapp.R
import com.TI23B1.inventoryapp.adapters.RecentItemsAdapter
import com.TI23B1.inventoryapp.models.RecentItem
import com.TI23B1.inventoryapp.CargoIn
import com.TI23B1.inventoryapp.CargoOut
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvUsername: TextView
    private lateinit var tvBarangMasukCount: TextView
    private lateinit var tvBarangKeluarCount: TextView
    private lateinit var cardBarangMasuk: CardView
    private lateinit var cardBarangKeluar: CardView
    private lateinit var rvRecentItems: RecyclerView
    private lateinit var ivProfile: ImageView

    private lateinit var recentItemsAdapter: RecentItemsAdapter

    // User data
    private var userName: String? = null
    private var userEmail: String? = null
    private var userPhotoUrl: String? = null

    // Firebase Database reference
    private val database = FirebaseDatabase.getInstance()
    private val barangMasukRef = database.getReference("barang_masuk")
    private val barangKeluarRef = database.getReference("barang_keluar")

    // Tag for logging
    private val TAG = "HomeFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            userName = it.getString("user_name")
            userEmail = it.getString("user_email")
            userPhotoUrl = it.getString("user_photo_url")
        }

        setupViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadData()
    }

    private fun setupViews(view: View) {
        tvGreeting = view.findViewById(R.id.tv_greeting)
        tvUsername = view.findViewById(R.id.tv_username)
        tvBarangMasukCount = view.findViewById(R.id.tv_barang_masuk_count)
        tvBarangKeluarCount = view.findViewById(R.id.tv_barang_keluar_count)
        cardBarangMasuk = view.findViewById(R.id.card_barang_masuk)
        cardBarangKeluar = view.findViewById(R.id.card_barang_keluar)
        rvRecentItems = view.findViewById(R.id.rv_recent_items)
        ivProfile = view.findViewById(R.id.iv_profile)
    }

    private fun setupRecyclerView() {
        recentItemsAdapter = RecentItemsAdapter(
            onMoreOptionsClick = { item: RecentItem, menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.action_details -> {
                        Toast.makeText(context, "Details for: ${item.name}", Toast.LENGTH_SHORT).show()
                        when (item.type) {
                            "IN" -> {
                                // Navigate to incoming goods detail
                                // findNavController().navigate(
                                //     R.id.action_home_to_incoming_detail,
                                //     bundleOf("cargoId" to item.cargoId)
                                // )
                            }
                            "OUT" -> {
                                // Navigate to outgoing goods detail
                                // findNavController().navigate(
                                //     R.id.action_home_to_outgoing_detail,
                                //     bundleOf("cargoId" to item.cargoId)
                                // )
                            }
                        }
                    }
                    R.id.action_delete -> {
                        Toast.makeText(context, "Delete: ${item.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        rvRecentItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = recentItemsAdapter
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
        }
    }

    private fun setupClickListeners() {
        cardBarangMasuk.setOnClickListener {
            // navigate
        }
        cardBarangKeluar.setOnClickListener {
            // navigate
        }
    }

    private fun loadData() {
        tvGreeting.text = getTimeBasedGreeting()
        tvUsername.text = userName ?: "User Name"
        updateDashboardCounts()
        loadRecentItemsFromFirebase()
    }

    private fun updateDashboardCounts() {
        Log.d(TAG, "Fetching dashboard counts...")
        barangMasukRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount.toInt()
                tvBarangMasukCount.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read barang_masuk count: ${error.message}", error.toException())
            }
        })

        barangKeluarRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "barang_keluar snapshot exists: ${snapshot.exists()}")
                Log.d(TAG, "barang_keluar children count: ${snapshot.childrenCount}")
                val count = snapshot.childrenCount.toInt()
                tvBarangKeluarCount.text = count.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read barang_keluar count: ${error.message}", error.toException())
            }
        })
    }

    private fun loadRecentItemsFromFirebase() {
        val recentItemsList = mutableListOf<RecentItem>()
        Log.d(TAG, "Starting to load recent items from Firebase...")

        // Fetch barang_masuk items, ordered by tanggalMasuk
        barangMasukRef.orderByChild("tanggalMasuk").limitToLast(5).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "barang_masuk recent items snapshot exists: ${snapshot.exists()}")
                Log.d(TAG, "barang_masuk recent items children count: ${snapshot.childrenCount}")
                snapshot.children.forEach { dataSnapshot ->
                    Log.d(TAG, "Processing barang_masuk child: ${dataSnapshot.key}, value: ${dataSnapshot.value}")
                    val cargoIn = dataSnapshot.getValue(CargoIn::class.java)
                    if (cargoIn == null) {
                        Log.e(TAG, "Failed to parse CargoIn for key: ${dataSnapshot.key}. Raw value: ${dataSnapshot.value}")
                    } else {
                        val recentItem = RecentItem.fromCargoIn(cargoIn)
                        recentItemsList.add(recentItem)
                        Log.d(TAG, "Added IN item: ${recentItem.name}, stock: ${recentItem.stock}, timestamp: ${recentItem.timestamp}")
                    }
                }
                Log.d(TAG, "Finished processing barang_masuk items. Current recentItemsList size: ${recentItemsList.size}")
                fetchBarangKeluarItems(recentItemsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read barang_masuk recent items: ${error.message}", error.toException())
                fetchBarangKeluarItems(recentItemsList) // Still try to fetch outgoing if incoming fails
            }
        })
    }

    private fun fetchBarangKeluarItems(currentRecentItems: MutableList<RecentItem>) {
        Log.d(TAG, "Starting to fetch barang_keluar items. Current list size: ${currentRecentItems.size}")
        barangKeluarRef.orderByChild("tanggalKeluar").limitToLast(5).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "barang_keluar recent items snapshot exists: ${snapshot.exists()}")
                Log.d(TAG, "barang_keluar recent items children count: ${snapshot.childrenCount}")
                snapshot.children.forEach { dataSnapshot ->
                    Log.d(TAG, "Processing barang_keluar child: ${dataSnapshot.key}, value: ${dataSnapshot.value}")
                    val cargoOut = dataSnapshot.getValue(CargoOut::class.java)
                    if (cargoOut == null) {
                        Log.e(TAG, "Failed to parse CargoOut for key: ${dataSnapshot.key}. Raw value: ${dataSnapshot.value}")
                    } else {
                        val recentItem = RecentItem.fromCargoOut(cargoOut)
                        currentRecentItems.add(recentItem)
                        Log.d(TAG, "Added OUT item: ${recentItem.name}, stock: ${recentItem.stock}, timestamp: ${recentItem.timestamp}")
                    }
                }
                Log.d(TAG, "Finished processing barang_keluar items. Final recentItemsList size: ${currentRecentItems.size}")

                // Sort the combined list by timestamp (most recent first)
                val sortedList = currentRecentItems.sortedByDescending { it.timestamp }
                Log.d(TAG, "Sorted recentItemsList size: ${sortedList.size}")
                sortedList.forEach { Log.d(TAG, "Sorted item: ${it.name} (${it.type}) - ${it.timestamp}") }

                recentItemsAdapter.submitList(sortedList)
                Log.d(TAG, "Submitted list to adapter.")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read barang_keluar recent items: ${error.message}", error.toException())
                Toast.makeText(context, "Gagal Memuat Barang Keluar", Toast.LENGTH_SHORT).show()
                // Still sort and submit what we have if outgoing fails
                val sortedList = currentRecentItems.sortedByDescending { it.timestamp }
                Log.d(TAG, "Sorted recentItemsList (onCancelled) size: ${sortedList.size}")
                recentItemsAdapter.submitList(sortedList)
                Log.d(TAG, "Submitted list to adapter (onCancelled).")
            }
        })
    }

    private fun getTimeBasedGreeting(): String {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "Selamat Pagi,"
            in 12..14 -> "Selamat Siang,"
            in 15..18 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
    }
}