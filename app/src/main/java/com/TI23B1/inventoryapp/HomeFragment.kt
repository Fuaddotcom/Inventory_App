package com.TI23B1.inventoryapp.fragments

import android.os.Bundle
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
import com.TI23B1.inventoryapp.adapters.RecentItemsAdapter // THIS IS THE IMPORTANT IMPORT
import com.TI23B1.inventoryapp.models.RecentItem
import androidx.core.os.bundleOf

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
            onItemClick = { item: RecentItem ->
                Toast.makeText(context, "Card clicked: ${item.name}", Toast.LENGTH_SHORT).show()
            },
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
        tvUsername.text = userName ?: "Yovan Julian"
        tvBarangMasukCount.text = "287"
        tvBarangKeluarCount.text = "110"

        val recentItems = listOf(
            RecentItem(id = 1, name = "Axioo NoteBook Hype 1", stock = 15, type = "IN", unit = "pcs", cargoId = "CRG001"),
            RecentItem(id = 2, name = "Dell Monitor 24 inch", stock = 8, type = "OUT", cargoId = "CRG002"),
            RecentItem(id = 3, name = "Logitech Mouse Wireless", stock = 25, type = "IN", unit = "pcs", cargoId = "CRG003"),
            RecentItem(id = 4, name = "HP Printer LaserJet", stock = 3, type = "OUT", unit = "unit", cargoId = "CRG004")
        )
        recentItemsAdapter.submitList(recentItems)
    }

    private fun onRecentItemClick(item: RecentItem) {
        Toast.makeText(context, "Original onRecentItemClick (no longer primary): ${item.name}", Toast.LENGTH_SHORT).show()
    }

    private fun updateDashboardCounts() { /* ... Firebase logic ... */ }
    private fun loadRecentItemsFromFirebase() { /* ... Firebase logic ... */ }

    private fun getTimeBasedGreeting(): String {
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        return when (currentHour) {
            in 5..11 -> "Selamat Pagi,"
            in 12..14 -> "Selamat Siang,"
            in 15..18 -> "Selamat Sore,"
            else -> "Selamat Malam,"
        }
    }
}